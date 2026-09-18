package com.ssh.entity;

import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * YouthEntity BDD Tests
 *
 * Purpose: Verify Youth entity construction and alumni behaviour.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@SpringBootTest
class YouthEntityTest {

    @Autowired
    private Validator validator;

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Given a user, When a Youth is constructed, Then user is set and isAlumni is false")
        void shouldSetUserAndNotBeAlumni_WhenConstructed() {
            // Given
            User user = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");

            // When
            Youth youth = new Youth(user);

            // Then
            assertThat(youth.getUser()).isEqualTo(user);
            assertThat(youth.isAlumni()).isFalse();
            assertThat(youth.getAlumniDate()).isNull();
        }
    }

    @Nested
    @DisplayName("Alumni Status")
    class AlumniStatus {

        @Test
        @DisplayName("Given an active youth, When marked as alumni, Then isAlumni is true and date is set")
        void shouldBecomeAlumni_WhenMarkedAsAlumni() {
            // Given
            User user = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            Youth youth = new Youth(user);

            // When
            youth.markAsAlumni();

            // Then
            assertThat(youth.isAlumni()).isTrue();
            assertThat(youth.getAlumniDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Experience Management")
    class ExperienceManagement {

        @Test
        @DisplayName("Given a youth, When an experience is added, Then the list grows and links back")
        void shouldAddExperience_ToExperienceList() {
            // Given
            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            Youth youth = new Youth(youthUser);

            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(smeUser, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            Experience experience = new Experience(youth, task);

            // When
            youth.addExperience(experience);

            // Then
            assertThat(youth.getExperiences()).hasSize(1);
            assertThat(experience.getYouth()).isEqualTo(youth);
        }

        @Test
        @DisplayName("Given a youth with an experience, When removed, Then the list shrinks")
        void shouldRemoveExperience_FromExperienceList() {
            // Given
            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            Youth youth = new Youth(youthUser);

            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(smeUser, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            Experience experience = new Experience(youth, task);
            youth.addExperience(experience);

            // When
            youth.removeExperience(experience);

            // Then
            assertThat(youth.getExperiences()).isEmpty();
            assertThat(experience.getYouth()).isNull();
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given a youth, When toString is called, Then isAlumni is included")
        void shouldIncludeIsAlumni_WhenToStringCalled() {
            // Given
            User user = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            Youth youth = new Youth(user);

            // When
            String result = youth.toString();

            // Then
            assertThat(result).contains("isAlumni");
            assertThat(result).contains("false");
        }
    }
}
