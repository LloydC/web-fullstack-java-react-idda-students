package com.ironhack.ironboard.entity;

// =============================================
// Task entity — Day 04 (Unit 3): Introduction to JPA
// =============================================
// CHANGED from Unit 2: This class is now a JPA ENTITY.
// Same pattern as Project -- @Entity, @Table, @Id, @Column.
//
// NEW ANNOTATION: @Enumerated(EnumType.STRING)
// =============================================
// WHY STRING instead of ORDINAL?
//   @Enumerated(EnumType.ORDINAL) → stores 0, 1, 2 in the database
//   @Enumerated(EnumType.STRING)  → stores "TODO", "IN_PROGRESS", "DONE"
//
//   ORDINAL is dangerous: if you reorder enum values or insert one
//   in the middle, all existing database rows silently get the WRONG
//   status. Example: add BLOCKED before IN_PROGRESS → old rows with
//   ordinal 1 now map to BLOCKED instead of IN_PROGRESS. Data corruption!
//
//   STRING is safe: the actual name is stored. Reordering doesn't matter.
//   The only risk is RENAMING an enum value (which is rare and obvious).
//
// COMMON MISTAKE: Forgetting @Enumerated. Without it, JPA defaults to
// ORDINAL, which silently works until you change the enum order.
//
// NOTE: projectId stays as a plain Long field for now. We use
// @Column(name = "project_id") to map it to the database column.
// On Day 7 (relationships), this becomes a @ManyToOne association
// with the Project entity. For now, it's just a foreign key value.
// =============================================

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    // PATTERN: Always use EnumType.STRING for enums in the database
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TaskStatus status = TaskStatus.TODO;

    @Column(length = 50)
    private String type;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    public Task() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
