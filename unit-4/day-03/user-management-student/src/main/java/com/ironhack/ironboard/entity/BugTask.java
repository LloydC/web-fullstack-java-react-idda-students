package com.ironhack.ironboard.entity;

// Task subclass for bug tasks. Uses @Inheritance(SINGLE_TABLE).

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("BUG")
public class BugTask extends Task {

    @Column(length = 20)
    private String severity;

    public BugTask() {
    }

    @Override
    public TaskType getType() {
        return TaskType.BUG;
    }

    @Override
    public String getSeverity() {
        return severity;
    }

    @Override
    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
