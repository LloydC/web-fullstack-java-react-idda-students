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
public class TaskMapper {

    // Private constructor prevents instantiation -- all methods are static
    private TaskMapper() {
    }

    // =============================================
    // toModel(CreateTaskRequest) -- Inbound conversion for POST
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
    // =============================================
    public static List<TaskResponse> toResponseList(List<Task> tasks) {
        List<TaskResponse> result = new ArrayList<>();
        for (Task task : tasks) {
            result.add(toResponse(task));
        }
        return result;
    }
}
