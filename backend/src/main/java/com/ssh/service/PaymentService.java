package com.ssh.service;

import com.ssh.dto.response.PaymentResponse;
import com.ssh.entity.Payment;
import com.ssh.entity.Task;
import com.ssh.entity.Youth;
import com.ssh.event.PaymentPaidEvent;
import com.ssh.exception.BusinessRuleException;
import com.ssh.exception.DuplicateResourceException;
import com.ssh.exception.ResourceNotFoundException;
import com.ssh.repository.PaymentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Payment creation and sandbox payouts.
 *
 * Verification creates exactly one PENDING payment per task. The payout action
 * is restricted to ADMIN, debits the funder reserve wallet and marks the
 * payment PAID with a SANDBOX reference.
 */
@Service
public class PaymentService {

    public static final double PLATFORM_FEE_PERCENT = 10.0;

    private final PaymentRepository paymentRepository;
    private final WalletService walletService;
    private final UserAccountService userAccountService;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentService(PaymentRepository paymentRepository,
                          WalletService walletService,
                          UserAccountService userAccountService,
                          ApplicationEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.walletService = walletService;
        this.userAccountService = userAccountService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Create the one payment for a verified task.
     *
     * totalAmount = task budget (0 when unset)
     * platformFee = 10% (rounded to cents)
     * youthAmount = remainder
     */
    @Transactional
    public Payment createForVerifiedTask(Task task, Youth youth) {
        if (paymentRepository.findByTask(task).isPresent()) {
            throw new DuplicateResourceException(
                "A payment already exists for task " + task.getId());
        }

        double total = task.getBudget() != null ? Math.max(0.0, task.getBudget()) : 0.0;
        Payment payment = new Payment(task, youth, total, PLATFORM_FEE_PERCENT);
        return paymentRepository.save(payment);
    }

    @Transactional
    public PaymentResponse getForUser(Long paymentId, String email) {
        var user = userAccountService.requireUser(email);
        Payment payment = requirePayment(paymentId);

        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        boolean isPaidYouth = payment.getYouth() != null
            && payment.getYouth().getUser().getId().equals(user.getId());
        boolean isOwningSme = payment.getTask() != null
            && payment.getTask().getSme() != null
            && payment.getTask().getSme().getUser().getId().equals(user.getId());

        if (!isAdmin && !isPaidYouth && !isOwningSme) {
            throw new AccessDeniedException("You cannot view this payment");
        }
        return PaymentResponse.from(payment);
    }

    public List<PaymentResponse> listMine(String youthEmail) {
        Youth youth = userAccountService.requireYouth(youthEmail);
        return paymentRepository.findByYouth(youth)
            .stream()
            .map(PaymentResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * Sandbox payout: debit the funder reserve and mark the payment PAID.
     * Only an admin can release money.
     */
    @Transactional
    public PaymentResponse payout(Long paymentId, String adminEmail) {
        var admin = userAccountService.requireUser(adminEmail);
        userAccountService.requireAnyRole(admin, "ADMIN");

        Payment payment = requirePayment(paymentId);
        if ("PAID".equals(payment.getStatus())) {
            throw new BusinessRuleException("Payment " + paymentId + " has already been paid");
        }
        if (!"PENDING".equals(payment.getStatus())) {
            throw new BusinessRuleException(
                "Only a PENDING payment can be paid (current: " + payment.getStatus() + ")");
        }

        double amount = payment.getYouthAmount() != null ? payment.getYouthAmount() : 0.0;
        String reference = "SANDBOX-" + UUID.randomUUID();

        if (amount > 0) {
            walletService.debitReserve(amount, "Payout for payment " + paymentId);
        }

        payment.markAsPaid(reference);
        Payment saved = paymentRepository.save(payment);

        eventPublisher.publishEvent(new PaymentPaidEvent(
            saved.getId(),
            saved.getYouth().getId(),
            saved.getYouthAmount(),
            saved.getReference()
        ));

        return PaymentResponse.from(saved);
    }

    public PaymentResponse getPayment(Long paymentId) {
        return PaymentResponse.from(requirePayment(paymentId));
    }

    private Payment requirePayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
    }
}
