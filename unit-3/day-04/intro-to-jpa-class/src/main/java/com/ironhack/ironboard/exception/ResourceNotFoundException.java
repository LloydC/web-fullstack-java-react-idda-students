// =============================================
// ResourceNotFoundException - Custom exception for missing resources
// =============================================
//
// WHY: We create a custom exception class so that our GlobalExceptionHandler
// can specifically catch "resource not found" scenarios and return a proper
// 404 HTTP response with a consistent JSON body. Without this, we'd either
// return null (which gives a confusing 200 with empty body) or use generic
// exceptions that don't convey the right HTTP semantics.
//
// PATTERN: Custom exception per HTTP error type. Many real-world apps have:
//   - ResourceNotFoundException    -> 404
//   - BadRequestException          -> 400
//   - UnauthorizedException        -> 401
//   - ConflictException            -> 409
// Each one maps to a specific HTTP status in the GlobalExceptionHandler.
//
// WHY: We extend RuntimeException (unchecked) instead of Exception (checked).
//   - Unchecked: does NOT need to be declared with "throws" in method signatures.
//     This keeps service/controller method signatures clean.
//   - Checked exceptions (extends Exception) force every caller to either
//     catch or declare the exception -- this is verbose and unnecessary here
//     because our GlobalExceptionHandler catches it globally.
//
// COMMON MISTAKE: Students sometimes extend Exception instead of RuntimeException.
//   If you do that, every method that calls findById() would need:
//     public Project findById(Long id) throws ResourceNotFoundException { ... }
//   And every caller of THAT method would also need "throws" -- it propagates
//   up the entire call chain. RuntimeException avoids all of this.
//
// =============================================
/**
 * ResourceNotFoundException - Step 04: Full CRUD + Exceptions
 *
 * NEW: Custom exception thrown when a requested resource (Project, Task, etc.) is not found.
 * Extends RuntimeException so it does not need to be declared in method signatures.
 *
 * The GlobalExceptionHandler catches this exception and returns a 404 response
 * with a consistent JSON error format.
 */
package com.ironhack.ironboard.exception;

public class ResourceNotFoundException extends RuntimeException {

    // TIP: Two constructors give us flexibility in how we throw this exception.
    //   Constructor 1: Custom message for edge cases
    //     throw new ResourceNotFoundException("No active projects found")
    //   Constructor 2: Standard pattern for ID lookups (used 90% of the time)
    //     throw new ResourceNotFoundException("Project", id)

    /**
     * Constructor with a custom message.
     *
     * @param message the error message
     */
    // WHY: super(message) passes the message to RuntimeException, which stores it.
    // Later, ex.getMessage() in the GlobalExceptionHandler retrieves it for the JSON response.
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
    // PATTERN: Convenience constructor that builds a consistent message format.
    // This ensures every "not found" error follows the same pattern:
    //   "Project not found with id: 42"
    //   "Task not found with id: 7"
    // TIP: This is the constructor you'll use most often. It makes the error
    // message self-documenting -- the client immediately knows WHAT wasn't found
    // and WHICH id was requested.
    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }
}
