package com.ssh.event;

/**
 * Published inside the verification transaction once the task, experience,
 * payment and ledger entry have all been created.
 */
public record TaskVerifiedEvent(
    Long taskId,
    Long experienceId,
    Long paymentId,
    Long youthId,
    Long smeId,
    Double totalAmount,
    Double platformFee,
    Double youthAmount
) {
}
