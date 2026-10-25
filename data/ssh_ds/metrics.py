"""Placement-readiness baseline and impact metrics.

Everything here is a transparent heuristic or a plain count, not a trained
model. With seed data the numbers show how the metrics work; they are not
real outcomes.
"""
import datetime as dt
import statistics

from .tiers import MAX_TIER, tier_floor, youth_tier

METRIC_DEFINITIONS = {
    "time_to_first_verified_experience": (
        "Days from a youth's account creation to the verified_at of their first "
        "VERIFIED experience. Reported as the median (days) plus the share verified "
        "within 30 days. Youth with no verified experience yet are counted separately "
        "as 'waiting', not averaged in, so the median is not flattered."),
    "tier_funnel": (
        "How many youth reach each stage, counting VERIFIED experiences only: "
        "registered, then each tier as defined by the backend (Tier 1 = "
        f"{tier_floor(1)}+ verified, Tier 2 = {tier_floor(2)}+, Tier 3 = {tier_floor(3)}+), "
        "then placed (alumni). Tier 1 is also the 'past the first-timer trap' stage. "
        "Each stage shows conversion from the previous stage. active_by_tier counts "
        "non-alumni youth currently in each tier."),
    "placement_rate": (
        "Placed (alumni) youth divided by all registered youth, plus the median days "
        "from first verified experience to placement. Small cohorts swing a lot, so "
        "always show the counts next to the rate."),
}

READINESS_WEIGHTS = {"experience": 35, "quality": 25, "breadth": 15, "recency": 15, "skills": 10}
RATING_PRIOR, RATING_PRIOR_WEIGHT = 3.5, 2
EXPERIENCE_TARGET = 6      # verified tasks for full experience credit
BREADTH_TARGET = 3         # distinct categories for full breadth credit
RECENCY_WINDOW_DAYS = 90   # no verified work for this long = no recency credit
SKILLS_TARGET = 5


def _to_dt(value):
    if value is None or isinstance(value, dt.datetime):
        return value
    return dt.datetime.fromisoformat(value)


def readiness_score(profile, now):
    """Heuristic 0-100 placement-readiness score for one youth.

    `profile` comes from matching.build_youth_profile (verified_count,
    avg_rating, category_history, last_verified_at) plus `skills`.
    Returns {"score", "label", "components", "next_steps"}; the components are
    points earned out of their maximum, so the score is easy to explain.
    """
    n = profile.get("verified_count", 0)
    avg = profile.get("avg_rating")
    cats = len(profile.get("category_history") or {})
    last = _to_dt(profile.get("last_verified_at"))
    n_skills = len(profile.get("skills") or [])

    pts = {"experience": min(n / EXPERIENCE_TARGET, 1) * READINESS_WEIGHTS["experience"]}
    if n and avg is not None:
        shrunk = (avg * n + RATING_PRIOR * RATING_PRIOR_WEIGHT) / (n + RATING_PRIOR_WEIGHT)
        pts["quality"] = max(0, min(1, (shrunk - 1) / 4)) * READINESS_WEIGHTS["quality"]
    else:
        pts["quality"] = 0.0
    pts["breadth"] = min(cats / BREADTH_TARGET, 1) * READINESS_WEIGHTS["breadth"]
    if last is not None:
        days = max(0, (now - last).days)
        pts["recency"] = max(0, 1 - days / RECENCY_WINDOW_DAYS) * READINESS_WEIGHTS["recency"]
    else:
        pts["recency"] = 0.0
    pts["skills"] = min(n_skills / SKILLS_TARGET, 1) * READINESS_WEIGHTS["skills"]

    score = round(sum(pts.values()), 1)
    label = "Getting started" if score < 35 else "Building momentum" if score < 65 else "Placement-ready"

    steps = []
    if n < EXPERIENCE_TARGET:
        steps.append(f"Complete {EXPERIENCE_TARGET - n} more verified task"
                     f"{'s' if EXPERIENCE_TARGET - n != 1 else ''}")
    if cats < BREADTH_TARGET:
        steps.append("Try a task in a new category to show range")
    if last is None or (now - last).days > 30:
        steps.append("Take a task soon: recent verified work counts most")
    if n_skills < SKILLS_TARGET:
        steps.append("Add more skills to your profile")
    return {"score": score, "label": label,
            "components": {k: round(v, 1) for k, v in pts.items()},
            "next_steps": steps}


def _first_verified(experiences):
    first = {}
    for e in experiences:
        if e["status"] != "VERIFIED" or not e.get("verified_at"):
            continue
        t = _to_dt(e["verified_at"])
        if e["youth_id"] not in first or t < first[e["youth_id"]]:
            first[e["youth_id"]] = t
    return first


def time_to_first_verified(ds, now):
    first = _first_verified(ds["experiences"])
    days = []
    for y in ds["youth"]:
        if y["id"] in first:
            days.append((first[y["id"]] - _to_dt(y["created_at"])).total_seconds() / 86400)
    waiting = [y for y in ds["youth"] if y["id"] not in first]
    return {
        "verified_youth": len(days), "waiting_youth": len(waiting),
        "median_days": round(statistics.median(days), 1) if days else None,
        "share_within_30_days": round(sum(d <= 30 for d in days) / len(days), 3) if days else None,
        "median_days_waiting": round(statistics.median(
            (now - _to_dt(y["created_at"])).total_seconds() / 86400 for y in waiting), 1) if waiting else None,
    }


def tier_funnel(ds):
    counts = {y["id"]: 0 for y in ds["youth"]}
    for e in ds["experiences"]:
        if e["status"] == "VERIFIED":
            counts[e["youth_id"]] += 1
    youth = ds["youth"]
    stages = [("registered", len(youth))]
    for t in range(1, MAX_TIER + 1):
        stages.append((f"tier_{t}", sum(youth_tier(c) >= t for c in counts.values())))
    stages.append(("placed", sum(y["is_alumni"] for y in youth)))
    out, prev = [], None
    for name, n in stages:
        out.append({"stage": name, "youth": n,
                    "conversion_from_previous": None if prev in (None, 0) else round(n / prev, 3)})
        prev = n
    current = {t: sum(1 for y in youth if not y["is_alumni"] and youth_tier(counts[y["id"]]) == t)
               for t in range(MAX_TIER + 1)}
    return {"stages": out, "active_by_tier": current}


def placement_rate(ds):
    youth = ds["youth"]
    placed = [y for y in youth if y["is_alumni"]]
    first = _first_verified(ds["experiences"])
    days = [(_to_dt(y["alumni_date"]) - first[y["id"]]).days
            for y in placed if y["id"] in first and y.get("alumni_date")]
    return {"placed": len(placed), "registered": len(youth),
            "rate": round(len(placed) / len(youth), 3) if youth else None,
            "median_days_first_verified_to_placement": statistics.median(days) if days else None}


def impact_report(ds, now):
    return {"definitions": METRIC_DEFINITIONS,
            "time_to_first_verified_experience": time_to_first_verified(ds, now),
            "tier_funnel": tier_funnel(ds),
            "placement_rate": placement_rate(ds)}
