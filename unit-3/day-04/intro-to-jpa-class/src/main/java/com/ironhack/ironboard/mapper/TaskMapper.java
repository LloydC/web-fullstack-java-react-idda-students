package com.ironhack.ironboard.mapper;

import com.ironhack.ironboard.dto.request.CreateTaskRequest;
import com.ironhack.ironboard.dto.request.UpdateTaskRequest;
import com.ironhack.ironboard.dto.response.TaskResponse;
import com.ironhack.ironboard.entity.Task;
import com.ironhack.ironboard.entity.TaskStatus;

import java.util.ArrayList;
import java.util.List;

// =============================================
// TASK MAPPER -- Day 8: DTOs & Mappers
// =============================================
// NEW: Converts between Task DTOs and Task models.
//
// Same pattern as ProjectMapper: static utility class
// with a private constructor and static conversion methods.
//
// This mapper converts in FOUR directions:
//   1. toModel(CreateTaskRequest):  request DTO -> model  [inbound, create]
//   2. toModel(UpdateTaskRequest):  request DTO -> model  [inbound, update]
//   3. toResponse():  Task -> TaskResponse                [outbound, single]
//   4. toResponseList():  List<Task> -> List<TaskResponse> [outbound, list]
//
// KEY DIFFERENCE from ProjectMapper: Enum conversion.
//
// The Task model has:     TaskStatus status (enum)
// The DTOs have:          String status
//
// Inbound (toModel):  String -> TaskStatus via TaskStatus.valueOf()
// Outbound (toResponse):  TaskStatus -> String via .name()
//
// WHY .name() and not .toString()?
// - .name() always returns the exact enum constant name
//   (e.g., "IN_PROGRESS"). It cannot be overridden.
// - .toString() CAN be overridden in the enum to return
//   something different (e.g., "In Progress").
// - For API responses, .name() is safer because it's
//   predictable and matches what clients send back.
//
// COMMON MISTAKE: Calling TaskStatus.valueOf(null) throws
// NullPointerException. The null check in toModel() guards
// against this for PATCH (where status may not be sent).
// =============================================
public class TaskMapper {

    // PATTERN: Private constructor prevents instantiation -- all methods are static
    private TaskMapper() {
    }

    // =============================================
    // toModel(CreateTaskRequest) -- Inbound conversion for POST
    //
    // PATTERN: Same as ProjectMapper.toModel() -- only sets
    // the fields the client provides. Internal fields (id, status)
    // are NOT set here -- the service handles those.
    //
    // NOTE: status is intentionally NOT set. The service defaults
    // new tasks to TaskStatus.TODO. The client doesn't choose
    // the initial status.
    // =============================================
    public static Task toModel(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setType(request.getType());
        task.setProjectId(request.getProjectId());
        return task;
    }

    // =============================================
    // toModel(UpdateTaskRequest) -- Inbound conversion for PUT/PATCH
    //
    // PATTERN: Same method name, different parameter type (overloading).
    //
    // NOTE: String -> Enum conversion happens here:
    //   request.getStatus() is a String ("TODO", "IN_PROGRESS", "DONE")
    //   TaskStatus.valueOf() converts it to the enum constant.
    //
    // TIP: The null check is important for PATCH. If status is null
    // (not sent in the JSON), we leave task.status as null. The service's
    // partialUpdate() interprets null as "don't change this field."
    //
    // COMMON MISTAKE: Calling TaskStatus.valueOf(null) throws
    // NullPointerException. Always check for null first.
    //
    // TIP: If the client sends an invalid status (e.g., "INVALID"),
    // valueOf() throws IllegalArgumentException. We wrap it in a
    // try-catch to provide a descriptive error message listing the
    // valid values. The GlobalExceptionHandler catches the re-thrown
    // IllegalArgumentException and returns 400 Bad Request.
    //
    // WHY re-throw instead of letting the raw JDK exception through?
    // The default message ("No enum constant com.ironhack.ironboard.entity.TaskStatus.INVALID")
    // leaks internal class names. The custom message is user-friendly:
    // "Invalid status: 'INVALID'. Must be one of: TODO, IN_PROGRESS, DONE"
    // =============================================
    public static Task toModel(UpdateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setType(request.getType());
        if (request.getStatus() != null) {
            try {
                task.setStatus(TaskStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid status: '" + request.getStatus() + "'. Must be one of: TODO, IN_PROGRESS, DONE");
            }
        }
        return task;
    }

    // =============================================
    // toResponse() -- Converts a Task model to a TaskResponse DTO
    //
    // PATTERN: Since TaskResponse is immutable (final fields),
    // we pass all values through the constructor.
    //
    // NOTE: status is always set (defaults to TODO, service
    // always assigns it), so .name() is safe without a null check.
    // =============================================
    public static TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getType(),
                task.getProjectId()
        );
    }

    // =============================================
    // toResponseList() -- Converts a list of Task models to TaskResponse DTOs
    //
    // PATTERN: Centralizes list conversion in the mapper instead
    // of the controller. The controller calls this single method
    // instead of writing its own for-loop.
    // =============================================
    public static List<TaskResponse> toResponseList(List<Task> tasks) {
        List<TaskResponse> result = new ArrayList<>();
        for (Task task : tasks) {
            result.add(toResponse(task));
        }
        return result;
    }
}
