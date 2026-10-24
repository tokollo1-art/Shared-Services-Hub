package com.ssh.dto.response;

import com.ssh.entity.Payment;

import java.time.LocalDateTime;

public record PaymentResponse(
    Long id,
    Long taskId,
    String taskTitle,
    Long youthId,
    String youthName,
    Double totalAmount,
    Double platformFee,
    Double youthAmount,
    String status,
    String reference,
    LocalDateTime paidAt,
    LocalDateTime createdAt
) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getTask() != null ? payment.getTask().getId() : null,
            payment.getTask() != null ? payment.getTask().getTitle() : null,
            payment.getYouth() != null ? payment.getYouth().getId() : null,
            payment.getYouth() != null ? payment.getYouth().getUser().getName() : null,
            payment.getTotalAmount(),
            payment.getPlatformFee(),
            payment.getYouthAmount(),
            payment.getStatus(),
            payment.getReference(),
            payment.getPaidAt(),
            payment.getCreatedAt()
        );
    }
}
