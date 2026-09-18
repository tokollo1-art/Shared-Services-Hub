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
 * ExperienceEntity BDD Tests
 *
 * Purpose: Verify Experience entity validation, construction, and verification.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@SpringBootTest
class ExperienceEntityTest {

    @Autowired
    private Validator validator;

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Given a valid experience, When validated, Then there are no violations")
        void shouldHaveNoViolations_WhenExperienceIsValid() {
            // Given
            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            Youth youth = new Youth(youthUser);

            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(smeUser, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            Experience experience = new Experience(youth, task);

            // When
            Set<ConstraintViolation<Experience>> violations = validator.validate(experience);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given valid arguments, When constructed, Then status is PENDING and isVerified is false")
        void shouldSetFieldsAndBePending_WhenConstructed() {
            // Given
            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            Youth youth = new Youth(youthUser);

            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(smeUser, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            // When
            Experience experience = new Experience(youth, task);

            // Then
            assertThat(experience.getYouth()).isEqualTo(youth);
            assertThat(experience.getTask()).isEqualTo(task);
            assertThat(experience.getStatus()).isEqualTo("PENDING");
            assertThat(experience.isVerified()).isFalse();
        }
    }

    @Nested
    @DisplayName("Verification")
    class Verification {

        @Test
        @DisplayName("Given a pending experience, When verified, Then status is VERIFIED and rating is set")
        void shouldVerify_WhenCalled() {
            // Given
            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            Youth youth = new Youth(youthUser);

            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(smeUser, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            Experience experience = new Experience(youth, task);

            // When
            experience.verify("Excellent work, delivered on time", 4.8);

            // Then
            assertThat(experience.isVerified()).isTrue();
            assertThat(experience.getStatus()).isEqualTo("VERIFIED");
            assertThat(experience.getVerifiedAt()).isNotNull();
            assertThat(experience.getFeedback()).isEqualTo("Excellent work, delivered on time");
            assertThat(experience.getRating()).isEqualTo(4.8);
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given an experience, When toString is called, Then status is included")
        void shouldIncludeStatus_WhenToStringCalled() {
            // Given
            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            Youth youth = new Youth(youthUser);

            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(smeUser, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            Experience experience = new Experience(youth, task);

            // When
            String result = experience.toString();

            // Then
            assertThat(result).contains("PENDING");
            assertThat(result).contains("isVerified");
        }
    }
}
