package ch.sbb.mcp.commons.exception;

import ch.sbb.mcp.commons.validation.ValidationException;
import ch.sbb.mcp.commons.client.ApiClientException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void validationException_ShouldStoreMessage() {
        ValidationException ex = new ValidationException("invalid");
        assertEquals("invalid", ex.getMessage());
    }

    @Test
    void apiClientException_ShouldStoreFields() {
        ApiClientException ex = new ApiClientException("failed", 500, "INTERNAL_ERROR");
        assertEquals("failed", ex.getMessage());
        assertEquals(500, ex.getStatusCode());
        assertEquals("INTERNAL_ERROR", ex.getResponseBody());
        
        ApiClientException exWithCause = new ApiClientException("failed", new RuntimeException("cause"));
        assertEquals("failed", exWithCause.getMessage());
        assertTrue(exWithCause.getCause() instanceof RuntimeException);
    }
}
