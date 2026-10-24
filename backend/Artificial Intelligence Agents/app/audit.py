from __future__ import annotations

import logging
from typing import Any
from uuid import UUID

from app.schemas import AgentAction, AgentMode
from app.spring_client import SpringClient

logger = logging.getLogger(__name__)


class AuditLogger:
    def __init__(self, spring: SpringClient) -> None:
        self._spring = spring
        self._buffer: list[AgentAction] = []

    async def log(
        self,
        *,
        agent: str,
        trigger_event_id: UUID,
        inputs_summary: str,
        decision: str,
        reasoning: str,
        outputs: dict[str, Any] | None = None,
        counts: dict[str, int] | None = None,
        mode: AgentMode | str,
        used_fallback: bool | None = None,
        model_version: str | None = None,
        latency_ms: int,
    ) -> AgentAction:
        if used_fallback is None:
            used_fallback = AgentMode(mode) == AgentMode.FALLBACK
        action = AgentAction(
            agent=agent,
            trigger_event_id=trigger_event_id,
            inputs_summary=inputs_summary,
            decision=decision,
            reasoning=reasoning,
            outputs=outputs or {},
            counts=counts or {},
            mode=AgentMode(mode),
            used_fallback=used_fallback,
            model_version=model_version,
            latency_ms=latency_ms,
        )
        self._buffer.append(action)
        logger.info(
            "audit.%s",
            agent,
            extra={
                "event_id": str(trigger_event_id),
                "decision": decision,
                "mode": str(mode),
                "used_fallback": used_fallback,
                "model_version": model_version,
                "latency_ms": latency_ms,
                "counts": action.counts,
            },
        )
        idempotency_key = f"{trigger_event_id}:{agent}:{decision}"
        try:
            await self._spring.post_action(
                action.model_dump(by_alias=True, mode="json"),
                idempotency_key=idempotency_key,
            )
        except Exception as exc:
            logger.warning(
                "audit.post failed (kept in buffer): %s",
                exc,
                extra={"event_id": str(trigger_event_id)},
            )
        return action

    def recent(self, limit: int = 50) -> list[AgentAction]:
        return self._buffer[-limit:]
