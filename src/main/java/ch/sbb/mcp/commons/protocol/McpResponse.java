package ch.sbb.mcp.commons.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * MCP JSON-RPC 2.0 Response.
 * 
 * <p>Represents a response to an MCP client following the JSON-RPC 2.0 specification.</p>
 * 
 *
 * @param jsonrpc Protocol version (always "2.0")
 * @param id Request identifier (correlates with request)
 * @param result Result data (present on success, null on error)
 * @param error Error data (present on error, null on success)
 */
public record McpResponse(
    String jsonrpc,
    Object id,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Object result,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    McpError error
) {
    /**
     * Creates a successful response.
     */
    public static McpResponse success(Object id, Object result) {
        return new McpResponse("2.0", id, result, null);
    }
    
    /**
     * Creates an error response.
     */
    public static McpResponse error(Object id, McpError error) {
        return new McpResponse("2.0", id, null, error);
    }
    
    /**
     * MCP Error structure.
     */
    public record McpError(
        int code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Object data
    ) {
        public static final int PARSE_ERROR = -32700;
        public static final int INVALID_REQUEST = -32600;
        public static final int METHOD_NOT_FOUND = -32601;
        public static final int INVALID_PARAMS = -32602;
        public static final int INTERNAL_ERROR = -32603;
        
        public static McpError parseError(String message) {
            return new McpError(PARSE_ERROR, message, null);
        }
        
        public static McpError invalidRequest(String message) {
            return new McpError(INVALID_REQUEST, message, null);
        }
        
        public static McpError methodNotFound(String method) {
            return new McpError(METHOD_NOT_FOUND, "Method not found: " + method, null);
        }
        
        public static McpError invalidParams(String message) {
            return new McpError(INVALID_PARAMS, message, null);
        }
        
        public static McpError internalError(String message) {
            return new McpError(INTERNAL_ERROR, message, null);
        }
    }
}
