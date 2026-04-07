// =============================================
// TaskStatus - Enum for task states (unchanged from previous steps)
// =============================================
/**
 * TaskStatus - Step 04: Introduction to JPA
 *
 * Enum representing the possible states of a task.
 * Using an enum instead of a String ensures only valid values are used.
 *
 * No changes from Step 02.
 */
package com.ironhack.ironboard.entity;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}
