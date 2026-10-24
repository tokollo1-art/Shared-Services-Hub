from __future__ import annotations

import logging
import time

from app.agents.base import AgentResult, BaseAgent
from app.audit import AuditLogger
from app.availability import AvailabilityGuard
from app.event_store import EventStore
from app.llm_client import LLMClient
from app.schemas import (
    AgentMode,
    Availability,
    EligibleYouth,
    Event,
    InvitationRequest,
    ShortlistResponse,
    TaskPostedEvent,
)
from app.spring_client import SpringClient, YouthBusyError
from app import templates

logger = logging.getLogger(__name__)

_MAX_SHORTLIST = 3


class MatchmakerAgent(BaseAgent):
    name = "matchmaker"

    def __init__(
        self,
        *,
        event_store: EventStore,
        audit: AuditLogger,
        spring: SpringClient,
        guard: AvailabilityGuard,
        llm: LLMClient,
        store: dict[int, ShortlistResponse],
    ) -> None:
        super().__init__(event_store=event_store, audit=audit)
        self._spring = spring
        self._guard = guard
        self._llm = llm
        self._store = store

    async def _handle(self, event: Event) -> AgentResult:
        if not isinstance(event, TaskPostedEvent):
            return AgentResult(handled=False, skipped_reason="wrong_event_type")

        started = time.monotonic()
        task = event.task
        used_llm = False

        eligible = await self._spring.eligible_youth(task.id)

        free: list[EligibleYouth] = []
        awaiting: list[EligibleYouth] = []
        skipped_busy: list[int] = []
        skipped_unknown: list[int] = []

        for youth in eligible:
            state = youth.availability
            if state == Availability.BUSY:
                skipped_busy.append(youth.id)
                continue
            if state == Availability.UNKNOWN:
                skipped_unknown.append(youth.id)
                continue
            if state == Availability.FREE:
                free.append(youth)
            elif state == Availability.AWAITING_VERIFICATION:
                awaiting.append(youth)

        ranked = free + awaiting
        shortlist = ranked[:_MAX_SHORTLIST]

        why_notes: dict[int, str] = {}
        for youth in shortlist:
            fallback_note = templates.why_note(
                task_title=task.title,
                required_skills=task.required_skills,
                youth_skills=youth.skills,
                availability=youth.availability.value,
                tier=youth.tier,
                distance_bucket=youth.distance_bucket,
            )
            prompt = (
                f"Write one short sentence explaining why this youth is a good match "
                f"for the task. Do not include names.\n"
                f"Task: {task.title}\n"
                f"Required skills: {', '.join(task.required_skills)}\n"
                f"Youth skills: {', '.join(youth.skills)}\n"
                f"Availability: {youth.availability.value}\n"
                f"Tier: {youth.tier}"
            )
            result = await self._llm.call(
                prompt=prompt,
                fallback=lambda fb=fallback_note: {"why": fb},
                schema_hint='{"why": "<one sentence>"}',
            )
            why_notes[youth.id] = str(result.data.get("why", fallback_note)).strip()
            if result.mode == "LLM":
                used_llm = True

        counts = {
            "free": len(free),
            "awaiting": len(awaiting),
            "busySkipped": len(skipped_busy),
        }

        self._store[task.id] = ShortlistResponse(
            task_id=task.id,
            free=free[: len(shortlist)],
            awaiting=awaiting[: max(0, len(shortlist) - len(free))],
            why=why_notes,
            counts=counts,
        )

        if not shortlist:
            return AgentResult(
                handled=True,
                mode=AgentMode.FALLBACK,
                skipped_reason="no_eligible_youth",
                outputs={
                    "shortlisted": [],
                    "invited": [],
                    "skippedBusy": skipped_busy,
                    "counts": counts,
                },
            )

        invited: list[int] = []
        blocked: list[int] = []
        for youth in shortlist:
            if not await self._guard.is_invitable(youth.id):
                blocked.append(youth.id)
                logger.info(
                    "matchmaker.invite_blocked",
                    extra={
                        "task_id": task.id,
                        "youth_id": youth.id,
                        "reason": "availability changed",
                    },
                )
                continue
            try:
                await self._spring.invite(
                    InvitationRequest(
                        task_id=task.id,
                        youth_id=youth.id,
                        note=why_notes[youth.id],
                    )
                )
                invited.append(youth.id)
            except YouthBusyError:
                blocked.append(youth.id)
                logger.info(
                    "matchmaker.invite_blocked",
                    extra={
                        "task_id": task.id,
                        "youth_id": youth.id,
                        "reason": "youth busy",
                    },
                )

        latency = int((time.monotonic() - started) * 1000)
        return AgentResult(
            handled=True,
            mode=AgentMode.LLM if used_llm else AgentMode.FALLBACK,
            model_version=self._llm.model_name if used_llm else None,
            outputs={
                "shortlisted": [y.id for y in shortlist],
                "invited": invited,
                "blocked": blocked,
                "skippedBusy": skipped_busy,
                "counts": counts,
            },
        )
