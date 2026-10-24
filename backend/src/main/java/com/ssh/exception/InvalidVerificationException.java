package com.ssh.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when verification input is missing or outside its allowed range.
 * The controller also applies Bean Validation; this is the service-level
 * guard so the invariant holds for every caller.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidVerificationException extends RuntimeException {

    public InvalidVerificationException(String message) {
        super(message);
    }
}
