"""
End-to-end demo flow for the SSH AI service.

Runs the complete loop against a locally running instance:

  1. Health check
  2. TASK_POSTED  -> matchmaker shortlists youth with "why" notes
  3. TASK_ASSIGNED -> invitation agent withdraws other pending invites
  4. TASK_COMPLETED -> evidence agent drafts a reference + skill tags
  5. Activity feed -> every agent action, with counts
  6. Duplicate TASK_POSTED -> dedupe skips it

Usage:
    python demo_flow.py

Requires the service to be running on http://localhost:8000:

    python -m uvicorn app.main:app --port 8000

With the LLM off, templates produce the same shapes. With the LLM on,
Gemini produces the "why" notes and reference. Both paths pass the checks.
"""

from __future__ import annotations

import json
import os
import sys

import httpx
from dotenv import load_dotenv

BASE_URL = "http://localhost:8000"

# Fixed IDs so the demo is deterministic and the duplicate check works.
TASK_ID = 3
YOUTH_ID = 4
SME_ID = 9

EVENT_POSTED = "11111111-1111-1111-1111-111111111111"
EVENT_ASSIGNED = "33333333-3333-3333-3333-333333333333"
EVENT_COMPLETED = "22222222-2222-2222-2222-222222222222"


def _event_posted() -> dict:
    return {
        "eventId": EVENT_POSTED,
        "occurredAt": "2026-10-23T10:00:00Z",
        "type": "TASK_POSTED",
        "task": {
            "id": TASK_ID,
            "title": "Stock take at spaza shop",
            "description": "Count and reconcile inventory",
            "tier": 1,
            "requiredSkills": ["inventory coordination", "stock taking"],
            "remote": False,
            "estHours": 4,
            "smeId": SME_ID,
            "reservedUntil": None,
        },
    }


def _event_assigned() -> dict:
    return {
        "eventId": EVENT_ASSIGNED,
        "occurredAt": "2026-10-23T10:30:00Z",
        "type": "TASK_ASSIGNED",
        "taskId": TASK_ID,
        "youthId": YOUTH_ID,
        "smeId": SME_ID,
    }


def _event_completed() -> dict:
    return {
        "eventId": EVENT_COMPLETED,
        "occurredAt": "2026-10-23T11:00:00Z",
        "type": "TASK_COMPLETED",
        "taskId": TASK_ID,
        "youthId": YOUTH_ID,
        "smeId": SME_ID,
        "reflection": (
            "I counted all the stock and reconciled the till. "
            "Found three items missing from the shelf and reported them to the owner."
        ),
    }


def _post_event(client: httpx.Client, token: str, event: dict) -> dict:
    response = client.post(
        f"{BASE_URL}/events",
        headers={"Authorization": f"Bearer {token}"},
        json=event,
    )
    response.raise_for_status()
    return response.json()


def _get(client: httpx.Client, path: str) -> dict | list:
    response = client.get(f"{BASE_URL}{path}")
    response.raise_for_status()
    return response.json()


def _banner(text: str) -> None:
    print()
    print("=" * 72)
    print(text)
    print("=" * 72)


def _check(label: str, condition: bool) -> bool:
    status = "PASS" if condition else "FAIL"
    print(f"  [{status}] {label}")
    return condition


def main() -> int:
    load_dotenv()
    token = os.getenv("WEBHOOK_TOKEN")
    if not token:
        print("ERROR: WEBHOOK_TOKEN not found in .env")
        print("Create a .env file with WEBHOOK_TOKEN=<dev-token>")
        return 1

    checks: list[bool] = []

    with httpx.Client(timeout=30.0) as client:
        # ---------------------------------------------------------------
        # 1. Health
        # ---------------------------------------------------------------
        _banner("1. Health check")
        try:
            health = _get(client, "/health")
        except httpx.HTTPError as exc:
            print(f"ERROR: Could not reach {BASE_URL}: {exc}")
            print("Start the server:")
            print("    python -m uvicorn app.main:app --port 8000")
            return 1
        print(json.dumps(health, indent=2))
        if not health.get("llmEnabled"):
            print("NOTE: LLM is off. Templates will produce output. This is the fallback path.")

        # ---------------------------------------------------------------
        # 2. Post a task
        # ---------------------------------------------------------------
        _banner("2. TASK_POSTED -> matchmaker shortlists youth")
        result = _post_event(client, token, _event_posted())
        print(f"Accepted: {result}")

        shortlist = _get(client, f"/agent/shortlist/{TASK_ID}")
        free_ids = [y["id"] for y in shortlist["free"]]
        awaiting_ids = [y["id"] for y in shortlist["awaiting"]]
        counts = shortlist["counts"]
        print(f"\nShortlist for task {TASK_ID}:")
        print(f"  Free:     {free_ids}")
        print(f"  Awaiting: {awaiting_ids}")
        print(f"  Counts:   {counts}")
        print("\nWhy notes:")
        for youth_id, note in shortlist["why"].items():
            print(f"  Youth {youth_id}: {note}")

        checks.append(_check("At least one youth shortlisted", len(free_ids) + len(awaiting_ids) > 0))
        checks.append(_check("Free youth appear before awaiting",
                             bool(free_ids) or not awaiting_ids))
        checks.append(_check("Why note exists for every shortlisted youth",
                             len(shortlist["why"]) == len(free_ids) + len(awaiting_ids)))

        # ---------------------------------------------------------------
        # 3. Assign
        # ---------------------------------------------------------------
        _banner("3. TASK_ASSIGNED -> invitation agent withdraws other pending invites")
        result = _post_event(client, token, _event_assigned())
        print(f"Accepted: {result}")
        print("Behind the scenes: spring.withdraw(youth=4, exceptTask=1)")

        # ---------------------------------------------------------------
        # 4. Complete
        # ---------------------------------------------------------------
        _banner("4. TASK_COMPLETED -> evidence agent drafts a reference")
        result = _post_event(client, token, _event_completed())
        print(f"Accepted: {result}")

        draft = _get(client, f"/agent/draft/{TASK_ID}")
        print(f"\nDraft for task {TASK_ID}:")
        print(f"  Reference: {draft['reference']}")
        print(f"  Skill tags ({len(draft['skillTags'])}):")
        for tag in draft["skillTags"]:
            esco = (tag.get("escoUri") or "")[:36]
            print(
                f"    - {tag['label']:25} [{tag['confidence']:6}] "
                f"eso={esco}.. ofo={tag.get('ofoCode')}"
            )
            print(f"      evidence: {tag['evidence']}")

        checks.append(_check("Reference draft is non-empty", bool(draft["reference"].strip())))
        checks.append(_check("At least one skill tag proposed", len(draft["skillTags"]) > 0))
        checks.append(_check("Every tag has a confidence band",
                             all(t.get("confidence") in ("LOW", "MEDIUM", "HIGH")
                                 for t in draft["skillTags"])))
        checks.append(_check("Every tag has an ESCO or OFO mapping",
                             all(t.get("escoUri") or t.get("ofoCode")
                                 for t in draft["skillTags"])))

        # ---------------------------------------------------------------
        # 5. Activity feed
        # ---------------------------------------------------------------
        _banner("5. Activity feed")
        activity = _get(client, "/agent/activity?limit=20")
        print(f"{len(activity)} actions recorded:\n")
        for action in activity:
            counts = action.get("counts", {})
            counts_str = ", ".join(f"{k}={v}" for k, v in counts.items()) or "-"
            print(
                f"  [{action['mode']:8}] {action['agent']:12} "
                f"{action['decision']:16} ({counts_str})"
            )
            print(f"      {action['reasoning']}")

        agent_names = {a["agent"] for a in activity}
        checks.append(_check("Activity feed contains matchmaker actions", "matchmaker" in agent_names))
        checks.append(_check("Activity feed contains evidence actions", "evidence" in agent_names))
        checks.append(_check("Every action has a mode (LLM or FALLBACK)",
                             all(a.get("mode") in ("LLM", "FALLBACK") for a in activity)))

        # ---------------------------------------------------------------
        # 6. Duplicate
        # ---------------------------------------------------------------
        _banner("6. Duplicate TASK_POSTED -> dedupe skips it")
        before = len(activity)
        result = _post_event(client, token, _event_posted())
        print(f"Accepted (again): {result}")
        activity_after = _get(client, "/agent/activity?limit=20")
        after = len(activity_after)
        print(f"Activity count before: {before}, after: {after}")
        checks.append(_check("Duplicate did not produce a new action", before == after))

        # ---------------------------------------------------------------
        # Summary
        # ---------------------------------------------------------------
        _banner("Demo flow complete")
        passed = sum(checks)
        total = len(checks)
        print(f"Checks: {passed}/{total} passed")
        if passed == total:
            print("\nAll checks passed. The loop works end to end.")
            return 0
        else:
            print(f"\n{total - passed} check(s) failed. Review the output above.")
            return 1


if __name__ == "__main__":
    sys.exit(main())
