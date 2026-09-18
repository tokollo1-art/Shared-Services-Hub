package com.ssh.repository;

import com.ssh.entity.Payment;
import com.ssh.entity.Task;
import com.ssh.entity.Youth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Payment Repository
 *
 * Purpose: Database access for Payment entities.
 *
 * WHY SSH needs this repository:
 *   - Find payments by youth (their earnings history)
 *   - Find the payment for a specific task
 *   - Find pending payments awaiting disbursement
 *   - Find paid payments for impact reporting
 *
 * TODO: Add method to calculate total paid to a youth
 * TODO: Add method to find payments in a date range
 * TODO: Add method to count payments by status
 *
 * TDD: Write PaymentRepositoryTest first
 *       - shouldFindPayments_ByYouth()
 *       - shouldFindPayment_ByTask()
 *       - shouldFindPayments_ByStatus()
 *       - shouldFindPendingPayments()
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find all payments received by a specific youth.
     * Used for the youth's income dashboard.
     */
    List<Payment> findByYouth(Youth youth);

    /**
     * Find the payment linked to a specific task.
     * Every task has exactly one payment (one-to-one in practice).
     */
    Optional<Payment> findByTask(Task task);

    /**
     * Find all payments with a specific status.
     * Statuses: PENDING, PROCESSING, PAID, FAILED, REFUNDED.
     */
    List<Payment> findByStatus(String status);

    /**
     * Find all payments that have not yet been disbursed.
     * Used for admin dashboards to track payments awaiting action.
     */
    List<Payment> findByStatusNot(String status);

    // TODO: Add @Query("SELECT SUM(p.youthAmount) FROM Payment p WHERE p.youth = :youth AND p.status = 'PAID'")
    //       Double findTotalPaidToYouth(Youth youth);
    // TODO: Add List<Payment> findByPaidAtBetween(LocalDateTime start, LocalDateTime end);
    // TODO: Add long countByStatus(String status);
}
