package com.ssh.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Optional note supplied by a youth when applying for a task.
 */
public record ApplicationRequest(
    @Size(max = 500)
    @Schema(
        description = "Short motivation shown to the SME",
        example = "I have run two Shopify catalogues and can start immediately."
    )
    String coverNote
) {
}
