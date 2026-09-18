package com.ssh.repository;

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
 * TaskRepository BDD Tests
 *
 * Purpose: Verify database access for Task entities.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @Nested
    @DisplayName("Find by SME")
    class FindBySME {

        @Test
        @DisplayName("Given tasks posted by different SMEs, When finding by SME, Then only that SME's tasks are returned")
        void shouldFindTasks_BySme() {
            // Given
            User smeUser1 = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persist(smeUser1);
            SME sme1 = new SME(smeUser1, "Thandi's Accounting");
            entityManager.persist(sme1);

            User smeUser2 = new User("Zanele Mokoena", "zanele@ssh.co.za", "Pass123", "SME");
            entityManager.persist(smeUser2);
            SME sme2 = new SME(smeUser2, "Zanele's Designs");
            entityManager.persist(sme2);

            entityManager.persist(new Task("Design a logo", "Brand identity", "DESIGN", sme1));
            entityManager.persist(new Task("Bookkeeping", "Monthly books", "FINANCE", sme1));
            entityManager.persist(new Task("Social media", "Content plan", "MARKETING", sme2));
            entityManager.flush();

            // When
            List<Task> thandiTasks = taskRepository.findBySme(sme1);

            // Then
            assertThat(thandiTasks).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Find by Status")
    class FindByStatus {

        @Test
        @DisplayName("Given tasks with different statuses, When finding by status, Then only matching tasks are returned")
        void shouldFindTasks_ByStatus() {
            // Given
            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persist(smeUser);
            SME sme = new SME(smeUser, "Thandi's Accounting");
            entityManager.persist(sme);

            Task open = new Task("Design a logo", "Brand identity", "DESIGN", sme);
            open.setStatus("OPEN");
            entityManager.persist(open);

            Task inProgress = new Task("Bookkeeping", "Monthly books", "FINANCE", sme);
            inProgress.setStatus("IN_PROGRESS");
            entityManager.persist(inProgress);

            entityManager.flush();

            // When
            List<Task> openTasks = taskRepository.findByStatus("OPEN");
            List<Task> inProgressTasks = taskRepository.findByStatus("IN_PROGRESS");

            // Then
            assertThat(openTasks).hasSize(1);
            assertThat(inProgressTasks).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Find by Assigned Youth")
    class FindByAssignedYouth {

        @Test
        @DisplayName("Given tasks assigned to different youth, When finding by youth, Then only that youth's tasks are returned")
        void shouldFindTasks_ByAssignedYouth() {
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
            task1.setAssignedTo(youth1);
            entityManager.persist(task1);

            Task task2 = new Task("Bookkeeping", "Monthly books", "FINANCE", sme);
            task2.setAssignedTo(youth1);
            entityManager.persist(task2);

            Task task3 = new Task("Social media", "Content plan", "MARKETING", sme);
            task3.setAssignedTo(youth2);
            entityManager.persist(task3);

            entityManager.flush();

            // When
            List<Task> siphoTasks = taskRepository.findByAssignedTo(youth1);

            // Then
            assertThat(siphoTasks).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Find by Category")
    class FindByCategory {

        @Test
        @DisplayName("Given tasks in different categories, When finding by category, Then only matching tasks are returned")
        void shouldFindTasks_ByCategory() {
            // Given
            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persist(smeUser);
            SME sme = new SME(smeUser, "Thandi's Accounting");
            entityManager.persist(sme);

            entityManager.persist(new Task("Design a logo", "Brand identity", "DESIGN", sme));
            entityManager.persist(new Task("Bookkeeping", "Monthly books", "FINANCE", sme));
            entityManager.persist(new Task("Social media", "Content plan", "MARKETING", sme));
            entityManager.flush();

            // When
            List<Task> designTasks = taskRepository.findByCategory("DESIGN");

            // Then
            assertThat(designTasks).hasSize(1);
            assertThat(designTasks.get(0).getCategory()).isEqualTo("DESIGN");
        }
    }
}
