package ch.sbb.mcp.commons.handler;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents the result of a tool execution.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolResult {
    
    private final boolean success;
    private final Object content;
    private final String errorMessage;
    private final String errorType;
    
    private ToolResult(boolean success, Object content, String errorMessage, String errorType) {
        this.success = success;
        this.content = content;
        this.errorMessage = errorMessage;
        this.errorType = errorType;
    }
    
    /**
     * Create a successful tool result.
     *
     * @param content the result content
     * @return successful tool result
     */
    public static ToolResult success(Object content) {
        return new ToolResult(true, content, null, null);
    }
    
    /**
     * Create an error tool result.
     *
     * @param errorMessage the error message
     * @return error tool result
     */
    public static ToolResult error(String errorMessage) {
        return new ToolResult(false, null, errorMessage, "Error");
    }
    
    /**
     * Create an error tool result with error type.
     *
     * @param errorMessage the error message
     * @param errorType the error type (e.g., "ValidationException")
     * @return error tool result
     */
    public static ToolResult error(String errorMessage, String errorType) {
        return new ToolResult(false, null, errorMessage, errorType);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public Object getContent() {
        return content;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public String getErrorType() {
        return errorType;
    }
}
