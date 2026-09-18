package com.ssh.repository;

import com.ssh.entity.Payment;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PaymentRepository BDD Tests
 *
 * Purpose: Verify database access for Payment entities.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    @Nested
    @DisplayName("Find by Youth")
    class FindByYouth {

        @Test
        @DisplayName("Given payments for different youth, When finding by youth, Then only that youth's payments are returned")
        void shouldFindPayments_ByYouth() {
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

            entityManager.persist(new Payment(task1, youth1, 1000.0, 10.0));
            entityManager.persist(new Payment(task2, youth1, 500.0, 10.0));
            entityManager.persist(new Payment(task3, youth2, 800.0, 10.0));
            entityManager.flush();

            // When
            List<Payment> siphoPayments = paymentRepository.findByYouth(youth1);

            // Then
            assertThat(siphoPayments).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Find by Task")
    class FindByTask {

        @Test
        @DisplayName("Given a payment linked to a task, When finding by task, Then the payment is returned")
        void shouldFindPayment_ByTask() {
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

            Payment payment = new Payment(task, youth, 1000.0, 10.0);
            entityManager.persistAndFlush(payment);

            // When
            Optional<Payment> found = paymentRepository.findByTask(task);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getTotalAmount()).isEqualTo(1000.0);
        }
    }

    @Nested
    @DisplayName("Find by Status")
    class FindByStatus {

        @Test
        @DisplayName("Given payments with different statuses, When finding by status, Then only matching payments are returned")
        void shouldFindPayments_ByStatus() {
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

            Payment pending = new Payment(task1, youth, 1000.0, 10.0);
            pending.setStatus("PENDING");
            entityManager.persist(pending);

            Payment paid = new Payment(task2, youth, 500.0, 10.0);
            paid.setStatus("PAID");
            entityManager.persist(paid);

            entityManager.flush();

            // When
            List<Payment> pendingPayments = paymentRepository.findByStatus("PENDING");
            List<Payment> paidPayments = paymentRepository.findByStatus("PAID");

            // Then
            assertThat(pendingPayments).hasSize(1);
            assertThat(paidPayments).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Find Pending Payments")
    class FindPendingPayments {

        @Test
        @DisplayName("Given paid and unpaid payments, When finding unpaid, Then only unpaid are returned")
        void shouldFindPendingPayments() {
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

            Payment paid = new Payment(task1, youth, 1000.0, 10.0);
            paid.setStatus("PAID");
            entityManager.persist(paid);

            Payment pending = new Payment(task2, youth, 500.0, 10.0);
            pending.setStatus("PENDING");
            entityManager.persist(pending);

            entityManager.flush();

            // When
            List<Payment> notPaid = paymentRepository.findByStatusNot("PAID");

            // Then
            assertThat(notPaid).hasSize(1);
            assertThat(notPaid.get(0).getStatus()).isEqualTo("PENDING");
        }
    }
}
