package com.ssh.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Mandatory input for verifying completed work.
 *
 * All three fields are required. The rating must be between 1 and 5, and the
 * reference must point at concrete evidence the SME actually checked.
 */
public record VerificationRequest(
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5")
    @Schema(description = "Rating from 1 to 5", example = "5", minimum = "1", maximum = "5")
    Double rating,

    @NotBlank(message = "Feedback is required")
    @Size(max = 1000)
    @Schema(
        description = "What went well and what the youth should keep doing",
        example = "Excellent catalogue work. All 150 products were tagged and the photos are sharp."
    )
    String feedback,

    @NotBlank(message = "Reference is required")
    @Size(max = 255)
    @Schema(
        description = "Evidence the SME checked (URL, delivery note or file reference)",
        example = "https://shop.example.com/collections/all"
    )
    String reference
) {
}
