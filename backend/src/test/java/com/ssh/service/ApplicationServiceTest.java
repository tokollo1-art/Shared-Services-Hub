package com.ssh.service;

import com.ssh.dto.request.ApplicationRequest;
import com.ssh.dto.response.ApplicationResponse;
import com.ssh.entity.Application;
import com.ssh.entity.ApplicationStatus;
import com.ssh.entity.SME;
import com.ssh.entity.Task;
import com.ssh.entity.User;
import com.ssh.entity.Youth;
import com.ssh.exception.BusinessRuleException;
import com.ssh.exception.DuplicateResourceException;
import com.ssh.exception.TierEligibilityException;
import com.ssh.repository.ApplicationRepository;
import com.ssh.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    private static final LocalDateTime NOW =
        LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final String YOUTH_EMAIL = "sipho@ssh.co.za";
    private static final String OTHER_YOUTH_EMAIL = "naledi@ssh.co.za";
    private static final String SME_EMAIL = "thandi@ssh.co.za";

    @Mock private ApplicationRepository applicationRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private UserAccountService userAccountService;
    @Mock private TierEligibilityService tierEligibilityService;

    private ApplicationService applicationService;

    private Youth youth;
    private Youth otherYouth;
    private User smeUser;
    private SME sme;
    private Task task;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
            Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
        applicationService = new ApplicationService(
            applicationRepository, taskRepository, userAccountService,
            tierEligibilityService, clock);

        User youthUser = new User("Sipho Dlamini", YOUTH_EMAIL, "hash", "YOUTH");
        youthUser.setId(3L);
        youth = new Youth(youthUser);
        youth.setId(30L);

        User otherYouthUser = new User("Naledi Mokoena", OTHER_YOUTH_EMAIL, "hash", "YOUTH");
        otherYouthUser.setId(4L);
        otherYouth = new Youth(otherYouthUser);
        otherYouth.setId(40L);

        smeUser = new User("Thandi Nkosi", SME_EMAIL, "hash", "SME");
        smeUser.setId(1L);
        sme = new SME(smeUser, "Thandi's Accounting");
        sme.setId(10L);

        task = new Task("Design a logo", "Brand identity", "DESIGN", sme);
        task.setId(100L);
    }

    @Nested
    @DisplayName("Apply")
    class Apply {

        @Test
        @DisplayName("Given an open task and room in the tier, When applying, Then a 48-hour PENDING reservation is created")
        void shouldCreatePendingApplication() {
            when(userAccountService.requireYouth(YOUTH_EMAIL)).thenReturn(youth);
            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
            when(applicationRepository.findByTaskAndYouth(task, youth)).thenReturn(Optional.empty());
            when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            ApplicationResponse response = applicationService.apply(
                task.getId(), new ApplicationRequest("I can start today"), YOUTH_EMAIL);

            assertThat(response.status()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(response.coverNote()).isEqualTo("I can start today");
            assertThat(response.expiresAt()).isEqualTo(NOW.plusHours(48));
            verify(tierEligibilityService).assertCanApply(youth);
        }

        @Test
        @DisplayName("Given an already PENDING application, When applying again, Then 409 duplicate")
        void shouldRejectDuplicatePendingApplication() {
            Application existing = new Application(
                task, youth, "First", NOW.plusHours(48));
            when(userAccountService.requireYouth(YOUTH_EMAIL)).thenReturn(youth);
            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
            when(applicationRepository.findByTaskAndYouth(task, youth))
                .thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> applicationService.apply(
                task.getId(), new ApplicationRequest("Second"), YOUTH_EMAIL))
                .isInstanceOf(DuplicateResourceException.class);

            verify(applicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given an ACCEPTED application, When applying again, Then 409 duplicate")
        void shouldRejectDuplicateAcceptedApplication() {
            Application existing = new Application(
                task, youth, "First", NOW.plusHours(48));
            existing.setStatus(ApplicationStatus.ACCEPTED);
            when(userAccountService.requireYouth(YOUTH_EMAIL)).thenReturn(youth);
            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
            when(applicationRepository.findByTaskAndYouth(task, youth))
                .thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> applicationService.apply(
                task.getId(), new ApplicationRequest(null), YOUTH_EMAIL))
                .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("Given an EXPIRED application, When applying again, Then the same row is reactivated")
        void shouldReactivateExpiredApplication() {
            Application expired = new Application(
                task, youth, "Old", NOW.minusHours(1));
            expired.setId(200L);
            expired.setStatus(ApplicationStatus.EXPIRED);

            when(userAccountService.requireYouth(YOUTH_EMAIL)).thenReturn(youth);
            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
            when(applicationRepository.findByTaskAndYouth(task, youth))
                .thenReturn(Optional.of(expired));
            when(applicationRepository.save(expired)).thenReturn(expired);

            ApplicationResponse response = applicationService.apply(
                task.getId(), new ApplicationRequest("Try again"), YOUTH_EMAIL);

            assertThat(response.id()).isEqualTo(200L);
            assertThat(response.status()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(response.coverNote()).isEqualTo("Try again");
            assertThat(response.decidedAt()).isNull();
            assertThat(response.expiresAt()).isEqualTo(NOW.plusHours(48));
        }

        @Test
        @DisplayName("Given a stale PENDING application, When applying again, Then it expires and reactivates instead of 409")
        void shouldExpireStalePendingApplication_AndReactivate() {
            Application stale = new Application(
                task, youth, "Old", NOW.minusHours(1));
            stale.setId(201L);
            // The row was never swept by a tier/listing call, so it is still PENDING.
            stale.setStatus(ApplicationStatus.PENDING);

            when(userAccountService.requireYouth(YOUTH_EMAIL)).thenReturn(youth);
            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
            when(applicationRepository.findByTaskAndYouth(task, youth))
                .thenReturn(Optional.of(stale));
            when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            ApplicationResponse response = applicationService.apply(
                task.getId(), new ApplicationRequest("Second attempt"), YOUTH_EMAIL);

            assertThat(response.id()).isEqualTo(201L);
            assertThat(response.status()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(response.expiresAt()).isEqualTo(NOW.plusHours(48));
            assertThat(response.coverNote()).isEqualTo("Second attempt");
        }

        @Test
        @DisplayName("Given a non-open task, When applying, Then the business rule rejects it")
        void shouldRejectApplication_WhenTaskIsNotOpen() {
            task.setStatus("IN_PROGRESS");
            when(userAccountService.requireYouth(YOUTH_EMAIL)).thenReturn(youth);
            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

            assertThatThrownBy(() -> applicationService.apply(
                task.getId(), new ApplicationRequest(null), YOUTH_EMAIL))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not open");

            verify(applicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Given the Tier 0 ceiling is reached, When applying, Then eligibility blocks it")
        void shouldRejectApplication_WhenTierCeilingIsReached() {
            when(userAccountService.requireYouth(YOUTH_EMAIL)).thenReturn(youth);
            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
            when(applicationRepository.findByTaskAndYouth(task, youth)).thenReturn(Optional.empty());
            doThrow(new TierEligibilityException("Tier TIER_0 allows at most 1"))
                .when(tierEligibilityService).assertCanApply(youth);

            assertThatThrownBy(() -> applicationService.apply(
                task.getId(), new ApplicationRequest(null), YOUTH_EMAIL))
                .isInstanceOf(TierEligibilityException.class);

            verify(applicationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Assignment checks")
    class AssignmentChecks {

        @Test
        @DisplayName("Given a live PENDING application, When required for assignment, Then it is returned")
        void shouldReturnPendingApplication() {
            Application pending = new Application(
                task, youth, null, NOW.plusHours(1));
            when(applicationRepository.findByTaskAndYouth(task, youth))
                .thenReturn(Optional.of(pending));

            Application result = applicationService.requirePendingApplication(task, youth);

            assertThat(result).isSameAs(pending);
        }

        @Test
        @DisplayName("Given an expired reservation, When required for assignment, Then it expires and assignment is blocked")
        void shouldExpireReservation_WhenAssignmentIsTooLate() {
            Application expired = new Application(
                task, youth, null, NOW.minusMinutes(1));
            when(applicationRepository.findByTaskAndYouth(task, youth))
                .thenReturn(Optional.of(expired));

            assertThatThrownBy(() ->
                applicationService.requirePendingApplication(task, youth))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("expired");

            assertThat(expired.getStatus()).isEqualTo(ApplicationStatus.EXPIRED);
            verify(applicationRepository).save(expired);
        }

        @Test
        @DisplayName("Given a REJECTED application, When required for assignment, Then it is rejected")
        void shouldRejectAssignment_WhenApplicationIsNotPending() {
            Application rejected = new Application(
                task, youth, null, NOW.plusHours(1));
            rejected.setStatus(ApplicationStatus.REJECTED);
            when(applicationRepository.findByTaskAndYouth(task, youth))
                .thenReturn(Optional.of(rejected));

            assertThatThrownBy(() ->
                applicationService.requirePendingApplication(task, youth))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not PENDING");
        }

        @Test
        @DisplayName("Given no application, When required for assignment, Then it is rejected")
        void shouldRejectAssignment_WhenThereIsNoApplication() {
            when(applicationRepository.findByTaskAndYouth(task, youth))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                applicationService.requirePendingApplication(task, youth))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("has not applied");
        }

        @Test
        @DisplayName("Given a chosen applicant, When accepted, Then all other pending applicants are rejected")
        void shouldRejectOtherApplicants_WhenOneIsAccepted() {
            Application accepted = new Application(
                task, youth, null, NOW.plusHours(1));
            accepted.setId(1L);
            Application other = new Application(
                task, otherYouth, null, NOW.plusHours(1));
            other.setId(2L);

            when(applicationRepository.findByTaskOrderByCreatedAtAsc(task))
                .thenReturn(List.of(accepted, other));
            when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            applicationService.accept(task, accepted);

            assertThat(accepted.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
            assertThat(other.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        }
    }

    @Nested
    @DisplayName("Withdraw")
    class Withdraw {

        @Test
        @DisplayName("Given the applicant, When withdrawing, Then the reservation is released")
        void shouldWithdrawPendingApplication() {
            Application pending = new Application(
                task, youth, null, NOW.plusHours(10));
            pending.setId(5L);
            when(userAccountService.requireYouth(YOUTH_EMAIL)).thenReturn(youth);
            when(applicationRepository.findById(5L)).thenReturn(Optional.of(pending));
            when(applicationRepository.save(pending)).thenReturn(pending);

            ApplicationResponse response =
                applicationService.withdraw(5L, YOUTH_EMAIL);

            assertThat(response.status()).isEqualTo(ApplicationStatus.WITHDRAWN);
        }

        @Test
        @DisplayName("Given another youth, When withdrawing, Then access is denied")
        void shouldDenyWithdrawal_WhenCallerIsNotTheApplicant() {
            Application pending = new Application(
                task, youth, null, NOW.plusHours(10));
            pending.setId(5L);
            when(userAccountService.requireYouth(OTHER_YOUTH_EMAIL)).thenReturn(otherYouth);
            when(applicationRepository.findById(5L)).thenReturn(Optional.of(pending));

            assertThatThrownBy(() -> applicationService.withdraw(5L, OTHER_YOUTH_EMAIL))
                .isInstanceOf(AccessDeniedException.class);

            verify(applicationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("SME listing")
    class SmeListing {

        @Test
        @DisplayName("Given the owning SME, When listing applications, Then all are returned")
        void shouldListApplications_ForOwningSme() {
            Application pending = new Application(
                task, youth, null, NOW.plusHours(10));
            pending.setId(7L);
            when(userAccountService.requireSme(SME_EMAIL)).thenReturn(sme);
            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
            when(applicationRepository.findByTaskOrderByCreatedAtAsc(task))
                .thenReturn(List.of(pending));

            List<ApplicationResponse> applications =
                applicationService.listForTask(task.getId(), SME_EMAIL);

            assertThat(applications).hasSize(1);
            assertThat(applications.get(0).youthId()).isEqualTo(youth.getId());
        }

        @Test
        @DisplayName("Given a different SME, When listing applications, Then access is denied")
        void shouldDenyListing_WhenCallerDoesNotOwnTask() {
            User otherUser = new User("Peter", "peter@ssh.co.za", "hash", "SME");
            otherUser.setId(2L);
            SME otherSme = new SME(otherUser, "Molefe Trading");
            otherSme.setId(20L);

            when(userAccountService.requireSme("peter@ssh.co.za")).thenReturn(otherSme);
            when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

            assertThatThrownBy(() ->
                applicationService.listForTask(task.getId(), "peter@ssh.co.za"))
                .isInstanceOf(AccessDeniedException.class);
        }
    }
}
