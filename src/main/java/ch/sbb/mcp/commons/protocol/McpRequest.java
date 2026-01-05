package ch.sbb.mcp.commons.protocol;

/**
 * MCP JSON-RPC 2.0 Request.
 * 
 * <p>Represents an incoming request from an MCP client following the JSON-RPC 2.0 specification.</p>
 * 
 * @param jsonrpc Protocol version (must be "2.0")
 * @param id Request identifier (for correlation with response)
 * @param method Method name (e.g., "initialize", "tools/list", "tools/call")
 * @param params Method parameters (optional)
 */
public record McpRequest(
    String jsonrpc,
    Object id,
    String method,
    Object params
) {
    /**
     * Validates that this is a valid JSON-RPC 2.0 request.
     */
    public boolean isValid() {
        return "2.0".equals(jsonrpc) && method != null && !method.isBlank();
    }
}
