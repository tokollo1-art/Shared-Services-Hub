import datetime as dt

import pytest

from ssh_ds import score_match, rank_candidates, build_youth_profile
from ssh_ds.taxonomy import (ALL_SKILLS, display_skill, parse_skills,
                             skill_similarity)

TASK = {"required_skills": ["excel", "data_entry"], "category": "Digital Operations",
        "location": "Soweto", "work_mode": "ONSITE", "commitment": "PART_TIME"}


def youth(**kw):
    base = {"id": 1, "skills": ["excel", "data_entry"], "location": "Soweto",
            "availability": "PART_TIME", "open_to_remote": True, "max_travel_km": 20,
            "interests": ["Digital Operations"], "verified_count": 0}
    base.update(kw)
    return base


# ------------------------------------------------------------------ taxonomy
def test_skill_similarity_ordering():
    assert skill_similarity("excel", "excel") == 1.0
    assert skill_similarity("excel", "bookkeeping") > skill_similarity("excel", "typing") > 0
    assert skill_similarity("excel", "photography") == 0.0
    assert skill_similarity("excel", "data_entry") == skill_similarity("data_entry", "excel")


def test_display_names_round_trip():
    for tag in ALL_SKILLS:
        assert parse_skills(display_skill(tag)) == [tag]


def test_parse_free_text_and_unknowns():
    assert parse_skills("MS Excel, data entry; WhatsApp, Juggling") == [
        "excel", "data_entry", "whatsapp_business"]
    assert parse_skills(None) == [] and parse_skills("") == []


# ------------------------------------------------------------------ matching
def test_score_shape_and_range():
    r = score_match(youth(), TASK)
    assert set(r) == {"score", "reasons", "components", "cold_start"}
    assert 0 <= r["score"] <= 100 and r["reasons"]
    assert all(0 <= v <= 1 for v in r["components"].values())


def test_cold_start_flag_and_branch():
    assert score_match(youth(), TASK)["cold_start"] is True
    r = score_match(youth(verified_count=2, avg_rating=4.5,
                          category_history={"Digital Operations": 2}), TASK)
    assert r["cold_start"] is False and "rating" in r["components"]


def test_cold_start_can_score_high():
    # A perfect-fit newcomer must not be buried just for having no history.
    assert score_match(youth(), TASK)["score"] >= 90


def test_skills_exact_beats_related_beats_none():
    exact = score_match(youth(), TASK)["score"]
    related = score_match(youth(skills=["bookkeeping", "typing"]), TASK)["score"]
    none = score_match(youth(skills=["photography"]), TASK)["score"]
    assert exact > related > none


def test_distance_monotonic():
    near = score_match(youth(location="Soweto"), TASK)["score"]
    mid = score_match(youth(location="Johannesburg CBD"), TASK)["score"]
    far = score_match(youth(location="Soshanguve"), TASK)["score"]
    assert near > mid > far


def test_online_task_ignores_distance_but_respects_preference():
    task = {**TASK, "work_mode": "ONLINE"}
    far = score_match(youth(location="Soshanguve"), task)
    assert far["components"]["location"] == 1.0
    assert score_match(youth(location="Soshanguve", open_to_remote=False), task)["components"]["location"] < 1.0


def test_availability():
    ft = {**TASK, "commitment": "FULL_TIME"}
    assert score_match(youth(availability="FULL_TIME"), ft)["components"]["availability"] == 1.0
    assert score_match(youth(availability="WEEKENDS"), ft)["components"]["availability"] < 1.0
    assert score_match(youth(availability="WEEKENDS"), {**TASK, "commitment": "FLEXIBLE"})["components"]["availability"] == 1.0


def test_rating_and_category_history_help():
    base = dict(verified_count=3, category_history={"Digital Operations": 3})
    strong = score_match(youth(avg_rating=4.8, **base), TASK)["score"]
    weak = score_match(youth(avg_rating=2.5, **base), TASK)["score"]
    assert strong > weak
    no_hist = score_match(youth(avg_rating=4.8, verified_count=3, category_history={"Retail & Sales": 3}), TASK)["score"]
    assert strong > no_hist


def test_small_samples_are_shrunk():
    one = score_match(youth(avg_rating=5.0, verified_count=1, category_history={}), TASK)["components"]["rating"]
    many = score_match(youth(avg_rating=5.0, verified_count=8, category_history={}), TASK)["components"]["rating"]
    assert one < many


def test_missing_data_is_neutral_not_a_crash():
    r = score_match({"id": 9}, {"title": "x"})
    assert 0 <= r["score"] <= 100


def test_deterministic_and_does_not_mutate():
    y, t = youth(), dict(TASK)
    before = (dict(y), dict(t))
    assert score_match(y, t) == score_match(y, t)
    assert (y, t) == before


def test_reasons_mention_real_signals():
    r = score_match(youth(), TASK)
    text = " ".join(r["reasons"]).lower()
    assert "excel" in text and "km" in text


def test_rank_candidates_order_and_ties():
    ys = [youth(id=3), youth(id=1), youth(id=2, skills=["photography"])]
    ranked = rank_candidates(ys, TASK, top_n=3)
    assert [r["youth_id"] for r in ranked] == [1, 3, 2]   # tie broken by id
    assert len(rank_candidates(ys, TASK, top_n=1)) == 1


def test_build_youth_profile_counts_verified_only():
    exps = [
        {"youth_id": 1, "task_id": 10, "status": "VERIFIED", "rating": 5.0, "verified_at": "2026-10-01T10:00:00"},
        {"youth_id": 1, "task_id": 11, "status": "VERIFIED", "rating": 3.0, "verified_at": "2026-10-05T10:00:00"},
        {"youth_id": 1, "task_id": 12, "status": "PENDING", "rating": None, "verified_at": None},
        {"youth_id": 2, "task_id": 10, "status": "VERIFIED", "rating": 1.0, "verified_at": "2026-10-02T10:00:00"},
    ]
    tasks = {10: {"category": "A"}, 11: {"category": "A"}, 12: {"category": "B"}}
    p = build_youth_profile({"id": 1}, exps, tasks)
    assert p["verified_count"] == 2 and p["avg_rating"] == 4.0
    assert p["category_history"] == {"A": 2} and p["last_verified_at"] == "2026-10-05T10:00:00"
