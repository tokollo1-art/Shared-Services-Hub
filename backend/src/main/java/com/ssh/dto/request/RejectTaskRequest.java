package com.ssh.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Mandatory reason for sending completed work back for revision.
 */
public record RejectTaskRequest(
    @NotBlank(message = "A revision reason is required")
    @Size(max = 1000)
    @Schema(
        description = "Actionable reason shown to the assigned youth",
        example = "Please redo the SKU column: 22 products still have the old codes."
    )
    String reason
) {
}
