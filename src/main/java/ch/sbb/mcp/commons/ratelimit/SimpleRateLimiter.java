package ch.sbb.mcp.commons.ratelimit;

import org.springframework.http.HttpStatus;

import ch.sbb.mcp.commons.exception.McpException;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple token bucket rate limiter for preventing DoS attacks.
 * 
 * <p>This implementation uses the token bucket algorithm to limit the rate of operations
 * per client identifier (e.g., IP address, session ID).</p>
 * 
 * <p><strong>Thread Safety:</strong> This class is thread-safe and uses concurrent data structures.</p>
 */
public class SimpleRateLimiter {
    
    private final int maxTokens;
    private final Duration refillInterval;
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    
    /**
     * Creates a new rate limiter.
     * 
     * @param maxTokens Maximum number of tokens (requests) allowed
     * @param refillInterval Time interval for refilling tokens
     */
    public SimpleRateLimiter(int maxTokens, Duration refillInterval) {
        this.maxTokens = maxTokens;
        this.refillInterval = refillInterval;
    }
    
    /**
     * Attempts to acquire a token for the given client.
     * 
     * @param clientId Client identifier (e.g., IP address, session ID)
     * @throws McpException if rate limit is exceeded
     */
    public void checkRateLimit(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, k -> new TokenBucket(maxTokens));
        
        if (!bucket.tryConsume()) {
            throw new McpException(
                "Rate limit exceeded. Please try again later.",
                HttpStatus.TOO_MANY_REQUESTS,
                "RATE_LIMIT_EXCEEDED"
            );
        }
    }
    
    /**
     * Checks if a client is currently rate limited without consuming a token.
     * 
     * @param clientId Client identifier
     * @return true if client is rate limited, false otherwise
     */
    public boolean isRateLimited(String clientId) {
        TokenBucket bucket = buckets.get(clientId);
        return bucket != null && bucket.getAvailableTokens() == 0;
    }
    
    /**
     * Clears rate limit data for a specific client.
     * 
     * @param clientId Client identifier
     */
    public void clearRateLimit(String clientId) {
        buckets.remove(clientId);
    }
    
    /**
     * Clears all rate limit data.
     */
    public void clearAll() {
        buckets.clear();
    }
    
    /**
     * Token bucket for a single client.
     */
    private class TokenBucket {
        private final AtomicInteger tokens;
        private volatile Instant lastRefill;
        
        TokenBucket(int initialTokens) {
            this.tokens = new AtomicInteger(initialTokens);
            this.lastRefill = Instant.now();
        }
        
        synchronized boolean tryConsume() {
            refillIfNeeded();
            
            int currentTokens = tokens.get();
            if (currentTokens > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }
        
        synchronized int getAvailableTokens() {
            refillIfNeeded();
            return tokens.get();
        }
        
        private void refillIfNeeded() {
            Instant now = Instant.now();
            Duration elapsed = Duration.between(lastRefill, now);
            
            if (elapsed.compareTo(refillInterval) >= 0) {
                // Refill tokens
                int tokensToAdd = (int) (elapsed.toMillis() / refillInterval.toMillis());
                int currentTokens = tokens.get();
                int newTokens = Math.min(currentTokens + tokensToAdd, maxTokens);
                tokens.set(newTokens);
                lastRefill = now;
            }
        }
    }
}
