import datetime as dt
import json
import re
import sqlite3
from collections import Counter

import pytest

from ssh_ds import eligible, score_match, build_youth_profile
from ssh_ds.metrics import impact_report, readiness_score
from ssh_ds.seed import NOW, generate, to_sql
from ssh_ds.taxonomy import parse_skills
from ssh_ds.tiers import youth_tier


@pytest.fixture(scope="module")
def ds():
    return generate(42)


def ts(s):
    return dt.datetime.fromisoformat(s)


# ---------------------------------------------------------------------- counts
def test_required_counts(ds):
    assert len(ds["youth"]) == 50 and len(ds["smes"]) == 15 and len(ds["funders"]) == 3
    assert len(ds["tasks"]) == 150
    assert sum(e["status"] == "VERIFIED" for e in ds["experiences"]) == 100
    assert Counter(t["tier"] for t in ds["tasks"]).keys() == {0, 1, 2}
    assert len(ds["users"]) == 1 + 3 + 15 + 50


def test_deterministic():
    assert generate(42) == generate(42)
    assert generate(42) != generate(7)


# ------------------------------------------------------------------- integrity
def test_unique_ids_and_emails(ds):
    for key in ["users", "youth", "smes", "tasks", "experiences"]:
        ids = [r["id"] for r in ds[key]]
        assert len(ids) == len(set(ids))
    emails = [u["email"] for u in ds["users"]]
    assert len(emails) == len(set(emails))
    regs = [s["registration_number"] for s in ds["smes"]]
    assert len(regs) == len(set(regs))
    assert all(re.fullmatch(r"\d{4}/\d{6}/07", r) for r in regs)


def test_foreign_keys(ds):
    users = {u["id"]: u["role"] for u in ds["users"]}
    smes = {s["id"] for s in ds["smes"]}
    youth = {y["id"] for y in ds["youth"]}
    tasks = {t["id"]: t for t in ds["tasks"]}
    assert all(users[y["user_id"]] == "YOUTH" for y in ds["youth"])
    assert all(users[s["user_id"]] == "SME" for s in ds["smes"])
    assert all(t["sme_id"] in smes for t in ds["tasks"])
    assert all(t["assigned_to"] in youth for t in ds["tasks"] if t["assigned_to"])
    for e in ds["experiences"]:
        assert e["youth_id"] in youth
        assert tasks[e["task_id"]]["assigned_to"] == e["youth_id"]


def test_task_state_consistency(ds):
    for t in ds["tasks"]:
        if t["status"] in ("OPEN", "CANCELLED"):
            assert t["assigned_to"] is None
        else:
            assert t["assigned_to"] is not None
    verified = {e["task_id"] for e in ds["experiences"] if e["status"] == "VERIFIED"}
    assert {t["id"] for t in ds["tasks"] if t["status"] == "VERIFIED"} == verified
    for e in ds["experiences"]:
        if e["status"] == "VERIFIED":
            assert e["is_verified"] and e["verified_at"] and 1 <= e["rating"] <= 5 and e["feedback"]
        else:
            assert not e["is_verified"] and e["rating"] is None


def test_no_dates_in_the_future_and_order_makes_sense(ds):
    users = {u["id"]: ts(u["created_at"]) for u in ds["users"]}
    for e in ds["experiences"]:
        assert ts(e["created_at"]) <= NOW
        if e["verified_at"]:
            assert ts(e["start_date"]) <= ts(e["end_date"]) <= ts(e["verified_at"]) <= NOW
    tasks = {t["id"]: t for t in ds["tasks"]}
    for t in ds["tasks"]:
        assert ts(t["created_at"]) <= NOW
    for y in ds["youth"]:
        firsts = [ts(e["start_date"]) for e in ds["experiences"] if e["youth_id"] == y["id"]]
        for f in firsts:
            assert f >= ts(y["created_at"])


def test_each_youth_does_one_task_at_a_time(ds):
    for y in ds["youth"]:
        spans = sorted((ts(e["start_date"]), ts(e["verified_at"] or e["end_date"]))
                       for e in ds["experiences"] if e["youth_id"] == y["id"])
        for (s1, e1), (s2, e2) in zip(spans, spans[1:]):
            assert e1 <= s2


# ----------------------------------------------------------------- tier rules
def test_verified_history_respects_tier_rules(ds):
    tasks = {t["id"]: t for t in ds["tasks"]}
    for y in ds["youth"]:
        mine = sorted((e for e in ds["experiences"] if e["youth_id"] == y["id"] and e["status"] == "VERIFIED"),
                      key=lambda e: e["start_date"])
        for count_before, e in enumerate(mine):
            assert eligible(count_before, tasks[e["task_id"]]["tier"]), (y["id"], count_before)


def test_active_work_respects_tier_rules(ds):
    verified = Counter(e["youth_id"] for e in ds["experiences"] if e["status"] == "VERIFIED")
    for t in ds["tasks"]:
        if t["status"] in ("IN_PROGRESS", "COMPLETED"):
            assert eligible(verified[t["assigned_to"]], t["tier"])


def test_open_tasks_have_tier_0_supply(ds):
    open_t = Counter(t["tier"] for t in ds["tasks"] if t["status"] == "OPEN")
    assert open_t[0] >= 15 and open_t[1] > 0 and open_t[2] > 0


def test_alumni(ds):
    alumni = [y for y in ds["youth"] if y["is_alumni"]]
    assert len(alumni) == 3
    verified = Counter(e["youth_id"] for e in ds["experiences"] if e["status"] == "VERIFIED")
    for y in alumni:
        assert y["alumni_date"] and ts(y["alumni_date"]) <= NOW and verified[y["id"]] >= 5
        assert not any(t["assigned_to"] == y["id"] and t["status"] in ("IN_PROGRESS", "COMPLETED") for t in ds["tasks"])


# ------------------------------------------------------------------- realism
def test_ratings_are_plausible(ds):
    r = [e["rating"] for e in ds["experiences"] if e["status"] == "VERIFIED"]
    assert 3.6 <= sum(r) / len(r) <= 4.6 and min(r) < 4 and max(r) == 5


def test_tasks_need_skills_the_taxonomy_knows(ds):
    for t in ds["tasks"]:
        assert t["required_skills"] and parse_skills(t["required_skills"]) == t["required_skills"]
    for y in ds["youth"]:
        assert 4 <= len(y["skills"]) <= 5 and parse_skills(y["skills"]) == y["skills"]


def test_assigned_youth_usually_have_relevant_skills(ds):
    tasks = {t["id"]: t for t in ds["tasks"]}
    hits = [bool(set(tasks[e["task_id"]]["required_skills"]) & set(next(y for y in ds["youth"] if y["id"] == e["youth_id"])["skills"]))
            for e in ds["experiences"] if e["status"] == "VERIFIED"]
    assert sum(hits) / len(hits) > 0.7


def test_synthetic_hygiene(ds):
    assert all(u["email"].endswith("@example.com") for u in ds["users"])
    assert ds["meta"]["synthetic"] is True


def test_match_function_prefers_real_history(ds):
    """Sanity: scoring the real assignments, the assigned youth should beat the
    average eligible youth on a clear majority of tasks."""
    tasks = {t["id"]: t for t in ds["tasks"]}
    youth = {y["id"]: y for y in ds["youth"]}
    exps = ds["experiences"]
    wins = total = 0
    for e in exps:
        if e["status"] != "VERIFIED":
            continue
        t = tasks[e["task_id"]]
        before = [x for x in exps if x["status"] == "VERIFIED" and x["verified_at"] < e["start_date"]]
        sc = lambda y: score_match(build_youth_profile(y, before, tasks), t)["score"]
        mine = sc(youth[e["youth_id"]])
        pool = [sc(y) for y in youth.values() if y["id"] != e["youth_id"]]
        total += 1
        wins += mine > sum(pool) / len(pool)
    assert wins / total > 0.75


# ---------------------------------------------------------------------- metrics
def test_readiness_orders_sensibly(ds):
    tasks = {t["id"]: t for t in ds["tasks"]}
    prof = {y["id"]: build_youth_profile(y, ds["experiences"], tasks) for y in ds["youth"]}
    score = {i: readiness_score(p, NOW)["score"] for i, p in prof.items()}
    alumni = [score[y["id"]] for y in ds["youth"] if y["is_alumni"]]
    rest = [score[y["id"]] for y in ds["youth"] if not y["is_alumni"]]
    zero = [score[i] for i, p in prof.items() if p["verified_count"] == 0]
    assert min(alumni) > sum(rest) / len(rest) and max(zero) < min(alumni)
    assert all(0 <= s <= 100 for s in score.values())


def test_readiness_next_steps_and_labels():
    r = readiness_score({"verified_count": 0, "skills": ["excel"]}, NOW)
    assert r["label"] == "Getting started" and r["next_steps"] and r["score"] < 35
    full = readiness_score({"verified_count": 8, "avg_rating": 4.8,
                            "category_history": {"a": 3, "b": 3, "c": 2},
                            "last_verified_at": NOW - dt.timedelta(days=3),
                            "skills": list("abcde")}, NOW)
    assert full["label"] == "Placement-ready" and full["score"] > 85


def test_impact_report(ds):
    rep = impact_report(ds, NOW)
    t = rep["time_to_first_verified_experience"]
    assert t["verified_youth"] + t["waiting_youth"] == 50 and t["waiting_youth"] == 13
    assert 3 <= t["median_days"] <= 40
    f = {s["stage"]: s["youth"] for s in rep["tier_funnel"]["stages"]}
    assert f["registered"] == 50 and f["1_plus_verified"] == 37
    assert f["registered"] >= f["1_plus_verified"] >= f["tier_1"] >= f["tier_2"]
    assert rep["tier_funnel"]["active_by_tier"][0] + rep["tier_funnel"]["active_by_tier"][1] + rep["tier_funnel"]["active_by_tier"][2] == 47
    assert rep["placement_rate"]["placed"] == 3 and rep["placement_rate"]["rate"] == 0.06
    assert set(rep["definitions"]) >= {"time_to_first_verified_experience", "tier_funnel", "placement_rate"}


# -------------------------------------------------------------------------- SQL
def test_sql_loads_into_a_database(ds):
    sql = to_sql(ds)
    body = "\n".join(l for l in sql.splitlines() if not l.startswith(("SELECT setval", "--", "BEGIN", "COMMIT")))
    con = sqlite3.connect(":memory:")
    for table, cols in re.findall(r"INSERT INTO (\w+) \(([^)]*)\)", body):
        con.execute(f"CREATE TABLE IF NOT EXISTS {table} ({cols})")
    con.executescript(body)
    assert con.execute("select count(*) from youth").fetchone()[0] == 50
    assert con.execute("select count(*) from tasks").fetchone()[0] == 150
    assert con.execute("select count(*) from experiences where status='VERIFIED'").fetchone()[0] == 100
    assert con.execute("select count(*) from users where password like '$2b$%'").fetchone()[0] == 69
    assert "setval(pg_get_serial_sequence('tasks'" in sql


def test_sql_escapes_quotes():
    from ssh_ds.seed import _sql
    assert _sql("O'Brien") == "'O''Brien'" and _sql(None) == "NULL" and _sql(True) == "TRUE"


def test_skills_string_round_trips_through_sql(ds):
    sql = to_sql(ds)
    assert "'Excel, Data Entry" in sql or "Data Entry" in sql
