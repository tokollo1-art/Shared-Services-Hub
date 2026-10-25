"""Seed data generator: 50 youth, 15 SMEs, 3 funders, 150 tasks, 100 verified
experiences, set in Gauteng. All people and businesses are fictional.

    python -m ssh_ds.seed --out out        # writes seed_data.json + seed.sql

The SQL fills the tables the backend entities define: users, smes, youth,
skills, youth_skills, task_skills, tasks, experiences, ledger_entries,
payments, wallets, wallet_transactions. Load it into an EMPTY database whose
tables already exist (start the backend once, or see tests/schema/).

Deterministic: the same seed always gives the same data. History is simulated
in time order, so every verified experience obeyed the tier rules (tiers.py,
mirroring Tier.java) at the moment it was assigned.
"""
import argparse
import datetime as dt
import json
import random
import uuid
from pathlib import Path

from . import catalog
from .ledger import build_chain
from .locations import AREAS, distance_km
from .taxonomy import SKILLS, display_skill
from .tiers import MAX_TIER, RESERVATION_WINDOW_HOURS, youth_tier

CATEGORIES = list(catalog.TASK_TEMPLATES)

NOW = dt.datetime(2026, 10, 22, 12, 0, 0)   # "today" for the demo data

# Verified experiences per youth: 50 youth, exactly 100 in total.
# By backend tier: 13 in Tier 0, 21 in Tier 1 (1-2), 10 in Tier 2 (3-4), 6 in Tier 3 (5+).
VERIFIED_COUNTS = [0] * 13 + [1] * 11 + [2] * 10 + [3] * 6 + [4] * 4 + [5] * 3 + [6] * 2 + [8]
N_IN_PROGRESS, N_COMPLETED, N_OPEN, N_CANCELLED = 8, 4, 36, 2
OPEN_BY_TIER = {0: 16, 1: 10, 2: 7, 3: 3}
N_FRESH_OPEN_TIER0 = 4         # open tier 0 tasks still inside their 48h reservation window

PLATFORM_FEE_PERCENT = 10.0    # PaymentService.PLATFORM_FEE_PERCENT
RESERVE_WALLET_NAME = "SSH_PLATFORM_RESERVE"   # WalletService.RESERVE_WALLET_NAME
FUNDED_SHARE = 0.25            # share of each funder's commitment already paid into the reserve
PENDING_PAYOUTS = 5            # the most recent verified tasks still wait for an admin payout

# Bcrypt hash of the demo password "Password123!" for every seeded account.
DEMO_PASSWORD = "Password123!"
DEMO_PASSWORD_HASH = "$2b$10$N.mMNomn9XrSKy.WY8B3D.LcbOoaTxKqGRXyEXzA6sSS8ry4d1It2"

# Big townships and metro areas weigh more as youth home areas.
AREA_WEIGHTS = {"Soweto": 6, "Tembisa": 5, "Alexandra": 4, "Mamelodi": 4,
                "Katlehong": 3, "Diepsloot": 3, "Soshanguve": 3, "Orange Farm": 3}
BUDGET_RANGE = {0: (150, 600), 1: (400, 1200), 2: (800, 2500), 3: (1200, 3500)}
TURNOVER_BANDS = ["Under R1m", "R1m to R5m", "R5m to R10m", "R10m to R50m"]
MONTHS = ["January", "February", "March", "April", "May", "June", "July",
          "August", "September", "October", "November", "December"]
COVERS = {"FULL_TIME": ["FULL_TIME", "PART_TIME", "WEEKENDS", "FLEXIBLE"],
          "PART_TIME": ["PART_TIME", "WEEKENDS", "FLEXIBLE"],
          "WEEKENDS": ["WEEKENDS", "FLEXIBLE"]}


# --------------------------------------------------------------------- utils
def _stamp(rng, day):
    """A working-hours timestamp on `day`, never after NOW."""
    ts = dt.datetime.combine(day, dt.time(rng.randint(8, 17), rng.choice([0, 15, 30, 45])))
    return min(ts, NOW - dt.timedelta(hours=1))


def _iso(ts):
    return ts.isoformat(timespec="seconds") if ts else None


def _slug(text):
    return "".join(c.lower() if c.isalnum() else "." for c in text).strip(".")


def _weighted(rng, items, weights):
    return rng.choices(items, weights=weights, k=1)[0]


# ----------------------------------------------------------------- builders
def _build_smes(rng):
    smes, used = [], set()
    for i, (name, industry, desc, cats, mode, area, emp, bbbee) in enumerate(catalog.SME_SEED, 1):
        year = rng.randint(2018, 2025)
        while True:  # 99xxxx range: clearly synthetic, unique
            reg = f"{year}/{rng.randint(990000, 999999)}/07"
            if reg not in used:
                used.add(reg)
                break
        reg_date = dt.date(year, rng.randint(1, 12), rng.randint(1, 28))
        smes.append({
            "id": i, "user_id": 4 + i, "business_name": name,
            "registration_number": reg, "industry": industry, "description": desc,
            "employee_count": emp, "bbbee_level": bbbee,
            "is_verified": i not in (7, 14),          # two left unverified for the admin demo
            "work_mode": mode, "location": area,
            "turnover_band": _weighted(rng, TURNOVER_BANDS, [3, 4, 2, 1]),
            "registration_date": reg_date.isoformat(),
            "enterprise_status": "IN_BUSINESS",
            "financial_year_end": rng.choice(MONTHS),
            "categories": cats,
            "created_at": _iso(_stamp(rng, NOW.date() - dt.timedelta(days=rng.randint(260, 300)))),
        })
    return smes


def _build_youth(rng, counts):
    youth = []
    for i, n in enumerate(counts, 1):
        first, last = rng.choice(catalog.FIRST_NAMES), rng.choice(catalog.LAST_NAMES)
        areas = list(AREAS)
        area = _weighted(rng, areas, [AREA_WEIGHTS.get(a, 1) for a in areas])
        primary = rng.choice(CATEGORIES)
        other = rng.choice([c for c in CATEGORIES if c != primary])
        skills = rng.sample(catalog.CATEGORY_SKILLS[primary], k=3)
        extra = [x for x in catalog.CATEGORY_SKILLS[other] if x not in skills]
        skills += rng.sample(extra, k=rng.randint(1, 2))
        interests = [primary] + ([other] if rng.random() < 0.35 else [])
        youth.append({
            "id": i, "user_id": 19 + i, "name": f"{first} {last}",
            "email": f"{first}.{last}.{i}@example.com".lower().replace(" ", ""),
            "education": rng.choice(catalog.EDUCATION),
            "skills": skills,
            "portfolio": None,
            "bio": rng.choice(catalog.BIO_TEMPLATES).format(area=area, cat=primary),
            "location": area,
            "availability": _weighted(rng, ["FULL_TIME", "PART_TIME", "WEEKENDS"], [45, 35, 20]),
            "open_to_remote": rng.random() < 0.6,
            "max_travel_km": rng.choice([8, 15, 25, 35]),
            "interests": interests,
            "is_alumni": False, "alumni_date": None,
            "_target": n, "_quality": rng.betavariate(5, 2), "_primary": primary,
        })
    return youth


def _pick_category(rng, y):
    own = set()
    for sk in y["skills"]:
        own |= catalog.SKILL_TASK_CATEGORIES.get(sk, set())
    cats = sorted(own)
    return _weighted(rng, cats, [3 if c == y["_primary"] else 1 for c in cats])


def _template_level(tier):
    """Templates are written for tiers 0-2; tier 3 tasks reuse the tier 2 ones."""
    return min(tier, 2)


def _pick_template(rng, category, tier, skills):
    level = _template_level(tier)
    options = [t for t in catalog.TASK_TEMPLATES[category] if level in t[3]]
    weights = [1 + 2 * len(set(t[2]) & set(skills)) for t in options]
    return _weighted(rng, options, weights)


def _pick_sme(rng, smes, category, area):
    options = [s for s in smes if category in s["categories"]]
    options.sort(key=lambda s: distance_km(area, s["location"]) or 0)
    near = options[:3]
    return _weighted(rng, near, [3, 2, 1][:len(near)])


def _task_tier_for(rng, verified_so_far):
    """Tier of the next task for a youth with this many verified experiences.

    Never above the youth's own tier (tiers.eligible). Tier 0 youth only see
    tier 0 tasks; tier 1 youth stay at tier 1 (tier 0 is reserved for newcomers).
    """
    t = youth_tier(verified_so_far)
    if t <= 1:
        return t
    r = rng.random()
    if t == 2:
        return 1 if r < 0.3 else 2
    return 2 if r < 0.5 else 3 if r < 0.9 else 1


def _new_task(rng, sme, tmpl, tier, category, created, status, youth=None):
    title, detail, skills, _ = tmpl
    mode = sme["work_mode"]
    if category not in catalog.REMOTE_OK or mode == "ONSITE":
        mode = "ONSITE"
    elif mode == "HYBRID":
        mode = rng.choice(["ONSITE", "ONLINE"])
    pool = COVERS[youth["availability"]] if youth else ["PART_TIME", "FULL_TIME", "WEEKENDS", "FLEXIBLE"]
    lo, hi = BUDGET_RANGE[tier]
    days = _weighted(rng, [1, 2, 3, 5, 7], [25, 30, 20, 15, 10])
    return {
        "sme_id": sme["id"], "assigned_to": youth["id"] if youth else None,
        "title": title, "description": f"{detail} Posted by {sme['business_name']}.",
        "category": category, "status": status, "tier": tier,
        "required_skills": list(skills), "location": sme["location"],
        "remote": mode == "ONLINE",
        "est_hours": float(max(2, days * rng.choice([2, 3, 4]))),
        "budget": float(round(rng.randint(lo, hi) / 50) * 50),
        "duration_days": days, "is_paid": True,
        "revision_reason": None, "revision_count": 0,
        # tier 0 tasks are reserved for first-timers for 48h after posting
        "reserved_until": None,
        "created_at": _iso(created), "updated_at": _iso(created),
        # Matching inputs with no backend column (not written to SQL):
        "work_mode": mode, "commitment": rng.choice(pool),
    }


REFLECTION_OPENERS = [
    "I {verb} this task for {sme}. The main thing I did was {skills}.",
    "For {sme} I {verb} the work myself. It involved {skills}.",
    "This job for {sme} was about {skills}, and I {verb} it from start to finish.",
]
REFLECTION_CLOSERS = [
    "I checked my work before handing it in and asked when something was unclear.",
    "The owner gave feedback along the way and I adjusted what I had done.",
    "I kept a record of what I did so it could be reviewed.",
    "Next time I would plan the steps earlier so the work goes faster.",
]


def _reflection(rng, task, sme_name):
    """First-person account of the work, like the text a youth submits on completion.

    The evidence agent extracts skills from this, so it names the task's skills."""
    names = [display_skill(sk) for sk in task["required_skills"]] or ["the task"]
    listed = names[0] if len(names) == 1 else ", ".join(names[:-1]) + " and " + names[-1]
    opener = rng.choice(REFLECTION_OPENERS).format(
        verb=rng.choice(["completed", "finished", "carried out"]), sme=sme_name, skills=listed)
    return f"{opener} {task['title']}. {rng.choice(REFLECTION_CLOSERS)}"


REFERENCE_KINDS = {
    "Digital Operations": "Shared folder", "Marketing & Creative": "Proof approved",
    "Accounting & Finance": "Ledger page", "Customer Support": "Call log",
    "IT & Technical": "Job card", "Retail & Sales": "Stock sheet",
    "Education & Training": "Register", "Logistics & Events": "Delivery note",
}


def _reference(rng, task):
    return f"{REFERENCE_KINDS[task['category']]} {rng.choice('ABCDEF')}{rng.randint(1000, 9999)}"


def _rating(rng, y, task):
    overlap = len(set(task["required_skills"]) & set(y["skills"])) / max(1, len(task["required_skills"]))
    raw = 2.2 + 2.3 * y["_quality"] + 0.5 * overlap + rng.gauss(0, 0.55)
    return float(min(5, max(1, round(raw))))


# ----------------------------------------------------------------- generate
def generate(seed=42):
    rng = random.Random(seed)
    counts = VERIFIED_COUNTS[:]
    rng.shuffle(counts)
    smes = _build_smes(rng)
    youth = _build_youth(rng, counts)

    # alumni: the 8-experience youth, one of the 6s, one of the 5s
    order = sorted(youth, key=lambda y: -y["_target"])
    alumni = {order[0]["id"], order[1]["id"], next(y["id"] for y in order if y["_target"] == 5)}

    tasks, experiences = [], []

    # 1. Verified history, simulated forward in time per youth.
    for y in youth:
        n = y["_target"]
        plan = []
        for j in range(n):
            tier = _task_tier_for(rng, j)          # j verified so far
            cat = _pick_category(rng, y)
            tmpl = _pick_template(rng, cat, tier, y["skills"])
            sme = _pick_sme(rng, smes, cat, y["location"])
            wait = rng.randint(1, 14) if j == 0 else rng.randint(1, 8)
            if j == 0 and rng.random() < 0.25:
                wait = rng.randint(18, 45)         # a realistic tail of slow starts
            plan.append({"tier": tier, "cat": cat, "tmpl": tmpl, "sme": sme,
                         "wait": wait, "lag": rng.randint(0, 3)})
        total = 0
        draft = []
        for p in plan:
            task = _new_task(rng, p["sme"], p["tmpl"], p["tier"], p["cat"],
                             NOW, "VERIFIED", youth=y)   # created_at fixed below
            p["task"] = task
            total += p["wait"] + task["duration_days"] + p["lag"]
        slack = rng.randint(15, 40)                # days between last verification and NOW
        y["_joined"] = NOW.date() - dt.timedelta(days=total + slack) if n else \
            NOW.date() - dt.timedelta(days=rng.randint(8, 60))
        cursor = y["_joined"]
        for p in plan:
            task = p["task"]
            start = cursor + dt.timedelta(days=p["wait"])
            end = start + dt.timedelta(days=task["duration_days"])
            ver = end + dt.timedelta(days=p["lag"])
            created = _stamp(rng, start - dt.timedelta(days=rng.randint(2, 7)))
            task["created_at"] = task["updated_at"] = _iso(created)
            rating = _rating(rng, y, task)
            end_ts = _stamp(rng, end)
            verified_at = max(_stamp(rng, ver), end_ts + dt.timedelta(hours=rng.randint(1, 4)))
            tasks.append(task)
            experiences.append({
                "_task": task, "youth_id": y["id"], "status": "VERIFIED",
                "start_date": _iso(_stamp(rng, start)), "end_date": _iso(end_ts),
                "feedback": rng.choice(catalog.FEEDBACK[int(rating)]),
                "reference": _reference(rng, task),
                "is_verified": True, "verified_at": _iso(verified_at), "rating": rating,
                "created_at": _iso(end_ts), "updated_at": _iso(verified_at),
            })
            cursor = ver

    # 2. Work in flight: some in progress, some completed and awaiting verification.
    movers = [y for y in youth if y["id"] not in alumni]
    chosen = rng.sample(movers, N_IN_PROGRESS + N_COMPLETED)
    for k, y in enumerate(chosen):
        tier = _task_tier_for(rng, y["_target"])
        cat = _pick_category(rng, y)
        tmpl = _pick_template(rng, cat, tier, y["skills"])
        sme = _pick_sme(rng, smes, cat, y["location"])
        done = k >= N_IN_PROGRESS
        task = _new_task(rng, sme, tmpl, tier, cat, NOW, "COMPLETED" if done else "IN_PROGRESS", youth=y)
        if done:
            end = NOW.date() - dt.timedelta(days=rng.randint(0, 2))
            start = end - dt.timedelta(days=task["duration_days"])
        else:
            start = NOW.date() - dt.timedelta(days=rng.randint(1, 5))
        task["created_at"] = task["updated_at"] = _iso(_stamp(rng, start - dt.timedelta(days=rng.randint(2, 6))))
        tasks.append(task)
        if done:  # the platform opens a PENDING experience when work is completed
            experiences.append({
                "_task": task, "youth_id": y["id"], "status": "PENDING",
                "start_date": _iso(_stamp(rng, start)), "end_date": _iso(_stamp(rng, end)),
                "feedback": None, "reference": None, "is_verified": False,
                "verified_at": None, "rating": None,
                "created_at": _iso(_stamp(rng, end)), "updated_at": _iso(_stamp(rng, end)),
            })

    # 3. Open and cancelled tasks.
    tiers = [t for t, c in OPEN_BY_TIER.items() for _ in range(c)]
    assert len(tiers) == N_OPEN
    rng.shuffle(tiers)
    fresh_tier0 = 0
    cats = [CATEGORIES[i % len(CATEGORIES)] for i in range(N_OPEN + N_CANCELLED)]
    rng.shuffle(cats)
    for k in range(N_OPEN + N_CANCELLED):
        cancelled = k >= N_OPEN
        tier = tiers[k] if not cancelled else rng.choice([0, 1])
        cat = cats[k]
        sme = rng.choice([s for s in smes if cat in s["categories"]])
        tmpl = _pick_template(rng, cat, tier, [])
        days_ago = rng.randint(14, 40) if cancelled else rng.randint(0, 12)
        if tier == 0 and not cancelled and fresh_tier0 < N_FRESH_OPEN_TIER0:
            days_ago, fresh_tier0 = rng.choice([0, 1]), fresh_tier0 + 1
        task = _new_task(rng, sme, tmpl, tier, cat, _stamp(rng, NOW.date() - dt.timedelta(days=days_ago)),
                         "CANCELLED" if cancelled else "OPEN")
        tasks.append(task)

    # 4. Alumni, ids and users.
    for y in youth:
        last = max((e["verified_at"] for e in experiences
                    if e["youth_id"] == y["id"] and e["status"] == "VERIFIED"), default=None)
        if y["id"] in alumni and last:
            y["is_alumni"] = True
            y["alumni_date"] = _iso(_stamp(rng, dt.datetime.fromisoformat(last).date()
                                           + dt.timedelta(days=rng.randint(5, 12))))
        y["created_at"] = y["updated_at"] = _iso(_stamp(rng, y["_joined"]))

    tasks.sort(key=lambda t: (t["created_at"], t["title"]))
    for i, t in enumerate(tasks, 1):
        t["id"] = i
        if t["tier"] == 0:
            t["reserved_until"] = _iso(dt.datetime.fromisoformat(t["created_at"])
                                       + dt.timedelta(hours=RESERVATION_WINDOW_HOURS))
    experiences.sort(key=lambda e: (e["created_at"], e["youth_id"]))
    for i, e in enumerate(experiences, 1):
        e["id"], e["task_id"] = i, e.pop("_task")["id"]

    users = [{"id": 1, "name": "SSH Admin", "email": "admin@example.com", "role": "ADMIN",
              "created_at": _iso(NOW - dt.timedelta(days=300))}]
    funders = []
    for i, (name, amount, cohort) in enumerate(catalog.FUNDERS, 1):
        users.append({"id": 1 + i, "name": name, "email": f"funder{i}@example.com", "role": "CORPORATE",
                      "created_at": _iso(NOW - dt.timedelta(days=290))})
        funders.append({"id": i, "user_id": 1 + i, "name": name, "committed_amount": amount, "cohort": cohort})
    for s in smes:
        users.append({"id": s["user_id"], "name": f"Owner of {s['business_name']}",
                      "email": f"owner.{_slug(s['business_name'])}@example.com", "role": "SME",
                      "created_at": s["created_at"]})
    for y in youth:
        users.append({"id": y["user_id"], "name": y["name"], "email": y["email"], "role": "YOUTH",
                      "created_at": y["created_at"]})

    # 5. Public ledger codes (made on first use, so only youth with verified work have one).
    verified_n = {y["id"]: 0 for y in youth}
    for e in experiences:
        if e["status"] == "VERIFIED":
            verified_n[e["youth_id"]] += 1
    for y in youth:
        n = verified_n[y["id"]]
        y["ledger_code"] = uuid.UUID(int=rng.getrandbits(128), version=4).hex if n else None
        y["ledger_share_enabled"] = bool(n) and (y["is_alumni"] or (n >= 2 and rng.random() < 0.35))

    # 6. Skills tables, the hash chain, payments and the reserve wallet.
    skills = [{"id": i, **{k: s[k] for k in ("slug", "label", "category", "esco_uri", "ofo_code")}}
              for i, s in enumerate(SKILLS, 1)]
    skill_id = {s["slug"]: s["id"] for s in skills}
    youth_skills = [{"youth_id": y["id"], "skill_id": skill_id[sk]} for y in youth for sk in y["skills"]]
    task_skills = [{"task_id": t["id"], "skill_id": skill_id[sk]} for t in tasks for sk in t["required_skills"]]
    ledger_entries = build_chain(experiences)
    payments, wallet_tx = _payments_and_wallet(rng, tasks, experiences, funders)

    # Reflections come from their own RNG so adding them leaves every other value (and
    # the ledger hashes) exactly as before.
    rrng = random.Random(seed + 1)
    sme_name = {m["id"]: m["business_name"] for m in smes}
    for t in tasks:
        t["reflection"] = (_reflection(rrng, t, sme_name[t["sme_id"]])
                           if t["status"] in ("COMPLETED", "VERIFIED") else None)

    for y in youth:
        for k in [k for k in y if k.startswith("_")]:
            del y[k]
    return {
        "meta": {"generated_for": NOW.isoformat(timespec="seconds"), "seed": seed,
                 "demo_password": DEMO_PASSWORD, "synthetic": True},
        "users": users, "youth": youth, "smes": smes, "funders": funders,
        "skills": skills, "youth_skills": youth_skills, "task_skills": task_skills,
        "tasks": tasks, "experiences": experiences, "ledger_entries": ledger_entries,
        "payments": payments,
        "wallets": [{"id": 1, "name": RESERVE_WALLET_NAME, "currency": "ZAR",
                     "balance": wallet_tx[-1]["balance_after"], "created_at": wallet_tx[0]["created_at"],
                     "updated_at": wallet_tx[-1]["created_at"]}],
        "wallet_transactions": wallet_tx,
    }


def _round2(v):
    return float(int(v * 100 + 0.5)) / 100.0      # Math.round(v * 100.0) / 100.0 for v >= 0


def _payments_and_wallet(rng, tasks, experiences, funders):
    """One payment per verified task, and the reserve wallet's credits and debits."""
    by_task = {t["id"]: t for t in tasks}
    payments = []
    verified = sorted((e for e in experiences if e["status"] == "VERIFIED"),
                      key=lambda e: (e["verified_at"], e["id"]))
    for n, e in enumerate(verified):
        total = by_task[e["task_id"]]["budget"]
        fee = _round2(total * (PLATFORM_FEE_PERCENT / 100.0))
        verified_at = dt.datetime.fromisoformat(e["verified_at"])
        paid = verified_at + dt.timedelta(days=rng.randint(1, 3), hours=rng.randint(0, 5))
        is_paid = n < len(verified) - PENDING_PAYOUTS and paid <= NOW
        payments.append({
            "id": len(payments) + 1, "task_id": e["task_id"], "youth_id": e["youth_id"],
            "total_amount": total, "platform_fee": fee, "youth_amount": _round2(total - fee),
            "status": "PAID" if is_paid else "PENDING",
            "paid_at": _iso(paid) if is_paid else None,
            "reference": "SANDBOX-" + str(uuid.UUID(int=rng.getrandbits(128), version=4)) if is_paid else None,
            "created_at": e["verified_at"], "updated_at": _iso(paid) if is_paid else e["verified_at"],
        })

    events = [(_iso(NOW - dt.timedelta(days=280 - 3 * f["id"])), "CREDIT",
               round(f["committed_amount"] * FUNDED_SHARE, 2), f"Funder tranche: {f['name']}")
              for f in funders]
    events += [(p["paid_at"], "DEBIT", p["youth_amount"], f"Payout for payment {p['id']}")
               for p in payments if p["status"] == "PAID"]
    events.sort(key=lambda ev: (ev[0], ev[1] != "CREDIT"))
    tx, balance = [], 0.0
    for at, kind, amount, ref in events:
        balance = round(balance + amount if kind == "CREDIT" else balance - amount, 2)
        tx.append({"id": len(tx) + 1, "wallet_id": 1, "type": kind, "amount": amount,
                   "balance_after": balance, "reference": ref, "created_at": at})
    return payments, tx


# ---------------------------------------------------------------------- SQL
def _sql(v):
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "TRUE" if v else "FALSE"
    if isinstance(v, (int, float)):
        return repr(v)
    return "'" + str(v).replace("'", "''") + "'"


def _insert(table, cols, rows):
    out = [f"INSERT INTO {table} ({', '.join(cols)}) VALUES"]
    out.append(",\n".join("  (" + ", ".join(_sql(v) for v in r) + ")" for r in rows) + ";")
    return "\n".join(out)


# Columns written per table, in order. Every column is a real entity column
# (snake_case, Spring's default naming). Primitive boolean and int fields
# (users.is_active, smes.is_verified, youth.is_alumni, youth.ledger_share_enabled,
# tasks.is_paid, tasks.remote, tasks.revision_count, experiences.is_verified)
# are NOT NULL in Hibernate's DDL, so each one is always written.
TABLES = [
    ("users", "users", ["id", "name", "email", "password", "role", "is_active", "created_at", "updated_at"]),
    ("smes", "smes", ["id", "user_id", "business_name", "registration_number", "industry", "description",
                      "employee_count", "bbbee_level", "is_verified", "created_at", "updated_at"]),
    ("youth", "youth", ["id", "user_id", "education", "skills", "portfolio", "bio", "location", "availability",
                        "is_alumni", "alumni_date", "ledger_code", "ledger_share_enabled",
                        "created_at", "updated_at"]),
    ("skills", "skills", ["id", "slug", "label", "category", "esco_uri", "ofo_code"]),
    ("tasks", "tasks", ["id", "sme_id", "assigned_to", "title", "description", "category", "status", "budget",
                        "duration_days", "is_paid", "revision_reason", "revision_count", "tier",
                        "reserved_until", "remote", "location", "est_hours", "reflection", "created_at",
                        "updated_at"]),
    ("experiences", "experiences", ["id", "youth_id", "task_id", "status", "start_date", "end_date", "feedback",
                                    "is_verified", "verified_at", "rating", "reference", "created_at",
                                    "updated_at"]),
    ("youth_skills", "youth_skills", ["id", "youth_id", "skill_id"]),
    ("task_skills", "task_skills", ["id", "task_id", "skill_id"]),
    ("ledger_entries", "ledger_entries", ["id", "entry_sequence", "experience_id", "task_id", "youth_id", "rating",
                                          "feedback", "reference", "verified_at", "previous_hash", "hash",
                                          "created_at"]),
    ("payments", "payments", ["id", "task_id", "youth_id", "total_amount", "platform_fee", "youth_amount",
                              "status", "paid_at", "reference", "created_at", "updated_at"]),
    ("wallets", "wallets", ["id", "name", "balance", "currency", "created_at", "updated_at"]),
    ("wallet_transactions", "wallet_transactions", ["id", "wallet_id", "type", "amount", "balance_after",
                                                    "reference", "created_at"]),
]


def table_rows(ds):
    """{table: [row dict keyed by column]} for every seeded table."""
    skill_ids = {s["slug"]: s["id"] for s in ds["skills"]}
    join = lambda rows: [{"id": i, **r} for i, r in enumerate(rows, 1)]
    out = {
        "users": [{**u, "password": DEMO_PASSWORD_HASH, "is_active": True, "updated_at": u["created_at"]}
                  for u in ds["users"]],
        "smes": [{**s, "updated_at": s["created_at"]} for s in ds["smes"]],
        "youth": [{**y, "skills": ", ".join(display_skill(x) for x in y["skills"])} for y in ds["youth"]],
        "skills": ds["skills"],
        "tasks": ds["tasks"],
        "experiences": ds["experiences"],
        "youth_skills": join(ds["youth_skills"]),
        "task_skills": join(ds["task_skills"]),
        "ledger_entries": [{**e, "entry_sequence": e["sequence"]} for e in ds["ledger_entries"]],
        "payments": ds["payments"],
        "wallets": ds["wallets"],
        "wallet_transactions": ds["wallet_transactions"],
    }
    assert set(out) == {t[0] for t in TABLES} and skill_ids
    return out


def to_sql(ds):
    """Postgres inserts for the entity tables, in foreign-key order, in one transaction."""
    rows = table_rows(ds)
    parts = ["-- SSH seed data. Synthetic. Load into an EMPTY database whose tables already exist.",
             f"-- Every account's password is {DEMO_PASSWORD}", "BEGIN;"]
    for key, table, cols in TABLES:
        parts.append(_insert(table, cols, [[r.get(c) for c in cols] for r in rows[key]]))
    for _, table, _ in TABLES:
        parts.append(f"SELECT setval(pg_get_serial_sequence('{table}', 'id'), (SELECT MAX(id) FROM {table}));")
    parts.append("COMMIT;")
    return "\n\n".join(parts) + "\n"


def write_all(out_dir, seed=42):
    out = Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)
    ds = generate(seed)
    (out / "seed_data.json").write_text(json.dumps(ds, indent=2, ensure_ascii=False))
    (out / "seed.sql").write_text(to_sql(ds))
    return ds


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", default="out")
    ap.add_argument("--seed", type=int, default=42)
    args = ap.parse_args()
    d = write_all(args.out, args.seed)
    print({k: len(v) for k, v in d.items() if isinstance(v, list)})
