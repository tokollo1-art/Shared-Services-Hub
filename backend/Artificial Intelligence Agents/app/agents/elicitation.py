from __future__ import annotations

import logging
import time
from uuid import uuid4

from app.agents.base import AgentResult, BaseAgent
from app.audit import AuditLogger
from app.event_store import EventStore
from app.llm_client import LLMClient
from app.schemas import (
    AgentMode,
    ElicitResponse,
    Event,
    SkillSource,
)
from app.skills import confidence, mapper

logger = logging.getLogger(__name__)


class ElicitationAgent(BaseAgent):
    name = "elicitation"

    def __init__(
        self,
        *,
        event_store: EventStore,
        audit: AuditLogger,
        llm: LLMClient,
    ) -> None:
        super().__init__(event_store=event_store, audit=audit)
        self._llm = llm

    async def _handle(self, event: Event) -> AgentResult:
        return AgentResult(handled=False, skipped_reason="not_an_event_agent")

    async def elicit(
        self,
        youth_id: int,
        text: str,
    ) -> ElicitResponse:
        started = time.monotonic()

        raw_tags = await mapper.map_text_to_skills(
            text,
            llm=self._llm,
            source=SkillSource.SELF_REPORTED,
        )

        scored_tags = [
            confidence.score_tag(tag, required_skills=[]) for tag in raw_tags
        ]

        elicitation_id = uuid4()
        latency = int((time.monotonic() - started) * 1000)

        used_llm = any(
            tag.source == SkillSource.SELF_REPORTED and tag.confidence.value != "LOW"
            for tag in scored_tags
        )

        await self._audit.log(
            agent=self.name,
            trigger_event_id=elicitation_id,
            inputs_summary=f"elicitation youth={youth_id} text_len={len(text)}",
            decision="elicited",
            reasoning=f"Extracted {len(scored_tags)} self-reported tags",
            outputs={
                "elicitationId": str(elicitation_id),
                "youthId": youth_id,
                "skillTags": [t.model_dump(by_alias=True) for t in scored_tags],
            },
            counts={"tags": len(scored_tags)},
            mode=AgentMode.LLM if used_llm else AgentMode.FALLBACK,
            latency_ms=latency,
        )

        return ElicitResponse(
            elicitation_id=elicitation_id,
            skill_tags=scored_tags,
        )
