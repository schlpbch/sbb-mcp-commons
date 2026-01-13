package ch.sbb.mcp.commons.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration properties for MCP session management.
 *
 * <p>This configuration allows servers to enable or disable session management
 * based on their architectural needs:</p>
 * <ul>
 *   <li><strong>Stateful servers</strong> (e.g., journey-service-mcp): Enable sessions
 *       for UX enhancements like progress tracking and SSE notifications</li>
 *   <li><strong>Stateless servers</strong> (e.g., swiss-mobility-mcp): Disable sessions
 *       for scalability, managing state at the domain level instead</li>
 * </ul>
 *
 * <p>Example configuration:</p>
 * <pre>
 * mcp:
 *   session:
 *     enabled: true
 *     required: false
 *     timeout: 1h
 * </pre>
 *
 * @see ch.sbb.mcp.commons.session.McpSessionStore
 */
@Configuration
@ConfigurationProperties(prefix = "mcp.session")
public class McpSessionConfig {

    /**
     * Enable session management for this MCP server.
     *
     * <p>Set to {@code true} for servers that need UX enhancements like
     * progress tracking for long-running operations. Set to {@code false}
     * for stateless servers that manage state at the domain level.</p>
     *
     * <p>Default: {@code false} (stateless)</p>
     */
    private boolean enabled = false;

    /**
     * Require session header for all non-initialize requests.
     *
     * <p>Only applicable when {@code enabled=true}. When {@code true},
     * all requests except {@code initialize} must include a valid
     * {@code Mcp-Session-Id} header. When {@code false}, sessions are
     * optional and tools work without session context.</p>
     *
     * <p>Default: {@code false} (optional sessions)</p>
     */
    private boolean required = false;

    /**
     * Session timeout duration.
     *
     * <p>Sessions that haven't been accessed within this duration will
     * be automatically expired and removed from the session store.</p>
     *
     * <p>Default: 1 hour</p>
     */
    private Duration timeout = Duration.ofHours(1);

    /**
     * Maximum number of concurrent sessions.
     *
     * <p>When this limit is reached, the oldest sessions will be evicted
     * to make room for new ones.</p>
     *
     * <p>Default: 1000</p>
     */
    private int maxSessions = 1000;

    // Getters and setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public int getMaxSessions() {
        return maxSessions;
    }

    public void setMaxSessions(int maxSessions) {
        this.maxSessions = maxSessions;
    }
}
