package ch.sbb.mcp.commons.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for McpSessionConfig.
 */
@DisplayName("MCP Session Config Tests")
class McpSessionConfigTest {

    @Test
    @DisplayName("should have correct default values")
    void shouldHaveCorrectDefaults() {
        McpSessionConfig config = new McpSessionConfig();

        assertThat(config.isEnabled()).isFalse();
        assertThat(config.isRequired()).isFalse();
        assertThat(config.getTimeout()).isEqualTo(Duration.ofHours(1));
        assertThat(config.getMaxSessions()).isEqualTo(1000);
    }

    @Test
    @DisplayName("should allow enabling sessions")
    void shouldAllowEnablingSessions() {
        McpSessionConfig config = new McpSessionConfig();
        config.setEnabled(true);

        assertThat(config.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("should allow making sessions required")
    void shouldAllowMakingSessionsRequired() {
        McpSessionConfig config = new McpSessionConfig();
        config.setRequired(true);

        assertThat(config.isRequired()).isTrue();
    }

    @Test
    @DisplayName("should allow custom timeout")
    void shouldAllowCustomTimeout() {
        McpSessionConfig config = new McpSessionConfig();
        Duration customTimeout = Duration.ofMinutes(30);
        config.setTimeout(customTimeout);

        assertThat(config.getTimeout()).isEqualTo(customTimeout);
    }

    @Test
    @DisplayName("should allow custom max sessions")
    void shouldAllowCustomMaxSessions() {
        McpSessionConfig config = new McpSessionConfig();
        config.setMaxSessions(500);

        assertThat(config.getMaxSessions()).isEqualTo(500);
    }

    @Test
    @DisplayName("should support stateful server configuration")
    void shouldSupportStatefulServerConfig() {
        McpSessionConfig config = new McpSessionConfig();
        
        // Simulate journey-service-mcp configuration
        config.setEnabled(true);
        config.setRequired(false);  // Optional sessions
        config.setTimeout(Duration.ofHours(1));
        config.setMaxSessions(1000);

        assertThat(config.isEnabled()).isTrue();
        assertThat(config.isRequired()).isFalse();
        assertThat(config.getTimeout()).isEqualTo(Duration.ofHours(1));
        assertThat(config.getMaxSessions()).isEqualTo(1000);
    }

    @Test
    @DisplayName("should support stateless server configuration")
    void shouldSupportStatelessServerConfig() {
        McpSessionConfig config = new McpSessionConfig();
        
        // Simulate swiss-mobility-mcp configuration (default)
        // Sessions disabled by default

        assertThat(config.isEnabled()).isFalse();
        assertThat(config.isRequired()).isFalse();
    }

    @Test
    @DisplayName("should support required session mode")
    void shouldSupportRequiredSessionMode() {
        McpSessionConfig config = new McpSessionConfig();
        
        // Strict session mode
        config.setEnabled(true);
        config.setRequired(true);

        assertThat(config.isEnabled()).isTrue();
        assertThat(config.isRequired()).isTrue();
    }

    @Test
    @DisplayName("should handle various timeout durations")
    void shouldHandleVariousTimeoutDurations() {
        McpSessionConfig config = new McpSessionConfig();

        // Test different timeout values
        config.setTimeout(Duration.ofMinutes(5));
        assertThat(config.getTimeout()).isEqualTo(Duration.ofMinutes(5));

        config.setTimeout(Duration.ofHours(2));
        assertThat(config.getTimeout()).isEqualTo(Duration.ofHours(2));

        config.setTimeout(Duration.ofDays(1));
        assertThat(config.getTimeout()).isEqualTo(Duration.ofDays(1));
    }

    @Test
    @DisplayName("should handle edge case max sessions values")
    void shouldHandleEdgeCaseMaxSessions() {
        McpSessionConfig config = new McpSessionConfig();

        // Very small limit
        config.setMaxSessions(1);
        assertThat(config.getMaxSessions()).isEqualTo(1);

        // Very large limit
        config.setMaxSessions(100000);
        assertThat(config.getMaxSessions()).isEqualTo(100000);

        // Zero (unlimited)
        config.setMaxSessions(0);
        assertThat(config.getMaxSessions()).isEqualTo(0);
    }
}
