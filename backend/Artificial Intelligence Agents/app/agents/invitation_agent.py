from __future__ import annotations

import logging

from app.agents.base import AgentResult, BaseAgent
from app.audit import AuditLogger
from app.event_store import EventStore
from app.schemas import AgentMode, Event, TaskAssignedEvent, WithdrawRequest
from app.spring_client import SpringClient

logger = logging.getLogger(__name__)


class InvitationAgent(BaseAgent):
    name = "invitation"

    def __init__(
        self,
        *,
        event_store: EventStore,
        audit: AuditLogger,
        spring: SpringClient,
    ) -> None:
        super().__init__(event_store=event_store, audit=audit)
        self._spring = spring

    async def _handle(self, event: Event) -> AgentResult:
        if not isinstance(event, TaskAssignedEvent):
            return AgentResult(handled=False, skipped_reason="wrong_event_type")

        await self._spring.withdraw(
            WithdrawRequest(
                youth_id=event.youth_id,
                except_task_id=event.task_id,
            )
        )

        return AgentResult(
            handled=True,
            mode=AgentMode.FALLBACK,
            outputs={
                "withdrawnFor": event.youth_id,
                "exceptTaskId": event.task_id,
                "counts": {"withdrew": 1},
            },
        )
