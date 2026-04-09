package com.ironhack.ironboard.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Inheritance(strategy = InheritanceType.JOINED)
public class Project extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Embedded
    private ProjectMetadata metadata;

    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<Task> tasks = new ArrayList<>();

    public Project() {
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

    public ProjectMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ProjectMetadata metadata) {
        this.metadata = metadata;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
