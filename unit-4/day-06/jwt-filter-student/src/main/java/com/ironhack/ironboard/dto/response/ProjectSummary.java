package com.ironhack.ironboard.dto.response;

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
