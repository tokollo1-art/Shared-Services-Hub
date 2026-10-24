package com.ssh.entity;

import java.util.Locale;

/**
 * Task Status
 *
 * Purpose: Defines the lifecycle of a task and the transitions that are
 *          allowed between its states.
 *
 * WHY SSH needs this enum:
 *   - A task must only move through the agreed flow:
 *       OPEN -> IN_PROGRESS -> COMPLETED -> VERIFIED
 *   - Repeat operations (assign twice, complete twice, verify twice) must be
 *     rejected instead of silently overwriting the status
 *   - Rejected work returns to IN_PROGRESS, where the assigned youth can
 *     revise and resubmit it (COMPLETED -> IN_PROGRESS)
 *   - Keeping the rules in one place means every caller enforces the same
 *     state machine
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public enum TaskStatus {

    OPEN,
    IN_PROGRESS,
    COMPLETED,
    VERIFIED,
    CANCELLED;

    /**
     * Returns whether this status may move to {@code next}.
     *
     * <pre>
     * OPEN        -> IN_PROGRESS   (owning SME assigns a youth)
     * IN_PROGRESS -> COMPLETED     (assigned youth submits the work)
     * COMPLETED   -> VERIFIED      (owning SME verifies the work)
     * COMPLETED   -> IN_PROGRESS   (owning SME sends the work back for revision)
     * OPEN        -> CANCELLED     (SME withdraws the task)
     * IN_PROGRESS -> CANCELLED     (SME withdraws the task)
     * </pre>
     *
     * VERIFIED and CANCELLED are terminal.
     *
     * @param next the requested target status
     * @return true when the transition is allowed
     */
    public boolean canTransitionTo(TaskStatus next) {
        if (next == null) {
            return false;
        }
        return switch (this) {
            case OPEN -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == COMPLETED || next == CANCELLED;
            case COMPLETED -> next == VERIFIED || next == IN_PROGRESS;
            case VERIFIED, CANCELLED -> false;
        };
    }

    /**
     * Parses a stored status value.
     *
     * @param value the status as stored on the task
     * @return the matching status
     * @throws IllegalArgumentException when the value is null or unknown
     */
    public static TaskStatus from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Task status must not be null");
        }
        try {
            return TaskStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown task status: '" + value + "'", ex);
        }
    }
}
