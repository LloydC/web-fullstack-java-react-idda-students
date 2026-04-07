# IronBoard — Step 06: Hierarchy & Component Mapping

IronBoard introduces two JPA mapping strategies with full CRUD:

- **@Inheritance(JOINED)** (Project → DevelopmentProject, ConsultingProject): type-specific endpoints
- **@Embeddable** (ProjectMetadata): category + priority embedded in Project

## File change summary

| File                                               | Change                                                               |
| -------------------------------------------------- | -------------------------------------------------------------------- |
| `entity/ProjectMetadata.java`                      | NEW: @Embeddable with category + priority                            |
| `entity/Project.java`                              | + @Inheritance(JOINED), + @Embedded, + @PrePersist/@PreUpdate        |
| `entity/Task.java`                                 | + own @Id + timestamps with @PrePersist/@PreUpdate                   |
| `entity/DevelopmentProject.java`                   | NEW: extends Project, @PrimaryKeyJoinColumn, techStack               |
| `entity/ConsultingProject.java`                    | NEW: extends Project, @PrimaryKeyJoinColumn, clientName + hourlyRate |
| `repository/DevelopmentProjectRepository.java`     | NEW: subclass-specific repository                                    |
| `repository/ConsultingProjectRepository.java`      | NEW: subclass-specific repository                                    |
| `dto/request/CreateDevelopmentProjectRequest.java` | NEW: request DTO for dev projects                                    |
| `dto/request/CreateConsultingProjectRequest.java`  | NEW: request DTO for consulting projects                             |
| `dto/request/CreateProjectRequest.java`            | + category, priority (metadata)                                      |
| `dto/request/UpdateProjectRequest.java`            | + all subclass fields (instanceof-applied)                           |
| `dto/response/ProjectResponse.java`                | + projectType, metadata, subclass fields                             |
| `mapper/ProjectMapper.java`                        | + toModel overloads, instanceof in toResponse                        |
| `service/ProjectService.java`                      | + create/findAll per type, instanceof in update                      |
| `controller/ProjectController.java`                | + POST/GET per type, updated PUT/PATCH                               |

## Testing with Postman

### Create a base project (with embedded metadata)

```
POST http://localhost:8080/api/projects
Content-Type: application/json

{
  "name": "IronBoard",
  "description": "Project management app",
  "category": "WEB",
  "priority": "HIGH"
}
```

### Create a development project

```
POST http://localhost:8080/api/projects/development
Content-Type: application/json

{
  "name": "Mobile Banking App",
  "description": "iOS and Android banking",
  "category": "MOBILE",
  "priority": "HIGH",
  "techStack": "Java, React Native"
}
```

### Create a consulting project

```
POST http://localhost:8080/api/projects/consulting
Content-Type: application/json

{
  "name": "Cloud Migration",
  "description": "Migrate legacy systems to AWS",
  "category": "INFRASTRUCTURE",
  "priority": "MEDIUM",
  "clientName": "Acme Corp",
  "hourlyRate": 150.00
}
```

### Get all projects

```
GET http://localhost:8080/api/projects
```

### Step 5: Get by type

```
GET http://localhost:8080/api/projects/development
```

```
GET http://localhost:8080/api/projects/consulting
```

### Get single project by ID

```
GET http://localhost:8080/api/projects/2
```

### Update a development project (instanceof)

```
PUT http://localhost:8080/api/projects/2
Content-Type: application/json

{
  "name": "Mobile Banking App v2",
  "description": "Updated description",
  "category": "MOBILE",
  "priority": "CRITICAL",
  "techStack": "Kotlin, Jetpack Compose"
}
```

### Partial update a consulting project

```
PATCH http://localhost:8080/api/projects/3
Content-Type: application/json

{
  "hourlyRate": 175.00
}
```

### Delete a project

```
DELETE http://localhost:8080/api/projects/1
```

### Create and verify tasks still work

```
POST http://localhost:8080/api/tasks
Content-Type: application/json

{
  "title": "Setup database",
  "description": "Configure MySQL connection",
  "projectId": 2
}
```

```
GET http://localhost:8080/api/tasks
```
