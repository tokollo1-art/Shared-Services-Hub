"""Match scoring v1. Pure functions, no I/O, no randomness.

    from ssh_ds import score_match
    result = score_match(youth, task)
    # {"score": 78.4, "reasons": [...], "components": {...}, "cold_start": False}

Tier eligibility is NOT checked here. Filter by tier first (Slot 1's service),
then score the eligible youth.

Youth dict (all keys optional except where noted; missing ones are neutral):
    skills            list of skill slugs, labels, or the free-text DB string
    location          area name, e.g. "Soweto"
    availability      FULL_TIME | PART_TIME | WEEKENDS
    open_to_remote    bool (default True)
    max_travel_km     number (default 20)
    interests         list of task categories (used for cold start)
    verified_count    int, VERIFIED experiences only
    avg_rating        float or None, over verified experiences
    category_history  {category: verified count}
`build_youth_profile` fills the last three from raw experience rows.

Task dict:
    required_skills   list of slugs or labels (from task_skills)
    category          e.g. "Digital Operations"
    location          area name
    remote            bool, the tasks.remote column; True is treated as ONLINE
    work_mode         optional ONSITE | HYBRID | ONLINE; wins over `remote`
                      when present (HYBRID cannot be expressed by `remote`)
    commitment        optional FULL_TIME | PART_TIME | WEEKENDS | FLEXIBLE
                      (default FLEXIBLE). Not a database column.
"""
from .catalog import SKILL_TASK_CATEGORIES
from .locations import distance_km
from .taxonomy import display_skill, parse_skills, skill_coverage

# Weights sum to 1.0 in each branch.
WEIGHTS = {"skills": 0.35, "location": 0.20, "availability": 0.10,
           "rating": 0.15, "category_history": 0.20}
COLD_WEIGHTS = {"skills": 0.50, "location": 0.25, "availability": 0.10,
                "interest": 0.15}

DEFAULT_MAX_TRAVEL_KM = 20.0
HYBRID_TRAVEL_FACTOR = 1.5      # hybrid work needs fewer trips
RATING_PRIOR = 3.5              # shrink small samples toward this
RATING_PRIOR_WEIGHT = 2
HISTORY_SATURATION = 3          # 3 verified tasks in a category = full credit

_COVERS = {
    "FULL_TIME": {"FULL_TIME", "PART_TIME", "WEEKENDS", "FLEXIBLE"},
    "PART_TIME": {"PART_TIME", "WEEKENDS", "FLEXIBLE"},
    "WEEKENDS": {"WEEKENDS", "FLEXIBLE"},
}


def _pretty(tag):
    return display_skill(tag).lower()


def _work_mode(task):
    mode = task.get("work_mode")
    if mode:
        return mode.upper()
    return "ONLINE" if task.get("remote") else "ONSITE"


def _location_fit(youth, task):
    mode = _work_mode(task)
    remote_ok = youth.get("open_to_remote", True)
    if mode == "ONLINE":
        if remote_ok:
            return 1.0, "Online task and you're open to remote work"
        return 0.3, "Online task, but you prefer in-person work"
    d = distance_km(youth.get("location"), task.get("location"))
    if d is None:
        return 0.5, "Location not known, so distance wasn't scored"
    max_km = youth.get("max_travel_km") or DEFAULT_MAX_TRAVEL_KM
    if mode == "HYBRID":
        max_km *= HYBRID_TRAVEL_FACTOR
    # Smooth decay: 1.0 at 0 km, 0.5 at the youth's travel limit, ~0.2 at twice
    # the limit. No cliff, so nearer always scores higher.
    fit = 1.0 / (1.0 + (d / max_km) ** 2)
    if mode == "HYBRID" and remote_ok:
        fit = min(1.0, fit + 0.15)
    if d <= 5:
        return fit, f"Very close to you ({d:.0f} km)"
    if d <= 0.6 * max_km:
        return fit, f"Close to you ({d:.0f} km)"
    if d <= max_km:
        return fit, f"Within your travel range ({d:.0f} km)"
    return fit, f"Outside your travel range ({d:.0f} km)"


def _availability_fit(youth, task):
    need = (task.get("commitment") or "FLEXIBLE").upper()
    have = (youth.get("availability") or "").upper()
    if not have:
        return 0.7, "Availability not set, so schedule wasn't scored"
    if need in _COVERS.get(have, set()):
        if need == "FLEXIBLE":
            return 1.0, "Flexible hours"
        return 1.0, "Fits your availability"
    pretty = need.replace("_", " ").lower()
    return 0.3, f"Needs {pretty} work, you listed {have.replace('_', ' ').lower()}"


def _rating_fit(youth):
    n, avg = youth.get("verified_count", 0), youth.get("avg_rating")
    if not n or avg is None:
        return 0.0, None
    shrunk = (avg * n + RATING_PRIOR * RATING_PRIOR_WEIGHT) / (n + RATING_PRIOR_WEIGHT)
    fit = max(0.0, min(1.0, (shrunk - 1) / 4))
    reason = None
    if avg >= 4.0:
        reason = f"Strong record: {avg:.1f}/5 across {n} verified task{'s' if n != 1 else ''}"
    return fit, reason


def _history_fit(youth, task):
    cat = task.get("category")
    n = (youth.get("category_history") or {}).get(cat, 0)
    if not n:
        return 0.0, None
    return (min(1.0, n / HISTORY_SATURATION),
            f"Already verified in {n} {cat} task{'s' if n != 1 else ''}")


def _interest_fit(youth, task):
    cat = task.get("category")
    if cat and cat in (youth.get("interests") or []):
        return 1.0, f"You're interested in {cat}"
    own_cats = set()
    for s in parse_skills(youth.get("skills")):
        own_cats |= SKILL_TASK_CATEGORIES.get(s, set())
    if cat in own_cats:
        return 0.5, f"Fits your {cat} skills"
    return 0.0, None


def score_match(youth, task):
    """Score one youth against one task.

    Returns {"score": 0-100, "reasons": [str], "components": {name: 0-1},
    "cold_start": bool}. Reasons are ordered most useful first, so the
    matchmaker agent can use the top two or three for "why this match".
    """
    cold = not youth.get("verified_count")
    required = parse_skills(task.get("required_skills"))
    youth_skills = parse_skills(youth.get("skills"))

    if required:
        cov, details = skill_coverage(youth_skills, required)
    else:
        cov, details = 0.5, []  # task lists no skills: neutral
    exact = [d[0] for d in details if d[2] == 1.0]
    near = [d for d in details if 0 < d[2] < 1.0]
    skill_reasons = []
    if exact:
        skill_reasons.append("Skills match: " + ", ".join(_pretty(s) for s in exact))
    if near:
        skill_reasons.append("Related skills: " + ", ".join(
            f"{_pretty(d[1])} is close to {_pretty(d[0])}" for d in near))
    if required and not exact and not near:
        skill_reasons.append("No direct skill overlap")

    loc, loc_reason = _location_fit(youth, task)
    avail, av_reason = _availability_fit(youth, task)
    comps = {"skills": cov, "location": loc, "availability": avail}

    track_reasons = []
    if cold:
        comps["interest"], i_reason = _interest_fit(youth, task)
        weights = COLD_WEIGHTS
        if i_reason:
            track_reasons.append(i_reason)
        track_reasons.append("New to the ledger, so scored on skills, location and interests")
    else:
        comps["rating"], r_reason = _rating_fit(youth)
        comps["category_history"], h_reason = _history_fit(youth, task)
        weights = WEIGHTS
        track_reasons += [r for r in (h_reason, r_reason) if r]

    score = 100 * sum(weights[k] * comps[k] for k in weights)
    return {
        "score": round(score, 1),
        "reasons": skill_reasons + track_reasons + [loc_reason, av_reason],
        "components": {k: round(v, 3) for k, v in comps.items()},
        "cold_start": cold,
    }


def rank_candidates(youths, task, top_n=5, min_score=0.0):
    """Score already tier-eligible youth and return the best top_n.

    Each youth needs an "id". Ties break by id so results are stable.
    """
    scored = []
    for y in youths:
        r = score_match(y, task)
        if r["score"] >= min_score:
            scored.append({"youth_id": y["id"], **r})
    scored.sort(key=lambda r: (-r["score"], r["youth_id"]))
    return scored[:top_n]


def build_youth_profile(youth, experiences, tasks_by_id):
    """Add verified_count, avg_rating, category_history, last_verified_at.

    Only VERIFIED experiences count. `experiences` are rows with youth_id,
    task_id, status, rating, verified_at; `tasks_by_id` maps id -> task.
    """
    mine = [e for e in experiences
            if e["youth_id"] == youth["id"] and e.get("status") == "VERIFIED"]
    hist = {}
    for e in mine:
        cat = tasks_by_id[e["task_id"]]["category"]
        hist[cat] = hist.get(cat, 0) + 1
    ratings = [e["rating"] for e in mine if e.get("rating") is not None]
    dates = [e["verified_at"] for e in mine if e.get("verified_at")]
    return {**youth,
            "verified_count": len(mine),
            "avg_rating": sum(ratings) / len(ratings) if ratings else None,
            "category_history": hist,
            "last_verified_at": max(dates) if dates else None}
