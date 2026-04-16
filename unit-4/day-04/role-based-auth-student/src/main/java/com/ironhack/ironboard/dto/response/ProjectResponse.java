package com.ironhack.ironboard.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String projectType;
    private final String category;
    private final String priority;
    private final String techStack;
    private final String clientName;
    private final Double hourlyRate;
    private final String ownerEmail;
    private final List<TaskSummary> tasks;

    public ProjectResponse(Long id, String name, String description,
                           LocalDateTime createdAt, LocalDateTime updatedAt,
                           String projectType,
                           String category, String priority,
                           String techStack,
                           String clientName, Double hourlyRate,
                           String ownerEmail,
                           List<TaskSummary> tasks) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.projectType = projectType;
        this.category = category;
        this.priority = priority;
        this.techStack = techStack;
        this.clientName = clientName;
        this.hourlyRate = hourlyRate;
        this.ownerEmail = ownerEmail;
        this.tasks = tasks;
    }

    // Getters only -- response DTOs are immutable

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getProjectType() {
        return projectType;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    public String getTechStack() {
        return techStack;
    }

    public String getClientName() {
        return clientName;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public List<TaskSummary> getTasks() {
        return tasks;
    }
}
