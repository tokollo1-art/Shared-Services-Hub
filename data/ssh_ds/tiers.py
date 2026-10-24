"""Tier rules, as ASSUMED by Slot 4.

Only this is stated in the project plan: Tier 0 tasks are reserved for
first-timers (0-2 verified experiences), and an unclaimed Tier 0 task opens
to Tier 1 after the reservation window. The Tier 1 / Tier 2 cut-offs below
are my assumption. The Slot 1 tier eligibility service is the source of
truth; change the constants here if it uses different numbers.
"""

TIER0_MAX_VERIFIED = 2   # 0-2 verified experiences  -> Tier 0 (from the plan)
TIER1_MAX_VERIFIED = 5   # 3-5 verified experiences  -> Tier 1 (assumed)
                         # 6+ verified experiences   -> Tier 2 (assumed)


def youth_tier(verified_count):
    """Tier of a youth, counting VERIFIED experiences only."""
    if verified_count <= TIER0_MAX_VERIFIED:
        return 0
    if verified_count <= TIER1_MAX_VERIFIED:
        return 1
    return 2


def eligible(verified_count, task_tier, tier0_window_open=False):
    """Can a youth with this many verified experiences take a task of this tier?

    Tier 0 tasks are reserved for Tier 0 youth until the reservation window
    ends, then Tier 1 youth may take them too. Higher tasks need at least
    that tier.
    """
    tier = youth_tier(verified_count)
    if task_tier == 0:
        return tier == 0 or (tier_window_ok(tier, tier0_window_open))
    return tier >= task_tier


def tier_window_ok(tier, window_open):
    return window_open and tier == 1
