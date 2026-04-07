package com.ironhack.ironboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

// =============================================
// GlobalExceptionHandler — Centralized exception handling
// =============================================
// WHY: Without this class, every controller method would need its own
// try-catch blocks to handle exceptions and build error responses.
// That's repetitive and error-prone. @ControllerAdvice acts as a
// "catch net" that intercepts exceptions thrown from ANY controller
// and converts them to proper HTTP responses.
//
// PATTERN: @ControllerAdvice + @ExceptionHandler = centralized error handling.
//   The flow is:
//     1. Client sends request (e.g., GET /api/projects/999)
//     2. Controller calls service (projectService.findById(999))
//     3. Service throws ResourceNotFoundException("Project", 999)
//     4. Exception propagates OUT of the controller method (no try-catch needed)
//     5. Spring intercepts the exception BEFORE sending a response
//     6. Spring finds this @ControllerAdvice class
//     7. Spring matches the exception type to an @ExceptionHandler method
//     8. That method builds and returns the error response (404 JSON)
//
// NOTE: The order of @ExceptionHandler methods matters conceptually:
//   - Specific exceptions first (ResourceNotFoundException, MethodArgumentNotValidException)
//   - Generic catch-all last (Exception.class)
//   Spring picks the most specific handler that matches the thrown exception type.
//
// COMMON MISTAKE: Students forget to add @ControllerAdvice on the class.
//   Without it, Spring doesn't know this class should intercept exceptions.
//   The @ExceptionHandler methods would only work if placed directly
//   inside a controller (which defeats the purpose of centralization).
// =============================================
@ControllerAdvice
public class GlobalExceptionHandler {

    // =============================================
    // Handler 1: ResourceNotFoundException → 404 Not Found
    // =============================================
    // PATTERN: @ExceptionHandler(SomeException.class) tells Spring:
    //   "When SomeException is thrown anywhere in a controller, call THIS method."
    //
    // WHY: The exception flow for a 404:
    //   1. GET /api/projects/999
    //   2. ProjectController.getProjectById(999) calls projectService.findById(999)
    //   3. ProjectService.findById(999) → project is null → throws ResourceNotFoundException
    //   4. The exception bubbles up OUT of getProjectById() (no try-catch there!)
    //   5. Spring sees @ExceptionHandler(ResourceNotFoundException.class) here
    //   6. Spring calls handleResourceNotFound() with the exception
    //   7. We build an ErrorResponse and return it with 404 status
    //
    // COMMON MISTAKE: Students try to catch the exception in the controller with
    //   try-catch. That works but defeats the purpose of centralized handling.

    /**
     * Handles ResourceNotFoundException.
     * Returned when a requested resource does not exist (e.g., GET /api/projects/999).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // =============================================
    // Handler 2: MethodArgumentNotValidException → 400 Bad Request
    // =============================================
    // WHY: This exception is thrown automatically by Spring when @Valid fails.
    //   Example: POST /api/projects with body {"name": ""} → @NotBlank fails.
    //   Spring validates BEFORE the controller method body runs, so the controller
    //   never sees the invalid data.
    //
    // PATTERN: The flow for a validation error:
    //   1. POST /api/projects with body {"name": ""}
    //   2. Spring sees @Valid on the controller parameter
    //   3. Spring runs all validation annotations (@NotBlank, @Size, etc.)
    //   4. @NotBlank fails → Spring throws MethodArgumentNotValidException
    //   5. The controller method body NEVER executes
    //   6. This handler catches it and returns field-specific errors
    //
    // TIP: ex.getBindingResult().getFieldErrors() gives you ALL the field-level
    //   errors. Each FieldError has: the field name, the rejection message, etc.
    //   We build a List of strings in the format "fieldName: error message".
    //   Example: "name: Project name is required"
    //
    // WHY a List instead of a Map? A single field can have MULTIPLE failing
    //   constraints simultaneously. For example, if "name" has both @NotBlank
    //   and @Size(min=2), sending "" triggers BOTH errors. A Map<String, String>
    //   would silently overwrite the first error with the second (HashMap.put()
    //   replaces existing keys). A List preserves ALL errors.
    //
    // WHY ValidationErrorResponse instead of ErrorResponse: Validation errors
    //   need a LIST of field-specific messages (one per failed constraint), not a
    //   single message. A separate DTO keeps things clean — ErrorResponse for
    //   single-message errors, ValidationErrorResponse for field-specific errors.
    //
    // COMMON MISTAKE: Students forget @Valid in the controller method parameter.
    //   Without @Valid, Spring does NOT validate the request body, and this handler
    //   is never triggered. The invalid data passes through silently.

    /**
     * Handles validation errors from @Valid on request bodies.
     * Collects all field-level errors into a list for clear, field-specific feedback.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> fieldErrors = new ArrayList<>();
        for (var error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(error.getField() + ": " + error.getDefaultMessage());
        }

        ValidationErrorResponse response = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                fieldErrors
        );
        return ResponseEntity.badRequest().body(response);
    }

    // =============================================
    // Handler 3: IllegalArgumentException → 400 Bad Request
    // =============================================
    // NEW in Day 8: Catches IllegalArgumentException thrown when
    // the client sends an invalid enum value.
    //
    // Example: PATCH /api/tasks/1 with {"status": "INVALID"}
    //   1. Controller calls TaskMapper.toModel(request)
    //   2. Mapper calls TaskStatus.valueOf("INVALID")
    //   3. valueOf() throws IllegalArgumentException
    //   4. This handler catches it and returns 400
    //
    // NOTE: Without this handler, the IllegalArgumentException would
    // fall through to the catch-all handler and return 500 — wrong!
    // An invalid enum value is a client error (400), not a server error.
    // =============================================

    /**
     * Handles IllegalArgumentException (e.g., invalid enum values in request).
     * Returns 400 with the descriptive message set by the mapper.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }

    // =============================================
    // Handler 4: Catch-all Exception → 500 Internal Server Error
    // =============================================
    // WHY: This is the safety net. If any unexpected exception occurs (e.g.,
    //   NullPointerException, database connection error, etc.), this handler
    //   catches it and returns a generic 500 error.
    //
    // COMMON MISTAKE: Students put ex.getMessage() in the response body.
    //   NEVER do this in production! Internal exception messages can leak:
    //   - Database connection strings
    //   - SQL queries
    //   - Stack traces with class/method names
    //   - File system paths
    //   Always return a generic message: "An unexpected error occurred"
    //
    // TIP: In production, you'd log the full exception (with stack trace) to your
    //   logging system, but return only a generic message to the client.

    /**
     * Catch-all handler for any unexpected exceptions.
     * Returns a generic 500 error — never expose internal details to clients.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
