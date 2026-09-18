package com.ssh.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PaymentRequest BDD Tests
 *
 * Purpose: Verify validation and construction for PaymentRequest DTO.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
class PaymentRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Given valid payment data, When validated, Then there are no violations")
        void shouldHaveNoViolations_ForValidRequest() {
            // Given
            PaymentRequest request = new PaymentRequest(1L, 2L, 1000.0);

            // When
            Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Given a null task ID, When validated, Then a violation is returned")
        void shouldFailValidation_WhenTaskIdIsNull() {
            // Given
            PaymentRequest request = new PaymentRequest();
            request.setYouthId(2L);
            request.setTotalAmount(1000.0);

            // When
            Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("taskId"));
        }

        @Test
        @DisplayName("Given a null youth ID, When validated, Then a violation is returned")
        void shouldFailValidation_WhenYouthIdIsNull() {
            // Given
            PaymentRequest request = new PaymentRequest();
            request.setTaskId(1L);
            request.setTotalAmount(1000.0);

            // When
            Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("youthId"));
        }

        @Test
        @DisplayName("Given a null total amount, When validated, Then a violation is returned")
        void shouldFailValidation_WhenTotalAmountIsNull() {
            // Given
            PaymentRequest request = new PaymentRequest();
            request.setTaskId(1L);
            request.setYouthId(2L);

            // When
            Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("totalAmount"));
        }

        @Test
        @DisplayName("Given a negative total amount, When validated, Then a violation is returned")
        void shouldFailValidation_WhenTotalAmountIsNegative() {
            // Given
            PaymentRequest request = new PaymentRequest(1L, 2L, -100.0);

            // When
            Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("totalAmount"));
        }

        @Test
        @DisplayName("Given a negative platform fee percent, When validated, Then a violation is returned")
        void shouldFailValidation_WhenPlatformFeePercentIsNegative() {
            // Given
            PaymentRequest request = new PaymentRequest(1L, 2L, 1000.0);
            request.setPlatformFeePercent(-5.0);

            // When
            Set<ConstraintViolation<PaymentRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("platformFeePercent"));
        }
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then fields are set")
        void shouldSetFields_WhenConstructed() {
            // Given / When
            PaymentRequest request = new PaymentRequest(1L, 2L, 1000.0);

            // Then
            assertThat(request.getTaskId()).isEqualTo(1L);
            assertThat(request.getYouthId()).isEqualTo(2L);
            assertThat(request.getTotalAmount()).isEqualTo(1000.0);
        }

        @Test
        @DisplayName("Given an optional platform fee, When set, Then it is stored")
        void shouldStorePlatformFee_WhenSet() {
            // Given
            PaymentRequest request = new PaymentRequest(1L, 2L, 1000.0);

            // When
            request.setPlatformFeePercent(12.5);

            // Then
            assertThat(request.getPlatformFeePercent()).isEqualTo(12.5);
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given a payment request, When toString is called, Then key fields are present")
        void shouldIncludeKeyFields_WhenToStringCalled() {
            // Given
            PaymentRequest request = new PaymentRequest(1L, 2L, 1000.0);
            request.setPlatformFeePercent(10.0);

            // When
            String result = request.toString();

            // Then
            assertThat(result).contains("taskId=1");
            assertThat(result).contains("youthId=2");
            assertThat(result).contains("1000.0");
            assertThat(result).contains("10.0");
        }
    }
}
