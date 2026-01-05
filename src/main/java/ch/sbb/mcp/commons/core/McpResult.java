package ch.sbb.mcp.commons.core;

/**
 * Represents the result of an MCP tool invocation.
 *
 * <p>A tool result can be either successful (with data) or failed (with error).
 * This sealed interface enables exhaustive pattern matching.</p>
 *
 * @param <T> the type of the result data
 */
public sealed interface McpResult<T> {

    /**
     * Checks if this result is successful.
     *
     * @return true if successful
     */
    boolean isSuccess();

    /**
     * A successful tool result containing data.
     *
     * @param data the result data
     */
    record Success<T>(T data) implements McpResult<T> {
        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    /**
     * A failed tool result containing an error.
     *
     * @param error the error details
     */
    record Failure<T>(McpError error) implements McpResult<T> {
        @Override
        public boolean isSuccess() {
            return false;
        }
    }

    /**
     * Creates a successful result.
     *
     * @param data the result data
     * @return a Success instance
     */
    static <T> McpResult<T> success(T data) {
        return new Success<>(data);
    }

    /**
     * Creates a failed result.
     *
     * @param error the error details
     * @return a Failure instance
     */
    static <T> McpResult<T> failure(McpError error) {
        return new Failure<>(error);
    }

    /**
     * Creates a not found failure.
     *
     * @param message the error message
     * @return a Failure instance
     */
    static <T> McpResult<T> notFound(String message) {
        return failure(McpError.notFound(message));
    }

    /**
     * Creates an invalid input failure.
     *
     * @param message the error message
     * @param details additional details
     * @return a Failure instance
     */
    static <T> McpResult<T> invalidInput(String message, String details) {
        return failure(McpError.invalidInput(message, details));
    }
}
