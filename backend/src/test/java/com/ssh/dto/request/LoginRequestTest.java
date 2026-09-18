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
 * LoginRequest BDD Tests
 *
 * Purpose: Verify validation and construction for LoginRequest DTO.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
class LoginRequestTest {

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
        @DisplayName("Given valid credentials, When validated, Then there are no violations")
        void shouldHaveNoViolations_ForValidRequest() {
            // Given
            LoginRequest request = new LoginRequest("thandi@ssh.co.za", "SecurePass123");

            // When
            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Given a blank email, When validated, Then a violation is returned")
        void shouldFailValidation_WhenEmailIsBlank() {
            // Given
            LoginRequest request = new LoginRequest("   ", "SecurePass123");

            // When
            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @Test
        @DisplayName("Given an invalid email, When validated, Then a violation is returned")
        void shouldFailValidation_WhenEmailIsInvalid() {
            // Given
            LoginRequest request = new LoginRequest("not-an-email", "SecurePass123");

            // When
            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @Test
        @DisplayName("Given a blank password, When validated, Then a violation is returned")
        void shouldFailValidation_WhenPasswordIsBlank() {
            // Given
            LoginRequest request = new LoginRequest("thandi@ssh.co.za", "");

            // When
            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        }

        @Test
        @DisplayName("Given a password shorter than 6 characters, When validated, Then a violation is returned")
        void shouldFailValidation_WhenPasswordTooShort() {
            // Given
            LoginRequest request = new LoginRequest("thandi@ssh.co.za", "12345");

            // When
            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        }
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then fields are set")
        void shouldSetFields_WhenConstructed() {
            // Given / When
            LoginRequest request = new LoginRequest("thandi@ssh.co.za", "SecurePass123");

            // Then
            assertThat(request.getEmail()).isEqualTo("thandi@ssh.co.za");
            assertThat(request.getPassword()).isEqualTo("SecurePass123");
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given a login request, When toString is called, Then password is not included")
        void shouldExcludePassword_WhenToStringCalled() {
            // Given
            LoginRequest request = new LoginRequest("thandi@ssh.co.za", "SecurePass123");

            // When
            String result = request.toString();

            // Then
            assertThat(result).doesNotContain("SecurePass123");
            assertThat(result).contains("thandi@ssh.co.za");
        }
    }
}
