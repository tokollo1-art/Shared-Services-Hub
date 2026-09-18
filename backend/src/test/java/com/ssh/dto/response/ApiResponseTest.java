package com.ssh.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApiResponse BDD Tests
 *
 * Purpose: Verify construction and factory methods for ApiResponse DTO.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
class ApiResponseTest {

    @Nested
    @DisplayName("Success Factory")
    class SuccessFactory {

        @Test
        @DisplayName("Given data, When success is called, Then success is true and data is set")
        void shouldBuildSuccessResponse_WithData() {
            // Given
            String payload = "Some data";

            // When
            ApiResponse<String> response = ApiResponse.success(payload);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Success");
            assertThat(response.getData()).isEqualTo(payload);
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Given a custom message and data, When success is called, Then both are set")
        void shouldBuildSuccessResponse_WithCustomMessage() {
            // Given
            String payload = "Some data";

            // When
            ApiResponse<String> response = ApiResponse.success("Created successfully", payload);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Created successfully");
            assertThat(response.getData()).isEqualTo(payload);
        }
    }

    @Nested
    @DisplayName("Error Factory")
    class ErrorFactory {

        @Test
        @DisplayName("Given a message, When error is called, Then success is false and data is null")
        void shouldBuildErrorResponse_WithMessage() {
            // Given / When
            ApiResponse<Object> response = ApiResponse.error("Something went wrong");

            // Then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isEqualTo("Something went wrong");
            assertThat(response.getData()).isNull();
            assertThat(response.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Validation Error Factory")
    class ValidationErrorFactory {

        @Test
        @DisplayName("Given field errors, When validationError is called, Then errors are set")
        void shouldBuildValidationError_WithFieldErrors() {
            // Given
            Map<String, String> errors = new HashMap<>();
            errors.put("email", "Email must be valid");
            errors.put("password", "Password must be at least 8 characters");

            // When
            ApiResponse<Object> response =
                ApiResponse.validationError("Validation failed", errors);

            // Then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isEqualTo("Validation failed");
            assertThat(response.getData()).isNull();
            assertThat(response.getErrors()).hasSize(2);
            assertThat(response.getErrors()).containsEntry("email", "Email must be valid");
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GettersAndSetters {

        @Test
        @DisplayName("Given an empty response, When fields are set, Then they are stored")
        void shouldStoreFields_WhenSet() {
            // Given
            ApiResponse<String> response = new ApiResponse<>();

            // When
            response.setSuccess(true);
            response.setMessage("Custom message");
            response.setData("payload");

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Custom message");
            assertThat(response.getData()).isEqualTo("payload");
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given a success response, When toString is called, Then success and message are present")
        void shouldIncludeSuccessAndMessage_WhenToStringCalled() {
            // Given
            ApiResponse<String> response = ApiResponse.success("Done");

            // When
            String result = response.toString();

            // Then
            assertThat(result).contains("success=true");
            assertThat(result).contains("Success");
        }
    }
}
