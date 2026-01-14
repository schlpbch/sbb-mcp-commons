package ch.sbb.mcp.commons.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for managing MCP progress notifications.
 * 
 * <p>Implements the MCP {@code notifications/progress} specification to provide
 * real-time progress updates for long-running operations.</p>
 * 
 * <p>Progress notifications follow this format:</p>
 * <pre>{@code
 * {
 *   "jsonrpc": "2.0",
 *   "method": "notifications/progress",
 *   "params": {
 *     "progressToken": "getTripIntervals-session123-1703251200000",
 *     "progress": 50,
 *     "total": 100
 *   }
 * }
 * }</pre>
 * 
 * @see <a href="https://spec.modelcontextprotocol.io/specification/server/utilities/progress/">MCP Progress Spec</a>
 */
@Service
public class ProgressNotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(ProgressNotificationService.class);
    private static final long CLEANUP_THRESHOLD_MS = 300_000; // 5 minutes
    private static final AtomicLong tokenCounter = new AtomicLong(0);

    private final McpNotificationService notificationService;
    private final Map<String, ProgressState> activeProgress = new ConcurrentHashMap<>();
    
    public ProgressNotificationService(McpNotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Start a new progress tracking operation.
     * 
     * @param sessionId Session identifier
     * @param toolName Name of the tool being executed
     * @return Progress token for this operation
     */
    public String startProgress(String sessionId, String toolName) {
        String token = generateToken(sessionId, toolName);
        ProgressState state = new ProgressState(sessionId, toolName, Instant.now());
        activeProgress.put(token, state);
        
        sendProgress(sessionId, token, 0, null);
        log.debug("Started progress tracking: token={}, tool={}", token, toolName);
        
        return token;
    }
    
    /**
     * Update progress for an ongoing operation.
     * 
     * @param sessionId Session identifier
     * @param token Progress token
     * @param progress Current progress value (0-100)
     * @param total Optional total value (typically 100)
     */
    public void updateProgress(String sessionId, String token, int progress, Integer total) {
        ProgressState state = activeProgress.get(token);
        if (state == null) {
            log.warn("Progress update for unknown token: {}", token);
            return;
        }
        
        state.updateProgress(progress);
        sendProgress(sessionId, token, progress, total);
        log.trace("Progress update: token={}, progress={}/{}", token, progress, total);
    }
    
    /**
     * Mark a progress operation as complete.
     * 
     * @param sessionId Session identifier
     * @param token Progress token
     */
    public void completeProgress(String sessionId, String token) {
        ProgressState state = activeProgress.get(token);
        if (state != null) {
            sendProgress(sessionId, token, 100, 100);
            state.markComplete();
            log.debug("Completed progress tracking: token={}", token);
        }
        
        // Cleanup immediately on completion
        activeProgress.remove(token);
    }
    
    /**
     * Create a fluent progress tracker for step-based progress.
     * 
     * @param sessionId Session identifier
     * @param toolName Name of the tool
     * @param totalSteps Total number of steps
     * @return ProgressTracker instance
     */
    public ProgressTracker createTracker(String sessionId, String toolName, int totalSteps) {
        String token = startProgress(sessionId, toolName);
        return new ProgressTracker(this, sessionId, token, totalSteps);
    }
    
    /**
     * Generate a unique progress token.
     *
     * @param sessionId Session identifier
     * @param toolName Tool name
     * @return Unique progress token
     */
    private String generateToken(String sessionId, String toolName) {
        long timestamp = System.currentTimeMillis();
        long counter = tokenCounter.incrementAndGet();
        return String.format("%s-%s-%d-%d", toolName, sessionId, timestamp, counter);
    }
    
    /**
     * Send a progress notification via SSE.
     * 
     * @param sessionId Session identifier
     * @param token Progress token
     * @param progress Current progress value
     * @param total Optional total value
     */
    private void sendProgress(String sessionId, String token, int progress, Integer total) {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("progressToken", token);
        params.put("progress", progress);
        if (total != null) {
            params.put("total", total);
        }
        
        notificationService.sendNotification(sessionId, "notifications/progress", params);
    }
    
    /**
     * Periodic cleanup of stale progress tokens.
     * Runs every minute to remove tokens older than 5 minutes.
     */
    @Scheduled(fixedRate = 60_000)
    public void cleanupStaleTokens() {
        long now = System.currentTimeMillis();
        final java.util.concurrent.atomic.AtomicInteger removed = new java.util.concurrent.atomic.AtomicInteger(0);
        
        activeProgress.entrySet().removeIf(entry -> {
            ProgressState state = entry.getValue();
            long age = now - state.getStartTime().toEpochMilli();
            
            if (age > CLEANUP_THRESHOLD_MS) {
                log.debug("Removing stale progress token: {} (age: {}ms)", entry.getKey(), age);
                removed.incrementAndGet();
                return true;
            }
            return false;
        });
        
        if (removed.get() > 0) {
            log.info("Cleaned up {} stale progress tokens", removed.get());
        }
    }
    
    /**
     * Get the number of active progress operations.
     * 
     * @return Count of active progress tokens
     */
    public int getActiveCount() {
        return activeProgress.size();
    }
    
    /**
     * Internal state tracking for a progress operation.
     */
    private static class ProgressState {
        private final Instant startTime;
        
        public ProgressState(String sessionId, String toolName, Instant startTime) {
            this.startTime = startTime;
            // sessionId and toolName are intentionally not stored as they're only needed at creation
        }
        
        public void updateProgress(int progress) {
            // Progress is tracked via notifications, no need to store locally
        }
        
        public void markComplete() {
            // Completion is tracked via removal from activeProgress map
        }
        
        public Instant getStartTime() {
            return startTime;
        }
    }
}
