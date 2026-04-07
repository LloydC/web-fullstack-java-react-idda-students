package com.ironhack.ironboard.service;

import com.ironhack.ironboard.exception.ResourceNotFoundException;
import com.ironhack.ironboard.entity.Task;
import com.ironhack.ironboard.entity.TaskStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// =============================================
// TASK SERVICE — Day 8: DTOs & Mappers
// =============================================
// CHANGED from Day 7: create() gains a `type` parameter.
// Previously, type was only set in seed data. Now the API exposes
// a `type` field in CreateTaskRequest, so the service accepts it.
//
// Everything else is UNCHANGED from Day 7.
//
// PATTERN: Same "throw early, handle late" as ProjectService.
// findById() throws → controller propagates → GlobalExceptionHandler
// catches and formats the 404 response.
//
// NOTE: Constructor injection of ProjectService is preserved —
// we still validate that the project exists before creating a task.
// =============================================
@Service
public class TaskService {

    // =============================================
    // NOTE: This service still uses HashMap for storage.
    // Even though the entity classes now have JPA annotations,
    // we have NOT created repositories yet. Hibernate creates
    // the database tables on startup (check MySQL Workbench),
    // but the app still reads/writes from the HashMap.
    // On Day 5, we replace HashMap with JpaRepository.
    // =============================================

    // In-memory storage: maps task ID to Task object (replaced by JpaRepository on Day 5)
    private final Map<Long, Task> tasks = new HashMap<>();

    // Simple counter for generating unique IDs (replaced by @GeneratedValue on Day 5)
    private Long nextId = 1L;

    // Dependency on another service — injected via constructor
    private final ProjectService projectService;

    // PATTERN: Constructor injection — Spring provides the ProjectService bean automatically
    public TaskService(ProjectService projectService) {
        this.projectService = projectService;

        // Seed some tasks so we have data to work with
        Task t1 = new Task();
        t1.setId(nextId++);
        t1.setTitle("Set up Spring Boot project");
        t1.setDescription("Initialize the project with Spring Initializr");
        t1.setStatus(TaskStatus.DONE);
        t1.setType("FEATURE");
        t1.setProjectId(1L);
        tasks.put(t1.getId(), t1);

        Task t2 = new Task();
        t2.setId(nextId++);
        t2.setTitle("Create REST endpoints");
        t2.setDescription("Implement GET and POST endpoints for projects and tasks");
        t2.setStatus(TaskStatus.IN_PROGRESS);
        t2.setType("FEATURE");
        t2.setProjectId(1L);
        tasks.put(t2.getId(), t2);

        Task t3 = new Task();
        t3.setId(nextId++);
        t3.setTitle("Add input validation");
        t3.setDescription("Use Jakarta Validation annotations on model fields");
        t3.setStatus(TaskStatus.TODO);
        t3.setType("FEATURE");
        t3.setProjectId(1L);
        tasks.put(t3.getId(), t3);
    }

    /**
     * Returns all tasks as a list.
     * HashMap.values() equivalent — becomes repository.findAll() on Day 5.
     */
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    // =============================================
    // CHANGED: ResourceNotFoundException instead of RuntimeException
    // =============================================
    // WHY: Same pattern as ProjectService.findById().
    // Throw immediately when the resource is missing — don't return null.
    // The exception propagates through the controller and is caught by
    // GlobalExceptionHandler.handleResourceNotFound() → 404 JSON response.
    // =============================================

    /**
     * Looks up a task by ID.
     * Throws ResourceNotFoundException if the task does not exist.
     */
    public Task findById(Long id) {
        // HashMap.get(id) + null check equivalent — becomes repository.findById(id).orElseThrow() on Day 5
        Task task = tasks.get(id);
        if (task == null) {
            throw new ResourceNotFoundException("Task", id);
        }
        return task;
    }

    // =============================================
    // CHANGED from Day 7: create() now receives a Task model object
    // instead of raw parameters. Same pattern as ProjectService.create().
    //
    // The service sets the INTERNAL fields:
    //   - id (auto-generated)
    //   - status (defaults to TODO -- new tasks always start as TODO)
    //
    // PATTERN: Cross-service validation -- before creating a task,
    // we verify the referenced project exists by calling
    // projectService.findById(). If the project doesn't exist,
    // ProjectService throws ResourceNotFoundException -> 404.
    // =============================================

    /**
     * Assigns an ID and default status to the task, stores it, and returns it.
     * Verifies that the referenced project exists before creating the task.
     */
    public Task create(Task task) {
        // Cross-service validation — verify the project exists before creating a task
        projectService.findById(task.getProjectId());

        // HashMap.put() equivalent — becomes repository.save() on Day 5
        task.setId(nextId++);
        task.setStatus(TaskStatus.TODO);
        tasks.put(task.getId(), task);
        return task;
    }

    /**
     * Returns all tasks that belong to a specific project.
     */
    public List<Task> findByProjectId(Long projectId) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.getProjectId().equals(projectId)) {
                result.add(task);
            }
        }
        return result;
    }

    // =============================================
    // FULL UPDATE -- FOR PUT
    // =============================================
    // CHANGED from Day 7: Receives a Task model object instead
    // of raw parameters. The mapper (TaskMapper.toModel()) converts
    // the UpdateTaskRequest DTO to a model in the controller.
    //
    // PATTERN: fullUpdate() replaces title, description, and type
    // unconditionally. Status is NOT changeable via PUT -- use
    // PATCH for that.
    //
    // TIP: findById() does the existence check for us.
    // If the task doesn't exist, ResourceNotFoundException
    // propagates to GlobalExceptionHandler -> 404.
    // =============================================

    /**
     * Replaces title, description, and type of an existing task (PUT semantics).
     * Status is not changed -- use partialUpdate() for that.
     *
     * @param id the task ID to update
     * @param updates a Task model containing the new field values
     * @return the updated task
     * @throws ResourceNotFoundException if the task doesn't exist
     */
    public Task fullUpdate(Long id, Task updates) {
        Task task = findById(id);
        task.setTitle(updates.getTitle());
        task.setDescription(updates.getDescription());
        task.setType(updates.getType());
        return task;
    }

    // =============================================
    // PARTIAL UPDATE -- FOR PATCH
    // =============================================
    // CHANGED from Day 7: Same model-based signature as fullUpdate().
    //
    // PATTERN: partialUpdate() only sets non-null fields.
    // PATCH can change status and type, which PUT cannot.
    // =============================================

    /**
     * Updates only the non-null fields of an existing task (PATCH semantics).
     * Null fields are left untouched.
     *
     * @param id the task ID to update
     * @param updates a Task model containing the new field values (nulls = don't change)
     * @return the updated task
     * @throws ResourceNotFoundException if the task doesn't exist
     */
    public Task partialUpdate(Long id, Task updates) {
        Task task = findById(id);

        if (updates.getTitle() != null) {
            task.setTitle(updates.getTitle());
        }
        if (updates.getDescription() != null) {
            task.setDescription(updates.getDescription());
        }
        if (updates.getStatus() != null) {
            task.setStatus(updates.getStatus());
        }
        if (updates.getType() != null) {
            task.setType(updates.getType());
        }

        return task;
    }

    // =============================================
    // DELETE METHOD
    // =============================================
    // PATTERN: Same verify-then-delete as ProjectService.
    // findById() → ResourceNotFoundException → GlobalExceptionHandler → 404.
    // =============================================

    /**
     * Deletes a task from the HashMap.
     *
     * @param id the task ID to delete
     * @throws ResourceNotFoundException if the task doesn't exist
     */
    public void delete(Long id) {
        // HashMap.remove() equivalent — becomes repository.deleteById() on Day 5
        findById(id);
        tasks.remove(id);
    }
}
