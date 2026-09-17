package com.ssh.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserEntity BDD Tests
 * Purpose: Verify User entity validation, construction, and behaviour.
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@SpringBootTest
class UserEntityTest {

    @Autowired
    private Validator validator;

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Given a valid user, When validated, Then there are no violations")
        void shouldHaveNoViolations_WhenUserIsValid() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "YOUTH");

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Given a blank name, When validated, Then a violation is returned")
        void shouldFailValidation_WhenNameIsBlank() {
            // Given
            User user = new User("   ", "thandi@ssh.co.za", "SecurePass123", "YOUTH");

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }

        @Test
        @DisplayName("Given an invalid email, When validated, Then a violation is returned")
        void shouldFailValidation_WhenEmailIsInvalid() {
            // Given
            User user = new User("Thandi Nkosi", "not-an-email", "SecurePass123", "YOUTH");

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @Test
        @DisplayName("Given a name over 100 characters, When validated, Then a violation is returned")
        void shouldFailValidation_WhenNameTooLong() {
            // Given
            String longName = "A".repeat(101);
            User user = new User(longName, "thandi@ssh.co.za", "SecurePass123", "YOUTH");

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }

        @Test
        @DisplayName("Given a blank role, When validated, Then a violation is returned")
        void shouldFailValidation_WhenRoleIsBlank() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "");

            // When
            Set<ConstraintViolation<User>> violations = validator.validate(user);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("role"));
        }
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then fields are set and user is active")
        void shouldSetFieldsAndBeActive_WhenConstructed() {
            // Given / When
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "YOUTH");

            // Then
            assertThat(user.getName()).isEqualTo("Thandi Nkosi");
            assertThat(user.getEmail()).isEqualTo("thandi@ssh.co.za");
            assertThat(user.getPassword()).isEqualTo("SecurePass123");
            assertThat(user.getRole()).isEqualTo("YOUTH");
            assertThat(user.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Timestamps")
    class Timestamps {

        @Test
        @DisplayName("Given a new user, When persisted, Then createdAt and updatedAt are set")
        void shouldSetTimestamps_WhenPersisted() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "YOUTH");

            // When
            user.onCreate();

            // Then
            assertThat(user.getCreatedAt()).isNotNull();
            assertThat(user.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Given an existing user, When updated, Then updatedAt is refreshed")
        void shouldRefreshUpdatedAt_WhenUpdated() throws InterruptedException {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "YOUTH");
            user.onCreate();
            var original = user.getUpdatedAt();
            Thread.sleep(10);

            // When
            user.onUpdate();

            // Then
            assertThat(user.getUpdatedAt()).isAfter(original);
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given a user, When toString is called, Then password is not included")
        void shouldExcludePassword_WhenToStringCalled() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "YOUTH");

            // When
            String result = user.toString();

            // Then
            assertThat(result).doesNotContain("SecurePass123");
            assertThat(result).contains("thandi@ssh.co.za");
            assertThat(result).contains("YOUTH");
        }
    }
}
