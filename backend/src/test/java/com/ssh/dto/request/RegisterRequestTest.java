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
 * RegisterRequest BDD Tests
 *
 * Purpose: Verify validation and construction for RegisterRequest DTO.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
class RegisterRequestTest {

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
        @DisplayName("Given valid registration data, When validated, Then there are no violations")
        void shouldHaveNoViolations_ForValidRequest() {
            // Given
            RegisterRequest request = new RegisterRequest(
                "Thandi Nkosi",
                "thandi@ssh.co.za",
                "SecurePass123",
                "YOUTH"
            );

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Given a blank name, When validated, Then a violation is returned")
        void shouldFailValidation_WhenNameIsBlank() {
            // Given
            RegisterRequest request = new RegisterRequest(
                "   ",
                "thandi@ssh.co.za",
                "SecurePass123",
                "YOUTH"
            );

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }

        @Test
        @DisplayName("Given an invalid email, When validated, Then a violation is returned")
        void shouldFailValidation_WhenEmailIsInvalid() {
            // Given
            RegisterRequest request = new RegisterRequest(
                "Thandi Nkosi",
                "not-an-email",
                "SecurePass123",
                "YOUTH"
            );

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @Test
        @DisplayName("Given a password shorter than 8 characters, When validated, Then a violation is returned")
        void shouldFailValidation_WhenPasswordTooShort() {
            // Given
            RegisterRequest request = new RegisterRequest(
                "Thandi Nkosi",
                "thandi@ssh.co.za",
                "short",
                "YOUTH"
            );

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        }

        @Test
        @DisplayName("Given a blank role, When validated, Then a violation is returned")
        void shouldFailValidation_WhenRoleIsBlank() {
            // Given
            RegisterRequest request = new RegisterRequest(
                "Thandi Nkosi",
                "thandi@ssh.co.za",
                "SecurePass123",
                ""
            );

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("role"));
        }

        @Test
        @DisplayName("Given an invalid role value, When validated, Then a violation is returned")
        void shouldFailValidation_WhenRoleIsInvalid() {
            // Given
            RegisterRequest request = new RegisterRequest(
                "Thandi Nkosi",
                "thandi@ssh.co.za",
                "SecurePass123",
                "INVALID_ROLE"
            );

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("role"));
        }

        @Test
        @DisplayName("Given a valid SME role, When validated, Then there are no violations")
        void shouldAllowSmeRole_WhenValid() {
            // Given
            RegisterRequest request = new RegisterRequest(
                "Thandi Nkosi",
                "thandi@ssh.co.za",
                "SecurePass123",
                "SME"
            );

            // When
            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then core fields are set")
        void shouldSetCoreFields_WhenConstructed() {
            // Given / When
            RegisterRequest request = new RegisterRequest(
                "Thandi Nkosi",
                "thandi@ssh.co.za",
                "SecurePass123",
                "SME"
            );

            // Then
            assertThat(request.getName()).isEqualTo("Thandi Nkosi");
            assertThat(request.getEmail()).isEqualTo("thandi@ssh.co.za");
            assertThat(request.getPassword()).isEqualTo("SecurePass123");
            assertThat(request.getRole()).isEqualTo("SME");
        }

        @Test
        @DisplayName("Given an SME request, When optional business fields are set, Then they are stored")
        void shouldStoreOptionalBusinessFields_WhenSet() {
            // Given
            RegisterRequest request = new RegisterRequest(
                "Thandi Nkosi",
                "thandi@ssh.co.za",
                "SecurePass123",
                "SME"
            );

            // When
            request.setBusinessName("Thandi's Accounting");
            request.setRegistrationNumber("2020/123456/07");
            request.setIndustry("Finance");

            // Then
            assertThat(request.getBusinessName()).isEqualTo("Thandi's Accounting");
            assertThat(request.getRegistrationNumber()).isEqualTo("2020/123456/07");
            assertThat(request.getIndustry()).isEqualTo("Finance");
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given a register request, When toString is called, Then password is not included")
        void shouldExcludePassword_WhenToStringCalled() {
            // Given
            RegisterRequest request = new RegisterRequest(
                "Thandi Nkosi",
                "thandi@ssh.co.za",
                "SecurePass123",
                "YOUTH"
            );

            // When
            String result = request.toString();

            // Then
            assertThat(result).doesNotContain("SecurePass123");
            assertThat(result).contains("thandi@ssh.co.za");
            assertThat(result).contains("YOUTH");
        }
    }
}
