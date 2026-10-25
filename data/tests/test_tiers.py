import re
from pathlib import Path

import pytest

from ssh_ds.tiers import (MAX_TIER, RESERVATION_WINDOW_HOURS, TIERS, eligible, max_reservations,
                          tier_floor, youth_tier)

BACKEND = Path(__file__).resolve().parents[2] / "backend" / "src" / "main" / "java" / "com" / "ssh"


@pytest.mark.parametrize("verified,tier", [(0, 0), (1, 1), (2, 1), (3, 2), (4, 2), (5, 3), (6, 3), (40, 3)])
def test_youth_tier_boundaries(verified, tier):
    assert youth_tier(verified) == tier


def test_negative_count_is_rejected_like_the_backend():
    with pytest.raises(ValueError):
        youth_tier(-1)


def test_reservation_caps():
    assert [max_reservations(t) for t in range(4)] == [1, 2, 3, None]
    assert [tier_floor(t) for t in range(4)] == [0, 1, 3, 5]
    assert MAX_TIER == 3 and RESERVATION_WINDOW_HOURS == 48


def test_task_eligibility():
    # tier N task needs a tier >= N youth
    assert eligible(0, 0) and not eligible(0, 1)
    assert eligible(1, 1) and eligible(2, 1) and not eligible(2, 3) and eligible(5, 3)
    # tier 0 tasks are reserved for newcomers until the window ends
    assert not eligible(1, 0) and not eligible(5, 0)
    assert eligible(1, 0, reservation_active=False)
    assert not eligible(3, 0, reservation_active=False)   # only tier 1 is let in


def test_tiers_mirror_tier_java():
    """Fails if Tier.java or TierEligibilityService changes and tiers.py does not."""
    source = (BACKEND / "entity" / "Tier.java")
    if not source.exists():
        pytest.skip("backend sources not in this checkout")
    text = source.read_text(encoding="utf-8")
    java = re.findall(r"TIER_(\d)\((\d+),\s*(Integer\.MAX_VALUE|\d+),\s*(Integer\.MAX_VALUE|\d+)\)", text)
    parsed = tuple((int(t), int(lo), None if hi.startswith("Integer") else int(hi),
                    None if cap.startswith("Integer") else int(cap)) for t, lo, hi, cap in java)
    assert parsed == TIERS
    svc = (BACKEND / "service" / "TierEligibilityService.java").read_text(encoding="utf-8")
    assert f"Duration.ofHours({RESERVATION_WINDOW_HOURS})" in svc
