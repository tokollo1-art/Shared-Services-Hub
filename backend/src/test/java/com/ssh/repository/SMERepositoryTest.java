package com.ssh.repository;

import com.ssh.entity.SME;
import com.ssh.entity.User;
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
 * SMERepository BDD Tests
 * Purpose: Verify database access for SME entities.
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SMERepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SMERepository smeRepository;

    @Nested
    @DisplayName("Find by User")
    class FindByUser {

        @Test
        @DisplayName("Given a persisted SME, When finding by user, Then the SME is returned")
        void shouldFindSME_ByUser() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            entityManager.persistAndFlush(user);
            SME sme = new SME(user, "Thandi's Accounting");
            entityManager.persistAndFlush(sme);

            // When
            Optional<SME> found = smeRepository.findByUser(user);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getBusinessName()).isEqualTo("Thandi's Accounting");
        }

        @Test
        @DisplayName("Given a user with no SME, When finding by user, Then empty is returned")
        void shouldReturnEmpty_WhenUserHasNoSME() {
            // Given
            User user = new User("Sipho Dlamini", "sipho@ssh.co.za", "Pass123", "YOUTH");
            entityManager.persistAndFlush(user);

            // When
            Optional<SME> found = smeRepository.findByUser(user);

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find Verified SMEs")
    class FindVerifiedSMEs {

        @Test
        @DisplayName("Given verified and unverified SMEs, When finding verified, Then only verified SMEs are returned")
        void shouldFindVerifiedSMEs() {
            // Given
            User user1 = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persist(user1);
            SME verified = new SME(user1, "Thandi's Accounting");
            verified.setVerified(true);
            entityManager.persist(verified);

            User user2 = new User("Zanele Mokoena", "zanele@ssh.co.za", "Pass123", "SME");
            entityManager.persist(user2);
            SME unverified = new SME(user2, "Zanele's Designs");
            unverified.setVerified(false);
            entityManager.persist(unverified);

            entityManager.flush();

            // When
            List<SME> verifiedSMEs = smeRepository.findByIsVerifiedTrue();

            // Then
            assertThat(verifiedSMEs).hasSize(1);
            assertThat(verifiedSMEs.get(0).getBusinessName()).isEqualTo("Thandi's Accounting");
        }
    }

    @Nested
    @DisplayName("Find by Industry")
    class FindByIndustry {

        @Test
        @DisplayName("Given SMEs in different industries, When finding by industry, Then only matching SMEs are returned")
        void shouldFindSMEs_ByIndustry() {
            // Given
            User user1 = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persist(user1);
            SME accounting = new SME(user1, "Thandi's Accounting");
            accounting.setIndustry("Finance");
            entityManager.persist(accounting);

            User user2 = new User("Zanele Mokoena", "zanele@ssh.co.za", "Pass123", "SME");
            entityManager.persist(user2);
            SME design = new SME(user2, "Zanele's Designs");
            design.setIndustry("Creative");
            entityManager.persist(design);

            entityManager.flush();

            // When
            List<SME> financeSMEs = smeRepository.findByIndustry("Finance");

            // Then
            assertThat(financeSMEs).hasSize(1);
            assertThat(financeSMEs.get(0).getBusinessName()).isEqualTo("Thandi's Accounting");
        }
    }

    @Nested
    @DisplayName("Find by Business Name")
    class FindByBusinessName {

        @Test
        @DisplayName("Given a persisted SME, When finding by business name, Then the SME is returned")
        void shouldFindSME_ByBusinessName() {
            // Given
            User user = new User("Thandi Nkosi", "thandi@ssh.co.za", "Pass123", "SME");
            entityManager.persistAndFlush(user);
            SME sme = new SME(user, "Thandi's Accounting");
            entityManager.persistAndFlush(sme);

            // When
            Optional<SME> found = smeRepository.findByBusinessName("Thandi's Accounting");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getBusinessName()).isEqualTo("Thandi's Accounting");
        }
    }
}
