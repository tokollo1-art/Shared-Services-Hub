"""Tier rules, mirrored from the backend.

Source of truth: backend/src/main/java/com/ssh/entity/Tier.java and
service/TierEligibilityService.java. A youth's tier comes ONLY from their count
of VERIFIED experiences:

    Tier 0: 0 verified   max 1 active reservation
    Tier 1: 1-2 verified max 2
    Tier 2: 3-4 verified max 3
    Tier 3: 5+ verified  unlimited

An "active reservation" is a PENDING application inside its window, or a task
IN_PROGRESS for the youth. The reservation window is 48 hours.

Task tiers (tasks.tier) use the same 0-3 scale. Two things are NOT defined by
the backend yet, so this module states the rule the seed data follows:

* A task of tier N >= 1 needs a youth of tier >= N. (AgentService.isTierEligible
  compares them this way.)
* A tier 0 task is reserved for tier 0 youth until `tasks.reserved_until`, then
  it opens to tier 1 youth too. (Task.reservedUntil documents this; no code
  enforces it yet.)

Known inconsistency in the backend, not copied here: AgentService.tierOf uses
its own 3 / 10 thresholds and only three tiers. Tier.java is the agreed rule.
"""

# (tier, min verified, max verified or None, max concurrent reservations or None)
TIERS = (
    (0, 0, 0, 1),
    (1, 1, 2, 2),
    (2, 3, 4, 3),
    (3, 5, None, None),
)
MAX_TIER = 3
RESERVATION_WINDOW_HOURS = 48


def youth_tier(verified_count):
    """Tier of a youth, counting VERIFIED experiences only (Tier.fromVerifiedCount)."""
    if verified_count < 0:
        raise ValueError("Verified experience count cannot be negative")
    for tier, lo, hi, _ in TIERS:
        if verified_count >= lo and (hi is None or verified_count <= hi):
            return tier
    return MAX_TIER


def max_reservations(tier):
    """Active reservation ceiling for a tier; None means unlimited."""
    return TIERS[tier][3]


def tier_floor(tier):
    """Smallest verified-experience count that puts a youth in `tier`."""
    return TIERS[tier][1]


def eligible(verified_count, task_tier, reservation_active=True):
    """Can a youth with this many verified experiences take a task of this tier?

    `reservation_active` is True while the tier 0 reservation is still running
    (now < tasks.reserved_until). Once it has ended, tier 1 youth may take tier
    0 tasks too. It only matters for tier 0 tasks. The default is the strict
    reading: still reserved.
    """
    tier = youth_tier(verified_count)
    if task_tier == 0:
        return tier == 0 or (tier == 1 and not reservation_active)
    return tier >= task_tier
