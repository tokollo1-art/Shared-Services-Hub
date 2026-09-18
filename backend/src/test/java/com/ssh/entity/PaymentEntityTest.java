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
 * PaymentEntity BDD Tests
 *
 * Purpose: Verify Payment entity validation, calculation, and payout behaviour.
 *
 * Structure: BDD Given/When/Then, nested by scenario.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@SpringBootTest
class PaymentEntityTest {

    @Autowired
    private Validator validator;

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Given a valid payment, When validated, Then there are no violations")
        void shouldHaveNoViolations_WhenPaymentIsValid() {
            // Given
            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            Youth youth = new Youth(youthUser);

            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(smeUser, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            Payment payment = new Payment(task, youth, 1000.0, 10.0);

            // When
            Set<ConstraintViolation<Payment>> violations = validator.validate(payment);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Construction and Breakdown")
    class Breakdown {

        @Test
        @DisplayName("Given a total of R1000 and 10% fee, When constructed, Then youth receives R900")
        void shouldCalculateBreakdown_WhenConstructed() {
            // Given
            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            Youth youth = new Youth(youthUser);

            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(smeUser, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            // When
            Payment payment = new Payment(task, youth, 1000.0, 10.0);

            // Then
            assertThat(payment.getTotalAmount()).isEqualTo(1000.0);
            assertThat(payment.getPlatformFee()).isEqualTo(100.0);
            assertThat(payment.getYouthAmount()).isEqualTo(900.0);
            assertThat(payment.getStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("Given a default fee, When recalculated, Then 10% is applied")
        void shouldCalculateWithDefaultFee_WhenPercentIsNull() {
            // Given
            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            Youth youth = new Youth(youthUser);

            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(smeUser, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            Payment payment = new Payment();
            payment.setTask(task);
            payment.setYouth(youth);
            payment.setTotalAmount(500.0);

            // When
            payment.calculateBreakdown(null);

            // Then
            assertThat(payment.getPlatformFee()).isEqualTo(50.0);
            assertThat(payment.getYouthAmount()).isEqualTo(450.0);
        }
    }

    @Nested
    @DisplayName("Payout")
    class Payout {

        @Test
        @DisplayName("Given a pending payment, When marked as paid, Then status is PAID and paidAt is set")
        void shouldMarkAsPaid_WhenCalled() {
            // Given
            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            Youth youth = new Youth(youthUser);

            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(smeUser, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            Payment payment = new Payment(task, youth, 1000.0, 10.0);

            // When
            payment.markAsPaid("PAY-2026-0001");

            // Then
            assertThat(payment.getStatus()).isEqualTo("PAID");
            assertThat(payment.getPaidAt()).isNotNull();
            assertThat(payment.getReference()).isEqualTo("PAY-2026-0001");
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Given a payment, When toString is called, Then status and amounts are included")
        void shouldIncludeStatusAndAmounts_WhenToStringCalled() {
            // Given
            User youthUser = new User("Sipho Dlamini", "sipho@ssh.co.za", "SecurePass123", "YOUTH");
            Youth youth = new Youth(youthUser);

            User smeUser = new User("Thandi Nkosi", "thandi@ssh.co.za", "SecurePass123", "SME");
            SME sme = new SME(smeUser, "Thandi's Accounting");
            Task task = new Task("Design a logo", "Create a brand identity", "DESIGN", sme);

            Payment payment = new Payment(task, youth, 1000.0, 10.0);

            // When
            String result = payment.toString();

            // Then
            assertThat(result).contains("PENDING");
            assertThat(result).contains("1000.0");
            assertThat(result).contains("900.0");
        }
    }
}
