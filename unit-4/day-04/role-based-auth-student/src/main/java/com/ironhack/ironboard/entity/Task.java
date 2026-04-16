package com.ironhack.ironboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@DiscriminatorValue("TASK")
public class Task extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TaskStatus status = TaskStatus.TODO;

    // Step 07: Replaces Long projectId with entity reference (owning side).
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public Task() {
    }

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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    // Step 07: "Virtual methods", overriden on FeatureTask and BugTask.
    // Virtual method calls dispatch correctly through proxies (proxy delegates to real entity).
    // Contrast: ProjectMapper uses instanceof — ok there because projects are eagerly loaded.

    public TaskType getType() {
        return TaskType.TASK;
    }

    public Integer getStoryPoints() {
        return null;
    }

    public void setStoryPoints(Integer storyPoints) {
    }

    public String getSeverity() {
        return null;
    }

    public void setSeverity(String severity) {
    }
}
