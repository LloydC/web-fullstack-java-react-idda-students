package com.ironhack.ironboard.service;

import com.ironhack.ironboard.dto.request.FullUpdateTaskRequest;
import com.ironhack.ironboard.dto.request.PatchUpdateTaskRequest;
import com.ironhack.ironboard.dto.request.CreateTaskRequest;
import com.ironhack.ironboard.entity.Project;
import com.ironhack.ironboard.entity.Task;
import com.ironhack.ironboard.entity.TaskStatus;
import com.ironhack.ironboard.entity.TaskType;
import com.ironhack.ironboard.exception.ResourceNotFoundException;
import com.ironhack.ironboard.mapper.TaskMapper;
import com.ironhack.ironboard.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    /**
     * Looks up a task by ID.
     * Throws ResourceNotFoundException if the task does not exist.
     */
    @Transactional(readOnly = true)
    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    /**
     * Returns all tasks that belong to a specific project.
     */
    @Transactional(readOnly = true)
    public List<Task> findByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<Task> findByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    /**
     * Persists a new task to the database.
     * Loads the referenced project and sets it on the task.
     */
    @Transactional
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
     * All required fields must be present — @Valid on the DTO handles that.
     */
    @Transactional
    public Task fullUpdate(Long id, FullUpdateTaskRequest request) {
        Task task = findById(id);

        // Convert status String -- TaskStatus enum
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
    @Transactional
    public Task partialUpdate(Long id, PatchUpdateTaskRequest request) {
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
    @Transactional
    public void delete(Long id) {
        findById(id);
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Object[]> countByStatusForProject(Long projectId) {
        return taskRepository.countByStatusForProject(projectId);
    }

    @Transactional(readOnly = true)
    public List<Object[]> countTasksPerProject() {
        return taskRepository.countTasksPerProject();
    }

    @Transactional(readOnly = true)
    public List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status) {
        return taskRepository.findByProjectIdAndStatusNative(projectId, status.name());
    }

    // Step 09: batch create — all tasks in one transaction, rollback on failure.
    @Transactional
    public List<Task> batchCreate(List<CreateTaskRequest> requests, Long projectId) {
        Project project = projectService.findById(projectId);
        List<Task> created = new ArrayList<>();
        for (CreateTaskRequest request : requests) {
            Task task = TaskMapper.toModel(request);
            task.setProject(project);
            task.setStatus(TaskStatus.TODO);
            created.add(taskRepository.save(task));
        }
        return created;
    }
}
