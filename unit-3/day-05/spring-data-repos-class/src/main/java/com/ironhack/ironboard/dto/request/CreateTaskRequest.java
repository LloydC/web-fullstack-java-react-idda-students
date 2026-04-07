package com.ironhack.ironboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// =============================================
// CREATE TASK REQUEST DTO — Inbound DTO for POST /api/tasks
// =============================================
// NEW in this step: Request DTOs define what the CLIENT SENDS
// when creating a task. The server defaults status to TODO automatically.
//
// NOTE: status is NOT included here because new tasks always
// start as TODO. The service sets this default.
//
// NOTE: projectId is @NotNull because every task must belong
// to a project. The service validates that the project exists.
// =============================================
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 200, message = "Task title must be between 2 and 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    private String type;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    // No-arg constructor (required for JSON deserialization)
    public CreateTaskRequest() {
    }

    // Getters and setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
