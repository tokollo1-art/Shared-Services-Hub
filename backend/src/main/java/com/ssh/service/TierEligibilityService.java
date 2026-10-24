package com.ssh.service;

import com.ssh.dto.response.TierInfoResponse;
import com.ssh.entity.Application;
import com.ssh.entity.ApplicationStatus;
import com.ssh.entity.Tier;
import com.ssh.entity.Youth;
import com.ssh.exception.TierEligibilityException;
import com.ssh.repository.ApplicationRepository;
import com.ssh.repository.ExperienceRepository;
import com.ssh.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tier eligibility and reservation-window rules.
 *
 * Verified experiences are the only tier input. Active reservations are
 * non-expired PENDING applications plus tasks the youth is currently working
 * on (IN_PROGRESS). The same checks run on apply AND on assign so a stale
 * application cannot be used to bypass the ceiling.
 */
@Service
public class TierEligibilityService {

    public static final Duration RESERVATION_WINDOW = Duration.ofHours(48);
    public static final long RESERVATION_WINDOW_HOURS = RESERVATION_WINDOW.toHours();

    private final ExperienceRepository experienceRepository;
    private final ApplicationRepository applicationRepository;
    private final TaskRepository taskRepository;
    private final Clock clock;

    public TierEligibilityService(ExperienceRepository experienceRepository,
                                  ApplicationRepository applicationRepository,
                                  TaskRepository taskRepository,
                                  Clock clock) {
        this.experienceRepository = experienceRepository;
        this.applicationRepository = applicationRepository;
        this.taskRepository = taskRepository;
        this.clock = clock;
    }

    /**
     * Current tier for a youth, based on VERIFIED experiences only.
     */
    public Tier resolveTier(Youth youth) {
        long verified = experienceRepository.countByYouthAndIsVerifiedTrue(youth);
        return Tier.fromVerifiedCount(verified);
    }

    public TierInfoResponse getTierInfo(Youth youth) {
        expireStaleReservations(youth);
        Tier tier = resolveTier(youth);
        long verified = experienceRepository.countByYouthAndIsVerifiedTrue(youth);
        long active = countActiveReservations(youth);
        return TierInfoResponse.of(
            tier,
            verified,
            active,
            RESERVATION_WINDOW_HOURS,
            LocalDateTime.now(clock)
        );
    }

    /**
     * Enforces the ceiling when a youth creates (or re-activates) an
     * application. The new reservation is not persisted yet, so the youth
     * must be strictly below the ceiling.
     */
    @Transactional
    public void assertCanApply(Youth youth) {
        expireStaleReservations(youth);
        Tier tier = resolveTier(youth);
        long active = countActiveReservations(youth);
        if (active >= tier.getMaxConcurrentReservations()) {
            throw new TierEligibilityException(
                "Tier " + tier.name() + " allows at most "
                    + formatLimit(tier) + " active reservation(s); "
                    + "the youth currently has " + active
                    + ". The reservation window is " + RESERVATION_WINDOW_HOURS + " hours.");
        }
    }

    /**
     * Enforces the ceiling again at assignment time. The pending application
     * is already counted in the active reservations, so being at the ceiling
     * is allowed; exceeding it is not.
     */
    @Transactional
    public void assertCanBeAssigned(Youth youth) {
        expireStaleReservations(youth);
        Tier tier = resolveTier(youth);
        long active = countActiveReservations(youth);
        if (active > tier.getMaxConcurrentReservations()) {
            throw new TierEligibilityException(
                "Tier " + tier.name() + " allows at most "
                    + formatLimit(tier) + " active reservation(s); "
                    + "the youth currently has " + active + ".");
        }
    }

    /**
     * Non-expired PENDING applications plus IN_PROGRESS tasks. ACCEPTED
     * applications are represented by their IN_PROGRESS task, so counting
     * both would double reserve the same slot.
     */
    @Transactional
    public long countActiveReservations(Youth youth) {
        expireStaleReservations(youth);
        long pending = applicationRepository
            .findByYouthAndStatusIn(youth, List.of(ApplicationStatus.PENDING))
            .stream()
            .filter(Application::isPending)
            .count();
        long inProgress = taskRepository
            .countByAssignedToAndStatus(youth, "IN_PROGRESS");
        return pending + inProgress;
    }

    /**
     * Lazily expires this youth's stale PENDING applications. Expiry releases
     * the reservation slot; the OPEN task simply stops being held for them.
     */
    @Transactional
    public void expireStaleReservations(Youth youth) {
        LocalDateTime now = LocalDateTime.now(clock);
        applicationRepository
            .findByYouthAndStatusIn(youth, List.of(ApplicationStatus.PENDING))
            .stream()
            .filter(application -> application.isExpired(now))
            .forEach(application -> {
                application.expire();
                applicationRepository.save(application);
            });
    }

    /**
     * Expires every stale PENDING application (used by dashboards).
     */
    @Transactional
    public int expireAllStaleReservations() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Application> stale = applicationRepository
            .findByStatusAndExpiresAtBefore(ApplicationStatus.PENDING, now);
        for (Application application : stale) {
            if (application.isExpired(now)) {
                application.expire();
                applicationRepository.save(application);
            }
        }
        return stale.size();
    }

    public LocalDateTime reservationEndsAt() {
        return LocalDateTime.now(clock).plus(RESERVATION_WINDOW);
    }

    private static String formatLimit(Tier tier) {
        return tier.hasUnlimitedReservations()
            ? "unlimited"
            : String.valueOf(tier.getMaxConcurrentReservations());
    }
}
