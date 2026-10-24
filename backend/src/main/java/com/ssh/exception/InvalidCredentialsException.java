package com.ssh.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Invalid Credentials Exception
 *
 * Purpose: Thrown when a login attempt cannot be authenticated.
 *
 * WHY SSH needs this exception:
 *   - The login endpoint must answer with 401 Unauthorized (not 400 Bad Request)
 *     when the caller does not supply usable credentials
 *   - Keeps the HTTP status mapping on the exception, like the other
 *     exceptions in this package
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
