package com.ssh.controller;

import com.ssh.dto.response.ApiResponse;
import com.ssh.dto.response.LedgerEntryResponse;
import com.ssh.dto.response.LedgerVerificationResponse;
import com.ssh.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public, unauthenticated proof endpoints. Anyone can inspect verified work
 * and re-check the hash chain without holding a JWT.
 */
@RestController
@RequestMapping("/api/v1/ledger")
@Tag(name = "Public Ledger", description = "Public hash-chained verification of verified work")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/entries")
    @Operation(summary = "List all public ledger entries (no authentication)")
    public ResponseEntity<ApiResponse<List<LedgerEntryResponse>>> entries() {
        List<LedgerEntryResponse> entries = ledgerService.listEntries()
            .stream()
            .map(LedgerEntryResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success("Public ledger", entries));
    }

    @GetMapping("/entries/{id}")
    @Operation(summary = "Get one public ledger entry (no authentication)")
    public ResponseEntity<ApiResponse<LedgerEntryResponse>> entry(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
            "Ledger entry",
            LedgerEntryResponse.from(ledgerService.getEntry(id))));
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify the full hash chain (no authentication)")
    public ResponseEntity<ApiResponse<LedgerVerificationResponse>> verifyChain() {
        LedgerService.ChainVerification result = ledgerService.verifyChain();
        LedgerVerificationResponse response = result.valid()
            ? LedgerVerificationResponse.valid(result.checkedEntries())
            : LedgerVerificationResponse.invalid(
                result.checkedEntries(), result.brokenAtSequence(), result.message());
        return ResponseEntity.ok(ApiResponse.success("Ledger verification", response));
    }

    @GetMapping("/verify/{experienceId}")
    @Operation(summary = "Verify one experience's ledger proof (no authentication)")
    public ResponseEntity<ApiResponse<LedgerVerificationResponse>> verifyExperience(
        @PathVariable Long experienceId) {

        LedgerService.ChainVerification result = ledgerService.verifyExperience(experienceId);
        LedgerVerificationResponse response = result.valid()
            ? LedgerVerificationResponse.valid(result.checkedEntries())
            : LedgerVerificationResponse.invalid(
                result.checkedEntries(), result.brokenAtSequence(), result.message());
        return ResponseEntity.ok(ApiResponse.success("Experience verification", response));
    }
}
