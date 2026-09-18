package com.ssh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Payment Entity
 * Purpose: Represents a payment made to a youth for completing a task.
 *          Corporates fund the payment; SSH pays the youth directly.
 * WHY SSH needs this entity:
 *   - Corporates fund youth income (SMEs never handle money)
 *   - The payment breakdown shows: total funded, platform fee, youth amount
 *   - Direct payment prevents exploitation and ensures fair compensation
 *   - Enables verified impact reporting for corporate sponsors
 * Payment breakdown:
 *   - totalAmount    = what the corporate funded (e.g., R1,000)
 *   - platformFee    = SSH's cut (e.g., 10% = R100)
 *   - youthAmount    = what the youth receives (e.g., R900)
 * TODO: Add status enum (PENDING, PROCESSING, PAID, FAILED, REFUNDED)
 * TODO: Add PaymentMethod entity or fields for payout details
 * TODO: Add method to calculate breakdown from total
 * TODO: Add @JsonIgnore to task and youth to prevent circular reference
 * TDD: Write PaymentEntityTest first
 *       - shouldHaveNoViolations_ForValidPayment()
 *       - shouldCalculateBreakdown_FromTotalAmount()
 *       - shouldFailValidation_WhenAmountIsNegative()
 *       - shouldMarkAsPaid_WhenStatusChanges()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Setter
@Getter
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Many-to-One relationship with Task.
     * The task that was completed and is being paid for.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /**
     * Many-to-One relationship with Youth.
     * The youth receiving the payment.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "youth_id", nullable = false)
    private Youth youth;

    /**
     * The total amount funded by the corporate for this task.
     */
    @PositiveOrZero(message = "Total amount must be positive or zero")
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    /**
     * The platform fee taken by SSH.
     */
    @PositiveOrZero(message = "Platform fee must be positive or zero")
    @Column(name = "platform_fee", nullable = false)
    private Double platformFee;

    /**
     * The amount the youth receives after the platform fee.
     */
    @PositiveOrZero(message = "Youth amount must be positive or zero")
    @Column(name = "youth_amount", nullable = false)
    private Double youthAmount;

    @NotBlank(message = "Status is required")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, PROCESSING, PAID, FAILED, REFUNDED

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Size(max = 255)
    @Column(length = 255)
    private String reference;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public Payment() {}

    public Payment(Task task, Youth youth, Double totalAmount, Double platformFeePercent) {
        this.task = task;
        this.youth = youth;
        this.totalAmount = totalAmount;
        this.status = "PENDING";
        calculateBreakdown(platformFeePercent);
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Calculates the payment breakdown from the total amount.
     *
     * @param platformFeePercent the platform fee as a percentage (e.g., 10.0 for 10%)
     */
    public void calculateBreakdown(Double platformFeePercent) {
        if (this.totalAmount == null) {
            this.platformFee = 0.0;
            this.youthAmount = 0.0;
            return;
        }
        double feePercent = platformFeePercent != null ? platformFeePercent : 10.0;
        this.platformFee = this.totalAmount * (feePercent / 100.0);
        this.youthAmount = this.totalAmount - this.platformFee;
    }

    /**
     * Marks the payment as PAID and records the timestamp.
     * Should only be called when funds have been disbursed.
     */
    public void markAsPaid(String reference) {
        this.status = "PAID";
        this.paidAt = LocalDateTime.now();
        this.reference = reference;
    }

    // ============================================
    // TO STRING
    // ============================================

    @Override
    public String toString() {
        return "Payment{" +
            "id=" + id +
            ", totalAmount=" + totalAmount +
            ", platformFee=" + platformFee +
            ", youthAmount=" + youthAmount +
            ", status='" + status + '\'' +
            '}';
    }
}
