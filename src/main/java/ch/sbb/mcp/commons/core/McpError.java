package ch.sbb.mcp.commons.core;

/**
 * Represents an error that occurred during MCP tool execution.
 *
 * <p>MCP errors are returned to the AI agent in a structured format
 * that allows them to understand what went wrong and potentially
 * retry with different parameters.</p>
 *
 * @param code    error code for categorization
 * @param message human-readable error message
 * @param details additional details for debugging (optional)
 * @param retryable whether the operation can be retried
 */
public record McpError(
    ErrorCode code,
    String message,
    String details,
    boolean retryable
) {

    /**
     * Standard MCP error codes.
     */
    public enum ErrorCode {
        /** The requested resource was not found */
        NOT_FOUND("NOT_FOUND"),

        /** The input parameters were invalid */
        INVALID_INPUT("INVALID_INPUT"),

        /** The external API returned an error */
        EXTERNAL_API_ERROR("EXTERNAL_API_ERROR"),

        /** The request timed out */
        TIMEOUT("TIMEOUT"),

        /** Rate limit exceeded */
        RATE_LIMITED("RATE_LIMITED"),

        /** Internal server error */
        INTERNAL_ERROR("INTERNAL_ERROR"),

        /** Service temporarily unavailable */
        SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE"),

        /** Authentication/authorization error */
        UNAUTHORIZED("UNAUTHORIZED");

        private final String code;

        ErrorCode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    /**
     * Creates a NOT_FOUND error.
     */
    public static McpError notFound(String message) {
        return new McpError(ErrorCode.NOT_FOUND, message, null, false);
    }

    /**
     * Creates a validation error (INVALID_INPUT).
     */
    public static McpError validation(String message) {
        return new McpError(ErrorCode.INVALID_INPUT, message, null, false);
    }

    /**
     * Creates an INVALID_INPUT error.
     */
    public static McpError invalidInput(String message, String details) {
        return new McpError(ErrorCode.INVALID_INPUT, message, details, false);
    }

    /**
     * Creates an EXTERNAL_API_ERROR error.
     */
    public static McpError externalApiError(String message, String details) {
        return new McpError(ErrorCode.EXTERNAL_API_ERROR, message, details, true);
    }

    /**
     * Creates a TIMEOUT error.
     */
    public static McpError timeout(String message) {
        return new McpError(ErrorCode.TIMEOUT, message, null, true);
    }

    /**
     * Creates a RATE_LIMITED error.
     */
    public static McpError rateLimited(String message) {
        return new McpError(ErrorCode.RATE_LIMITED, message, null, true);
    }

    /**
     * Creates an INTERNAL_ERROR error.
     */
    public static McpError internalError(String message, String details) {
        return new McpError(ErrorCode.INTERNAL_ERROR, message, details, false);
    }

    /**
     * Creates a SERVICE_UNAVAILABLE error.
     */
    public static McpError serviceUnavailable(String message) {
        return new McpError(ErrorCode.SERVICE_UNAVAILABLE, message, null, true);
    }
}
