package ch.sbb.mcp.commons.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fluent API for tracking progress through multiple steps.
 * 
 * <p>Simplifies progress tracking by automatically calculating percentages
 * based on the current step and total steps.</p>
 * 
 * <p>Usage:</p>
 * <pre>{@code
 * ProgressTracker progress = progressService.createTracker(sessionId, "getTripIntervals", 4);
 * 
 * getOrigin()
 *     .doOnNext(args -> progress.step("Resolving origin"))
 *     .flatMap(...)
 *     .doOnNext(origin -> progress.step("Resolving destination"))
 *     .flatMap(...)
 *     .doOnNext(dest -> progress.step("Fetching journeys"))
 *     .flatMap(...)
 *     .doOnNext(journeys -> progress.step("Processing results"))
 *     .flatMap(...)
 *     .doOnSuccess(result -> progress.complete())
 *     .doOnError(error -> progress.complete());
 * }</pre>
 */
public class ProgressTracker {
    
    private static final Logger log = LoggerFactory.getLogger(ProgressTracker.class);
    
    private final ProgressNotificationService service;
    private final String sessionId;
    private final String token;
    private final int totalSteps;
    
    private int currentStep = 0;
    
    /**
     * Create a new progress tracker.
     * 
     * @param service Progress notification service
     * @param sessionId Session identifier
     * @param token Progress token
     * @param totalSteps Total number of steps
     */
    public ProgressTracker(
            ProgressNotificationService service,
            String sessionId, 
            String token, 
            int totalSteps) {
        if (totalSteps <= 0) {
            throw new IllegalArgumentException("totalSteps must be positive");
        }
        this.service = service;
        this.sessionId = sessionId;
        this.token = token;
        this.totalSteps = totalSteps;
    }
    
    /**
     * Mark a step as complete and send progress update.
     * 
     * @param stepName Description of the step being started/completed
     * @return This tracker for chaining
     */
    public ProgressTracker step(String stepName) {
        if (currentStep >= totalSteps) {
            log.warn("Attempted to update completed progress tracker: {}", token);
            // Still send progress update but stay at 99%
            service.updateProgress(sessionId, token, 99, 100);
            return this;
        }
        
        currentStep++;
        int progress = calculateProgress();
        service.updateProgress(sessionId, token, progress, 100);
        log.trace("Progress step: {} ({}/{}) - {}%", stepName, currentStep, totalSteps, progress);
        
        return this;
    }
    
    /**
     * Mark a step as complete without a description.
     * 
     * @return This tracker for chaining
     */
    public ProgressTracker step() {
        return step("Step " + (currentStep + 1));
    }
    
    /**
     * Update progress to a specific percentage.
     * 
     * <p>Use this for fine-grained control when step-based progress isn't suitable.</p>
     * 
     * @param percentage Progress percentage (0-100)
     * @return This tracker for chaining
     */
    public ProgressTracker setProgress(int percentage) {
        if (currentStep > totalSteps) {
            log.debug("Attempted to update completed progress tracker: {}", token);
            return this;
        }
        
        // Clamp percentage to 0-100
        int clampedPercentage = Math.max(0, Math.min(100, percentage));
        
        service.updateProgress(sessionId, token, clampedPercentage, 100);
        log.trace("Progress set to: {}%", clampedPercentage);
        
        return this;
    }
    
    /**
     * Complete the progress tracking.
     * 
     * <p>Sends a final progress notification with 100% completion.</p>
     */
    public void complete() {
        if (isCompleted()) {
            log.debug("Progress tracker already completed: {}", token);
            return;
        }
        
        // Ensure we mark as done
        currentStep = totalSteps + 1;
        service.completeProgress(sessionId, token);
        log.debug("Progress tracker completed: {} ({}/{} steps)", token, currentStep, totalSteps);
    }
    
    /**
     * Calculate current progress percentage based on completed steps.
     * 
     * @return Progress percentage (0-100)
     */
    private int calculateProgress() {
        if (totalSteps <= 0) return 100;
        int progress = (int) Math.round((double) currentStep / totalSteps * 100);
        return Math.min(99, progress); // Cap at 99% until explicitly completed
    }
    
    /**
     * Get the progress token.
     * 
     * @return Progress token
     */
    public String getToken() {
        return token;
    }

    /**
     * Get the current step number.
     *
     * @return Current step (0-based or 1-based depending on usage, currently 0 initially)
     */
    public int getCurrentStep() {
        return Math.min(currentStep, totalSteps);
    }

    /**
     * Get the total number of steps.
     *
     * @return Total steps
     */
    public int getTotalSteps() {
        return totalSteps;
    }

    /**
     * Check if the tracker is completed.
     *
     * @return true if completed
     */
    public boolean isCompleted() {
        return currentStep > totalSteps;
    }
}
