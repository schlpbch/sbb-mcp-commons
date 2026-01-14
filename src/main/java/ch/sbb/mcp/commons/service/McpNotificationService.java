package ch.sbb.mcp.commons.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage MCP notification sinks and broadcast notifications.
 */
@Service
public class McpNotificationService {
    private static final Logger log = LoggerFactory.getLogger(McpNotificationService.class);
    private static McpNotificationService instance;

    private final Map<String, Sinks.Many<ServerSentEvent<String>>> sessionSinks = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public McpNotificationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        instance = this;
    }

    /**
     * Get the singleton instance for access from non-Spring managed components.
     */
    public static McpNotificationService getInstance() {
        return instance;
    }

    public void registerSink(String sessionId, Sinks.Many<ServerSentEvent<String>> sink) {
        sessionSinks.put(sessionId, sink);
        log.debug("Registered SSE sink for session: {}", sessionId);
    }

    public void removeSink(String sessionId) {
        sessionSinks.remove(sessionId);
        log.debug("Removed SSE sink for session: {}", sessionId);
    }

    public void sendNotification(String sessionId, String method, Object params) {
        try {
            Map<String, Object> notification = Map.of(
                "jsonrpc", "2.0",
                "method", method,
                "params", params
            );
            String data = objectMapper.writeValueAsString(notification);
            sendSseEvent(sessionId, "message", data);
        } catch (Exception e) {
            log.error("Failed to send notification to session: {}", sessionId, e);
        }
    }

    public void sendSseEvent(String sessionId, String event, String data) {
        Sinks.Many<ServerSentEvent<String>> sink = sessionSinks.get(sessionId);
        if (sink != null) {
            sink.tryEmitNext(ServerSentEvent.<String>builder()
                    .event(event)
                    .data(data)
                    .build());
        }
    }

    public void broadcastNotification(String method, Object params) {
        sessionSinks.keySet().forEach(sessionId -> sendNotification(sessionId, method, params));
    }
}
