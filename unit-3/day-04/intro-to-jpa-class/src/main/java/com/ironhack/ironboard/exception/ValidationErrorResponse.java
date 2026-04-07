package com.ironhack.ironboard.exception;

import java.time.LocalDateTime;
import java.util.List;

// =============================================
// ValidationErrorResponse — Error response for validation failures
// =============================================
// WHY: Validation errors need a different structure than "not found"
// or "server error" responses. Instead of a single message, we need
// a LIST of field-specific errors — one entry per failed constraint.
//
// PATTERN: Two DTOs for two kinds of errors:
//   - ErrorResponse: single message (404, 500, etc.)
//   - ValidationErrorResponse: field-specific errors (400 validation)
//
// TIP: The fieldErrors list contains entries like:
//   "name: Project name is required"
//   "title: Task title must be between 2 and 200 characters"
// Each entry combines the field name and the error message.
//
// WHY a List instead of a Map? A single field can have MULTIPLE failing
// constraints (e.g., @NotBlank + @Size both fail on ""). A Map<String, String>
// would silently overwrite earlier errors for the same field. A List
// preserves ALL errors — no data loss.
//
// NOTE: Like ErrorResponse, this class is immutable (getters only).
// The timestamp is set automatically in the constructor.
// =============================================
public class ValidationErrorResponse {

    private int status;
    private String error;
    private List<String> fieldErrors;
    private LocalDateTime timestamp;

    public ValidationErrorResponse(int status, String error, List<String> fieldErrors) {
        this.status = status;
        this.error = error;
        this.fieldErrors = fieldErrors;
        this.timestamp = LocalDateTime.now();
    }

    // Getters only — immutable after construction

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public List<String> getFieldErrors() {
        return fieldErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
