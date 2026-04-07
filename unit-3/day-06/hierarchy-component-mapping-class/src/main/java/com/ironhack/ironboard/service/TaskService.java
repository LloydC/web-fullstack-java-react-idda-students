package com.ironhack.ironboard.service;

import com.ironhack.ironboard.entity.Task;
import com.ironhack.ironboard.entity.TaskStatus;
import com.ironhack.ironboard.exception.ResourceNotFoundException;
import com.ironhack.ironboard.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// =============================================
// TASK SERVICE — Step 05: Spring Data Repositories
// =============================================
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;

    // Constructor injection -- Spring provides both beans
    public TaskService(TaskRepository taskRepository, ProjectService projectService) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
    }

    /**
     * Returns all tasks from the database.
     */
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    /**
     * Looks up a task by ID.
     */
    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }


    /**
     * Returns all tasks that belong to a specific project.
     */
    public List<Task> findByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    /**
     * Persists a new task to the database.
     * Verifies that the referenced project exists before saving.
     */
    public Task create(Task task) {
        // Verify the project exists -- throws ResourceNotFoundException if not found
        projectService.findById(task.getProjectId());

        task.setStatus(TaskStatus.TODO);
        return taskRepository.save(task);
    }

    /**
     * Replaces title, description, and type of an existing task (PUT semantics).
     */
    public Task fullUpdate(Long id, Task updates) {
        Task task = findById(id);
        task.setTitle(updates.getTitle());
        task.setDescription(updates.getDescription());
        task.setType(updates.getType());
        return taskRepository.save(task);
    }

    // =============================================
    // PARTIAL UPDATE -- PATCH semantics (fetch → modify non-nulls → save)
    // =============================================

    /**
     * Updates only the non-null fields of an existing task (PATCH semantics).
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

        return taskRepository.save(task);
    }

    // =============================================
    // DELETE -- Verify-then-delete
    // =============================================
    /**
     * Deletes a task from the database.
     */
    public void delete(Long id) {
        findById(id);
        taskRepository.deleteById(id);
    }
}
