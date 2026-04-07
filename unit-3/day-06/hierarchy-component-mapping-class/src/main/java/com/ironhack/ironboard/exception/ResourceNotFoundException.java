// =============================================
// ResourceNotFoundException - Custom exception for missing resources
// =============================================
package com.ironhack.ironboard.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }
}
