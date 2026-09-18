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
 * TaskRequest BDD Tests
 *
 * Purpose: Verify validation and construction for TaskRequest DTO.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
class TaskRequestTest {

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
        @DisplayName("Given valid task data, When validated, Then there are no violations")
        void shouldHaveNoViolations_ForValidRequest() {
            // Given
            TaskRequest request = new TaskRequest(
                "Design a logo",
                "Create a brand identity",
                "DESIGN"
            );

            // When
            Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Given a blank title, When validated, Then a violation is returned")
        void shouldFailValidation_WhenTitleIsBlank() {
            // Given
            TaskRequest request = new TaskRequest(
                "   ",
                "Create a brand identity",
                "DESIGN"
            );

            // When
            Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("title"));
        }

        @Test
        @DisplayName("Given a title over 200 characters, When validated, Then a violation is returned")
        void shouldFailValidation_WhenTitleTooLong() {
            // Given
            TaskRequest request = new TaskRequest(
                "A".repeat(201),
                "Create a brand identity",
                "DESIGN"
            );

            // When
            Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("title"));
        }

        @Test
        @DisplayName("Given a description over 1000 characters, When validated, Then a violation is returned")
        void shouldFailValidation_WhenDescriptionTooLong() {
            // Given
            TaskRequest request = new TaskRequest(
                "Design a logo",
                "A".repeat(1001),
                "DESIGN"
            );

            // When
            Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("description"));
        }

        @Test
        @DisplayName("Given a blank category, When validated, Then a violation is returned")
        void shouldFailValidation_WhenCategoryIsBlank() {
            // Given
            TaskRequest request = new TaskRequest(
                "Design a logo",
                "Create a brand identity",
                ""
            );

            // When
            Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("category"));
        }

        @Test
        @DisplayName("Given a negative budget, When validated, Then a violation is returned")
        void shouldFailValidation_WhenBudgetIsNegative() {
            // Given
            TaskRequest request = new TaskRequest(
                "Design a logo",
                "Create a brand identity",
                "DESIGN"
            );
            request.setBudget(-100.0);

            // When
            Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("budget"));
        }

        @Test
        @DisplayName("Given a zero duration, When validated, Then a violation is returned")
        void shouldFailValidation_WhenDurationIsNotPositive() {
            // Given
            TaskRequest request = new TaskRequest(
                "Design a logo",
                "Create a brand identity",
                "DESIGN"
            );
            request.setDurationDays(0);

            // When
            Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("durationDays"));
        }
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then core fields are set")
        void shouldSetCoreFields_WhenConstructed() {
            // Given / When
            TaskRequest request = new TaskRequest(
                "Design a logo",
                "Create a brand identity",
                "DESIGN"
            );

            // Then
            assertThat(request.getTitle()).isEqualTo("Design a logo");
            assertThat(request.getDescription()).isEqualTo("Create a brand identity");
            assertThat(request.getCategory()).isEqualTo("DESIGN");
        }

        @Test
        @DisplayName("Given optional fields set, When read, Then they are stored")
        void shouldStoreOptionalFields_WhenSet() {
            // Given
            TaskRequest request = new TaskRequest(
                "Design a logo",
                "Create a brand identity",
                "DESIGN"
            );

            // When
            request.setBudget(1000.0);
            request.setDurationDays(14);
            request.setPaid(true);

            // Then
            assertThat(request.getBudget()).isEqualTo(1000.0);
            assertThat(request.getDurationDays()).isEqualTo(14);
            assertThat(request.isPaid()).isTrue();
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given a task request, When toString is called, Then key fields are present")
        void shouldIncludeKeyFields_WhenToStringCalled() {
            // Given
            TaskRequest request = new TaskRequest(
                "Design a logo",
                "Create a brand identity",
                "DESIGN"
            );
            request.setBudget(1000.0);

            // When
            String result = request.toString();

            // Then
            assertThat(result).contains("Design a logo");
            assertThat(result).contains("DESIGN");
            assertThat(result).contains("1000.0");
        }
    }
}
