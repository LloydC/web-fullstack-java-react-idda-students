# IronBoard — Step 07: Entity Relationships

IronBoard introduces entity relationships, @MappedSuperclass, and a second inheritance strategy:

- **@MappedSuperclass** (BaseEntity): shared id, createdAt, updatedAt across all entities
- **@ManyToOne / @OneToMany** (Task → Project): bidirectional relationship replaces raw FK, cascade REMOVE
- **@Inheritance(SINGLE_TABLE)** (Task → FeatureTask, BugTask): discriminator column approach
- **@Inheritance(JOINED)** (Project → DevelopmentProject, ConsultingProject): retained from Step 06

## What changed from Step 06

### Step-by-step changes

**1. Extract @MappedSuperclass (BaseEntity)**

- Create `entity/BaseEntity.java` — abstract @MappedSuperclass with id, createdAt, updatedAt, @PrePersist/@PreUpdate
- Modify `entity/Project.java` — extends BaseEntity, remove own @Id, timestamps, lifecycle callbacks
- Modify `entity/Task.java` — extends BaseEntity, remove own @Id, timestamps, lifecycle callbacks

**2. Add @ManyToOne relationship (Task → Project)**

- Modify `entity/Task.java` — replace `Long projectId` with `@ManyToOne @JoinColumn Project project`
- Modify `entity/Project.java` — add `@OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE) List<Task> tasks`

**3. Add Task inheritance (SINGLE_TABLE)**

- Modify `entity/Task.java` — add `@Inheritance(SINGLE_TABLE)` + `@DiscriminatorColumn(name = "type")`
- Create `entity/TaskType.java` — enum for API dispatch (TASK, FEATURE, BUG)
- Create `entity/FeatureTask.java` — @Entity + @DiscriminatorValue("FEATURE"), adds storyPoints
- Create `entity/BugTask.java` — @Entity + @DiscriminatorValue("BUG"), adds severity

**4. Update DTOs for subclass fields**

- Modify `dto/request/CreateTaskRequest.java` — add type (dispatch), storyPoints, severity
- Modify `dto/request/UpdateTaskRequest.java` — add storyPoints, severity (applied based on the loaded entity's type)
- Modify `dto/response/TaskResponse.java` — replace projectId with embedded ProjectSummary, add type, storyPoints, severity

**5. Update mapper with type-based creation and overridden methods**

- Modify `mapper/TaskMapper.java` — toModel() dispatches by TaskType enum, toResponse() uses overridden methods + embeds ProjectSummary. Remove toModel(UpdateTaskRequest) — service handles updates.

**6. Update service for entity relationships**

- Modify `service/TaskService.java` — create() takes (Task, Long projectId) and loads Project entity. fullUpdate()/partialUpdate() accept UpdateTaskRequest DTO directly with overridden methods for subclass fields.

**7. Update controller for new service signatures**

- Modify `controller/TaskController.java` — POST passes projectId to service, PUT/PATCH pass DTO directly.

## File change summary

| File | Change |
| --- | --- |
| `entity/BaseEntity.java` | NEW: @MappedSuperclass with id, timestamps, callbacks |
| `entity/TaskType.java` | NEW: enum for API dispatch (TASK, FEATURE, BUG) |
| `entity/FeatureTask.java` | NEW: extends Task, @DiscriminatorValue("FEATURE"), storyPoints |
| `entity/BugTask.java` | NEW: extends Task, @DiscriminatorValue("BUG"), severity |
| `entity/Project.java` | extends BaseEntity, remove own id/timestamps, + @OneToMany tasks (cascade REMOVE) |
| `entity/Task.java` | extends BaseEntity, remove own id/timestamps, replace projectId with @ManyToOne, + @Inheritance(SINGLE_TABLE) |
| `dto/request/CreateTaskRequest.java` | + type, storyPoints, severity |
| `dto/request/UpdateTaskRequest.java` | + storyPoints, severity |
| `dto/response/TaskResponse.java` | replace projectId with ProjectSummary + type + subclass fields |
| `mapper/TaskMapper.java` | toModel() dispatches by TaskType, toResponse() uses overridden methods + ProjectSummary |
| `service/TaskService.java` | create(Task, Long), fullUpdate/partialUpdate accept DTO, overridden methods for subclasses |
| `controller/TaskController.java` | POST passes projectId to service, PUT/PATCH pass DTO directly |
| `pom.xml` | Updated description |

## Setup

```sql
DROP DATABASE IF EXISTS ironboard;
CREATE DATABASE ironboard;
```

```bash
mvn clean compile
mvn spring-boot:run
```

Watch the console. Hibernate prints CREATE TABLE for `projects`, `development_projects`, `consulting_projects`, `tasks`. The `tasks` table has a `type` discriminator column, `story_points`, and `severity` columns.

---

## Concept: @MappedSuperclass — shared timestamps

All entities extend BaseEntity. The `createdAt` and `updatedAt` fields are inherited, not duplicated. `@PrePersist` sets both on first save. `@PreUpdate` refreshes `updatedAt` on every subsequent save.

First, create a project so we have something to attach tasks to:

```
POST http://localhost:8080/api/projects/development
Content-Type: application/json

{
  "name": "IronBoard",
  "description": "Project management app",
  "category": "WEB",
  "priority": "HIGH",
  "techStack": "Java, Spring Boot"
}
```

**Check:** The response includes `createdAt` and `updatedAt` with the same value (both set by `@PrePersist` on first insert). These fields come from BaseEntity, not from Project directly.

---

## Concept: @ManyToOne — Task references Project

On Day 6, Task stored `projectId` as a plain Long. Now it stores an actual `@ManyToOne Project project` reference. When you create a task, the service loads the Project entity by ID and sets it on the task.

```
POST http://localhost:8080/api/tasks
Content-Type: application/json

{
  "title": "Setup database",
  "description": "Configure MySQL connection",
  "projectId": 1
}
```

**Flow:** The controller receives the request. The mapper creates a base Task from the DTO. The controller passes the Task and the `projectId` to the service. The service calls `projectService.findById(1)` to load the actual Project entity, then calls `task.setProject(project)`. Hibernate inserts the task into the `tasks` table with `project_id = 1` as the foreign key.

**Check the response (201):**
- `project` is an embedded object: `{ "id": 1, "name": "IronBoard", "projectType": "DEVELOPMENT" }` — this is a ProjectSummary, built by the mapper from the `@ManyToOne` relationship
- `type` is `"TASK"` (base type, no subclass)
- `storyPoints` and `severity` are null

---

## Concept: @Inheritance(SINGLE_TABLE) — creating different task types

Task uses `@Inheritance(SINGLE_TABLE)` with a `type` discriminator column. All task types live in ONE table. The `type` field in the request controls which subclass the mapper creates.

### Create a FeatureTask

```
POST http://localhost:8080/api/tasks
Content-Type: application/json

{
  "title": "User authentication",
  "description": "Implement login flow",
  "projectId": 1,
  "type": "FEATURE",
  "storyPoints": 8
}
```

**Flow:** The mapper reads `type = FEATURE` from the request DTO. It dispatches with an if/else on the TaskType enum and creates a FeatureTask instance (not a base Task). It sets `storyPoints` on the FeatureTask. The controller then passes the FeatureTask and the `projectId` to the service. The service loads the Project entity by ID and calls `task.setProject(project)` to establish the relationship. Then it saves the task. Hibernate inserts a row into the `tasks` table with `type = 'FEATURE'` and `story_points = 8`.

**Check:** `type` is `"FEATURE"`, `storyPoints` is `8`, `severity` is null.

### Create a BugTask

```
POST http://localhost:8080/api/tasks
Content-Type: application/json

{
  "title": "Fix login crash",
  "description": "App crashes on invalid password",
  "projectId": 1,
  "type": "BUG",
  "severity": "HIGH"
}
```

**Flow:** Same dispatch pattern. The mapper creates a BugTask with `severity = "HIGH"`. The controller passes it with the projectId to the service, which loads the Project entity and sets the relationship. Then it saves the task. Hibernate inserts a row into the `tasks` table with `type = 'BUG'` and `severity = 'HIGH'`.

**Check:** `type` is `"BUG"`, `severity` is `"HIGH"`, `storyPoints` is null.

---

## Concept: @OneToMany + EAGER — project returns its tasks

Project has `@OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)`. When you load a project by ID, Hibernate also loads all its tasks in the same operation.

```
GET http://localhost:8080/api/projects/1
```

**Flow:** The controller calls `projectService.findById(1)`. The repository loads the Project. Because of `fetch = EAGER`, Hibernate also runs a query for all tasks with `project_id = 1`. The mapper (called by the controller) converts each task to a TaskSummary (id, title, status, type) and embeds the list in the ProjectResponse.

**Check:** The response includes a `tasks` array with all three tasks. Each task shows its `type` (`"TASK"`, `"FEATURE"`, `"BUG"`). This is the @OneToMany relationship made visible in the API.

---

## Concept: Overridden method update — Task subclass fields without instanceof

Task has methods (`getType()`, `getStoryPoints()`, `setStoryPoints()`, `getSeverity()`, `setSeverity()`) that are overridden in FeatureTask and BugTask. The service calls these directly — no instanceof, no casting. If you call `setStoryPoints()` on a BugTask, it does nothing (the base Task's setter is a no-op).

```
PUT http://localhost:8080/api/tasks/2
Content-Type: application/json

{
  "title": "User auth v2",
  "description": "Updated description",
  "storyPoints": 13
}
```

**Flow:** The controller passes the UpdateTaskRequest to the service. The service loads the task by ID — Hibernate returns the real FeatureTask. The service calls `task.setStoryPoints(13)` — since the loaded entity is a FeatureTask, the overridden setter runs and updates the field. It also calls `task.setSeverity(null)` — on a FeatureTask, `setSeverity()` does nothing because FeatureTask does not override it — the base Task's empty implementation runs. Hibernate updates the row.

**Check:** `storyPoints` is now `13`. `updatedAt` changed (set by `@PreUpdate`). `createdAt` unchanged.



### Partial update a BugTask

```
PATCH http://localhost:8080/api/tasks/3
Content-Type: application/json

{
  "severity": "CRITICAL"
}
```

**Flow:** The service loads the BugTask. Only `severity` is non-null in the request, so only that field is updated via the overridden setter. All other fields are skipped (null check).

**Check:** `severity` changed to `"CRITICAL"`. Everything else unchanged.

---

## Concept: Cascade REMOVE — delete project deletes its tasks

The `@OneToMany` on Project has `cascade = CascadeType.REMOVE`. When a project is deleted, Hibernate automatically deletes all tasks that reference it. Individual tasks can still be deleted independently.

### Delete a single task (no cascade)

```
DELETE http://localhost:8080/api/tasks/1
```

**Flow:** The service deletes just this task. The project is untouched. Other tasks are untouched.

**Check:** 204 No Content. `GET /api/tasks` returns 2 remaining tasks.

### Delete a project (cascade kicks in)

```
DELETE http://localhost:8080/api/projects/1
```

**Flow:** The service calls `deleteById(1)`. Hibernate detects `cascade = CascadeType.REMOVE` on the `tasks` collection. Before deleting the project, Hibernate deletes all tasks where `project_id = 1`. Then it deletes the project row. If the project was a DevelopmentProject, Hibernate also deletes from `development_projects`.

**Check:** 204 No Content. `GET /api/tasks` returns an empty list — the tasks were cascade-deleted with the project.