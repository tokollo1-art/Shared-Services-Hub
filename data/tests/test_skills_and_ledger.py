import datetime as dt
import json
from collections import Counter
from pathlib import Path

import pytest

from ssh_ds.ledger import GENESIS_HASH, canonical, java_local_datetime, sha256, verify_chain
from ssh_ds.seed import NOW, PENDING_PAYOUTS, PLATFORM_FEE_PERCENT, generate
from ssh_ds.taxonomy import ALL_SKILLS, SKILLS, SKILL_BY_SLUG, slugify

ROOT = Path(__file__).resolve().parents[2]
AGENT_SKILLS = ROOT / "backend" / "Artificial Intelligence Agents" / "app" / "skills" / "esco_subset.json"


@pytest.fixture(scope="module")
def ds():
    return generate(42)


# --------------------------------------------------------------------- skills
def test_taxonomy_is_the_agents_curated_list():
    """The skills table must hold the same skills the agents map free text onto."""
    if not AGENT_SKILLS.exists():
        pytest.skip("agent sources not in this checkout")
    theirs = json.loads(AGENT_SKILLS.read_text(encoding="utf-8"))
    assert [s["label"] for s in SKILLS] == [e["label"] for e in theirs]
    assert [s["esco_uri"] for s in SKILLS] == [e["esco_uri"] for e in theirs]
    assert [s["ofo_code"] for s in SKILLS] == [e["ofo_code"] for e in theirs]


def test_slugs_are_unique_lowercase_and_fit_the_column():
    assert len(ALL_SKILLS) == len(set(ALL_SKILLS)) == 99
    for s in SKILLS:
        assert s["slug"] == slugify(s["label"]) and s["slug"] == s["slug"].lower()
        assert len(s["slug"]) <= 100 and len(s["label"]) <= 200 and len(s["category"]) <= 50
        assert " " not in s["slug"]
    assert slugify("cash-up procedures") == "cash-up-procedures"


def test_skills_tables_agree_with_the_people_and_tasks(ds):
    by_id = {s["id"]: s["slug"] for s in ds["skills"]}
    assert [s["slug"] for s in ds["skills"]] == ALL_SKILLS or set(by_id.values()) == set(ALL_SKILLS)
    youth = {y["id"]: y for y in ds["youth"]}
    tasks = {t["id"]: t for t in ds["tasks"]}
    ys, ts_ = Counter(), Counter()
    for row in ds["youth_skills"]:
        assert by_id[row["skill_id"]] in youth[row["youth_id"]]["skills"]
        ys[row["youth_id"]] += 1
    for row in ds["task_skills"]:
        assert by_id[row["skill_id"]] in tasks[row["task_id"]]["required_skills"]
        ts_[row["task_id"]] += 1
    assert all(ys[y["id"]] == len(y["skills"]) for y in ds["youth"])
    assert all(ts_[t["id"]] == len(t["required_skills"]) for t in ds["tasks"])
    pairs = [(r["youth_id"], r["skill_id"]) for r in ds["youth_skills"]]
    assert len(pairs) == len(set(pairs))
    pairs = [(r["task_id"], r["skill_id"]) for r in ds["task_skills"]]
    assert len(pairs) == len(set(pairs))


# --------------------------------------------------------------------- ledger
def test_java_datetime_format_matches_java():
    """Expected strings are what java.time.LocalDateTime.toString returns."""
    assert java_local_datetime(dt.datetime(2026, 3, 4, 10, 15)) == "2026-03-04T10:15"
    assert java_local_datetime("2026-03-04T10:15:30") == "2026-03-04T10:15:30"
    assert java_local_datetime(dt.datetime(2026, 3, 4, 10, 15, 30, 120000)) == "2026-03-04T10:15:30.120"
    assert java_local_datetime(dt.datetime(2026, 3, 4, 10, 15, 0, 5000)) == "2026-03-04T10:15:00.005"
    assert java_local_datetime(dt.datetime(2026, 3, 4, 10, 15, 0, 123999)) == "2026-03-04T10:15:00.123"


def test_canonical_layout():
    e = {"sequence": 7, "experience_id": 8, "task_id": 9, "youth_id": 10, "rating": 4.0,
         "feedback": None, "reference": "DN 1", "verified_at": "2026-03-04T10:15:00",
         "previous_hash": GENESIS_HASH}
    assert canonical(e) == "7|8|9|10|4.0||DN 1|2026-03-04T10:15|GENESIS"


def test_one_entry_per_verified_experience_and_the_chain_verifies(ds):
    verified = {e["id"]: e for e in ds["experiences"] if e["status"] == "VERIFIED"}
    chain = ds["ledger_entries"]
    assert len(chain) == len(verified) == 100
    assert {c["experience_id"] for c in chain} == set(verified)
    assert [c["sequence"] for c in chain] == list(range(1, 101))
    assert chain[0]["previous_hash"] == GENESIS_HASH
    assert verify_chain(chain) == (True, None)
    for c in chain:
        e = verified[c["experience_id"]]
        assert (c["task_id"], c["youth_id"], c["rating"], c["feedback"], c["reference"], c["verified_at"]) == \
               (e["task_id"], e["youth_id"], e["rating"], e["feedback"], e["reference"], e["verified_at"])
    stamps = [c["verified_at"] for c in chain]
    assert stamps == sorted(stamps)


def test_tampering_breaks_the_chain(ds):
    chain = [dict(c) for c in ds["ledger_entries"]]
    chain[40]["rating"] = 1.0
    assert verify_chain(chain) == (False, 41)
    chain = [dict(c) for c in ds["ledger_entries"]]
    chain[10]["previous_hash"] = sha256("x")
    assert verify_chain(chain) == (False, 11)


def test_verified_experiences_have_a_reference_and_ledger_codes(ds):
    for e in ds["experiences"]:
        assert bool(e["reference"]) == (e["status"] == "VERIFIED")
        assert e["reference"] is None or len(e["reference"]) <= 255
    verified = Counter(e["youth_id"] for e in ds["experiences"] if e["status"] == "VERIFIED")
    codes = [y["ledger_code"] for y in ds["youth"] if y["ledger_code"]]
    assert len(codes) == len(set(codes)) == sum(1 for y in ds["youth"] if verified[y["id"]])
    assert all(len(c) == 32 for c in codes)
    for y in ds["youth"]:
        if y["ledger_share_enabled"]:
            assert y["ledger_code"]
        if y["is_alumni"]:
            assert y["ledger_share_enabled"]
    assert any(y["ledger_share_enabled"] for y in ds["youth"]) and not all(
        y["ledger_share_enabled"] for y in ds["youth"] if y["ledger_code"])


# ------------------------------------------------------------------- payments
def test_payments_follow_payment_service(ds):
    tasks = {t["id"]: t for t in ds["tasks"]}
    verified_tasks = {e["task_id"]: e for e in ds["experiences"] if e["status"] == "VERIFIED"}
    assert {p["task_id"] for p in ds["payments"]} == set(verified_tasks)
    assert len(ds["payments"]) == len(set(p["task_id"] for p in ds["payments"]))
    for p in ds["payments"]:
        t = tasks[p["task_id"]]
        assert p["youth_id"] == t["assigned_to"] and p["total_amount"] == t["budget"]
        assert p["platform_fee"] == round(p["total_amount"] * PLATFORM_FEE_PERCENT / 100, 2)
        assert p["youth_amount"] == round(p["total_amount"] - p["platform_fee"], 2)
        assert p["status"] in ("PAID", "PENDING")
        assert (p["status"] == "PAID") == bool(p["paid_at"] and p["reference"])
        if p["status"] == "PAID":
            assert p["reference"].startswith("SANDBOX-")
            assert dt.datetime.fromisoformat(p["paid_at"]) <= NOW
    pending = [p for p in ds["payments"] if p["status"] == "PENDING"]
    assert len(pending) == PENDING_PAYOUTS            # left for the admin payout demo
    assert all(p["id"] > 100 - PENDING_PAYOUTS for p in pending)   # the most recent ones


def test_reserve_wallet_balances_add_up(ds):
    (wallet,) = ds["wallets"]
    assert wallet["name"] == "SSH_PLATFORM_RESERVE" and wallet["currency"] == "ZAR"
    tx = ds["wallet_transactions"]
    balance = 0.0
    for t in tx:
        balance = round(balance + t["amount"] if t["type"] == "CREDIT" else balance - t["amount"], 2)
        assert t["balance_after"] == balance and balance >= 0
    assert wallet["balance"] == balance
    paid = {p["id"]: p for p in ds["payments"] if p["status"] == "PAID"}
    debits = [t for t in tx if t["type"] == "DEBIT"]
    assert len(debits) == len(paid)
    assert sum(t["amount"] for t in debits) == pytest.approx(sum(p["youth_amount"] for p in paid.values()))
    stamps = [t["created_at"] for t in tx]
    assert stamps == sorted(stamps)
