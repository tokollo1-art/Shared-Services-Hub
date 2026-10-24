package com.ssh.event;

/**
 * Published when the sandbox payout marks a payment PAID.
 */
public record PaymentPaidEvent(
    Long paymentId,
    Long youthId,
    Double amount,
    String reference
) {
}
