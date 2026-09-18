package com.ssh.repository;

import com.ssh.entity.Experience;
import com.ssh.entity.SME;
import com.ssh.entity.Task;
import com.ssh.entity.User;
import com.ssh.entity.Youth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ExperienceRepository BDD Tests
 *
 * Purpose: Verify database access for Experience entities (the Experience Ledger).
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExperienceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExperienceRepository experienceRepository;

    @Nested
    @DisplayName("Find by Youth")
    class FindByYouth {

        @Test
        @DisplayName("Given experiences for different youth, When finding by youth, Then only that youth's experiences are returned")
        void shouldFindExperiences_ByYouth() {
            // Given
            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persist(smeUser);
            SME sme = new SME(smeUser, "Thandi's Accounting");
            entityManager.persist(sme);

            User youthUser1 = new User("Sipho Dlamini", "sipho@ssh.co.za", "Pass123", "YOUTH");
            entityManager.persist(youthUser1);
            Youth youth1 = new Youth(youthUser1);
            entityManager.persist(youth1);

            User youthUser2 = new User("Lerato Khumalo", "lerato@ssh.co.za", "Pass123", "YOUTH");
            entityManager.persist(youthUser2);
            Youth youth2 = new Youth(youthUser2);
            entityManager.persist(youth2);

            Task task1 = new Task("Design a logo", "Brand identity", "DESIGN", sme);
            entityManager.persist(task1);
            Task task2 = new Task("Bookkeeping", "Monthly books", "FINANCE", sme);
            entityManager.persist(task2);
            Task task3 = new Task("Social media", "Content plan", "MARKETING", sme);
            entityManager.persist(task3);

            entityManager.persist(new Experience(youth1, task1));
            entityManager.persist(new Experience(youth1, task2));
            entityManager.persist(new Experience(youth2, task3));
            entityManager.flush();

            // When
            List<Experience> siphoExperiences = experienceRepository.findByYouth(youth1);

            // Then
            assertThat(siphoExperiences).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Find by Task")
    class FindByTask {

        @Test
        @DisplayName("Given an experience linked to a task, When finding by task, Then the experience is returned")
        void shouldFindExperiences_ByTask() {
            // Given
            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persist(smeUser);
            SME sme = new SME(smeUser, "Thandi's Accounting");
            entityManager.persist(sme);

            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "Pass123", "YOUTH");
            entityManager.persist(youthUser);
            Youth youth = new Youth(youthUser);
            entityManager.persist(youth);

            Task task = new Task("Design a logo", "Brand identity", "DESIGN", sme);
            entityManager.persist(task);

            entityManager.persist(new Experience(youth, task));
            entityManager.flush();

            // When
            List<Experience> experiences = experienceRepository.findByTask(task);

            // Then
            assertThat(experiences).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Find Verified Experiences")
    class FindVerifiedExperiences {

        @Test
        @DisplayName("Given verified and unverified experiences, When finding verified, Then only verified are returned")
        void shouldFindVerifiedExperiences() {
            // Given
            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persist(smeUser);
            SME sme = new SME(smeUser, "Thandi's Accounting");
            entityManager.persist(sme);

            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "Pass123", "YOUTH");
            entityManager.persist(youthUser);
            Youth youth = new Youth(youthUser);
            entityManager.persist(youth);

            Task task1 = new Task("Design a logo", "Brand identity", "DESIGN", sme);
            entityManager.persist(task1);
            Task task2 = new Task("Bookkeeping", "Monthly books", "FINANCE", sme);
            entityManager.persist(task2);

            Experience verified = new Experience(youth, task1);
            verified.setVerified(true);
            entityManager.persist(verified);

            Experience unverified = new Experience(youth, task2);
            unverified.setVerified(false);
            entityManager.persist(unverified);

            entityManager.flush();

            // When
            List<Experience> verifiedExperiences = experienceRepository.findByIsVerifiedTrue();

            // Then
            assertThat(verifiedExperiences).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Find Pending Experiences")
    class FindPendingExperiences {

        @Test
        @DisplayName("Given verified and unverified experiences, When finding unverified, Then only unverified are returned")
        void shouldFindPendingExperiences() {
            // Given
            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persist(smeUser);
            SME sme = new SME(smeUser, "Thandi's Accounting");
            entityManager.persist(sme);

            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "Pass123", "YOUTH");
            entityManager.persist(youthUser);
            Youth youth = new Youth(youthUser);
            entityManager.persist(youth);

            Task task1 = new Task("Design a logo", "Brand identity", "DESIGN", sme);
            entityManager.persist(task1);
            Task task2 = new Task("Bookkeeping", "Monthly books", "FINANCE", sme);
            entityManager.persist(task2);

            Experience verified = new Experience(youth, task1);
            verified.setVerified(true);
            entityManager.persist(verified);

            Experience unverified = new Experience(youth, task2);
            unverified.setVerified(false);
            entityManager.persist(unverified);

            entityManager.flush();

            // When
            List<Experience> pendingExperiences = experienceRepository.findByIsVerifiedFalse();

            // Then
            assertThat(pendingExperiences).hasSize(1);
        }
    }
}
