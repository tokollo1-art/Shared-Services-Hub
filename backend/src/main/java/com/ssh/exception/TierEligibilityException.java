package com.ssh.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a youth has reached the reservation ceiling for their tier.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class TierEligibilityException extends RuntimeException {

    public TierEligibilityException(String message) {
        super(message);
    }
}
