package com.ironhack.ironboard.service;

import com.ironhack.ironboard.dto.request.UpdateTaskRequest;
import com.ironhack.ironboard.entity.Project;
import com.ironhack.ironboard.entity.Task;
import com.ironhack.ironboard.entity.TaskStatus;
import com.ironhack.ironboard.entity.TaskType;
import com.ironhack.ironboard.exception.ResourceNotFoundException;
import com.ironhack.ironboard.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;

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
     * Throws ResourceNotFoundException if the task does not exist.
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

    public List<Task> findByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    /**
     * Persists a new task to the database.
     * Loads the referenced project and sets it on the task.
     */
    public Task create(Task task, Long projectId) {
        Project project = projectService.findById(projectId);
        task.setProject(project);
        task.setStatus(TaskStatus.TODO);
        return taskRepository.save(task);
    }

    // Update approach: virtual method dispatch (no instanceof needed).
    // Contrast: ProjectService uses instanceof instead.

    /**
     * Replaces fields of an existing task (PUT semantics).
     */
    public Task fullUpdate(Long id, UpdateTaskRequest request) {
        Task task = findById(id);

        // mandatory fields
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required for full update");
        }
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("Status is required for full update");
        }
        try {
            task.setStatus(TaskStatus.valueOf(request.getStatus()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status: '" + request.getStatus() + "'. Must be one of: TODO, IN_PROGRESS, DONE");
        }

        // base fields
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        // subclass fields
        if (task.getType() == TaskType.FEATURE) {
            if (request.getStoryPoints() == null) {
                throw new IllegalArgumentException(
                        "Story points is required for full update of a FEATURE task");
            }
            task.setStoryPoints(request.getStoryPoints());
        } else if (task.getType() == TaskType.BUG) {
            if (request.getSeverity() == null || request.getSeverity().isBlank()) {
                throw new IllegalArgumentException(
                        "Severity is required for full update of a BUG task");
            }
            task.setSeverity(request.getSeverity());
        }

        return taskRepository.save(task);
    }

    /**
     * Updates only the non-null fields of an existing task (PATCH semantics).
     */
    public Task partialUpdate(Long id, UpdateTaskRequest request) {
        Task task = findById(id);

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            try {
                task.setStatus(TaskStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid status: '" + request.getStatus() + "'. Must be one of: TODO, IN_PROGRESS, DONE");
            }
        }

        if (task.getType() == TaskType.FEATURE) {
            if (request.getStoryPoints() != null) {
                task.setStoryPoints(request.getStoryPoints());
            }
        } else if (task.getType() == TaskType.BUG) {
            if (request.getSeverity() != null) {
                task.setSeverity(request.getSeverity());
            }
        }

        return taskRepository.save(task);
    }

    /**
     * Deletes a task from the database.
     *
     * @param id the task ID to delete
     * @throws ResourceNotFoundException if the task doesn't exist
     */
    public void delete(Long id) {
        findById(id);
        taskRepository.deleteById(id);
    }
}
