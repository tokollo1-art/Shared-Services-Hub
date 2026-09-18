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
 * SMEProfileRequest BDD Tests
 *
 * Purpose: Verify validation and construction for SMEProfileRequest DTO.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
class SMEProfileRequestTest {

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
        @DisplayName("Given valid SME profile data, When validated, Then there are no violations")
        void shouldHaveNoViolations_ForValidRequest() {
            // Given
            SMEProfileRequest request = new SMEProfileRequest("Thandi's Accounting");

            // When
            Set<ConstraintViolation<SMEProfileRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Given a blank business name, When validated, Then a violation is returned")
        void shouldFailValidation_WhenBusinessNameIsBlank() {
            // Given
            SMEProfileRequest request = new SMEProfileRequest("   ");

            // When
            Set<ConstraintViolation<SMEProfileRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("businessName"));
        }

        @Test
        @DisplayName("Given a business name over 100 characters, When validated, Then a violation is returned")
        void shouldFailValidation_WhenBusinessNameTooLong() {
            // Given
            SMEProfileRequest request = new SMEProfileRequest("A".repeat(101));

            // When
            Set<ConstraintViolation<SMEProfileRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("businessName"));
        }

        @Test
        @DisplayName("Given a negative employee count, When validated, Then a violation is returned")
        void shouldFailValidation_WhenEmployeeCountIsNegative() {
            // Given
            SMEProfileRequest request = new SMEProfileRequest("Thandi's Accounting");
            request.setEmployeeCount(-5);

            // When
            Set<ConstraintViolation<SMEProfileRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("employeeCount"));
        }

        @Test
        @DisplayName("Given a description over 500 characters, When validated, Then a violation is returned")
        void shouldFailValidation_WhenDescriptionTooLong() {
            // Given
            SMEProfileRequest request = new SMEProfileRequest("Thandi's Accounting");
            request.setDescription("A".repeat(501));

            // When
            Set<ConstraintViolation<SMEProfileRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("description"));
        }
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given a business name, When constructed, Then businessName is set")
        void shouldSetBusinessName_WhenConstructed() {
            // Given / When
            SMEProfileRequest request = new SMEProfileRequest("Thandi's Accounting");

            // Then
            assertThat(request.getBusinessName()).isEqualTo("Thandi's Accounting");
        }

        @Test
        @DisplayName("Given optional fields set, When read, Then they are stored")
        void shouldStoreOptionalFields_WhenSet() {
            // Given
            SMEProfileRequest request = new SMEProfileRequest("Thandi's Accounting");

            // When
            request.setRegistrationNumber("2020/123456/07");
            request.setIndustry("Finance");
            request.setEmployeeCount(12);
            request.setBbbeeLevel("Level 2");

            // Then
            assertThat(request.getRegistrationNumber()).isEqualTo("2020/123456/07");
            assertThat(request.getIndustry()).isEqualTo("Finance");
            assertThat(request.getEmployeeCount()).isEqualTo(12);
            assertThat(request.getBbbeeLevel()).isEqualTo("Level 2");
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given an SME profile request, When toString is called, Then key fields are present")
        void shouldIncludeKeyFields_WhenToStringCalled() {
            // Given
            SMEProfileRequest request = new SMEProfileRequest("Thandi's Accounting");
            request.setIndustry("Finance");

            // When
            String result = request.toString();

            // Then
            assertThat(result).contains("Thandi's Accounting");
            assertThat(result).contains("Finance");
        }
    }
}
