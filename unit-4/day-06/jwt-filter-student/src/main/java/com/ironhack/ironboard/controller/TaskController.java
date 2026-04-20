package com.ironhack.ironboard.controller;

import com.ironhack.ironboard.dto.request.CreateTaskRequest;
import com.ironhack.ironboard.dto.request.FullUpdateTaskRequest;
import com.ironhack.ironboard.dto.request.PatchUpdateTaskRequest;
import com.ironhack.ironboard.dto.response.TaskResponse;
import com.ironhack.ironboard.mapper.TaskMapper;
import com.ironhack.ironboard.entity.Task;
import com.ironhack.ironboard.entity.TaskStatus;
import com.ironhack.ironboard.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET /api/tasks with optional filters: ?projectId=1&status=TODO
    @GetMapping
    public List<TaskResponse> getTasks(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) TaskStatus status) {
        List<Task> tasks;
        if (projectId != null && status != null) {
            tasks = taskService.findByProjectIdAndStatus(projectId, status);
        } else if (projectId != null) {
            tasks = taskService.findByProjectId(projectId);
        } else if (status != null) {
            tasks = taskService.findByStatus(status);
        } else {
            tasks = taskService.findAll();
        }

        return TaskMapper.toResponseList(tasks);
    }

    // Step 09: POST /api/tasks/batch?projectId=1
    @PostMapping("/batch")
    public ResponseEntity<List<TaskResponse>> batchCreateTasks(
            @RequestParam Long projectId,
            @RequestBody List<CreateTaskRequest> requests) {
        List<Task> created = taskService.batchCreate(requests, projectId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TaskMapper.toResponseList(created));
    }

    // GET /api/tasks/stats?projectId=1
    @GetMapping("/stats")
    public List<Object[]> getTaskStats(@RequestParam(required = false) Long projectId) {
        if (projectId != null) {
            return taskService.countByStatusForProject(projectId);
        }
        return taskService.countTasksPerProject();
    }

    // GET /api/tasks/{id}
    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable Long id) {
        Task task = taskService.findById(id);
        return TaskMapper.toResponse(task);
    }

    // POST /api/tasks
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = TaskMapper.toModel(request);
        Task created = taskService.create(task, request.getProjectId());
        TaskResponse response = TaskMapper.toResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/tasks/{id} -- Full update
    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id,
                                   @Valid @RequestBody FullUpdateTaskRequest request) {
        Task task = taskService.fullUpdate(id, request);
        return TaskMapper.toResponse(task);
    }

    // PATCH /api/tasks/{id} -- Partial update
    @PatchMapping("/{id}")
    public TaskResponse patchTask(@PathVariable Long id,
                                  @Valid @RequestBody PatchUpdateTaskRequest request) {
        Task task = taskService.partialUpdate(id, request);
        return TaskMapper.toResponse(task);
    }

    // DELETE /api/tasks/{id} (admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
