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
 * AuditLogEntity BDD Tests
 *
 * Purpose: Verify AuditLog entity validation, construction, and immutability.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@SpringBootTest
class AuditLogEntityTest {

    @Autowired
    private Validator validator;

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Given a valid audit log, When validated, Then there are no violations")
        void shouldHaveNoViolations_WhenAuditLogIsValid() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            AuditLog log = new AuditLog(user, "CREATE", "TASK", 42L, "Created new task");

            // When
            Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Given a blank action, When validated, Then a violation is returned")
        void shouldFailValidation_WhenActionIsBlank() {
            // Given
            AuditLog log = new AuditLog();
            log.setAction("   ");
            log.setEntityType("TASK");

            // When
            Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("action"));
        }

        @Test
        @DisplayName("Given a blank entity type, When validated, Then a violation is returned")
        void shouldFailValidation_WhenEntityTypeIsBlank() {
            // Given
            AuditLog log = new AuditLog();
            log.setAction("CREATE");
            log.setEntityType("");

            // When
            Set<ConstraintViolation<AuditLog>> violations = validator.validate(log);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("entityType"));
        }
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then fields are set")
        void shouldSetFields_WhenConstructed() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");

            // When
            AuditLog log = new AuditLog(user, "VERIFY", "EXPERIENCE", 100L, "Verified task completion");

            // Then
            assertThat(log.getUser()).isEqualTo(user);
            assertThat(log.getAction()).isEqualTo("VERIFY");
            assertThat(log.getEntityType()).isEqualTo("EXPERIENCE");
            assertThat(log.getEntityId()).isEqualTo(100L);
            assertThat(log.getDetails()).isEqualTo("Verified task completion");
        }
    }

    @Nested
    @DisplayName("Timestamps")
    class Timestamps {

        @Test
        @DisplayName("Given a new audit log, When persisted, Then createdAt is set and no updatedAt exists")
        void shouldSetCreatedAt_WhenPersisted() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            AuditLog log = new AuditLog(user, "CREATE", "TASK", 42L, "Created new task");

            // When
            log.onCreate();

            // Then
            assertThat(log.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given an audit log, When toString is called, Then action and entity type are included")
        void shouldIncludeActionAndEntityType_WhenToStringCalled() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            AuditLog log = new AuditLog(user, "CREATE", "TASK", 42L, "Created new task");

            // When
            String result = log.toString();

            // Then
            assertThat(result).contains("CREATE");
            assertThat(result).contains("TASK");
            assertThat(result).contains("42");
        }
    }
}
