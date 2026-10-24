from __future__ import annotations

import logging

from app.schemas import Availability
from app.spring_client import SpringClient

logger = logging.getLogger(__name__)


class AvailabilityGuard:
    def __init__(self, spring: SpringClient) -> None:
        self._spring = spring

    async def state(self, youth_id: int) -> Availability:
        """
        Read the youth's availability from Spring's /status endpoint.

        Never derives the value from raw IDs. Returns UNKNOWN on any
        failure so the caller can fail closed.
        """
        try:
            status = await self._spring.youth_status(youth_id)
        except Exception as exc:
            logger.warning(
                "availability.fetch_failed",
                extra={"youth_id": youth_id, "error": str(exc)},
            )
            return Availability.UNKNOWN

        value = status.availability
        if not isinstance(value, Availability):
            try:
                value = Availability(value)
            except ValueError:
                logger.warning(
                    "availability.unknown_value",
                    extra={"youth_id": youth_id, "value": str(value)},
                )
                return Availability.UNKNOWN
        return value

    async def is_invitable(self, youth_id: int) -> bool:
        state = await self.state(youth_id)
        return state in (Availability.FREE, Availability.AWAITING_VERIFICATION)
