package com.ssh.event;

import com.ssh.entity.AuditLog;
import com.ssh.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Durable audit trail for verification and payout events.
 *
 * The listener runs AFTER_COMMIT: it never changes the outcome of the business
 * transaction, and it leaves a compliance record for every verified task and
 * every disbursement.
 */
@Component
public class DomainEventListener {

    private static final Logger log = LoggerFactory.getLogger(DomainEventListener.class);

    private final AuditLogRepository auditLogRepository;

    public DomainEventListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskVerified(TaskVerifiedEvent event) {
        log.info(
            "Task {} verified: experience={}, payment={}, youth={}, total={}, fee={}, youthAmount={}",
            event.taskId(), event.experienceId(), event.paymentId(), event.youthId(),
            event.totalAmount(), event.platformFee(), event.youthAmount()
        );
        persist(new AuditLog(
            null,
            "VERIFY",
            "TASK",
            event.taskId(),
            "experience=" + event.experienceId()
                + "; payment=" + event.paymentId()
                + "; youth=" + event.youthId()
                + "; total=" + event.totalAmount()
                + "; platformFee=" + event.platformFee()
                + "; youthAmount=" + event.youthAmount()
        ));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentPaid(PaymentPaidEvent event) {
        log.info("Payment {} paid to youth {}: R{} ({})",
            event.paymentId(), event.youthId(), event.amount(), event.reference());
        persist(new AuditLog(
            null,
            "PAY",
            "PAYMENT",
            event.paymentId(),
            "youth=" + event.youthId()
                + "; amount=" + event.amount()
                + "; reference=" + event.reference()
        ));
    }

    private void persist(AuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
        } catch (RuntimeException ex) {
            // The business transaction already committed; never surface an
            // audit-write failure as a failed verification/payout.
            log.error("Failed to persist audit log: {}", auditLog.getDetails(), ex);
        }
    }
}
