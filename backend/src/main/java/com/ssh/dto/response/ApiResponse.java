package com.ssh.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * API Response DTO
 *
 * Purpose: Standard envelope for every API response.
 *
 * WHY SSH needs this DTO:
 *   - Consistent response shape across all endpoints
 *   - Easy for the frontend to parse: success, message, data
 *   - Carries validation errors in a structured way
 *   - Includes a timestamp for debugging and logging
 *
 * TODO: Add trace ID field for distributed tracing
 * TODO: Add pagination metadata if wrapped pages are needed
 *
 * TDD: Write ApiResponseTest first
 *       - shouldBuildSuccessResponse_WithData()
 *       - shouldBuildErrorResponse_WithMessage()
 *       - shouldBuildValidationError_WithFieldErrors()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
public class ApiResponse<T> {

    private boolean success;

    private String message;

    private T data;

    private Map<String, String> errors;

    private LocalDateTime timestamp;

    // ============================================
    // CONSTRUCTORS
    // ============================================

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // ============================================
    // STATIC FACTORY METHODS
    // ============================================

    /**
     * Build a success response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    /**
     * Build a success response with a custom message and data.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Build an error response with a message and no data.
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>(false, message, null);
        return response;
    }

    /**
     * Build a validation error response with field-level errors.
     */
    public static <T> ApiResponse<T> validationError(String message, Map<String, String> errors) {
        ApiResponse<T> response = new ApiResponse<>(false, message, null);
        response.setErrors(errors);
        return response;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // ============================================
    // TO STRING
    // ============================================

    @Override
    public String toString() {
        return "ApiResponse{" +
            "success=" + success +
            ", message='" + message + '\'' +
            ", timestamp=" + timestamp +
            '}';
    }
}
