package ch.sbb.mcp.commons.model;

/**
 * Represents an API error with code, message, and optional field.
 *
 * @param code error code (e.g., "VALIDATION_ERROR", "NOT_FOUND")
 * @param message human-readable error message
 * @param field optional field name that caused the error
 */
public record ApiError(String code, String message, String field) {
    
    public ApiError(String code, String message) {
        this(code, message, null);
    }
}
