package ch.sbb.mcp.commons.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void apiError_ShouldStoreValues() {
        ApiError error = new ApiError("404", "Not Found");
        assertEquals("404", error.code());
        assertEquals("Not Found", error.message());
        assertNull(error.field());
    }

    @Test
    void baseApiRequest_ShouldStoreCommonFields() {
        BaseApiRequest request = new BaseApiRequest() {};
        request.setRequestId("req-123");
        request.setLanguage("de");
        java.time.Instant now = java.time.Instant.now();
        request.setTimestamp(now);
        
        assertEquals("req-123", request.getRequestId());
        assertEquals("de", request.getLanguage());
        assertEquals(now, request.getTimestamp());
    }

    @Test
    void baseApiResponse_ShouldStoreCommonFields() {
        BaseApiResponse response = new BaseApiResponse() {};
        response.setResponseId("resp-123");
        java.time.Instant now = java.time.Instant.now();
        response.setTimestamp(now);
        
        ApiError error = new ApiError("error", "msg");
        response.addError(error);

        assertEquals("resp-123", response.getResponseId());
        assertEquals(now, response.getTimestamp());
        assertTrue(response.hasErrors());
        assertEquals(1, response.getErrors().size());
        assertEquals(error, response.getErrors().get(0));
    }
}
