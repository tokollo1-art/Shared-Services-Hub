package com.ssh.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Payment Request DTO
 *
 * Purpose: Carries payment initiation data from the client to the backend.
 *
 * WHY SSH needs this DTO:
 *   - Only exposes fields needed to create a Payment
 *   - Validates amounts before calculation
 *   - Prevents clients from setting status, paidAt, or reference
 *   - Allows optional platform fee override (for admin adjustments)
 *
 * TODO: Add currency field if multi-currency support is added
 * TODO: Add payout method reference (bank account, mobile money)
 *
 * TDD: Write PaymentRequestTest first
 *       - shouldHaveNoViolations_ForValidRequest()
 *       - shouldFailValidation_WhenTaskIdIsNull()
 *       - shouldFailValidation_WhenYouthIdIsNull()
 *       - shouldFailValidation_WhenTotalAmountIsNull()
 *       - shouldFailValidation_WhenTotalAmountIsNegative()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class PaymentRequest {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotNull(message = "Youth ID is required")
    private Long youthId;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private Double totalAmount;

    @PositiveOrZero(message = "Platform fee percent must be zero or positive")
    private Double platformFeePercent;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public PaymentRequest() {}

    public PaymentRequest(Long taskId, Long youthId, Double totalAmount) {
        this.taskId = taskId;
        this.youthId = youthId;
        this.totalAmount = totalAmount;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getYouthId() {
        return youthId;
    }

    public void setYouthId(Long youthId) {
        this.youthId = youthId;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getPlatformFeePercent() {
        return platformFeePercent;
    }

    public void setPlatformFeePercent(Double platformFeePercent) {
        this.platformFeePercent = platformFeePercent;
    }

    // ============================================
    // TO STRING
    // ============================================

    @Override
    public String toString() {
        return "PaymentRequest{" +
            "taskId=" + taskId +
            ", youthId=" + youthId +
            ", totalAmount=" + totalAmount +
            ", platformFeePercent=" + platformFeePercent +
            '}';
    }
}
