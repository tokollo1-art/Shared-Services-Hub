"""Seed data generator: 50 youth, 15 SMEs, 3 funders, 150 tasks, 100 verified
experiences, set in Gauteng. All people and businesses are fictional.

    python -m ssh_ds.seed --out out        # writes seed_data.json + SQL files

Deterministic: the same seed always gives the same data. History is
simulated in time order, so every verified experience obeys the tier rules
at the moment it was assigned.
"""
import argparse
import datetime as dt
import json
import random
from pathlib import Path

from . import catalog
from .locations import AREAS, distance_km
from .taxonomy import (CATEGORIES, SKILL_CATEGORY, SKILLS_BY_CATEGORY,
                       display_skill)
from .tiers import youth_tier

NOW = dt.datetime(2026, 10, 22, 12, 0, 0)   # "today" for the demo data

# Verified experiences per youth: 50 youth, exactly 100 in total.
VERIFIED_COUNTS = [0] * 13 + [1] * 11 + [2] * 10 + [3] * 6 + [4] * 4 + [5] * 3 + [6] * 2 + [8]
N_IN_PROGRESS, N_COMPLETED, N_OPEN, N_CANCELLED = 8, 4, 36, 2
OPEN_BY_TIER = {0: 20, 1: 10, 2: 6}

# Bcrypt hash of the demo password "Password123!" for every seeded account.
DEMO_PASSWORD = "Password123!"
DEMO_PASSWORD_HASH = "$2b$10$N.mMNomn9XrSKy.WY8B3D.LcbOoaTxKqGRXyEXzA6sSS8ry4d1It2"

# Big townships and metro areas weigh more as youth home areas.
AREA_WEIGHTS = {"Soweto": 6, "Tembisa": 5, "Alexandra": 4, "Mamelodi": 4,
                "Katlehong": 3, "Diepsloot": 3, "Soshanguve": 3, "Orange Farm": 3}
BUDGET_RANGE = {0: (150, 600), 1: (400, 1200), 2: (800, 2500)}
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
        skills = rng.sample(SKILLS_BY_CATEGORY[primary], k=min(3, len(SKILLS_BY_CATEGORY[primary])))
        skills += rng.sample(SKILLS_BY_CATEGORY[other], k=rng.randint(1, 2))
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
    own = {SKILL_CATEGORY[s] for s in y["skills"]}
    cats = sorted(own)
    return _weighted(rng, cats, [3 if c == y["_primary"] else 1 for c in cats])


def _pick_template(rng, category, tier, skills):
    options = [t for t in catalog.TASK_TEMPLATES[category] if tier in t[3]]
    weights = [1 + 2 * len(set(t[2]) & set(skills)) for t in options]
    return _weighted(rng, options, weights)


def _pick_sme(rng, smes, category, area):
    options = [s for s in smes if category in s["categories"]]
    options.sort(key=lambda s: distance_km(area, s["location"]) or 0)
    near = options[:3]
    return _weighted(rng, near, [3, 2, 1][:len(near)])


def _task_tier_for(rng, verified_so_far):
    t = youth_tier(verified_so_far)
    if t == 0:
        return 0
    if t == 1:
        return 1
    return 1 if rng.random() < 0.3 else 2


def _new_task(rng, sme, tmpl, tier, category, created, status, youth=None):
    title, detail, skills, _ = tmpl
    mode = sme["work_mode"]
    if category not in catalog.REMOTE_OK or mode == "ONSITE":
        mode = "ONSITE"
    elif mode == "HYBRID":
        mode = rng.choice(["ONSITE", "ONLINE"])
    pool = COVERS[youth["availability"]] if youth else ["PART_TIME", "FULL_TIME", "WEEKENDS", "FLEXIBLE"]
    lo, hi = BUDGET_RANGE[tier]
    return {
        "sme_id": sme["id"], "assigned_to": youth["id"] if youth else None,
        "title": title, "description": f"{detail} Posted by {sme['business_name']}.",
        "category": category, "status": status, "tier": tier,
        "required_skills": list(skills), "work_mode": mode,
        "commitment": rng.choice(pool), "location": sme["location"],
        "budget": float(round(rng.randint(lo, hi) / 50) * 50),
        "duration_days": _weighted(rng, [1, 2, 3, 5, 7], [25, 30, 20, 15, 10]),
        "is_paid": True, "created_at": _iso(created), "updated_at": _iso(created),
    }


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
                "feedback": None, "is_verified": False, "verified_at": None, "rating": None,
                "created_at": _iso(_stamp(rng, end)), "updated_at": _iso(_stamp(rng, end)),
            })

    # 3. Open and cancelled tasks.
    tiers = [t for t, c in OPEN_BY_TIER.items() for _ in range(c)]
    assert len(tiers) == N_OPEN
    rng.shuffle(tiers)
    cats = [CATEGORIES[i % len(CATEGORIES)] for i in range(N_OPEN + N_CANCELLED)]
    rng.shuffle(cats)
    for k in range(N_OPEN + N_CANCELLED):
        cancelled = k >= N_OPEN
        tier = tiers[k] if not cancelled else rng.choice([0, 1])
        cat = cats[k]
        sme = rng.choice([s for s in smes if cat in s["categories"]])
        tmpl = _pick_template(rng, cat, tier, [])
        days_ago = rng.randint(14, 40) if cancelled else rng.randint(0, 12)
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

    for y in youth:
        for k in [k for k in y if k.startswith("_")]:
            del y[k]
    return {
        "meta": {"generated_for": NOW.isoformat(timespec="seconds"), "seed": seed,
                 "demo_password": DEMO_PASSWORD, "synthetic": True},
        "users": users, "youth": youth, "smes": smes, "funders": funders,
        "tasks": tasks, "experiences": experiences,
    }


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


SCHEMA_ADDITIONS = """\
-- Columns Slot 4 matching needs on top of the current entities.
-- Add the matching fields to the Java entities (Hibernate ddl-auto=update will
-- also create them). Safe to run more than once.
ALTER TABLE smes  ADD COLUMN IF NOT EXISTS work_mode VARCHAR(10);
ALTER TABLE smes  ADD COLUMN IF NOT EXISTS location VARCHAR(100);
ALTER TABLE smes  ADD COLUMN IF NOT EXISTS turnover_band VARCHAR(20);
ALTER TABLE smes  ADD COLUMN IF NOT EXISTS registration_date DATE;
ALTER TABLE smes  ADD COLUMN IF NOT EXISTS enterprise_status VARCHAR(20);
ALTER TABLE smes  ADD COLUMN IF NOT EXISTS financial_year_end VARCHAR(15);
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS required_skills TEXT;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS work_mode VARCHAR(10);
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS commitment VARCHAR(15);
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS location VARCHAR(100);
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS tier INTEGER;
ALTER TABLE youth ADD COLUMN IF NOT EXISTS open_to_remote BOOLEAN;
ALTER TABLE youth ADD COLUMN IF NOT EXISTS max_travel_km INTEGER;
ALTER TABLE youth ADD COLUMN IF NOT EXISTS interests TEXT;
"""


def to_sql(ds):
    """Postgres inserts matching the entity column names (snake_case)."""
    names = lambda skills: ", ".join(display_skill(s) for s in skills)
    parts = ["-- SSH seed data. Synthetic. Run schema_additions.sql first.",
             f"-- Every account's password is {DEMO_PASSWORD}", "BEGIN;"]
    parts.append(_insert("users", ["id", "name", "email", "password", "role", "is_active", "created_at", "updated_at"],
                         [[u["id"], u["name"], u["email"], DEMO_PASSWORD_HASH, u["role"], True,
                           u["created_at"], u["created_at"]] for u in ds["users"]]))
    parts.append(_insert("smes", ["id", "user_id", "business_name", "registration_number", "industry", "description",
                                  "employee_count", "bbbee_level", "is_verified", "work_mode", "location",
                                  "turnover_band", "registration_date", "enterprise_status", "financial_year_end",
                                  "created_at", "updated_at"],
                         [[s["id"], s["user_id"], s["business_name"], s["registration_number"], s["industry"],
                           s["description"], s["employee_count"], s["bbbee_level"], s["is_verified"],
                           s["work_mode"], s["location"], s["turnover_band"], s["registration_date"],
                           s["enterprise_status"], s["financial_year_end"], s["created_at"], s["created_at"]]
                          for s in ds["smes"]]))
    parts.append(_insert("youth", ["id", "user_id", "education", "skills", "portfolio", "bio", "location",
                                   "availability", "is_alumni", "alumni_date", "open_to_remote", "max_travel_km",
                                   "interests", "created_at", "updated_at"],
                         [[y["id"], y["user_id"], y["education"], names(y["skills"]), y["portfolio"], y["bio"],
                           y["location"], y["availability"], y["is_alumni"], y["alumni_date"],
                           y["open_to_remote"], y["max_travel_km"], ", ".join(y["interests"]),
                           y["created_at"], y["updated_at"]] for y in ds["youth"]]))
    parts.append(_insert("tasks", ["id", "sme_id", "assigned_to", "title", "description", "category", "status",
                                   "budget", "duration_days", "is_paid", "required_skills", "work_mode",
                                   "commitment", "location", "tier", "created_at", "updated_at"],
                         [[t["id"], t["sme_id"], t["assigned_to"], t["title"], t["description"], t["category"],
                           t["status"], t["budget"], t["duration_days"], t["is_paid"], names(t["required_skills"]),
                           t["work_mode"], t["commitment"], t["location"], t["tier"], t["created_at"],
                           t["updated_at"]] for t in ds["tasks"]]))
    parts.append(_insert("experiences", ["id", "youth_id", "task_id", "status", "start_date", "end_date",
                                         "feedback", "is_verified", "verified_at", "rating", "created_at",
                                         "updated_at"],
                         [[e["id"], e["youth_id"], e["task_id"], e["status"], e["start_date"], e["end_date"],
                           e["feedback"], e["is_verified"], e["verified_at"], e["rating"], e["created_at"],
                           e["updated_at"]] for e in ds["experiences"]]))
    for t in ["users", "smes", "youth", "tasks", "experiences"]:
        parts.append(f"SELECT setval(pg_get_serial_sequence('{t}', 'id'), (SELECT MAX(id) FROM {t}));")
    parts.append("COMMIT;")
    return "\n\n".join(parts) + "\n"


def write_all(out_dir, seed=42):
    out = Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)
    ds = generate(seed)
    (out / "seed_data.json").write_text(json.dumps(ds, indent=2, ensure_ascii=False))
    (out / "schema_additions.sql").write_text(SCHEMA_ADDITIONS)
    (out / "seed.sql").write_text(to_sql(ds))
    return ds


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", default="out")
    ap.add_argument("--seed", type=int, default=42)
    args = ap.parse_args()
    d = write_all(args.out, args.seed)
    print({k: len(v) for k, v in d.items() if isinstance(v, list)})
