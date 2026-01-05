package ch.sbb.mcp.commons.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for API responses with common fields.
 */
public abstract class BaseApiResponse {
    
    protected String responseId;
    protected Instant timestamp;
    protected List<ApiError> errors;
    
    protected BaseApiResponse() {
        this.timestamp = Instant.now();
        this.errors = new ArrayList<>();
    }
    
    public String getResponseId() {
        return responseId;
    }
    
    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public List<ApiError> getErrors() {
        return errors;
    }
    
    public void setErrors(List<ApiError> errors) {
        this.errors = errors;
    }
    
    public void addError(ApiError error) {
        this.errors.add(error);
    }
    
    public void addError(String code, String message) {
        this.errors.add(new ApiError(code, message, null));
    }
    
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
