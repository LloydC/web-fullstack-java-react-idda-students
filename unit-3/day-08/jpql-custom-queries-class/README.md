# IronBoard -- Step 08: JPQL, Custom Queries & DTO Refactor

IronBoard adds `@Query` methods for aggregation and native SQL, and splits the update DTOs so PUT and PATCH have proper validation.

## What changed from Step 07

### DTO refactor -- separate PUT and PATCH DTOs

| File                                         | Change                                                                                                                                            |
| -------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- |
| `dto/request/FullUpdateTaskRequest.java`     | NEW -- PUT DTO with `@NotBlank` title, `@NotNull` status                                                                                          |
| `dto/request/PatchUpdateTaskRequest.java`    | NEW -- PATCH DTO, all fields optional (replaces UpdateTaskRequest)                                                                                |
| `dto/request/UpdateTaskRequest.java`         | REMOVED -- split into FullUpdate + PatchUpdate                                                                                                    |
| `dto/request/FullUpdateProjectRequest.java`  | NEW -- PUT DTO with `@NotBlank` name                                                                                                              |
| `dto/request/PatchUpdateProjectRequest.java` | NEW -- PATCH DTO, all fields optional (replaces UpdateProjectRequest)                                                                             |
| `dto/request/UpdateProjectRequest.java`      | REMOVED -- split into FullUpdate + PatchUpdate                                                                                                    |
| `service/TaskService.java`                   | fullUpdate takes FullUpdateTaskRequest, partialUpdate takes PatchUpdateTaskRequest. Removed manual title/status if-checks -- @Valid handles them. |
| `service/ProjectService.java`                | Same pattern -- fullUpdate takes FullUpdateProjectRequest, partialUpdate takes PatchUpdateProjectRequest                                          |
| `controller/TaskController.java`             | PUT uses FullUpdateTaskRequest, PATCH uses PatchUpdateTaskRequest                                                                                 |
| `controller/ProjectController.java`          | Same pattern                                                                                                                                      |

### @Query additions

| File                             | Change                                                                                                  |
| -------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `repository/TaskRepository.java` | + countByStatusForProject (GROUP BY), countTasksPerProject, findByProjectIdAndStatusNative (native SQL) |
| `service/TaskService.java`       | + countByStatusForProject, countTasksPerProject, findByProjectIdAndStatus wrappers                      |
| `controller/TaskController.java` | + GET /stats endpoint, + combined ?projectId&status filter via native SQL                               |

## Setup

```sql
DROP DATABASE IF EXISTS ironboard;
CREATE DATABASE ironboard;
```

```bash
mvn clean compile
mvn spring-boot:run
```

### Create test data

Create a project, then tasks with different types and statuses so all Day 8 features are testable:

```
POST http://localhost:8080/api/projects/development
{
  "name": "IronBoard",
  "description": "Project management app",
  "category": "WEB",
  "priority": "HIGH",
  "techStack": "Java, Spring Boot"
}

POST http://localhost:8080/api/tasks
{
  "title": "Setup database",
  "projectId": 1
}

POST http://localhost:8080/api/tasks
{
  "title": "User authentication",
  "projectId": 1,
  "type": "FEATURE",
  "storyPoints": 8
}

POST http://localhost:8080/api/tasks
{
  "title": "Fix login crash",
  "projectId": 1,
  "type": "BUG",
  "severity": "HIGH"
}

PATCH http://localhost:8080/api/tasks/2
{
  "status": "IN_PROGRESS"
}
```

You now have 1 project with 3 tasks (2 TODO, 1 IN_PROGRESS) across 3 types (TASK, FEATURE, BUG). All features below are testable.

---

## Concept: Why separate DTOs for PUT and PATCH?

On Day 7, PUT and PATCH shared one DTO (`UpdateTaskRequest`) with no required-field annotations. That's an anti-pattern: structural validation belongs on the DTO, not in the service.

Day 8 splits the update DTO into two:

|                 | FullUpdateTaskRequest (PUT)                | PatchUpdateTaskRequest (PATCH) |
| --------------- | ------------------------------------------ | ------------------------------ |
| Title           | `@NotBlank` -- required                    | Optional (null = don't change) |
| Status          | `@NotNull` -- required                     | Optional (null = don't change) |
| Description     | Nullable (null clears it)                  | Optional (null = don't change) |
| StoryPoints     | Service validates for FEATURE              | Optional                       |
| Severity        | Service validates for BUG                  | Optional                       |
| `@Valid` effect | Catches missing title/status at controller | Only catches @Size violations  |

**What moved out of the service:**

- Title null-check -- `@NotBlank` on FullUpdateTaskRequest
- Status null-check -- `@NotNull` on FullUpdateTaskRequest

**What stays in the service (business logic, not structural):**

- Enum conversion: `TaskStatus.valueOf(request.getStatus())` + custom error message
- Type-dependent fields: storyPoints required for FEATURE, severity for BUG

### Test PUT validation

```
PUT http://localhost:8080/api/tasks/1
Content-Type: application/json

{
  "description": "Missing title and status"
}
```

**Check:** 400 Bad Request with `fieldErrors`:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "fieldErrors": [
    "title: Title is required for full update",
    "status: Status is required for full update"
  ]
}
```

This error comes from `@Valid` + DTO annotations -- the service method was never called.

### Test PATCH (no validation on required fields)

```
PATCH http://localhost:8080/api/tasks/1
Content-Type: application/json

{
  "description": "Only updating description"
}
```

**Check:** 200 OK -- description updated, title and status unchanged. `@Valid` on PatchUpdateTaskRequest only checks `@Size`, not required fields.

---

## Concept: GROUP BY aggregation -- counting tasks by status

Aggregation queries (COUNT, SUM, AVG with GROUP BY) are impossible with derived query methods. They return multiple values per row, not entities.

### Stats for a specific project

```
GET http://localhost:8080/api/tasks/stats?projectId=1
```

**Flow:** The controller calls `taskService.countByStatusForProject(1)`. The repository runs:

```
SELECT t.status, COUNT(t)
FROM Task t
WHERE t.project.id = :projectId
GROUP BY t.status
```

Notice `t.project.id` -- this is JPQL dot notation navigating the `@ManyToOne` relationship from Task to Project to its id field. Hibernate generates a JOIN behind the scenes.

The return type is `List<Object[]>`. Each element is an array with two values: the status (TaskStatus enum) and the count (Long). For example, if you created three tasks and all have the default TODO status: `[["TODO", 3]]`. If you changed some statuses via PATCH, you might see `[["TODO", 1], ["IN_PROGRESS", 2]]`.

**Check:** The response is a JSON array of arrays. Each inner array has a status string and a count number.

### Stats across all projects

```
GET http://localhost:8080/api/tasks/stats
```

**Flow:** Without a `projectId` parameter, the controller calls `taskService.countTasksPerProject()`. The repository runs:

```
SELECT t.project.name, COUNT(t)
FROM Task t
GROUP BY t.project.name
```

This navigates `t.project.name` -- from Task through the @ManyToOne relationship to the project's name. Hibernate JOINs the tasks and projects tables.

**Check:** Returns project names with their task counts. For example: `[["IronBoard", 3]]`.

---

## Concept: Native SQL -- combined filter with raw SQL

The `nativeQuery = true` flag tells Spring Data to pass the query directly to MySQL as raw SQL. This means you use table names and column names, not entity names and field names.

The `findByProjectIdAndStatusNative` method in TaskRepository filters tasks by both project and status using native SQL:

```sql
SELECT * FROM tasks WHERE project_id = :projectId AND status = :status
```

This could be done with a derived query (`findByProjectIdAndStatus`), but we use native SQL here to demonstrate the pattern. One important difference: native SQL works with raw column values, not Java enums. The service converts the `TaskStatus` enum to its string name before calling the repository:

```java
// In TaskService:
public List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status) {
    return taskRepository.findByProjectIdAndStatusNative(projectId, status.name());
}
```

### Test the combined filter

```
GET http://localhost:8080/api/tasks?projectId=1&status=TODO
```

**Flow:** The controller receives both `projectId` and `status` parameters. It calls `taskService.findByProjectIdAndStatus(1, TaskStatus.TODO)`. The service converts `TODO` to the string `"TODO"` via `status.name()` and passes it to the native SQL query. MySQL runs the raw SQL against the `tasks` table.

**Check:** Returns only tasks that belong to project 1 AND have status TODO.

You can also filter by each parameter independently:

```
GET http://localhost:8080/api/tasks?projectId=1
GET http://localhost:8080/api/tasks?status=IN_PROGRESS
```
