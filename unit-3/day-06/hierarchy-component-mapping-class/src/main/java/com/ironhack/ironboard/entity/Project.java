package com.ironhack.ironboard.entity;

// =============================================
// Project entity — Day 06 (Unit 3): Hierarchy & Component Mapping
// =============================================
// Step 06 — Project has its own @Id and timestamps.
//
// ─────────────────────────────────────────────
// INHERITANCE STRATEGY: JOINED
// ─────────────────────────────────────────────
// @Inheritance(strategy = InheritanceType.JOINED) means:
//   - Parent table: "projects" (id, name, description, category,
//     priority, created_at, updated_at)
//   - Child table: "development_projects" (id FK → projects,
//     tech_stack)
//   - Child table: "consulting_projects" (id FK → projects,
//     client_name, hourly_rate)
//
// Each child table has a FOREIGN KEY to the parent's id.
// To load a DevelopmentProject, Hibernate JOINs the two tables.
//
// WHY JOINED?
//   - NORMALIZED: no wasted columns (consulting rows don't have
//     tech_stack; development rows don't have client_name).
//   - CLEAN SCHEMA: matches relational design best practices.
//   - Trade-off: queries require JOINs, which are slower for
//     very large datasets. For most apps, this is fine.
//
// ─────────────────────────────────────────────
// @Embedded ProjectMetadata
// ─────────────────────────────────────────────
// The @Embedded annotation tells JPA to take the fields from
// ProjectMetadata (category, priority) and store them as columns
// in THIS entity's table ("projects").
//
// There is NO "project_metadata" table. The embedded fields appear
// as columns directly in "projects":
//   projects(id, name, description, category, priority, created_at, updated_at)
// ─────────────────────────────────────────────
// LIFECYCLE CALLBACKS: @PrePersist and @PreUpdate
// ─────────────────────────────────────────────
// These are JPA lifecycle callbacks — methods that JPA calls
// automatically at specific moments:
//   @PrePersist → called BEFORE the INSERT SQL statement
//   @PreUpdate  → called BEFORE the UPDATE SQL statement
// =============================================

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Inheritance(strategy = InheritanceType.JOINED)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // updatable = false prevents Hibernate from
    // including created_at in UPDATE statements. Once set
    // at creation time, it never changes.
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    // @Embedded flattens ProjectMetadata's fields (category, priority)
    // into the "projects" table. No separate table is created.
    @Embedded
    private ProjectMetadata metadata;

    public Project() {
    }

    // =============================================
    // @PrePersist — called once, right before the first INSERT.
    // Sets both createdAt and updatedAt to the current time.
    // =============================================
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // =============================================
    // @PreUpdate — called before every UPDATE SQL statement.
    // Only changes updatedAt; createdAt is protected by
    // updatable = false on the @Column.
    // =============================================
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
}
