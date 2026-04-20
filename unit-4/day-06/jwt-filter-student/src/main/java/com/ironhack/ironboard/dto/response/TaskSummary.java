package com.ironhack.ironboard.dto.response;

// Lightweight DTO for embedding tasks inside ProjectResponse.
public class TaskSummary {

    private final Long id;
    private final String title;
    private final String status;
    private final String type;

    public TaskSummary(Long id, String title, String status, String type) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.type = type;
    }

    // Getters only -- response DTOs are immutable

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }
}
