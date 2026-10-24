from __future__ import annotations

from typing import Any


def why_note(
    task_title: str,
    required_skills: list[str],
    youth_skills: list[str],
    availability: str,
    tier: int,
    distance_bucket: str | None = None,
) -> str:
    """
    Deterministic explanation of why a youth was shortlisted.

    No names, no PII. Uses only skills, availability, tier, and distance.
    """
    matched = [s for s in required_skills if s in youth_skills]
    if matched:
        skill_part = f"matched on {', '.join(matched[:2])}"
    else:
        skill_part = f"has {', '.join(youth_skills[:2])}" if youth_skills else "has relevant skills"

    availability_part = {
        "FREE": "free now",
        "AWAITING_VERIFICATION": "awaiting verification",
        "BUSY": "busy",
    }.get(availability, "available")

    tier_part = f"Tier {tier}"
    distance_part = f", {distance_bucket}" if distance_bucket else ""

    return (
        f"Tier {tier} · {availability_part} · {skill_part}{distance_part}. "
        f"Task: {task_title}."
    )


def reference_draft(
    task_title: str,
    required_skills: list[str],
    youth_skills: list[str],
    tier: int,
) -> str:
    """
    Deterministic reference draft for a completed task.

    No names. Describes the task and the skills evidenced, not the person.
    """
    matched = [s for s in required_skills if s in youth_skills]
    skills_line = (
        f"Skills demonstrated: {', '.join(matched)}."
        if matched
        else f"Required skills: {', '.join(required_skills)}."
    )
    return (
        f"Completed a Tier {tier} task: {task_title}. "
        f"{skills_line} "
        f"Work was submitted and verified through SSH."
    )


def skill_confidence_reason(
    skill_label: str,
    matched_requirements: list[str],
    llm_judgement: str,
) -> str:
    """
    Deterministic reason string for a skill tag's confidence band.
    """
    if matched_requirements:
        return (
            f"'{skill_label}' matches task requirement(s): "
            f"{', '.join(matched_requirements[:3])}. "
            f"AI judgement: {llm_judgement}."
        )
    return f"'{skill_label}' inferred from task description. AI judgement: {llm_judgement}."


def coach_nudge(
    youth_tier: int,
    verified_count: int,
    tasks_to_next_tier: int,
    next_task_title: str | None,
) -> str:
    """
    Deterministic encouragement for a youth. Never names a person.
    """
    if tasks_to_next_tier <= 0:
        return "You're at the highest tier. Keep applying to stay active."
    if tasks_to_next_tier == 1:
        base = "You're one verified task away from the next tier."
    else:
        base = f"You're {tasks_to_next_tier} verified tasks away from the next tier."
    if next_task_title:
        base += f" Apply to: {next_task_title}."
    else:
        base += " Browse open tasks to get started."
    return base


def coach_awaiting_message(verified_count: int) -> str:
    return (
        f"You have {verified_count} completed task(s) awaiting verification. "
        "You can still apply — you'll rank after free youth. "
        "Verification unlocks your next tier."
    )


def coach_busy_message() -> str:
    return (
        "You have an active task. Finish it first — "
        "a completed task is worth more than three applications."
    )


def proposed_task_reason(task_title: str, tier: int) -> str:
    return (
        f"Proposed from an open SME request: '{task_title}'. "
        f"Tier {tier}. Awaiting admin approval before posting."
    )


def availability_unknown_message(youth_id: int) -> str:
    return (
        f"Could not verify availability for youth {youth_id}. "
        "Skipping invite to avoid double-booking."
    )


def skipped_busy_message(youth_id: int, task_id: int) -> str:
    return (
        f"Skipped youth {youth_id} for task {task_id}: "
        "youth became busy during shortlist."
    )


def llm_fallback_marker() -> str:
    """
    Appended to LLM-generated text when the LLM was not available.
    Used by the audit feed to distinguish LLM output from templates.
    """
    return ""


TEMPLATES: dict[str, Any] = {
    "why_note": why_note,
    "reference_draft": reference_draft,
    "skill_confidence_reason": skill_confidence_reason,
    "coach_nudge": coach_nudge,
    "coach_awaiting_message": coach_awaiting_message,
    "coach_busy_message": coach_busy_message,
    "proposed_task_reason": proposed_task_reason,
    "availability_unknown_message": availability_unknown_message,
    "skipped_busy_message": skipped_busy_message,
}
