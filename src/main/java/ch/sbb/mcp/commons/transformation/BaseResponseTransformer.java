package ch.sbb.mcp.commons.transformation;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Base class for response transformers with common utility methods.
 *
 * @param <SOURCE> the source type (API response)
 * @param <TARGET> the target type (domain model)
 */
public abstract class BaseResponseTransformer<SOURCE, TARGET> 
    implements ResponseTransformer<SOURCE, TARGET> {
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter HUMAN_READABLE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    
    /**
     * Format an ISO date-time string to human-readable format.
     *
     * @param isoDateTime the ISO date-time string
     * @return formatted date-time (e.g., "05.01.2026 10:30")
     */
    protected String formatDateTime(String isoDateTime) {
        if (isoDateTime == null) {
            return null;
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(isoDateTime, ISO_FORMATTER);
            return dateTime.format(HUMAN_READABLE_FORMATTER);
        } catch (Exception e) {
            return isoDateTime; // Return original if parsing fails
        }
    }
    
    /**
     * Format a duration in minutes to human-readable format.
     *
     * @param minutes the duration in minutes
     * @return formatted duration (e.g., "2h 30m" or "45m")
     */
    protected String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + "m";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (mins == 0) {
            return hours + "h";
        }
        return hours + "h " + mins + "m";
    }
    
    /**
     * Format a duration object to human-readable format.
     *
     * @param duration the duration
     * @return formatted duration
     */
    protected String formatDuration(Duration duration) {
        if (duration == null) {
            return null;
        }
        return formatDuration((int) duration.toMinutes());
    }
    
    /**
     * Sanitize text by removing extra whitespace and trimming.
     *
     * @param text the text to sanitize
     * @return sanitized text
     */
    protected String sanitizeText(String text) {
        if (text == null) {
            return null;
        }
        return text.trim().replaceAll("\\s+", " ");
    }
    
    /**
     * Convert an Instant to ISO string.
     *
     * @param instant the instant
     * @return ISO string representation
     */
    protected String toIsoString(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.toString();
    }
    
    /**
     * Convert epoch milliseconds to ISO string.
     *
     * @param epochMillis the epoch milliseconds
     * @return ISO string representation
     */
    protected String toIsoString(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).toString();
    }
    
    /**
     * Safely get a value or return a default.
     *
     * @param value the value
     * @param defaultValue the default value if null
     * @param <T> the type
     * @return the value or default
     */
    protected <T> T getOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
