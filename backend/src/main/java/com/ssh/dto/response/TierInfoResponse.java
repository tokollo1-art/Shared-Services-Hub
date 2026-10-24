package com.ssh.dto.response;

import com.ssh.entity.Tier;

import java.time.LocalDateTime;

/**
 * Snapshot of a youth's eligibility. All limits are derived from VERIFIED
 * experiences only.
 */
public record TierInfoResponse(
    String tier,
    long verifiedExperienceCount,
    int minVerifiedExperiences,
    int maxVerifiedExperiences,
    long activeReservations,
    int maxConcurrentReservations,
    long reservationWindowHours,
    boolean canApply,
    LocalDateTime checkedAt
) {

    public static TierInfoResponse of(Tier tier, long verifiedCount, long activeReservations,
                                      long reservationWindowHours, LocalDateTime checkedAt) {
        boolean canApply = activeReservations < tier.getMaxConcurrentReservations();
        return new TierInfoResponse(
            tier.name(),
            verifiedCount,
            tier.getMinVerifiedExperiences(),
            tier.getMaxVerifiedExperiences(),
            activeReservations,
            tier.getMaxConcurrentReservations(),
            reservationWindowHours,
            canApply,
            checkedAt
        );
    }
}
