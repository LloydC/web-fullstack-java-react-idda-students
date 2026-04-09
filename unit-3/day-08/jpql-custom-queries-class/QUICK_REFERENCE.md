# IronBoard Architecture -- Quick Reference

## Request Flow

```
HTTP Request
     |
     v
 Controller        Receives HTTP, validates @Valid, delegates to service
     |
     v
  Mapper            Converts request DTO -- entity (create only)
     |
     v
  Service           Business logic, loads relationships, saves via repository
     |
     v
 Repository         JPA interface -- derived queries, @Query JPQL, native SQL
     |
     v
  Entity            JPA-mapped class -- Hibernate generates SQL
     |
     v
  Database          MySQL
     |
     v
  Entity            Hibernate maps result rows -- Java objects
     |
     v
  Mapper            Converts entity -- response DTO
     |
     v
 Controller        Returns ResponseEntity with status code
     |
     v
HTTP Response
```

## Layers

| Layer          | What it does                                                                  |
| -------------- | ----------------------------------------------------------------------------- |
| **Controller** | Receives HTTP requests, validates with @Valid, delegates to service, responds |
| **Service**    | Implements business rules, loads relationships, delegates to repository       |
| **Repository** | Executes queries, returns entities to the service                             |
| **Entity**     | Maps to database tables, defines fields and relationships                     |
| **DTO**        | Defines the API contract -- what clients send and receive                     |
| **Mapper**     | Converts between entities and DTOs                                            |
| **Exception**  | Returns consistent JSON errors for all failure cases                          |

## Entity Hierarchy

```
BaseEntity (@MappedSuperclass)
  ├── id, createdAt, updatedAt, @PrePersist/@PreUpdate
  |
  ├── Project (@Inheritance JOINED)
  |     ├── name, description, @Embedded metadata, @OneToMany tasks
  |     ├── DevelopmentProject (techStack)
  |     └── ConsultingProject (clientName, hourlyRate)
  |
  └── Task (@Inheritance SINGLE_TABLE, discriminator: "type")
        ├── title, description, status, @ManyToOne project
        ├── FeatureTask (storyPoints)     @DiscriminatorValue("FEATURE")
        └── BugTask (severity)            @DiscriminatorValue("BUG")
```

## DTOs per Entity

| Entity             | Create                          | PUT (full)               | PATCH (partial)           | Response              | Summary        |
| ------------------ | ------------------------------- | ------------------------ | ------------------------- | --------------------- | -------------- |
| Project            | CreateProjectRequest            | FullUpdateProjectRequest | PatchUpdateProjectRequest | ProjectResponse       | ProjectSummary |
| DevelopmentProject | CreateDevelopmentProjectRequest | (shared with Project)    | (shared with Project)     | (shared with Project) | --             |
| ConsultingProject  | CreateConsultingProjectRequest  | (shared with Project)    | (shared with Project)     | (shared with Project) | --             |
| Task               | CreateTaskRequest               | FullUpdateTaskRequest    | PatchUpdateTaskRequest    | TaskResponse          | TaskSummary    |

## API Endpoints

### Projects (`/api/projects`)

| Method | Path                        | DTO                             | Returns                | Notes                    |
| ------ | --------------------------- | ------------------------------- | ---------------------- | ------------------------ |
| GET    | `/api/projects`             | --                              | List\<ProjectSummary\> | Optional `?name=` filter |
| GET    | `/api/projects/development` | --                              | List\<ProjectSummary\> | DevelopmentProject only  |
| GET    | `/api/projects/consulting`  | --                              | List\<ProjectSummary\> | ConsultingProject only   |
| GET    | `/api/projects/{id}`        | --                              | ProjectResponse        | Includes embedded tasks  |
| POST   | `/api/projects`             | CreateProjectRequest            | ProjectResponse (201)  | Base project             |
| POST   | `/api/projects/development` | CreateDevelopmentProjectRequest | ProjectResponse (201)  |                          |
| POST   | `/api/projects/consulting`  | CreateConsultingProjectRequest  | ProjectResponse (201)  |                          |
| PUT    | `/api/projects/{id}`        | FullUpdateProjectRequest        | ProjectResponse        | All required fields      |
| PATCH  | `/api/projects/{id}`        | PatchUpdateProjectRequest       | ProjectResponse        | Only non-null applied    |
| DELETE | `/api/projects/{id}`        | --                              | 204                    | Cascade deletes tasks    |

### Tasks (`/api/tasks`)

| Method | Path               | DTO                    | Returns              | Notes                                        |
| ------ | ------------------ | ---------------------- | -------------------- | -------------------------------------------- |
| GET    | `/api/tasks`       | --                     | List\<TaskResponse\> | Optional `?projectId=` `?status=` or both    |
| GET    | `/api/tasks/stats` | --                     | List\<Object[]\>     | Optional `?projectId=` for per-project stats |
| GET    | `/api/tasks/{id}`  | --                     | TaskResponse         |                                              |
| POST   | `/api/tasks`       | CreateTaskRequest      | TaskResponse (201)   | `type` field dispatches subclass             |
| PUT    | `/api/tasks/{id}`  | FullUpdateTaskRequest  | TaskResponse         | Title + status required                      |
| PATCH  | `/api/tasks/{id}`  | PatchUpdateTaskRequest | TaskResponse         | Only non-null applied                        |
| DELETE | `/api/tasks/{id}`  | --                     | 204                  |                                              |

## Subclass Detection -- Two Approaches

IronBoard uses two different approaches intentionally so you see both options:

|              | Project                        | Task                          |
| ------------ | ------------------------------ | ----------------------------- |
| **Approach** | `instanceof`                   | Overridden `getType()` method |
| **Used in**  | ProjectMapper + ProjectService | TaskMapper + TaskService      |

**Overridden methods (`getType()`) is the recommended approach** -- it works in all situations, including through Hibernate proxies. `instanceof` can fail when Hibernate wraps an entity in a proxy object (common with LAZY loading). We use `instanceof` for Project because projects are always directly loaded (never proxied in our app), but this is not guaranteed in all codebases.

## Error Responses

| Exception                                | HTTP Status | Handler                                     |
| ---------------------------------------- | ----------- | ------------------------------------------- |
| ResourceNotFoundException                | 404         | GlobalExceptionHandler                      |
| MethodArgumentNotValidException (@Valid) | 400         | GlobalExceptionHandler (field-level errors) |
| IllegalArgumentException                 | 400         | GlobalExceptionHandler                      |
| Any other Exception                      | 500         | GlobalExceptionHandler (generic message)    |
