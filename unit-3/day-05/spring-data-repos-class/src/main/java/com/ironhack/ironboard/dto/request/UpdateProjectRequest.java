package com.ironhack.ironboard.dto.request;

import jakarta.validation.constraints.Size;

// =============================================
// UPDATE PROJECT REQUEST DTO — Inbound DTO for PUT/PATCH /api/projects/{id}
// =============================================
// PATTERN: Fields are optional — no @NotBlank, only @Size.
// This allows the same DTO to be used for both PUT and PATCH.
//
// WHY no @NotBlank? If we added @NotBlank on name, then PATCH
// requests that only send description (without name) would fail
// validation. @Size only validates when the field IS present.
//
// TIP: @Size on a null value does NOT trigger validation.
// So sending {"description": "new desc"} with name=null is fine.
// But sending {"name": "a"} would fail because @Size(min=2) rejects it.
// =============================================
public class UpdateProjectRequest {

    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    // No-arg constructor (required for JSON deserialization)
    public UpdateProjectRequest() {
    }

    // Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
