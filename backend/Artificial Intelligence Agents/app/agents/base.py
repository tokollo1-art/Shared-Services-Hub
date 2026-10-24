from __future__ import annotations

import logging
import time
from abc import ABC, abstractmethod
from typing import Any

from app.audit import AuditLogger
from app.event_store import EventStore
from app.schemas import AgentMode, Event

logger = logging.getLogger(__name__)


class AgentResult:
    def __init__(
        self,
        *,
        handled: bool,
        skipped_reason: str | None = None,
        outputs: dict[str, Any] | None = None,
        mode: AgentMode = AgentMode.FALLBACK,
        model_version: str | None = None,
    ) -> None:
        self.handled = handled
        self.skipped_reason = skipped_reason
        self.outputs = outputs or {}
        self.mode = mode
        self.model_version = model_version


class BaseAgent(ABC):
    name: str = "agent"

    def __init__(
        self,
        *,
        event_store: EventStore,
        audit: AuditLogger,
    ) -> None:
        self._event_store = event_store
        self._audit = audit

    @abstractmethod
    async def _handle(self, event: Event) -> AgentResult:
        """Return an AgentResult. Raising is allowed; BaseAgent will catch and audit."""

    async def run(self, event: Event) -> AgentResult:
        started = time.monotonic()
        if self._event_store.seen(event.event_id):
            logger.info(
                "agent.skip.duplicate",
                extra={"agent": self.name, "event_id": str(event.event_id)},
            )
            return AgentResult(handled=False, skipped_reason="duplicate")

        try:
            result = await self._handle(event)
        except Exception as exc:
            latency = _elapsed_ms(started)
            logger.exception(
                "agent.error",
                extra={"agent": self.name, "event_id": str(event.event_id)},
            )
            await self._audit.log(
                agent=self.name,
                trigger_event_id=event.event_id,
                inputs_summary=_summarize(event),
                decision="error",
                reasoning=f"{type(exc).__name__}: {exc}",
                outputs={},
                counts={},
                mode=AgentMode.FALLBACK,
                used_fallback=True,
                model_version=None,
                latency_ms=latency,
            )
            return AgentResult(handled=False, skipped_reason="error")

        latency = _elapsed_ms(started)
        await self._audit.log(
            agent=self.name,
            trigger_event_id=event.event_id,
            inputs_summary=_summarize(event),
            decision="handled" if result.handled else (result.skipped_reason or "skipped"),
            reasoning=result.skipped_reason or f"{self.name} completed",
            outputs=result.outputs,
            counts=_extract_counts(result.outputs),
            mode=result.mode,
            used_fallback=(result.mode == AgentMode.FALLBACK),
            model_version=result.model_version,
            latency_ms=latency,
        )

        if result.handled:
            self._event_store.mark(event.event_id)
        return result


def _summarize(event: Event) -> str:
    etype = event.type.value
    if hasattr(event, "task") and hasattr(event.task, "id"):
        return f"{etype} task={event.task.id}"
    if hasattr(event, "task_id"):
        return f"{etype} task={event.task_id} youth={getattr(event, 'youth_id', '?')}"
    return etype


def _extract_counts(outputs: dict[str, Any]) -> dict[str, int]:
    counts = outputs.get("counts")
    if isinstance(counts, dict) and all(isinstance(v, int) for v in counts.values()):
        return counts
    return {}


def _elapsed_ms(started: float) -> int:
    return int((time.monotonic() - started) * 1000)
