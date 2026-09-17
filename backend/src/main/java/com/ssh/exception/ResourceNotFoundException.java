package com.ssh.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Resource Not Found Exception
 *
 * Purpose: Thrown when a requested resource does not exist.
 *
 * WHY SSH needs this exception:
 *   - Consistent 404 responses across all controllers
 *   - Clear message for the frontend
 *   - Avoids returning null and leaking NullPointerException
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(resourceName + " not found with " + fieldName + ": '" + fieldValue + "'");
    }
}
