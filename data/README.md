# Slot 4: data science for SSH

Matching, seed data, readiness baseline and impact metrics. Pure Python 3, standard library only (pytest for tests).

```
ssh_ds/taxonomy.py   skills, categories, similarity, parse_skills("Excel, Canva")
ssh_ds/matching.py   score_match, rank_candidates, build_youth_profile   <- Slot 3 wraps this
ssh_ds/tiers.py      youth_tier, eligible (ASSUMED cut-offs, see below)
ssh_ds/metrics.py    readiness_score, impact_report, metric definitions
ssh_ds/seed.py       generator: python -m ssh_ds.seed --out out
ssh_ds/locations.py  Gauteng areas with coordinates
out/                 seed_data.json, schema_additions.sql, seed.sql
tests/               39 tests, run with: python -m pytest
```

## For Slot 3 (matchmaker agent)

```python
from ssh_ds import score_match, rank_candidates, build_youth_profile

profile = build_youth_profile(youth, experiences, tasks_by_id)   # counts VERIFIED only
result  = score_match(profile, task)
# {"score": 88.7, "reasons": ["Skills match: ...", ...], "components": {...}, "cold_start": True}

top = rank_candidates(eligible_profiles, task, top_n=5)          # ties break by youth id
```

* Filter by tier first. The function does not check eligibility.
* `reasons` are ordered most useful first. Use the top two or three for "why this match".
* `skills` and `required_skills` can be lists of tags or the free text stored in the database ("Excel, Data Entry"). Unknown skills are ignored.
* Missing fields never crash it; they score neutral.
* Scores: skills 35%, location 20%, category history 20%, rating 15%, availability 10%. Youth with no verified experience take the cold-start branch: skills 50%, location 25%, interests 15%, availability 10%. A perfect-fit newcomer can still score 100.
* Ratings are shrunk toward 3.5, so one 5-star task doesn't beat a long solid record.

## For whoever loads the database

```
psql ssh -f out/schema_additions.sql   # adds columns, safe to re-run
psql ssh -f out/seed.sql               # users, smes, youth, tasks, experiences, resets id sequences
```

Tested against Postgres 16 with tables shaped like the current entities. Every seeded account's password is `Password123!`.

New columns the matching needs (add the fields to the Java entities too):

| Table | Columns |
|---|---|
| `tasks` | `required_skills` (text), `work_mode` ONSITE/HYBRID/ONLINE, `commitment` FULL_TIME/PART_TIME/WEEKENDS/FLEXIBLE, `location`, `tier` (0 to 2) |
| `youth` | `open_to_remote`, `max_travel_km`, `interests` (comma-separated categories) |
| `smes` | `work_mode`, `location`, `turnover_band`, `registration_date`, `enterprise_status`, `financial_year_end` |

## What the data contains

50 youth, 15 SMEs, 3 funders (users with role CORPORATE), 150 tasks (100 verified, 8 in progress, 4 completed awaiting verification, 36 open, 2 cancelled), 100 verified experiences plus 4 pending. 3 alumni. Everything is fictional and Gauteng-based. Emails are `@example.com`. Registration numbers use a 99xxxx range so they can't be real. History is simulated in time order, so each verified task respected the tier rules when it was assigned.

## Assumptions to confirm

1. **Tier cut-offs.** The plan only says Tier 0 is 0 to 2 verified experiences. I assumed Tier 1 is 3 to 5 and Tier 2 is 6+. They are constants in `tiers.py`; change them to match Slot 1's tier service.
2. **No tier or application concept exists in the code yet.** `tier` on tasks is a new column.
3. **Distances are straight-line between area centres.** Roads are longer. Good enough for ranking.
4. **The readiness score and metrics are heuristics on synthetic data.** Don't present them as real outcomes.

## Impact metrics

Definitions are in `metrics.METRIC_DEFINITIONS`; `impact_report(dataset, now)` computes all three: time to first verified experience (median, with youth still waiting reported separately), the tier funnel, and placement rate. `readiness_score(profile, now)` returns a 0 to 100 score, a label, and next steps a coach agent could use.
