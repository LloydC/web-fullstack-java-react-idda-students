package com.ironhack.ironboard.exception;

public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor with a custom message.
     *
     * @param message the error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Convenience constructor that builds a standard "not found" message.
     * Example: "Project not found with id: 42"
     *
     * @param resourceName the type of resource (e.g., "Project", "Task")
     * @param id the ID that was not found
     */
    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }
}
