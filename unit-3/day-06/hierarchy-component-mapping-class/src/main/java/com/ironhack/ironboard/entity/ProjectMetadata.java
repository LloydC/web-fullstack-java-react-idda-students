package com.ironhack.ironboard.entity;

// =============================================
// ProjectMetadata — Day 06 (Unit 3): Hierarchy & Component Mapping
// =============================================
// PATTERN: @Embeddable — a value object with NO table of its own.
//
// @Embeddable means this class is NOT an entity. It has:
//   - NO @Id (no primary key)
//   - NO table in the database
//   - Its fields are FLATTENED into the owning entity's table.
//
// When Project has:
//   @Embedded private ProjectMetadata metadata;
//
// Hibernate adds "category" and "priority" columns directly
// to the "projects" table. There is NO "project_metadata" table.
//
// =============================================

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProjectMetadata {

    // @Column defines the column name and constraints.
    // These columns appear in the OWNING entity's table (projects),
    // not in a separate table.
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
