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
 * SMEEntity BDD Tests
 *
 * Purpose: Verify SME entity validation, construction, and behaviour.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@SpringBootTest
class SMEEntityTest {

    @Autowired
    private Validator validator;

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Given a valid SME, When validated, Then there are no violations")
        void shouldHaveNoViolations_WhenSMEIsValid() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(user, "Thandi's Accounting");

            // When
            Set<ConstraintViolation<SME>> violations = validator.validate(sme);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Given a blank business name, When validated, Then a violation is returned")
        void shouldFailValidation_WhenBusinessNameIsBlank() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(user, "   ");

            // When
            Set<ConstraintViolation<SME>> violations = validator.validate(sme);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("businessName"));
        }

        @Test
        @DisplayName("Given a business name over 100 characters, When validated, Then a violation is returned")
        void shouldFailValidation_WhenBusinessNameTooLong() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(user, "A".repeat(101));

            // When
            Set<ConstraintViolation<SME>> violations = validator.validate(sme);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("businessName"));
        }
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then fields are set and SME is unverified")
        void shouldSetFieldsAndBeUnverified_WhenConstructed() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");

            // When
            SME sme = new SME(user, "Thandi's Accounting");

            // Then
            assertThat(sme.getUser()).isEqualTo(user);
            assertThat(sme.getBusinessName()).isEqualTo("Thandi's Accounting");
            assertThat(sme.isVerified()).isFalse();
        }
    }

    @Nested
    @DisplayName("Task Management")
    class TaskManagement {

        @Test
        @DisplayName("Given an SME, When a task is added, Then the task list grows and links back")
        void shouldAddTask_ToTaskList() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(user, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            // When
            sme.addTask(task);

            // Then
            assertThat(sme.getTasks()).hasSize(1);
            assertThat(task.getSme()).isEqualTo(sme);
        }

        @Test
        @DisplayName("Given an SME with a task, When the task is removed, Then the task list shrinks")
        void shouldRemoveTask_FromTaskList() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(user, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);
            sme.addTask(task);

            // When
            sme.removeTask(task);

            // Then
            assertThat(sme.getTasks()).isEmpty();
            assertThat(task.getSme()).isNull();
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given an SME, When toString is called, Then key fields are present")
        void shouldIncludeKeyFields_WhenToStringCalled() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(user, "Thandi's Accounting");

            // When
            String result = sme.toString();

            // Then
            assertThat(result).contains("Thandi's Accounting");
            assertThat(result).contains("isVerified");
        }
    }
}
