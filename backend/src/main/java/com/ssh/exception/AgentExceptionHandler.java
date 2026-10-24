package com.ssh.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class AgentExceptionHandler {

    @ExceptionHandler(YouthBusyException.class)
    public ResponseEntity<Map<String, String>> handleYouthBusy(YouthBusyException e) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of(
                "code", "YOUTH_BUSY",
                "message", e.getMessage()
            ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of(
                "code", "NOT_FOUND",
                "message", e.getMessage()
            ));
    }
}
