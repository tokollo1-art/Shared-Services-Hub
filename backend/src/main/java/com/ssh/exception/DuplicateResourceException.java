package com.ssh.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Duplicate Resource Exception
 *
 * Purpose: Thrown when attempting to create a resource that already exists.
 *
 * WHY SSH needs this exception:
 *   - Registration must reject duplicate emails with a clear 409 response
 *   - Prevents conflicting records from silently overwriting each other
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(resourceName + " already exists with " + fieldName + ": '" + fieldValue + "'");
    }
}
