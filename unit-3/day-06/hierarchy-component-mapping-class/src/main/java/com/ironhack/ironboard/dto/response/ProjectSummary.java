package com.ironhack.ironboard.dto.response;

// =============================================
// PROJECT SUMMARY DTO -- Step 06: Hierarchy & Component Mapping
// =============================================
// Lightweight response DTO for list endpoints (GET /api/projects).
//
// CHANGED from Step 05: Added projectType to show the type
// of project (PROJECT, DEVELOPMENT, CONSULTING) in list views.
//
// PATTERN: Immutable (final fields, all-args constructor, getters only).
// =============================================
public class ProjectSummary {

    private final Long id;
    private final String name;
    private final String projectType;

    public ProjectSummary(Long id, String name, String projectType) {
        this.id = id;
        this.name = name;
        this.projectType = projectType;
    }

    // Getters only -- response DTOs are immutable

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProjectType() {
        return projectType;
    }
}
