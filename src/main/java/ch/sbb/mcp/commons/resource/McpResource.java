package ch.sbb.mcp.commons.resource;

import reactor.core.publisher.Mono;

/**
 * Marker interface for MCP resources that should be discoverable
 * via the resources/list and resources/read endpoints.
 * 
 * <p>All MCP resources should implement this interface to provide
 * metadata for service discovery and documentation.</p>
 * 
 * <p>Resources are automatically discovered by Spring's ApplicationContext
 * and registered in the MCP resource handler.</p>
 * 
 * @since 1.8.0
 */
public interface McpResource {
    
    /**
     * Gets the display name of this resource.
     * 
     * @return resource name (e.g., "Service Calendar")
     */
    String getResourceName();
    
    /**
     * Gets a human-readable description of this resource.
     * 
     * @return resource description
     */
    String getResourceDescription();
    
    /**
     * Gets the REST endpoint path for this resource.
     * 
     * @return endpoint path (e.g., "/mcp/service-calendar")
     */
    String getResourceEndpoint();
    
    /**
     * Gets the data model type name returned by this resource.
     * 
     * @return data model name (e.g., "ServiceCalendar")
     */
    String getResourceDataModel();
    
    /**
     * Indicates whether this resource is currently available.
     * 
     * <p>Default implementation returns true. Override to provide
     * dynamic availability based on configuration or dependencies.</p>
     * 
     * @return true if available, false otherwise
     */
    default boolean isAvailable() {
        return true;
    }
    
    /**
     * Gets the unique URI for this resource.
     * 
     * @return resource URI (e.g. "resource://service-calendar")
     */
    default String getResourceUri() {
        return "resource://" + getResourceName().toLowerCase().replace(" ", "-");
    }

    /**
     * Reads the content of this resource.
     * 
     * @return a Mono emitting the resource content as an Object
     */
    Mono<Object> readResource();
}
