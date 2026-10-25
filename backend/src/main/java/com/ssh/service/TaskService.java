package com.ssh.service;

import com.ssh.dto.request.TaskRequest;
import com.ssh.dto.request.VerificationRequest;
import com.ssh.dto.response.PaymentResponse;
import com.ssh.dto.response.TaskResponse;
import com.ssh.dto.response.VerificationResponse;
import com.ssh.entity.Application;
import com.ssh.entity.Experience;
import com.ssh.entity.Payment;
import com.ssh.entity.SME;
import com.ssh.entity.Task;
import com.ssh.entity.TaskStatus;
import com.ssh.entity.User;
import com.ssh.entity.Youth;
import com.ssh.event.TaskVerifiedEvent;
import com.ssh.exception.BusinessRuleException;
import com.ssh.exception.InvalidRequestException;
import com.ssh.exception.InvalidTaskTransitionException;
import com.ssh.exception.InvalidVerificationException;
import com.ssh.exception.ResourceNotFoundException;
import com.ssh.repository.TaskRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Task lifecycle orchestration.
 *
 * Assign requires a PENDING application and re-checks tier eligibility.
 * Complete records exactly one Experience and stores the youth's reflection.
 * Verify updates the task and the experience together, creates exactly one
 * Payment, appends to the hash chain and publishes events — all inside one
 * transaction.
 *
 * Slot 2 integration:
 *   - Injects EventPublisher to notify the Python AI service of the
 *     TASK_POSTED, TASK_ASSIGNED, TASK_COMPLETED and EXPERIENCE_VERIFIED
 *     events. The webhook is sent after commit by EventPublisher's
 *     @TransactionalEventListener.
 *   - Stores the youth's reflection on the task at completion. The
 *     reflection is what the evidence agent extracts skills from.
 *
 * @author SSH Team
 * @version 1.2
 * @since 2026
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ApplicationService applicationService;
    private final TierEligibilityService tierEligibilityService;
    private final ExperienceService experienceService;
    private final PaymentService paymentService;
    private final LedgerService ledgerService;
    private final UserAccountService userAccountService;
    private final ApplicationEventPublisher eventPublisher;
    private final EventPublisher sshEventPublisher;

    public TaskService(TaskRepository taskRepository,
                       ApplicationService applicationService,
                       TierEligibilityService tierEligibilityService,
                       ExperienceService experienceService,
                       PaymentService paymentService,
                       LedgerService ledgerService,
                       UserAccountService userAccountService,
                       ApplicationEventPublisher eventPublisher,
                       EventPublisher sshEventPublisher) {
        this.taskRepository = taskRepository;
        this.applicationService = applicationService;
        this.tierEligibilityService = tierEligibilityService;
        this.experienceService = experienceService;
        this.paymentService = paymentService;
        this.ledgerService = ledgerService;
        this.userAccountService = userAccountService;
        this.eventPublisher = eventPublisher;
        this.sshEventPublisher = sshEventPublisher;
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request, String smeEmail) {
        SME sme = userAccountService.requireSme(smeEmail);

        Task task = new Task(
            request.getTitle(),
            request.getDescription(),
            request.getCategory(),
            sme
        );
        task.setBudget(request.getBudget());
        task.setDurationDays(request.getDurationDays());
        task.setPaid(request.isPaid());

        Task saved = taskRepository.save(task);
        sshEventPublisher.publishTaskPosted(saved);
        return toTaskResponse(saved);
    }

    public List<TaskResponse> listOpenTasks() {
        return taskRepository.findByStatus(TaskStatus.OPEN.name())
            .stream()
            .map(this::toTaskResponse)
            .collect(Collectors.toList());
    }

    public List<TaskResponse> listMyTasks(String smeEmail) {
        SME sme = userAccountService.requireSme(smeEmail);
        return taskRepository.findBySme(sme)
            .stream()
            .map(this::toTaskResponse)
            .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long id) {
        return toTaskResponse(requireTask(id));
    }

    /**
     * Assign an OPEN task to a youth who has a live PENDING application.
     * Ownership, application state and tier eligibility are all re-checked.
     */
    @Transactional
    public TaskResponse assignTask(Long taskId, Long youthId, String smeEmail) {
        SME sme = userAccountService.requireSme(smeEmail);
        Task task = requireTask(taskId);
        requireTaskOwner(task, sme, "assign");

        if (!TaskStatus.OPEN.name().equals(task.getStatus())) {
            throw new InvalidTaskTransitionException(
                task.getId(), task.getStatusEnum(), TaskStatus.IN_PROGRESS);
        }

        Youth youth = userAccountService.requireYouth(youthId);
        Application application = applicationService.requirePendingApplication(task, youth);

        tierEligibilityService.assertCanBeAssigned(youth);

        task.assignTo(youth);
        applicationService.accept(task, application);

        Task saved = taskRepository.save(task);
        sshEventPublisher.publishTaskAssigned(saved.getId(), youth.getId(), sme.getId());
        return toTaskResponse(saved);
    }

    /**
     * Complete an IN_PROGRESS task. Creates (or re-opens) the single
     * Experience row for the task inside the same transaction.
     *
     * The reflection is the youth's own account of the work. It is stored
     * on the task and sent to the AI service with the TASK_COMPLETED event.
     */
    @Transactional
    public TaskResponse completeTask(Long taskId, String youthEmail, String reflection) {
        Youth youth = userAccountService.requireYouth(youthEmail);
        Task task = requireTask(taskId);

        if (task.getAssignedTo() == null
            || !task.getAssignedTo().getId().equals(youth.getId())) {
            throw new AccessDeniedException(
                "Only the youth assigned to this task can complete it");
        }

        task.complete();
        if (reflection != null && !reflection.isBlank()) {
            task.setReflection(reflection.trim());
        }
        experienceService.recordCompletion(task, youth);

        Task saved = taskRepository.save(task);
        sshEventPublisher.publishTaskCompleted(
            saved.getId(),
            youth.getId(),
            saved.getSme().getId(),
            saved.getReflection() != null ? saved.getReflection() : ""
        );
        return toTaskResponse(saved);
    }

    /**
     * Verify completed work.
     *
     * rating, feedback and reference are mandatory. The task and its
     * experience move to VERIFIED together, exactly one payment is created,
     * the experience is appended to the hash chain, and both the internal
     * TaskVerifiedEvent and the SSH webhook EXPERIENCE_VERIFIED event fire.
     */
    @Transactional
    public VerificationResponse verifyTask(Long taskId, VerificationRequest request, String smeEmail) {
        validateVerificationInput(request);

        User user = userAccountService.requireUser(smeEmail);
        Task task = requireTask(taskId);

        if (task.getAssignedTo() != null
            && task.getAssignedTo().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                "The assigned youth cannot verify their own work");
        }

        SME sme = userAccountService.requireSme(user);
        requireTaskOwner(task, sme, "verify");

        if (!TaskStatus.COMPLETED.name().equals(task.getStatus())) {
            throw new InvalidTaskTransitionException(
                task.getId(), task.getStatusEnum(), TaskStatus.VERIFIED);
        }

        Experience experience = experienceService.requireForTask(task);
        if (experience.isVerified()) {
            throw new BusinessRuleException(
                "The experience for task " + taskId + " is already verified");
        }

        task.verify();
        experience.verify(
            request.feedback().trim(),
            request.rating(),
            request.reference().trim()
        );
        Experience verifiedExperience = experienceService.save(experience);

        Payment payment = paymentService.createForVerifiedTask(task, verifiedExperience.getYouth());
        ledgerService.append(verifiedExperience);

        Task saved = taskRepository.save(task);

        eventPublisher.publishEvent(new TaskVerifiedEvent(
            saved.getId(),
            verifiedExperience.getId(),
            payment.getId(),
            verifiedExperience.getYouth().getId(),
            sme.getId(),
            payment.getTotalAmount(),
            payment.getPlatformFee(),
            payment.getYouthAmount()
        ));

        sshEventPublisher.publishExperienceVerified(
            saved.getId(),
            verifiedExperience.getYouth().getId(),
            sme.getId(),
            request.rating().intValue(),
            request.feedback(),
            request.reference(),
            List.of()
        );

        return new VerificationResponse(
            toTaskResponse(saved),
            experienceService.toResponse(verifiedExperience),
            PaymentResponse.from(payment)
        );
    }

    @Transactional
    public VerificationResponse verifyExperience(Long experienceId,
                                                 VerificationRequest request,
                                                 String smeEmail) {
        Experience experience = experienceService.requireExperience(experienceId);
        return verifyTask(experience.getTask().getId(), request, smeEmail);
    }

    @Transactional
    public TaskResponse rejectTask(Long taskId, String reason, String smeEmail) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidRequestException("A revision reason is required");
        }

        SME sme = userAccountService.requireSme(smeEmail);
        Task task = requireTask(taskId);
        requireTaskOwner(task, sme, "reject");

        task.sendBackForRevision(reason.trim());
        experienceService.rejectForRevision(task, reason.trim());

        return toTaskResponse(taskRepository.save(task));
    }

    // ============================================
    // HELPERS
    // ============================================

    private Task requireTask(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }

    private void requireTaskOwner(Task task, SME sme, String action) {
        if (!task.getSme().getId().equals(sme.getId())) {
            throw new AccessDeniedException(
                "Only the SME who owns this task can " + action + " it");
        }
    }

    private void validateVerificationInput(VerificationRequest request) {
        if (request == null) {
            throw new InvalidVerificationException("Verification input is required");
        }
        if (request.rating() == null || request.rating() < 1.0 || request.rating() > 5.0) {
            throw new InvalidVerificationException("Rating is required and must be between 1 and 5");
        }
        if (request.feedback() == null || request.feedback().isBlank()) {
            throw new InvalidVerificationException("Feedback is required");
        }
        if (request.reference() == null || request.reference().isBlank()) {
            throw new InvalidVerificationException("A reference is required");
        }
    }

    public TaskResponse toTaskResponse(Task task) {
        TaskResponse response = new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getStatus()
        );

        response.setDescription(task.getDescription());
        response.setCategory(task.getCategory());
        response.setBudget(task.getBudget());
        response.setDurationDays(task.getDurationDays());
        response.setPaid(task.isPaid());
        response.setRevisionReason(task.getRevisionReason());
        response.setRevisionCount(task.getRevisionCount());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        if (task.getSme() != null) {
            response.setSmeId(task.getSme().getId());
            response.setSmeBusinessName(task.getSme().getBusinessName());
        }

        if (task.getAssignedTo() != null) {
            response.setAssignedToId(task.getAssignedTo().getId());
            response.setAssignedToName(task.getAssignedTo().getUser().getName());
        }

        return response;
    }
}
