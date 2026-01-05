package ch.sbb.mcp.commons.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for API requests with common fields.
 */
public abstract class BaseApiRequest {
    
    protected String requestId;
    protected String language;
    protected Instant timestamp;
    
    protected BaseApiRequest() {
        this.requestId = UUID.randomUUID().toString();
        this.language = "en";
        this.timestamp = Instant.now();
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
