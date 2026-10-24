package com.ssh.service;

import com.ssh.dto.response.TierInfoResponse;
import com.ssh.entity.Application;
import com.ssh.entity.ApplicationStatus;
import com.ssh.entity.SME;
import com.ssh.entity.Task;
import com.ssh.entity.Tier;
import com.ssh.entity.User;
import com.ssh.entity.Youth;
import com.ssh.exception.TierEligibilityException;
import com.ssh.repository.ApplicationRepository;
import com.ssh.repository.ExperienceRepository;
import com.ssh.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TierEligibilityServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Mock private ExperienceRepository experienceRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private TaskRepository taskRepository;

    private TierEligibilityService tierService;
    private Youth youth;
    private Task task;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
        tierService = new TierEligibilityService(
            experienceRepository, applicationRepository, taskRepository, clock);

        User user = new User("Sipho", "sipho@ssh.co.za", "hash", "YOUTH");
        user.setId(3L);
        youth = new Youth(user);
        youth.setId(30L);

        User smeUser = new User("Thandi", "thandi@ssh.co.za", "hash", "SME");
        SME sme = new SME(smeUser, "Thandi's Accounting");
        sme.setId(10L);
        task = new Task("Design a logo", "Brand identity", "DESIGN", sme);
        task.setId(100L);
    }

    @Nested
    @DisplayName("Tier ladder")
    class TierLadder {

        @Test
        @DisplayName("Verified counts map to the agreed tiers")
        void shouldMapVerifiedCountsToTiers() {
            assertThat(Tier.fromVerifiedCount(0)).isEqualTo(Tier.TIER_0);
            assertThat(Tier.fromVerifiedCount(1)).isEqualTo(Tier.TIER_1);
            assertThat(Tier.fromVerifiedCount(2)).isEqualTo(Tier.TIER_1);
            assertThat(Tier.fromVerifiedCount(3)).isEqualTo(Tier.TIER_2);
            assertThat(Tier.fromVerifiedCount(4)).isEqualTo(Tier.TIER_2);
            assertThat(Tier.fromVerifiedCount(5)).isEqualTo(Tier.TIER_3);
            assertThat(Tier.fromVerifiedCount(50)).isEqualTo(Tier.TIER_3);
        }

        @Test
        @DisplayName("Only VERIFIED experiences drive the tier")
        void shouldCountVerifiedExperiencesOnly() {
            when(experienceRepository.countByYouthAndIsVerifiedTrue(youth)).thenReturn(0L);

            assertThat(tierService.resolveTier(youth)).isEqualTo(Tier.TIER_0);
            verify(experienceRepository).countByYouthAndIsVerifiedTrue(youth);
        }
    }

    @Nested
    @DisplayName("Tier 0 ceiling")
    class TierZeroCeiling {

        @Test
        @DisplayName("Given no active reservation, When a Tier 0 youth applies, Then it is allowed")
        void shouldAllowFirstTierZeroApplication() {
            when(experienceRepository.countByYouthAndIsVerifiedTrue(youth)).thenReturn(0L);
            when(applicationRepository.findByYouthAndStatusIn(youth, List.of(ApplicationStatus.PENDING)))
                .thenReturn(List.of());
            when(taskRepository.countByAssignedToAndStatus(youth, "IN_PROGRESS")).thenReturn(0L);

            tierService.assertCanApply(youth);
        }

        @Test
        @DisplayName("Given one PENDING application, When a Tier 0 youth applies again, Then the ceiling blocks it")
        void shouldBlockSecondTierZeroReservation() {
            Application pending = pendingApplication(NOW.plusHours(10));
            when(experienceRepository.countByYouthAndIsVerifiedTrue(youth)).thenReturn(0L);
            when(applicationRepository.findByYouthAndStatusIn(youth, List.of(ApplicationStatus.PENDING)))
                .thenReturn(List.of(pending));
            when(taskRepository.countByAssignedToAndStatus(youth, "IN_PROGRESS")).thenReturn(0L);

            assertThatThrownBy(() -> tierService.assertCanApply(youth))
                .isInstanceOf(TierEligibilityException.class)
                .hasMessageContaining("TIER_0")
                .hasMessageContaining("48 hours");
        }

        @Test
        @DisplayName("Given an IN_PROGRESS task, When a Tier 0 youth applies, Then the active task blocks it")
        void shouldBlockTierZero_WhenTaskIsInProgress() {
            when(experienceRepository.countByYouthAndIsVerifiedTrue(youth)).thenReturn(0L);
            when(applicationRepository.findByYouthAndStatusIn(youth, List.of(ApplicationStatus.PENDING)))
                .thenReturn(List.of());
            when(taskRepository.countByAssignedToAndStatus(youth, "IN_PROGRESS")).thenReturn(1L);

            assertThatThrownBy(() -> tierService.assertCanApply(youth))
                .isInstanceOf(TierEligibilityException.class);
        }

        @Test
        @DisplayName("Tier 1 allows two reservations and blocks the third")
        void shouldEnforceTierOneCeiling() {
            when(experienceRepository.countByYouthAndIsVerifiedTrue(youth)).thenReturn(1L);
            Application a = pendingApplication(NOW.plusHours(10));
            Application b = pendingApplication(NOW.plusHours(10));
            when(applicationRepository.findByYouthAndStatusIn(youth, List.of(ApplicationStatus.PENDING)))
                .thenReturn(List.of(a, b));
            when(taskRepository.countByAssignedToAndStatus(youth, "IN_PROGRESS")).thenReturn(0L);

            assertThatThrownBy(() -> tierService.assertCanApply(youth))
                .isInstanceOf(TierEligibilityException.class)
                .hasMessageContaining("TIER_1");
        }

        @Test
        @DisplayName("Tier 3 has no reservation ceiling")
        void shouldAllowUnlimitedReservationsForTierThree() {
            when(experienceRepository.countByYouthAndIsVerifiedTrue(youth)).thenReturn(5L);
            when(applicationRepository.findByYouthAndStatusIn(youth, List.of(ApplicationStatus.PENDING)))
                .thenReturn(List.of(
                    pendingApplication(NOW.plusHours(10)),
                    pendingApplication(NOW.plusHours(10)),
                    pendingApplication(NOW.plusHours(10)),
                    pendingApplication(NOW.plusHours(10))));
            when(taskRepository.countByAssignedToAndStatus(youth, "IN_PROGRESS")).thenReturn(0L);

            tierService.assertCanApply(youth);
            assertThat(Tier.TIER_3.hasUnlimitedReservations()).isTrue();
        }
    }

    @Nested
    @DisplayName("Reservation window")
    class ReservationWindow {

        @Test
        @DisplayName("Given a PENDING application older than 48 hours, When counted, Then it expires and releases the slot")
        void shouldExpireStaleReservation_AndReleaseTheSlot() {
            Application stale = pendingApplication(NOW.minusMinutes(1));
            when(applicationRepository.findByYouthAndStatusIn(youth, List.of(ApplicationStatus.PENDING)))
                .thenReturn(List.of(stale));
            when(taskRepository.countByAssignedToAndStatus(youth, "IN_PROGRESS")).thenReturn(0L);

            long active = tierService.countActiveReservations(youth);

            assertThat(active).isZero();
            assertThat(stale.getStatus()).isEqualTo(ApplicationStatus.EXPIRED);
            verify(applicationRepository).save(stale);
        }

        @Test
        @DisplayName("Given a fresh PENDING application, When counted, Then it still holds a slot")
        void shouldCountFreshReservation() {
            Application fresh = pendingApplication(NOW.plusHours(1));
            when(applicationRepository.findByYouthAndStatusIn(youth, List.of(ApplicationStatus.PENDING)))
                .thenReturn(List.of(fresh));
            when(taskRepository.countByAssignedToAndStatus(youth, "IN_PROGRESS")).thenReturn(0L);

            assertThat(tierService.countActiveReservations(youth)).isEqualTo(1);
            verify(applicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("The tier snapshot exposes the 48-hour window and current usage")
        void shouldExposeReservationWindowInTierInfo() {
            Application fresh = pendingApplication(NOW.plusHours(1));
            when(experienceRepository.countByYouthAndIsVerifiedTrue(youth)).thenReturn(0L);
            when(applicationRepository.findByYouthAndStatusIn(youth, List.of(ApplicationStatus.PENDING)))
                .thenReturn(List.of(fresh));
            when(taskRepository.countByAssignedToAndStatus(youth, "IN_PROGRESS")).thenReturn(0L);

            TierInfoResponse info = tierService.getTierInfo(youth);

            assertThat(info.tier()).isEqualTo("TIER_0");
            assertThat(info.verifiedExperienceCount()).isZero();
            assertThat(info.activeReservations()).isEqualTo(1);
            assertThat(info.maxConcurrentReservations()).isEqualTo(1);
            assertThat(info.reservationWindowHours()).isEqualTo(48);
            assertThat(info.canApply()).isFalse();
            assertThat(info.checkedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("A stale reservation expires in the global sweep")
        void shouldExpireAllStaleReservations() {
            Application stale = pendingApplication(NOW.minusHours(49));
            when(applicationRepository.findByStatusAndExpiresAtBefore(
                ApplicationStatus.PENDING, NOW)).thenReturn(List.of(stale));

            int expired = tierService.expireAllStaleReservations();

            assertThat(expired).isEqualTo(1);
            assertThat(stale.getStatus()).isEqualTo(ApplicationStatus.EXPIRED);
            verify(applicationRepository).save(stale);
        }
    }

    @Nested
    @DisplayName("Assign enforcement")
    class AssignEnforcement {

        @Test
        @DisplayName("Given active reservations are at the ceiling, When assigning, Then it is still allowed")
        void shouldAllowAssignment_AtTheCeiling() {
            when(experienceRepository.countByYouthAndIsVerifiedTrue(youth)).thenReturn(0L);
            when(applicationRepository.findByYouthAndStatusIn(youth, List.of(ApplicationStatus.PENDING)))
                .thenReturn(List.of(pendingApplication(NOW.plusHours(10))));
            when(taskRepository.countByAssignedToAndStatus(youth, "IN_PROGRESS")).thenReturn(0L);

            tierService.assertCanBeAssigned(youth);
        }

        @Test
        @DisplayName("Given active reservations exceed the ceiling, When assigning, Then it is blocked")
        void shouldBlockAssignment_WhenOverTheCeiling() {
            when(experienceRepository.countByYouthAndIsVerifiedTrue(youth)).thenReturn(0L);
            when(applicationRepository.findByYouthAndStatusIn(youth, List.of(ApplicationStatus.PENDING)))
                .thenReturn(List.of(
                    pendingApplication(NOW.plusHours(10)),
                    pendingApplication(NOW.plusHours(10))));
            when(taskRepository.countByAssignedToAndStatus(youth, "IN_PROGRESS")).thenReturn(0L);

            assertThatThrownBy(() -> tierService.assertCanBeAssigned(youth))
                .isInstanceOf(TierEligibilityException.class);
        }
    }

    private Application pendingApplication(LocalDateTime expiresAt) {
        Application application = new Application(task, youth, null, expiresAt);
        application.setStatus(ApplicationStatus.PENDING);
        return application;
    }
}
