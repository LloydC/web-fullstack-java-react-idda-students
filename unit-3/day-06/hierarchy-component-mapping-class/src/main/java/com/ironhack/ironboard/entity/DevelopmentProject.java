package com.ironhack.ironboard.entity;

// =============================================
// DevelopmentProject — Day 06 (Unit 3): Hierarchy & Component Mapping
// =============================================
// @Entity subclass extending Project (JOINED inheritance).
//
// Because Project uses @Inheritance(strategy = InheritanceType.JOINED),
// this entity creates a SEPARATE table "development_projects" that
// stores ONLY the fields defined in THIS class (techStack).
//
// Database layout:
//   projects                     development_projects
//   ───────────────────          ─────────────────────────
//   id (PK)                      id (PK, FK → projects.id)
//   name                         tech_stack
//   description
//   category
//   priority
//   created_at
//   updated_at
//
// The "id" column in "development_projects" is BOTH the primary
// key AND a foreign key pointing to "projects.id". This links
// the child row to its parent row. To load a DevelopmentProject,
// Hibernate JOINs both tables on this id.
// =============================================

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

// @PrimaryKeyJoinColumn(name = "id") explicitly names the
// foreign key column in the child table that references the parent's PK.
@Entity
@Table(name = "development_projects")
@PrimaryKeyJoinColumn(name = "id")
public class DevelopmentProject extends Project {

    // Only subclass-specific fields are declared here.
    @Column(name = "tech_stack", length = 200)
    private String techStack;

    public DevelopmentProject() {
    }

    public String getTechStack() {
        return techStack;
    }

    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }

}
