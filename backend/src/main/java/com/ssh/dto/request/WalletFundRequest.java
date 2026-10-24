package com.ssh.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Sandbox top-up for the funder reserve wallet.
 */
public record WalletFundRequest(
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Amount to add in ZAR", example = "25000")
    Double amount,

    @Size(max = 255)
    @Schema(description = "Optional funder reference", example = "CORP-PLEDGE-2026-001")
    String reference
) {
}
