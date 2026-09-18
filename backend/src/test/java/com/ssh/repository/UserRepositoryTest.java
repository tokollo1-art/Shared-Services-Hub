package com.ssh.repository;

import com.ssh.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepository BDD Tests
 * Purpose: Verify database access for User entities.
 * Structure: BDD Given/When/Then, nested by scenario.
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("Find by Email")
    class FindByEmail {

        @Test
        @DisplayName("Given a persisted user, When finding by email, Then the user is returned")
        void shouldFindUser_ByEmail() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "YOUTH");
            entityManager.persistAndFlush(user);

            // When
            Optional<User> found = userRepository.findByEmail("thandi@ssh.co.za");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("thandi@ssh.co.za");
        }

        @Test
        @DisplayName("Given no user with that email, When finding by email, Then empty is returned")
        void shouldReturnEmpty_WhenEmailNotFound() {
            // Given / When
            Optional<User> found = userRepository.findByEmail("nonexistent@ssh.co.za");

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Exists by Email")
    class ExistsByEmail {

        @Test
        @DisplayName("Given a persisted user, When checking email exists, Then true is returned")
        void shouldReturnTrue_WhenEmailExists() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "YOUTH");
            entityManager.persistAndFlush(user);

            // When
            boolean exists = userRepository.existsByEmail("thandi@ssh.co.za");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Given no user with that email, When checking email exists, Then false is returned")
        void shouldReturnFalse_WhenEmailDoesNotExist() {
            // Given / When
            boolean exists = userRepository.existsByEmail("nobody@ssh.co.za");

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Find by Role")
    class FindByRole {

        @Test
        @DisplayName("Given users with different roles, When finding by role, Then only matching users are returned")
        void shouldFindUsers_ByRole() {
            // Given
            entityManager.persist(new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "YOUTH"));
            entityManager.persist(new User("Sipho Dlamini", "sipho@ssh.co.za", "Pass123", "YOUTH"));
            entityManager.persist(new User("Zanele Mokoena", "zanele@ssh.co.za", "Pass123", "SME"));
            entityManager.flush();

            // When
            List<User> youth = userRepository.findByRole("YOUTH");
            List<User> smes = userRepository.findByRole("SME");

            // Then
            assertThat(youth).hasSize(2);
            assertThat(smes).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Find Active Users")
    class FindActiveUsers {

        @Test
        @DisplayName("Given active and inactive users, When finding active, Then only active users are returned")
        void shouldFindActiveUsers() {
            // Given
            User active = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "YOUTH");
            active.setActive(true);

            User inactive = new User("Sipho Dlamini", "sipho@ssh.co.za", "Pass123", "YOUTH");
            inactive.setActive(false);

            entityManager.persist(active);
            entityManager.persist(inactive);
            entityManager.flush();

            // When
            List<User> activeUsers = userRepository.findByIsActiveTrue();

            // Then
            assertThat(activeUsers).hasSize(1);
            assertThat(activeUsers.get(0).getEmail()).isEqualTo("thandi@ssh.co.za");
        }
    }
}
