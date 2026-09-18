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
 * TaskEntity BDD Tests
 *
 * Purpose: Verify Task entity validation, construction, and behaviour.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@SpringBootTest
class TaskEntityTest {

    @Autowired
    private Validator validator;

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Given a valid task, When validated, Then there are no violations")
        void shouldHaveNoViolations_WhenTaskIsValid() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(user, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            // When
            Set<ConstraintViolation<Task>> violations = validator.validate(task);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Given a blank title, When validated, Then a violation is returned")
        void shouldFailValidation_WhenTitleIsBlank() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(user, "Thandi's Accounting");
            Task task = new Task("   ", "Create a brand identity", "DESIGN", sme);

            // When
            Set<ConstraintViolation<Task>> violations = validator.validate(task);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("title"));
        }

        @Test
        @DisplayName("Given a title over 200 characters, When validated, Then a violation is returned")
        void shouldFailValidation_WhenTitleTooLong() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(user, "Thandi's Accounting");
            Task task = new Task("A".repeat(201), "Create a brand identity", "DESIGN", sme);

            // When
            Set<ConstraintViolation<Task>> violations = validator.validate(task);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("title"));
        }
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then fields are set and status is OPEN")
        void shouldSetFieldsAndBeOpen_WhenConstructed() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(user, "Thandi's Accounting");

            // When
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            // Then
            assertThat(task.getTitle()).isEqualTo("Design a logo");
            assertThat(task.getDescription()).isEqualTo("Create a brand identity");
            assertThat(task.getCategory()).isEqualTo("DESIGN");
            assertThat(task.getSme()).isEqualTo(sme);
            assertThat(task.getStatus()).isEqualTo("OPEN");
            assertThat(task.isPaid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Assignment")
    class Assignment {

        @Test
        @DisplayName("Given an unassigned task, When assigned to youth, Then assignedTo is set")
        void shouldAssignToYouth_WhenUnassigned() {
            // Given
            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(smeUser, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            Youth youth = new Youth(youthUser);

            // When
            task.setAssignedTo(youth);

            // Then
            assertThat(task.getAssignedTo()).isEqualTo(youth);
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitions {

        @Test
        @DisplayName("Given an open task, When status changes to IN_PROGRESS, Then status is updated")
        void shouldUpdateStatus_WhenChanged() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(user, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            // When
            task.setStatus("IN_PROGRESS");

            // Then
            assertThat(task.getStatus()).isEqualTo("IN_PROGRESS");
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given a task, When toString is called, Then key fields are present")
        void shouldIncludeKeyFields_WhenToStringCalled() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(user, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            // When
            String result = task.toString();

            // Then
            assertThat(result).contains("Design a logo");
            assertThat(result).contains("OPEN");
        }
    }
}
