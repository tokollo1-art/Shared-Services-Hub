package com.ssh.integration;

import com.ssh.dto.request.ApplicationRequest;
import com.ssh.dto.request.TaskRequest;
import com.ssh.dto.request.VerificationRequest;
import com.ssh.dto.response.ApplicationResponse;
import com.ssh.dto.response.PaymentResponse;
import com.ssh.dto.response.TierInfoResponse;
import com.ssh.dto.response.VerificationResponse;
import com.ssh.entity.Application;
import com.ssh.entity.ApplicationStatus;
import com.ssh.entity.Experience;
import com.ssh.entity.LedgerEntry;
import com.ssh.entity.Payment;
import com.ssh.entity.SME;
import com.ssh.entity.Task;
import com.ssh.entity.TaskStatus;
import com.ssh.entity.User;
import com.ssh.entity.Youth;
import com.ssh.event.PaymentPaidEvent;
import com.ssh.event.TaskVerifiedEvent;
import com.ssh.exception.InvalidTaskTransitionException;
import com.ssh.repository.ApplicationRepository;
import com.ssh.repository.ExperienceRepository;
import com.ssh.repository.LedgerEntryRepository;
import com.ssh.repository.PaymentRepository;
import com.ssh.repository.SMERepository;
import com.ssh.repository.TaskRepository;
import com.ssh.repository.UserRepository;
import com.ssh.repository.YouthRepository;
import com.ssh.service.ApplicationService;
import com.ssh.service.LedgerService;
import com.ssh.service.PaymentService;
import com.ssh.service.TaskService;
import com.ssh.service.TierEligibilityService;
import com.ssh.service.WalletService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end application flow against the real persistence layer:
 * apply -> assign -> complete -> verify -> Experience + Payment + ledger +
 * event, then sandbox payout. The test transaction rolls everything back.
 */
@SpringBootTest
@Transactional
@RecordApplicationEvents
class FullFlowIntegrationTest {

    @Autowired private TaskService taskService;
    @Autowired private ApplicationService applicationService;
    @Autowired private TierEligibilityService tierEligibilityService;
    @Autowired private PaymentService paymentService;
    @Autowired private WalletService walletService;
    @Autowired private LedgerService ledgerService;

    @Autowired private UserRepository userRepository;
    @Autowired private SMERepository smeRepository;
    @Autowired private YouthRepository youthRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private ExperienceRepository experienceRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private LedgerEntryRepository ledgerEntryRepository;
    @Autowired private EntityManager entityManager;

    @Autowired private ApplicationEvents events;

    @Test
    @DisplayName("A youth can apply, be assigned, complete, be verified, paid and publicly verified")
    void shouldCompleteTheFullLoop_AndEnforceTierRules() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        User smeUser = userRepository.save(new User(
            "SME " + suffix, "sme-" + suffix + "@test.local", "hash", "SME"));
        SME sme = smeRepository.save(new SME(smeUser, "Test SME " + suffix));

        User youthUser = userRepository.save(new User(
            "Youth " + suffix, "youth-" + suffix + "@test.local", "hash", "YOUTH"));
        Youth youth = youthRepository.save(new Youth(youthUser));

        User admin = userRepository.save(new User(
            "Admin " + suffix, "admin-" + suffix + "@test.local", "hash", "ADMIN"));

        TaskRequest taskRequest = new TaskRequest(
            "Integration task " + suffix, "Full loop", "TEST");
        taskRequest.setBudget(1000.0);
        taskRequest.setDurationDays(3);
        taskRequest.setPaid(true);
        Long taskId = taskService.createTask(taskRequest, smeUser.getEmail()).getId();

        // ---- Apply ----
        ApplicationResponse application = applicationService.apply(
            taskId, new ApplicationRequest("I can start immediately"), youthUser.getEmail());

        assertThat(application.status()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(application.expiresAt()).isNotNull();

        TierInfoResponse initialTier = tierEligibilityService.getTierInfo(youth);
        assertThat(initialTier.tier()).isEqualTo("TIER_0");
        assertThat(initialTier.verifiedExperienceCount()).isZero();
        assertThat(initialTier.activeReservations()).isEqualTo(1);
        assertThat(initialTier.canApply()).isFalse();

        // ---- Assign ----
        taskService.assignTask(taskId, youth.getId(), smeUser.getEmail());

        Task assigned = taskRepository.findById(taskId).orElseThrow();
        assertThat(assigned.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS.name());
        assertThat(assigned.getAssignedTo().getId()).isEqualTo(youth.getId());

        Application accepted = applicationRepository.findByTaskAndYouth(assigned, youth).orElseThrow();
        assertThat(accepted.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);

        // ---- Complete ----
        taskService.completeTask(taskId, youthUser.getEmail());

        Task completed = taskRepository.findById(taskId).orElseThrow();
        assertThat(completed.getStatus()).isEqualTo(TaskStatus.COMPLETED.name());

        Experience pendingExperience =
            experienceRepository.findFirstByTask(completed).orElseThrow();
        assertThat(pendingExperience.getStatus()).isEqualTo("PENDING");
        assertThat(pendingExperience.getYouth().getId()).isEqualTo(youth.getId());

        // ---- Verify ----
        VerificationResponse verification = taskService.verifyTask(
            taskId,
            new VerificationRequest(5.0, "Excellent catalogue work", "https://evidence/" + suffix),
            smeUser.getEmail()
        );

        Task verified = taskRepository.findById(taskId).orElseThrow();
        assertThat(verified.getStatus()).isEqualTo(TaskStatus.VERIFIED.name());

        Experience verifiedExperience = experienceRepository
            .findFirstByTask(verified).orElseThrow();
        assertThat(verifiedExperience.getStatus()).isEqualTo("VERIFIED");
        assertThat(verifiedExperience.isVerified()).isTrue();
        assertThat(verifiedExperience.getRating()).isEqualTo(5.0);
        assertThat(verifiedExperience.getReference()).isEqualTo("https://evidence/" + suffix);

        assertThat(verification.task().getStatus()).isEqualTo("VERIFIED");
        assertThat(verification.experience().getStatus()).isEqualTo("VERIFIED");

        Payment payment = paymentRepository.findByTask(verified).orElseThrow();
        assertThat(payment.getTotalAmount()).isEqualTo(1000.0);
        assertThat(payment.getPlatformFee()).isEqualTo(100.0);
        assertThat(payment.getYouthAmount()).isEqualTo(900.0);
        assertThat(payment.getStatus()).isEqualTo("PENDING");

        LedgerEntry ledgerEntry = ledgerEntryRepository
            .findByExperienceId(verifiedExperience.getId()).orElseThrow();
        assertThat(ledgerEntry.getPreviousHash()).isNotBlank();
        assertThat(ledgerEntry.getHash()).hasSize(64);
        assertThat(ledgerEntry.getReference()).isEqualTo("https://evidence/" + suffix);

        assertThat(ledgerService.verifyExperience(verifiedExperience.getId()).valid()).isTrue();
        assertThat(ledgerService.verifyChain().valid()).isTrue();

        assertThat(events.stream(TaskVerifiedEvent.class).count()).isEqualTo(1);

        // Verification upgrades the youth from Tier 0 to Tier 1 (1 verified experience).
        TierInfoResponse upgradedTier = tierEligibilityService.getTierInfo(youth);
        assertThat(upgradedTier.verifiedExperienceCount()).isEqualTo(1);
        assertThat(upgradedTier.tier()).isEqualTo("TIER_1");

        // ---- Duplicate prevention ----
        assertThatThrownBy(() -> taskService.verifyTask(
            taskId,
            new VerificationRequest(4.0, "Again", "https://evidence/again"),
            smeUser.getEmail()))
            .isInstanceOf(InvalidTaskTransitionException.class);

        assertThat(experienceRepository.findAll().stream()
            .filter(e -> e.getTask().getId().equals(taskId)).count()).isEqualTo(1);
        assertThat(paymentRepository.findByTask(verified)).isPresent();

        // ---- Sandbox payout ----
        double reserveBefore = walletService.getReserve(admin.getEmail()).balance();
        PaymentResponse paid = paymentService.payout(payment.getId(), admin.getEmail());

        assertThat(paid.status()).isEqualTo("PAID");
        assertThat(paid.reference()).startsWith("SANDBOX-");
        assertThat(paid.paidAt()).isNotNull();
        assertThat(walletService.getReserve(admin.getEmail()).balance())
            .isEqualTo(reserveBefore - 900.0);
        assertThat(events.stream(PaymentPaidEvent.class).count()).isEqualTo(1);

        // Re-read the chain from the database (the persistence context still holds
        // the pre-truncation timestamps) to prove the stored hashes are stable.
        entityManager.flush();
        entityManager.clear();
        assertThat(ledgerService.verifyChain().valid()).isTrue();
        assertThat(ledgerService.verifyExperience(verifiedExperience.getId()).valid()).isTrue();
    }
}
