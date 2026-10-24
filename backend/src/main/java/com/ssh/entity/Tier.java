package com.ssh.entity;

/**
 * Youth eligibility tiers.
 *
 * A youth's tier is derived ONLY from the number of VERIFIED experiences in
 * the Experience Ledger. Self-reported or in-progress work never counts.
 *
 * Tier limits (agreed project rules):
 *   Tier 0: 0 verified experiences, at most 1 active reservation
 *   Tier 1: 1-2 verified experiences, at most 2 active reservations
 *   Tier 2: 3-4 verified experiences, at most 3 active reservations
 *   Tier 3: 5+ verified experiences, unlimited active reservations
 *
 * A reservation is a PENDING application that has not passed its reservation
 * window, or a task that is currently IN_PROGRESS for the youth.
 */
public enum Tier {

    TIER_0(0, 0, 1),
    TIER_1(1, 2, 2),
    TIER_2(3, 4, 3),
    TIER_3(5, Integer.MAX_VALUE, Integer.MAX_VALUE);

    private final int minVerifiedExperiences;
    private final int maxVerifiedExperiences;
    private final int maxConcurrentReservations;

    Tier(int minVerifiedExperiences,
         int maxVerifiedExperiences,
         int maxConcurrentReservations) {
        this.minVerifiedExperiences = minVerifiedExperiences;
        this.maxVerifiedExperiences = maxVerifiedExperiences;
        this.maxConcurrentReservations = maxConcurrentReservations;
    }

    public int getMinVerifiedExperiences() {
        return minVerifiedExperiences;
    }

    public int getMaxVerifiedExperiences() {
        return maxVerifiedExperiences;
    }

    public int getMaxConcurrentReservations() {
        return maxConcurrentReservations;
    }

    /**
     * True when the tier has no reservation ceiling.
     */
    public boolean hasUnlimitedReservations() {
        return maxConcurrentReservations == Integer.MAX_VALUE;
    }

    /**
     * Resolve the tier for a verified-experience count.
     */
    public static Tier fromVerifiedCount(long verifiedExperiences) {
        if (verifiedExperiences < 0) {
            throw new IllegalArgumentException("Verified experience count cannot be negative");
        }
        for (Tier tier : values()) {
            if (verifiedExperiences >= tier.minVerifiedExperiences
                    && verifiedExperiences <= tier.maxVerifiedExperiences) {
                return tier;
            }
        }
        return TIER_3;
    }
}
