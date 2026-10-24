package com.ssh.controller;

import com.ssh.dto.response.ApiResponse;
import com.ssh.dto.response.PaymentResponse;
import com.ssh.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment breakdowns and sandbox payouts")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/mine")
    @Operation(summary = "List my payments (YOUTH only)")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> mine(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
            "My payments",
            paymentService.listMine(authentication.getName())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a payment (admin, owning SME, or paid youth)")
    public ResponseEntity<ApiResponse<PaymentResponse>> get(
        @PathVariable Long id,
        Authentication authentication) {

        return ResponseEntity.ok(ApiResponse.success(
            "Payment found",
            paymentService.getForUser(id, authentication.getName())));
    }

    @PostMapping("/{id}/payout")
    @Operation(
        summary = "Run a sandbox payout (ADMIN only)",
        description = "Debits the funder reserve wallet and marks the payment PAID with a "
            + "SANDBOX-<uuid> reference. SMEs never handle money."
    )
    public ResponseEntity<ApiResponse<PaymentResponse>> payout(
        @PathVariable Long id,
        Authentication authentication) {

        PaymentResponse payment = paymentService.payout(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Payment paid (sandbox)", payment));
    }
}
