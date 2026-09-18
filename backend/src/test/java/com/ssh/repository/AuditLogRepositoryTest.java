package com.ssh.repository;

import com.ssh.entity.AuditLog;
import com.ssh.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AuditLogRepository BDD Tests
 *
 * Purpose: Verify database access for AuditLog entities.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuditLogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Nested
    @DisplayName("Find by User")
    class FindByUser {

        @Test
        @DisplayName("Given logs by different users, When finding by user, Then only that user's logs are returned")
        void shouldFindLogs_ByUser() {
            // Given
            User user1 = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persist(user1);
            User user2 = new User("Sipho Dlamini", "sipho@ssh.co.za", "Pass123", "YOUTH");
            entityManager.persist(user2);

            entityManager.persist(new AuditLog(user1, "CREATE", "TASK", 1L, "Created task"));
            entityManager.persist(new AuditLog(user1, "UPDATE", "TASK", 1L, "Updated task"));
            entityManager.persist(new AuditLog(user2, "LOGIN", "USER", 2L, "Logged in"));
            entityManager.flush();

            // When
            List<AuditLog> thandiLogs = auditLogRepository.findByUser(user1);

            // Then
            assertThat(thandiLogs).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Find by Entity Type")
    class FindByEntityType {

        @Test
        @DisplayName("Given logs for different entity types, When finding by entity type, Then only matching logs are returned")
        void shouldFindLogs_ByEntityType() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persist(user);

            entityManager.persist(new AuditLog(user, "CREATE", "TASK", 1L, "Created task"));
            entityManager.persist(new AuditLog(user, "CREATE", "SME", 2L, "Created SME profile"));
            entityManager.persist(new AuditLog(user, "CREATE", "TASK", 3L, "Created another task"));
            entityManager.flush();

            // When
            List<AuditLog> taskLogs = auditLogRepository.findByEntityType("TASK");

            // Then
            assertThat(taskLogs).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Find by Entity ID")
    class FindByEntityId {

        @Test
        @DisplayName("Given logs for different entity IDs, When finding by entity ID, Then only matching logs are returned")
        void shouldFindLogs_ByEntityId() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persist(user);

            entityManager.persist(new AuditLog(user, "CREATE", "TASK", 42L, "Created task"));
            entityManager.persist(new AuditLog(user, "UPDATE", "TASK", 42L, "Updated task"));
            entityManager.persist(new AuditLog(user, "CREATE", "TASK", 100L, "Created another task"));
            entityManager.flush();

            // When
            List<AuditLog> logsFor42 = auditLogRepository.findByEntityId(42L);

            // Then
            assertThat(logsFor42).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Find by Action")
    class FindByAction {

        @Test
        @DisplayName("Given logs with different actions, When finding by action, Then only matching logs are returned")
        void shouldFindLogs_ByAction() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persist(user);

            entityManager.persist(new AuditLog(user, "CREATE", "TASK", 1L, "Created task"));
            entityManager.persist(new AuditLog(user, "LOGIN", "USER", 1L, "Logged in"));
            entityManager.persist(new AuditLog(user, "CREATE", "TASK", 2L, "Created another task"));
            entityManager.flush();

            // When
            List<AuditLog> createLogs = auditLogRepository.findByAction("CREATE");

            // Then
            assertThat(createLogs).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Find Recent Logs")
    class FindRecentLogs {

        @Test
        @DisplayName("Given logs from different times, When finding after a date, Then only later logs are returned")
        void shouldFindLogs_AfterDate() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persist(user);

            AuditLog older = new AuditLog(user, "CREATE", "TASK", 1L, "Old log");
            older.setCreatedAt(LocalDateTime.now().minusHours(2));
            entityManager.persist(older);

            AuditLog recent = new AuditLog(user, "UPDATE", "TASK", 1L, "Recent log");
            recent.setCreatedAt(LocalDateTime.now().minusMinutes(5));
            entityManager.persist(recent);

            entityManager.flush();

            // When
            List<AuditLog> recentLogs =
                auditLogRepository.findByCreatedAtAfter(LocalDateTime.now().minusHours(1));

            // Then
            assertThat(recentLogs).hasSize(1);
            assertThat(recentLogs.get(0).getDetails()).isEqualTo("Recent log");
        }
    }
}
