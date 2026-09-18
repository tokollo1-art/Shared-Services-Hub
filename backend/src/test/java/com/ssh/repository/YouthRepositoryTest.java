package com.ssh.repository;

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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * YouthRepository BDD Tests
 *
 * Purpose: Verify database access for Youth entities.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class YouthRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private YouthRepository youthRepository;

    @Nested
    @DisplayName("Find by User")
    class FindByUser {

        @Test
        @DisplayName("Given a persisted youth, When finding by user, Then the youth is returned")
        void shouldFindYouth_ByUser() {
            // Given
            User user = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            entityManager.persistAndFlush(user);
            Youth youth = new Youth(user);
            entityManager.persistAndFlush(youth);

            // When
            Optional<Youth> found = youthRepository.findByUser(user);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUser().getEmail()).isEqualTo("sipho@ssh.co.za");
        }

        @Test
        @DisplayName("Given a user with no youth profile, When finding by user, Then empty is returned")
        void shouldReturnEmpty_WhenUserHasNoYouth() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persistAndFlush(user);

            // When
            Optional<Youth> found = youthRepository.findByUser(user);

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find Active Youth")
    class FindActiveYouth {

        @Test
        @DisplayName("Given active and alumni youth, When finding active, Then only active youth are returned")
        void shouldFindActiveYouth() {
            // Given
            User user1 = new User("Sipho Dlamini", "sipho@ssh.co.za", "Pass123", "YOUTH");
            entityManager.persist(user1);
            Youth active = new Youth(user1);
            entityManager.persist(active);

            User user2 = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "YOUTH");
            entityManager.persist(user2);
            Youth alumni = new Youth(user2);
            alumni.setAlumni(true);
            entityManager.persist(alumni);

            entityManager.flush();

            // When
            List<Youth> activeYouth = youthRepository.findByIsAlumniFalse();

            // Then
            assertThat(activeYouth).hasSize(1);
            assertThat(activeYouth.get(0).getUser().getEmail()).isEqualTo("sipho@ssh.co.za");
        }
    }

    @Nested
    @DisplayName("Find Alumni Youth")
    class FindAlumniYouth {

        @Test
        @DisplayName("Given alumni and active youth, When finding alumni, Then only alumni are returned")
        void shouldFindAlumniYouth() {
            // Given
            User user1 = new User("Sipho Dlamini", "sipho@ssh.co.za", "Pass123", "YOUTH");
            entityManager.persist(user1);
            Youth active = new Youth(user1);
            entityManager.persist(active);

            User user2 = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "YOUTH");
            entityManager.persist(user2);
            Youth alumni = new Youth(user2);
            alumni.setAlumni(true);
            entityManager.persist(alumni);

            entityManager.flush();

            // When
            List<Youth> alumniYouth = youthRepository.findByIsAlumniTrue();

            // Then
            assertThat(alumniYouth).hasSize(1);
            assertThat(alumniYouth.get(0).getUser().getEmail()).isEqualTo("thandi@ssh.co.za");
        }
    }

    @Nested
    @DisplayName("Find by Location")
    class FindByLocation {

        @Test
        @DisplayName("Given youth in different locations, When finding by location, Then only matching youth are returned")
        void shouldFindYouth_ByLocation() {
            // Given
            User user1 = new User("Sipho Dlamini", "sipho@ssh.co.za", "Pass123", "YOUTH");
            entityManager.persist(user1);
            Youth youth1 = new Youth(user1);
            youth1.setLocation("Johannesburg");
            entityManager.persist(youth1);

            User user2 = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "YOUTH");
            entityManager.persist(user2);
            Youth youth2 = new Youth(user2);
            youth2.setLocation("Cape Town");
            entityManager.persist(youth2);

            entityManager.flush();

            // When
            List<Youth> jhbYouth = youthRepository.findByLocation("Johannesburg");

            // Then
            assertThat(jhbYouth).hasSize(1);
            assertThat(jhbYouth.get(0).getUser().getEmail()).isEqualTo("sipho@ssh.co.za");
        }
    }
}
