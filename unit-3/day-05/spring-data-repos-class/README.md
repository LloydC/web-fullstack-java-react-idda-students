# IronBoard — Step 05: Spring Data Repositories

IronBoard migrated from HashMap to MySQL via JpaRepository. Services rewritten to use repository injection. Data now persists across application restarts.

## What changed from Step 04

| File                                | Change                                                                              |
| ----------------------------------- | ----------------------------------------------------------------------------------- |
| `repository/ProjectRepository.java` | NEW: JpaRepository + findByNameContainingIgnoreCase                                 |
| `repository/TaskRepository.java`    | NEW: JpaRepository + findByProjectId, findByStatus, findByTitleContainingIgnoreCase |
| `service/ProjectService.java`       | REWRITE: HashMap -- repository calls                                                |
| `service/TaskService.java`          | REWRITE: HashMap -- repository calls                                                |
