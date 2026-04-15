package com.ironhack.ironboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProjectMetadata {

    @Column(length = 50)
    private String category;

    @Column(length = 20)
    private String priority;

    public ProjectMetadata() {
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
}
