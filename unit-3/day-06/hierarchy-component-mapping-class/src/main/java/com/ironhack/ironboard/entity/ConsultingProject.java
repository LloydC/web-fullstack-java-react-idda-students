package com.ironhack.ironboard.entity;

// =============================================
// ConsultingProject — Day 06 (Unit 3): Hierarchy & Component Mapping
// =============================================
// @Entity subclass extending Project (JOINED inheritance).
//
// Same pattern as DevelopmentProject — creates a separate table
// "consulting_projects" with ONLY the fields defined here
// (clientName, hourlyRate). Shared fields live in "projects".
//
// Database layout:
//   projects                     consulting_projects
//   ───────────────────          ─────────────────────────
//   id (PK)                      id (PK, FK → projects.id)
//   name                         client_name
//   description                  hourly_rate
//   category
//   priority
//   created_at
//   updated_at
//
// TIP: The "id" column in "consulting_projects" is BOTH the
// primary key AND a foreign key to "projects.id". Hibernate
// manages this relationship automatically.
// =============================================

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

// @PrimaryKeyJoinColumn(name = "id") — same as DevelopmentProject.
// Explicitly names the FK column that links this child table to "projects".
// See DevelopmentProject.java for a full explanation.
@Entity
@Table(name = "consulting_projects")
@PrimaryKeyJoinColumn(name = "id")
public class ConsultingProject extends Project {

    // PATTERN: Only subclass-specific fields are declared here.
    @Column(name = "client_name", length = 100)
    private String clientName;

    @Column(name = "hourly_rate")
    private Double hourlyRate;

    // JPA requires a no-arg constructor
    public ConsultingProject() {
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
}
