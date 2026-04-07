package com.ironhack.ironboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// =============================================
// CREATE DEVELOPMENT PROJECT REQUEST DTO — Inbound DTO for POST /api/projects/development
// =============================================
// PATTERN: Subclass-specific request DTO for creating DevelopmentProjects.
//
// WHY a separate DTO instead of reusing CreateProjectRequest?
//   DevelopmentProject has fields that base Project doesn't:
//   techStack. We need a DTO that captures
//   ALL required fields for a DevelopmentProject in one request.
//
// =============================================
public class CreateDevelopmentProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private String category;

    private String priority;

    // DevelopmentProject-specific fields.
    // These map to columns in the "development_projects" table.
    @Size(max = 200, message = "Tech stack must be at most 200 characters")
    private String techStack;

    public CreateDevelopmentProjectRequest() {
    }


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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTechStack() {
        return techStack;
    }

    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }

}
