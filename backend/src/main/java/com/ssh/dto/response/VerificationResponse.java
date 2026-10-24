package com.ssh.dto.response;

/**
 * Everything produced by one successful verification. Returning the task,
 * experience and payment together makes the loop demonstrable from Swagger.
 */
public record VerificationResponse(
    TaskResponse task,
    ExperienceResponse experience,
    PaymentResponse payment
) {
}
