package com.ssh.controller;

import com.ssh.dto.request.WalletFundRequest;
import com.ssh.dto.response.ApiResponse;
import com.ssh.dto.response.WalletResponse;
import com.ssh.dto.response.WalletTransactionResponse;
import com.ssh.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@Tag(name = "Wallets", description = "Funder reserve wallet and sandbox funding")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/reserve")
    @Operation(summary = "Get the funder reserve wallet (CORPORATE or ADMIN)")
    public ResponseEntity<ApiResponse<WalletResponse>> reserve(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
            "Funder reserve wallet",
            walletService.getReserve(authentication.getName())));
    }

    @PostMapping("/reserve/fund")
    @Operation(
        summary = "Top up the funder reserve (CORPORATE or ADMIN)",
        description = "Sandbox funding only; no real money moves."
    )
    public ResponseEntity<ApiResponse<WalletResponse>> fund(
        @Valid @RequestBody WalletFundRequest request,
        Authentication authentication) {

        return ResponseEntity.ok(ApiResponse.success(
            "Reserve wallet funded",
            walletService.fund(
                request.amount(),
                request.reference(),
                authentication.getName())));
    }

    @GetMapping("/reserve/transactions")
    @Operation(summary = "List reserve wallet transactions (CORPORATE or ADMIN)")
    public ResponseEntity<ApiResponse<List<WalletTransactionResponse>>> transactions(
        Authentication authentication) {

        return ResponseEntity.ok(ApiResponse.success(
            "Reserve wallet transactions",
            walletService.listTransactions(authentication.getName())));
    }
}
