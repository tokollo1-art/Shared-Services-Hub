package com.ssh.dto.request;

import jakarta.validation.constraints.Size;

/**
 * CompleteTaskRequest
 *
 * Purpose: The body of PATCH /api/v1/tasks/{id}/complete.
 *
 * The reflection is the youth's own account of the work they did. It is
 * optional — a youth can complete a task without writing anything — but
 * when present it is the raw material the AI evidence agent extracts
 * skills from, and the source for the reference draft the SME reviews.
 *
 * Capped at 5000 characters, matching the reflection limit in the Python
 * service's TaskCompletedEvent schema.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public record CompleteTaskRequest(
    @Size(max = 5000, message = "Reflection must be 5000 characters or fewer")
    String reflection
) {}
