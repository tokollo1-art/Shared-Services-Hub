package com.ssh.service;

import com.ssh.dto.request.VerificationRequest;
import com.ssh.dto.response.ExperienceResponse;
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
import com.ssh.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Task lifecycle tests: assignment requires a live application, completion
 * creates exactly one experience, verification creates the experience +
 * payment + ledger + event atomically, and rejection requires a reason.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final String OWNER_EMAIL = "thandi@ssh.co.za";
    private static final String OTHER_SME_EMAIL = "other@ssh.co.za";
    private static final String YOUTH_EMAIL = "sipho@ssh.co.za";
    private static final String OTHER_YOUTH_EMAIL = "naledi@ssh.co.za";

    @Mock private TaskRepository taskRepository;
    @Mock private ApplicationService applicationService;
    @Mock private TierEligibilityService tierEligibilityService;
    @Mock private ExperienceService experienceService;
    @Mock private PaymentService paymentService;
    @Mock private LedgerService ledgerService;
    @Mock private UserAccountService userAccountService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private TaskService taskService;

    private User ownerUser;
    private SME ownerSme;
    private User otherSmeUser;
    private SME otherSme;
    private User youthUser;
    private Youth youth;
    private User otherYouthUser;
    private Youth otherYouth;
    private Task task;
    private Application application;
    private Experience experience;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(
            taskRepository,
            applicationService,
            tierEligibilityService,
            experienceService,
            paymentService,
            ledgerService,
            userAccountService,
            eventPublisher
        );

        ownerUser = user(1L, "Thandi Nkosi", OWNER_EMAIL, "SME");
        ownerSme = sme(10L, ownerUser, "Thandi's Accounting");

        otherSmeUser = user(2L, "Peter Molefe", OTHER_SME_EMAIL, "SME");
        otherSme = sme(20L, otherSmeUser, "Molefe Trading");

        youthUser = user(3L, "Sipho Dlamini", YOUTH_EMAIL, "YOUTH");
        youth = youth(30L, youthUser);

        otherYouthUser = user(4L, "Naledi Mokoena", OTHER_YOUTH_EMAIL, "YOUTH");
        otherYouth = youth(40L, otherYouthUser);

        task = new Task("Design a logo", "Create a brand identity", "DESIGN", ownerSme);
        task.setId(100L);
        task.setBudget(1000.0);

        application = new Application(
            task, youth, "I can start today", LocalDateTime.now().plusHours(48));
        application.setId(200L);

        experience = new Experience(youth, task);
        experience.setId(500L);
    }

    // ============================================
    // ASSIGN
    // ============================================

    @Nested
    @DisplayName("Assign")
    class Assign {

        @Test
        @DisplayName("Given a live application, When the owning SME assigns, Then the task starts and the application is accepted")
        void shouldAssignOpenTask_WhenApplicantIsEligible() {
            givenSme(OWNER_EMAIL, ownerSme);
            givenTask();
            when(userAccountService.requireYouth(youth.getId())).thenReturn(youth);
            when(applicationService.requirePendingApplication(task, youth)).thenReturn(application);
            givenSaveReturnsTask();

            TaskResponse response =
                taskService.assignTask(task.getId(), youth.getId(), OWNER_EMAIL);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS.name());
            assertThat(task.getAssignedTo()).isEqualTo(youth);
            assertThat(response.getStatus()).isEqualTo("IN_PROGRESS");
            assertThat(response.getAssignedToId()).isEqualTo(youth.getId());

            verify(tierEligibilityService).assertCanBeAssigned(youth);
            verify(applicationService).accept(task, application);
        }

        @Test
        @DisplayName("Given a different SME, When assigning, Then access is denied")
        void shouldDenyAssignment_WhenCallerDoesNotOwnTask() {
            givenSme(OTHER_SME_EMAIL, otherSme);
            givenTask();

            assertThatThrownBy(() ->
                taskService.assignTask(task.getId(), youth.getId(), OTHER_SME_EMAIL))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("owns");

            assertThat(task.getStatus()).isEqualTo(TaskStatus.OPEN.name());
            assertThat(task.getAssignedTo()).isNull();
        }

        @Test
        @DisplayName("Given a youth, When assigning, Then access is denied")
        void shouldDenyAssignment_WhenCallerIsYouth() {
            when(userAccountService.requireSme(YOUTH_EMAIL))
                .thenThrow(new AccessDeniedException("Only an SME can perform this action"));

            assertThatThrownBy(() ->
                taskService.assignTask(task.getId(), youth.getId(), YOUTH_EMAIL))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only an SME");
        }

        @Test
        @DisplayName("Given no application, When assigning, Then the business rule rejects it")
        void shouldRejectAssignment_WhenYouthHasNotApplied() {
            givenSme(OWNER_EMAIL, ownerSme);
            givenTask();
            when(userAccountService.requireYouth(youth.getId())).thenReturn(youth);
            when(applicationService.requirePendingApplication(task, youth))
                .thenThrow(new BusinessRuleException("has not applied"));

            assertThatThrownBy(() ->
                taskService.assignTask(task.getId(), youth.getId(), OWNER_EMAIL))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("applied");

            assertThat(task.getStatus()).isEqualTo(TaskStatus.OPEN.name());
            verify(applicationService, never()).accept(any(), any());
        }

        @Test
        @DisplayName("Given a task already IN_PROGRESS, When assigned again, Then the transition is rejected")
        void shouldRejectRepeatAssignment() {
            task.setStatus(TaskStatus.IN_PROGRESS.name());
            givenSme(OWNER_EMAIL, ownerSme);
            givenTask();

            assertThatThrownBy(() ->
                taskService.assignTask(task.getId(), youth.getId(), OWNER_EMAIL))
                .isInstanceOf(InvalidTaskTransitionException.class)
                .hasMessageContaining("IN_PROGRESS");

            verify(applicationService, never()).requirePendingApplication(any(), any());
        }

        @Test
        @DisplayName("Given a COMPLETED task, When assigned, Then the transition is rejected")
        void shouldRejectAssignment_WhenTaskIsNotOpen() {
            task.setStatus(TaskStatus.COMPLETED.name());
            givenSme(OWNER_EMAIL, ownerSme);
            givenTask();

            assertThatThrownBy(() ->
                taskService.assignTask(task.getId(), youth.getId(), OWNER_EMAIL))
                .isInstanceOf(InvalidTaskTransitionException.class);
        }
    }

    // ============================================
    // COMPLETE
    // ============================================

    @Nested
    @DisplayName("Complete")
    class Complete {

        @Test
        @DisplayName("Given the assigned youth, When completing, Then one experience is recorded")
        void shouldCompleteTask_AndRecordExperience_WhenCallerIsAssignedYouth() {
            task.setStatus(TaskStatus.IN_PROGRESS.name());
            task.setAssignedTo(youth);
            givenYouth(YOUTH_EMAIL, youth);
            givenTask();
            when(experienceService.recordCompletion(task, youth)).thenReturn(experience);
            givenSaveReturnsTask();

            TaskResponse response = taskService.completeTask(task.getId(), YOUTH_EMAIL);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED.name());
            assertThat(response.getStatus()).isEqualTo("COMPLETED");
            verify(experienceService, times(1)).recordCompletion(task, youth);
        }

        @Test
        @DisplayName("Given a different youth, When completing, Then access is denied and no experience is created")
        void shouldDenyCompletion_WhenCallerIsNotAssignedYouth() {
            task.setStatus(TaskStatus.IN_PROGRESS.name());
            task.setAssignedTo(otherYouth);
            givenYouth(YOUTH_EMAIL, youth);
            givenTask();

            assertThatThrownBy(() -> taskService.completeTask(task.getId(), YOUTH_EMAIL))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("assigned");

            verify(experienceService, never()).recordCompletion(any(), any());
        }

        @Test
        @DisplayName("Given an SME, When completing, Then access is denied")
        void shouldDenyCompletion_WhenCallerIsSme() {
            when(userAccountService.requireYouth(OWNER_EMAIL))
                .thenThrow(new AccessDeniedException("Only a youth can perform this action"));

            assertThatThrownBy(() -> taskService.completeTask(task.getId(), OWNER_EMAIL))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only a youth");
        }

        @Test
        @DisplayName("Given an OPEN task, When completed, Then the transition is rejected")
        void shouldRejectCompletion_WhenTaskIsNotInProgress() {
            task.setAssignedTo(youth);
            givenYouth(YOUTH_EMAIL, youth);
            givenTask();

            assertThatThrownBy(() -> taskService.completeTask(task.getId(), YOUTH_EMAIL))
                .isInstanceOf(InvalidTaskTransitionException.class);

            verify(experienceService, never()).recordCompletion(any(), any());
        }

        @Test
        @DisplayName("Given an already COMPLETED task, When completed again, Then no second experience is created")
        void shouldRejectRepeatCompletion_WithoutCreatingASecondExperience() {
            task.setStatus(TaskStatus.COMPLETED.name());
            task.setAssignedTo(youth);
            givenYouth(YOUTH_EMAIL, youth);
            givenTask();

            assertThatThrownBy(() -> taskService.completeTask(task.getId(), YOUTH_EMAIL))
                .isInstanceOf(InvalidTaskTransitionException.class);

            verify(experienceService, never()).recordCompletion(any(), any());
        }
    }

    // ============================================
    // VERIFY
    // ============================================

    @Nested
    @DisplayName("Verify")
    class Verify {

        @Test
        @DisplayName("Given valid input, When the owning SME verifies, Then task, experience, payment, ledger and event are produced")
        void shouldVerifyTask_AndCreatePayment_WhenCallerOwnsTask() {
            task.setStatus(TaskStatus.COMPLETED.name());
            task.setAssignedTo(youth);

            Payment payment = new Payment(task, youth, 1000.0, 10.0);
            payment.setId(600L);

            when(userAccountService.requireUser(OWNER_EMAIL)).thenReturn(ownerUser);
            when(userAccountService.requireSme(ownerUser)).thenReturn(ownerSme);
            givenTask();
            when(experienceService.requireForTask(task)).thenReturn(experience);
            when(experienceService.save(experience)).thenReturn(experience);
            when(paymentService.createForVerifiedTask(task, youth)).thenReturn(payment);
            when(experienceService.toResponse(experience)).thenReturn(experienceResponse());
            givenSaveReturnsTask();

            VerificationRequest request =
                new VerificationRequest(5.0, "Excellent work", "https://portfolio.example.com/logo");

            VerificationResponse response =
                taskService.verifyTask(task.getId(), request, OWNER_EMAIL);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.VERIFIED.name());
            assertThat(experience.getStatus()).isEqualTo("VERIFIED");
            assertThat(experience.isVerified()).isTrue();
            assertThat(experience.getRating()).isEqualTo(5.0);
            assertThat(experience.getFeedback()).isEqualTo("Excellent work");
            assertThat(experience.getReference()).isEqualTo("https://portfolio.example.com/logo");
            assertThat(response.task().getStatus()).isEqualTo("VERIFIED");
            assertThat(response.payment().totalAmount()).isEqualTo(1000.0);
            assertThat(response.payment().platformFee()).isEqualTo(100.0);
            assertThat(response.payment().youthAmount()).isEqualTo(900.0);

            verify(paymentService, times(1)).createForVerifiedTask(task, youth);
            verify(ledgerService, times(1)).append(experience);

            ArgumentCaptor<TaskVerifiedEvent> eventCaptor =
                ArgumentCaptor.forClass(TaskVerifiedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            TaskVerifiedEvent event = eventCaptor.getValue();
            assertThat(event.taskId()).isEqualTo(task.getId());
            assertThat(event.experienceId()).isEqualTo(experience.getId());
            assertThat(event.paymentId()).isEqualTo(payment.getId());
            assertThat(event.youthAmount()).isEqualTo(900.0);
        }

        @Test
        @DisplayName("Given missing rating, When verifying, Then the request is rejected before any write")
        void shouldRejectVerification_WhenRatingIsMissing() {
            assertInvalid(new VerificationRequest(null, "Great", "ref"));
        }

        @Test
        @DisplayName("Given a rating below 1, When verifying, Then the request is rejected")
        void shouldRejectVerification_WhenRatingIsTooLow() {
            assertInvalid(new VerificationRequest(0.5, "Great", "ref"));
        }

        @Test
        @DisplayName("Given a rating above 5, When verifying, Then the request is rejected")
        void shouldRejectVerification_WhenRatingIsTooHigh() {
            assertInvalid(new VerificationRequest(5.5, "Great", "ref"));
        }

        @Test
        @DisplayName("Given blank feedback, When verifying, Then the request is rejected")
        void shouldRejectVerification_WhenFeedbackIsBlank() {
            assertInvalid(new VerificationRequest(4.0, "   ", "ref"));
        }

        @Test
        @DisplayName("Given a blank reference, When verifying, Then the request is rejected")
        void shouldRejectVerification_WhenReferenceIsBlank() {
            assertInvalid(new VerificationRequest(4.0, "Great", ""));
        }

        @Test
        @DisplayName("Given the assigned youth, When verifying their own work, Then access is denied")
        void shouldDenyVerification_WhenCallerIsAssignedYouth() {
            task.setStatus(TaskStatus.COMPLETED.name());
            task.setAssignedTo(youth);
            when(userAccountService.requireUser(YOUTH_EMAIL)).thenReturn(youthUser);
            givenTask();

            assertThatThrownBy(() -> taskService.verifyTask(
                task.getId(), validRequest(), YOUTH_EMAIL))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("assigned youth cannot verify");

            assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED.name());
        }

        @Test
        @DisplayName("Given a different SME, When verifying, Then access is denied")
        void shouldDenyVerification_WhenCallerDoesNotOwnTask() {
            task.setStatus(TaskStatus.COMPLETED.name());
            task.setAssignedTo(youth);
            when(userAccountService.requireUser(OTHER_SME_EMAIL)).thenReturn(otherSmeUser);
            when(userAccountService.requireSme(otherSmeUser)).thenReturn(otherSme);
            givenTask();

            assertThatThrownBy(() -> taskService.verifyTask(
                task.getId(), validRequest(), OTHER_SME_EMAIL))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("owns");
        }

        @Test
        @DisplayName("Given an IN_PROGRESS task, When verified, Then the transition is rejected")
        void shouldRejectVerification_WhenTaskIsNotCompleted() {
            task.setStatus(TaskStatus.IN_PROGRESS.name());
            task.setAssignedTo(youth);
            when(userAccountService.requireUser(OWNER_EMAIL)).thenReturn(ownerUser);
            when(userAccountService.requireSme(ownerUser)).thenReturn(ownerSme);
            givenTask();

            assertThatThrownBy(() -> taskService.verifyTask(
                task.getId(), validRequest(), OWNER_EMAIL))
                .isInstanceOf(InvalidTaskTransitionException.class);
        }

        @Test
        @DisplayName("Given an already VERIFIED task, When verified again, Then no second payment is created")
        void shouldRejectRepeatVerification_WithoutCreatingASecondPayment() {
            task.setStatus(TaskStatus.VERIFIED.name());
            task.setAssignedTo(youth);
            when(userAccountService.requireUser(OWNER_EMAIL)).thenReturn(ownerUser);
            when(userAccountService.requireSme(ownerUser)).thenReturn(ownerSme);
            givenTask();

            assertThatThrownBy(() -> taskService.verifyTask(
                task.getId(), validRequest(), OWNER_EMAIL))
                .isInstanceOf(InvalidTaskTransitionException.class);

            verify(paymentService, never()).createForVerifiedTask(any(), any());
        }

        @Test
        @DisplayName("Given no completion record, When verifying, Then the business rule rejects it")
        void shouldRejectVerification_WhenExperienceIsMissing() {
            task.setStatus(TaskStatus.COMPLETED.name());
            task.setAssignedTo(youth);
            when(userAccountService.requireUser(OWNER_EMAIL)).thenReturn(ownerUser);
            when(userAccountService.requireSme(ownerUser)).thenReturn(ownerSme);
            givenTask();
            when(experienceService.requireForTask(task))
                .thenThrow(new BusinessRuleException("must complete it first"));

            assertThatThrownBy(() -> taskService.verifyTask(
                task.getId(), validRequest(), OWNER_EMAIL))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("complete");

            verify(paymentService, never()).createForVerifiedTask(any(), any());
        }

        @Test
        @DisplayName("Given an experience id, When verified, Then the same task verification runs")
        void shouldVerifyThroughExperienceAlias() {
            task.setStatus(TaskStatus.COMPLETED.name());
            task.setAssignedTo(youth);

            Payment payment = new Payment(task, youth, 1000.0, 10.0);
            payment.setId(600L);

            when(experienceService.requireExperience(experience.getId())).thenReturn(experience);
            when(userAccountService.requireUser(OWNER_EMAIL)).thenReturn(ownerUser);
            when(userAccountService.requireSme(ownerUser)).thenReturn(ownerSme);
            givenTask();
            when(experienceService.requireForTask(task)).thenReturn(experience);
            when(experienceService.save(experience)).thenReturn(experience);
            when(paymentService.createForVerifiedTask(task, youth)).thenReturn(payment);
            when(experienceService.toResponse(experience)).thenReturn(experienceResponse());
            givenSaveReturnsTask();

            VerificationResponse response = taskService.verifyExperience(
                experience.getId(), validRequest(), OWNER_EMAIL);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.VERIFIED.name());
            assertThat(response.experience().getReference()).isEqualTo("https://portfolio.example.com/logo");
        }

        private void assertInvalid(VerificationRequest request) {
            assertThatThrownBy(() ->
                taskService.verifyTask(999L, request, OWNER_EMAIL))
                .isInstanceOf(InvalidVerificationException.class);

            verify(paymentService, never()).createForVerifiedTask(any(), any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    // ============================================
    // REJECT / RETURN FOR REVISION
    // ============================================

    @Nested
    @DisplayName("Send back for revision")
    class Reject {

        @Test
        @DisplayName("Given a reason, When the owner rejects, Then the task returns to IN_PROGRESS with the reason stored")
        void shouldReturnTaskToInProgress_WhenOwnerRejectsCompletedWork() {
            task.setStatus(TaskStatus.COMPLETED.name());
            task.setAssignedTo(youth);
            givenSme(OWNER_EMAIL, ownerSme);
            givenTask();
            givenSaveReturnsTask();

            TaskResponse response = taskService.rejectTask(
                task.getId(), "Fix the SKU column", OWNER_EMAIL);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS.name());
            assertThat(task.getAssignedTo()).isEqualTo(youth);
            assertThat(task.getRevisionReason()).isEqualTo("Fix the SKU column");
            assertThat(task.getRevisionCount()).isEqualTo(1);
            assertThat(response.getRevisionReason()).isEqualTo("Fix the SKU column");
            assertThat(response.getRevisionCount()).isEqualTo(1);
            verify(experienceService).rejectForRevision(task, "Fix the SKU column");
        }

        @Test
        @DisplayName("Given no reason, When rejecting, Then the request is rejected before any write")
        void shouldRejectRejection_WhenReasonIsMissing() {
            assertThatThrownBy(() ->
                taskService.rejectTask(task.getId(), "  ", OWNER_EMAIL))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("reason");

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given a different SME, When rejecting, Then access is denied")
        void shouldDenyRejection_WhenCallerDoesNotOwnTask() {
            task.setStatus(TaskStatus.COMPLETED.name());
            task.setAssignedTo(youth);
            givenSme(OTHER_SME_EMAIL, otherSme);
            givenTask();

            assertThatThrownBy(() ->
                taskService.rejectTask(task.getId(), "Rework", OTHER_SME_EMAIL))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("owns");
        }

        @Test
        @DisplayName("Given an IN_PROGRESS task, When rejected, Then the transition is rejected")
        void shouldRejectRejection_WhenTaskIsNotCompleted() {
            task.setStatus(TaskStatus.IN_PROGRESS.name());
            task.setAssignedTo(youth);
            givenSme(OWNER_EMAIL, ownerSme);
            givenTask();

            assertThatThrownBy(() ->
                taskService.rejectTask(task.getId(), "Rework", OWNER_EMAIL))
                .isInstanceOf(InvalidTaskTransitionException.class);
        }

        @Test
        @DisplayName("Given rejected work, When the youth resubmits, Then the completion path runs again")
        void shouldAllowResubmission_AfterRejection() {
            task.setStatus(TaskStatus.IN_PROGRESS.name());
            task.setAssignedTo(youth);
            task.setRevisionCount(1);
            givenYouth(YOUTH_EMAIL, youth);
            givenTask();
            when(experienceService.recordCompletion(task, youth)).thenReturn(experience);
            givenSaveReturnsTask();

            taskService.completeTask(task.getId(), YOUTH_EMAIL);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED.name());
            verify(experienceService).recordCompletion(task, youth);
        }
    }

    // ============================================
    // HELPERS
    // ============================================

    private static User user(Long id, String name, String email, String role) {
        User user = new User(name, email, "hashed-password", role);
        user.setId(id);
        return user;
    }

    private static SME sme(Long id, User user, String businessName) {
        SME sme = new SME(user, businessName);
        sme.setId(id);
        return sme;
    }

    private static Youth youth(Long id, User user) {
        Youth youth = new Youth(user);
        youth.setId(id);
        return youth;
    }

    private void givenSme(String email, SME sme) {
        when(userAccountService.requireSme(email)).thenReturn(sme);
    }

    private void givenYouth(String email, Youth youth) {
        when(userAccountService.requireYouth(email)).thenReturn(youth);
    }

    private void givenTask() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
    }

    private void givenSaveReturnsTask() {
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static VerificationRequest validRequest() {
        return new VerificationRequest(
            5.0, "Excellent work", "https://portfolio.example.com/logo");
    }

    private ExperienceResponse experienceResponse() {
        ExperienceResponse response = new ExperienceResponse();
        response.setId(experience.getId());
        response.setTaskId(task.getId());
        response.setStatus("VERIFIED");
        response.setVerified(true);
        response.setReference("https://portfolio.example.com/logo");
        return response;
    }
}
