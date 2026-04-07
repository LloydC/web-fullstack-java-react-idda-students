package com.ironhack.ironboard.dto.request;

import jakarta.validation.constraints.Size;

// =============================================
// UPDATE TASK REQUEST DTO — Inbound DTO for PUT/PATCH /api/tasks/{id}
// =============================================
// PATTERN: Same approach as UpdateProjectRequest — @Size only,
// no @NotBlank. All fields are optional for PATCH compatibility.
//
// NOTE: The status field is a String here, not TaskStatus enum.
// WHY: Request DTOs should be framework-agnostic. The mapper
// (TaskMapper.toModel()) converts the String to a TaskStatus enum
// before the service receives it. This keeps the DTO simple and
// the service unchanged.
//
// TIP: Valid status values are: "TODO", "IN_PROGRESS", "DONE".
// If an invalid string is sent (e.g., "INVALID"), valueOf() throws
// IllegalArgumentException. The GlobalExceptionHandler catches it
// and returns a 400 Bad Request.
// =============================================
public class UpdateTaskRequest {

    @Size(min = 2, max = 200, message = "Task title must be between 2 and 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    private String status;

    private String type;

    // No-arg constructor (required for JSON deserialization)
    public UpdateTaskRequest() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
