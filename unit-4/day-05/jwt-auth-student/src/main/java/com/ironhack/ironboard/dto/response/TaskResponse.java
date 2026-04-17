package com.ironhack.ironboard.dto.response;

public class TaskResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String status;
    private final String type;
    private final ProjectSummary project;
    private final Integer storyPoints;
    private final String severity;

    public TaskResponse(Long id, String title, String description, String status,
                        String type, ProjectSummary project,
                        Integer storyPoints, String severity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.type = type;
        this.project = project;
        this.storyPoints = storyPoints;
        this.severity = severity;
    }

    // Getters only -- response DTOs are immutable

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public ProjectSummary getProject() {
        return project;
    }

    public Integer getStoryPoints() {
        return storyPoints;
    }

    public String getSeverity() {
        return severity;
    }
}
