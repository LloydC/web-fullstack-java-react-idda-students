package com.ironhack.ironboard.entity;

// =============================================
// Project entity — Day 04 (Unit 3): Introduction to JPA
// =============================================
// CHANGED from Unit 2: This class is now a JPA ENTITY.
//
// In Unit 2, Project was a plain POJO -- just a Java class with
// fields, getters, and setters. It had NO connection to a database.
// Data lived in a HashMap inside the service layer.
//
// Now we add JPA annotations that tell Hibernate:
//   @Entity        → "this class maps to a database table"
//   @Table          → "the table is called 'projects'"
//   @Id             → "this field is the primary key"
//   @GeneratedValue → "the database generates the ID (AUTO_INCREMENT)"
//   @Column         → "map this field to a specific column with constraints"
//
// PATTERN: The entity class IS the schema definition.
// Hibernate reads these annotations on startup and creates/updates
// the database table to match. Check MySQL Workbench after running
// the app -- you'll see a "projects" table with the columns defined below.
//

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {

    // @Id = PRIMARY KEY. @GeneratedValue chooses how IDs are generated.
    // Common strategies:
    // - IDENTITY: database AUTO_INCREMENT / identity column (simple; very common with MySQL).
    // - SEQUENCE: uses a DB sequence (common with PostgreSQL/Oracle). Typically best when your DB supports sequences.
    // - TABLE: uses a separate table to simulate a sequence (portable, but slower; rarely chosen today).
    // - AUTO: provider chooses a sensible default based on the DB dialect (often SEQUENCE if available, else IDENTITY).
    // - UUID: uses a UUID (universally unique identifier) as the primary key.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column customizes the DB column: nullable=false -- NOT NULL, length -- VARCHAR(n)

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    // name="created_at" maps camelCase to snake_case. updatable=false -- set once on INSERT, never on UPDATE.
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Project() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
