package com.ironhack.ironboard.controller;

import com.ironhack.ironboard.dto.request.CreateTaskRequest;
import com.ironhack.ironboard.dto.request.UpdateTaskRequest;
import com.ironhack.ironboard.dto.response.TaskResponse;
import com.ironhack.ironboard.mapper.TaskMapper;
import com.ironhack.ironboard.entity.Task;
import com.ironhack.ironboard.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

// =============================================
// TASK CONTROLLER -- Day 8: DTOs & Mappers
// =============================================
// CHANGED from Day 7:
// 1. Methods now accept request DTOs instead of the Task model.
// 2. Methods now return TaskResponse DTOs instead of Task models.
// 3. TaskMapper converts between models and DTOs in all directions.
//
// PATTERN: Same conversion approach as ProjectController.
// Every write endpoint follows the same flow:
//   1. Request DTO -> TaskMapper.toModel(request) -> Task model
//   2. Task model -> service method -> updated Task model
//   3. Updated Task model -> TaskMapper.toResponse(task) -> TaskResponse DTO
//
// NOTE: The String-to-enum conversion (status field) now happens
// inside TaskMapper.toModel(UpdateTaskRequest), not in the controller.
// This is symmetric with the enum-to-String conversion in toResponse().
// The mapper handles both directions of enum conversion.
// =============================================
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    // Constructor injection -- Spring provides the TaskService bean automatically
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // =============================================
    // GET /api/tasks -- List all tasks (with optional projectId filter)
    // CHANGED: Return type is now List<TaskResponse>.
    //
    // PATTERN: The mapper handles the list conversion via
    // toResponseList(). This keeps the controller thin --
    // it only orchestrates, it doesn't do conversion logic.
    // =============================================
    @GetMapping
    public List<TaskResponse> getTasks(@RequestParam(required = false) Long projectId) {
        List<Task> tasks;
        if (projectId != null) {
            tasks = taskService.findByProjectId(projectId);
        } else {
            tasks = taskService.findAll();
        }

        return TaskMapper.toResponseList(tasks);
    }

    // GET /api/tasks/{id} -- CHANGED: Return type is now TaskResponse
    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable Long id) {
        Task task = taskService.findById(id);
        return TaskMapper.toResponse(task);
    }

    // =============================================
    // POST /api/tasks -- Create a new task
    // CHANGED: Uses TaskMapper.toModel(CreateTaskRequest) to convert
    // the request DTO to a model, then passes it to the service.
    //
    // PATTERN: Same as ProjectController.createProject():
    //   1. Mapper converts DTO to model (sets client-provided fields)
    //   2. Service sets internal fields (id, status = TODO)
    //   3. Service validates the projectId exists
    //   4. Mapper converts the saved model to a response DTO
    // =============================================
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = TaskMapper.toModel(request);
        Task created = taskService.create(task);
        TaskResponse response = TaskMapper.toResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =============================================
    // PUT /api/tasks/{id} -- Full update
    // CHANGED: Uses TaskMapper.toModel(UpdateTaskRequest) to convert
    // the request DTO to a model.
    //
    // NOTE: fullUpdate() applies title, description, and type.
    // Status is NOT changeable via PUT -- use PATCH.
    // The model carries all fields, but the service decides
    // which ones to apply.
    // =============================================
    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id,
                                   @Valid @RequestBody UpdateTaskRequest request) {
        Task updates = TaskMapper.toModel(request);
        Task task = taskService.fullUpdate(id, updates);
        return TaskMapper.toResponse(task);
    }

    // =============================================
    // PATCH /api/tasks/{id} -- Partial update
    // CHANGED: Same mapper pattern as PUT.
    //
    // NOTE: The String-to-enum conversion for status happens
    // inside TaskMapper.toModel(). The mapper calls
    // TaskStatus.valueOf(request.getStatus()) when status is non-null.
    //
    // TIP: If an invalid status string is sent (e.g., "INVALID"),
    // valueOf() throws IllegalArgumentException. The
    // GlobalExceptionHandler catches it and returns 400 Bad Request.
    // =============================================
    @PatchMapping("/{id}")
    public TaskResponse patchTask(@PathVariable Long id,
                                  @Valid @RequestBody UpdateTaskRequest request) {
        Task updates = TaskMapper.toModel(request);
        Task task = taskService.partialUpdate(id, updates);
        return TaskMapper.toResponse(task);
    }

    // DELETE /api/tasks/{id} -- No change (returns void with 204)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
