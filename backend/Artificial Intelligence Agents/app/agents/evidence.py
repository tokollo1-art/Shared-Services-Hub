from __future__ import annotations

import logging
import time

from app.agents.base import AgentResult, BaseAgent
from app.audit import AuditLogger
from app.event_store import EventStore
from app.llm_client import LLMClient
from app.schemas import (
    AgentMode,
    DraftResponse,
    Event,
    ReferenceDraftRequest,
    SkillSource,
    TaskCompletedEvent,
)
from app.skills import confidence, mapper
from app.spring_client import SpringClient
from app import templates

logger = logging.getLogger(__name__)


class EvidenceAgent(BaseAgent):
    name = "evidence"

    def __init__(
        self,
        *,
        event_store: EventStore,
        audit: AuditLogger,
        spring: SpringClient,
        llm: LLMClient,
        store: dict[int, DraftResponse],
    ) -> None:
        super().__init__(event_store=event_store, audit=audit)
        self._spring = spring
        self._llm = llm
        self._store = store

    async def _handle(self, event: Event) -> AgentResult:
        if not isinstance(event, TaskCompletedEvent):
            return AgentResult(handled=False, skipped_reason="wrong_event_type")

        started = time.monotonic()
        used_llm = False

        task = await self._spring.get_task(event.task_id)
        required_skills = task.required_skills
        title = task.title

        raw_tags = await mapper.map_text_to_skills(
            event.reflection,
            llm=self._llm,
            source=SkillSource.AI_EXTRACTED,
        )
        used_llm = used_llm or any(
            t.source == SkillSource.AI_EXTRACTED for t in raw_tags
        )

        if not raw_tags:
            raw_tags = self._fallback_tags(required_skills)

        scored_tags = [
            confidence.score_tag(tag, required_skills) for tag in raw_tags
        ]

        prompt = (
            f"Write a one-paragraph reference for a youth who completed this task.\n"
            f"Task: {title}\n"
            f"Required skills: {', '.join(required_skills)}\n"
            f"Their reflection: {event.reflection}\n"
            f"Skills identified: {', '.join(t.label for t in scored_tags)}\n"
            f"Do not include names. Use passive voice."
        )
        llm_result = await self._llm.call(
            prompt=prompt,
            fallback=lambda: {
                "reference": templates.reference_draft(
                    task_title=title,
                    required_skills=required_skills,
                    youth_skills=[t.label for t in scored_tags],
                    tier=task.tier,
                )
            },
            schema_hint='{"reference": "<one paragraph>"}',
        )
        reference_text = str(llm_result.data.get("reference", "")).strip()
        if not reference_text:
            reference_text = templates.reference_draft(
                task_title=title,
                required_skills=required_skills,
                youth_skills=[t.label for t in scored_tags],
                tier=task.tier,
            )
        used_llm = used_llm or llm_result.mode == "LLM"

        self._store[task.id] = DraftResponse(
            task_id=task.id,
            reference=reference_text,
            skill_tags=scored_tags,
        )

        await self._spring.save_draft(
            ReferenceDraftRequest(
                task_id=event.task_id,
                text=reference_text,
                skills=scored_tags,
            )
        )

        latency = int((time.monotonic() - started) * 1000)
        return AgentResult(
            handled=True,
            mode=AgentMode.LLM if used_llm else AgentMode.FALLBACK,
            model_version=self._llm.model_name if used_llm else None,
            outputs={
                "referenceDraft": reference_text,
                "skillTags": [t.model_dump(by_alias=True) for t in scored_tags],
                "counts": {
                    "tagsProposed": len(scored_tags),
                    "tagsHigh": sum(
                        1 for t in scored_tags
                        if t.confidence.value == "HIGH"
                    ),
                },
            },
        )

    def _fallback_tags(self, required_skills: list[str]):
        tags = []
        for skill in required_skills:
            tag = mapper.make_tag(
                skill,
                evidence="Required for this task",
                source=SkillSource.AI_EXTRACTED,
                reasons=["required skill for the task"],
            )
            if tag is not None:
                tags.append(tag)
        return tags
