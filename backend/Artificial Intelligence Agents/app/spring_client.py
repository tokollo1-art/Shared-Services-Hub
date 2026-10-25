from __future__ import annotations

from typing import Any

import httpx

from app.config import Settings
from app.schemas import (
    EligibleYouth,
    InvitationRequest,
    ReferenceDraftRequest,
    TaskPayload,
    WithdrawRequest,
    YouthStatusResponse,
)


class SpringError(Exception):
    def __init__(self, status: int, code: str, message: str) -> None:
        self.status = status
        self.code = code
        self.message = message
        super().__init__(f"Spring {status} {code}: {message}")


class YouthBusyError(SpringError):
    def __init__(self, message: str = "Youth is busy") -> None:
        super().__init__(409, "YOUTH_BUSY", message)


class NotEligibleError(SpringError):
    def __init__(self, message: str = "Youth is not eligible") -> None:
        super().__init__(409, "NOT_ELIGIBLE", message)


class SpringClient:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings
        self._mock = settings.mock_spring
        self._base = settings.spring_base_url.rstrip("/")
        self._headers = {
            "Authorization": f"Bearer {settings.agent_token.get_secret_value()}",
            "Content-Type": "application/json",
            "Accept": "application/json",
        }

    async def eligible_youth(self, task_id: int) -> list[EligibleYouth]:
        if self._mock:
            return self._mock_eligible_youth(task_id)
        body = await self._get(f"/api/v1/agent/tasks/{task_id}/eligible-youth")
        return [EligibleYouth.model_validate(item) for item in body]

    async def youth_status(self, youth_id: int) -> YouthStatusResponse:
        if self._mock:
            return self._mock_youth_status(youth_id)
        body = await self._get(f"/api/v1/agent/youth/{youth_id}/status")
        return YouthStatusResponse.model_validate(body)

    async def get_task(self, task_id: int) -> TaskPayload:
        if self._mock:
            return self._mock_task(task_id)
        body = await self._get(f"/api/v1/agent/tasks/{task_id}")
        return TaskPayload.model_validate(body)

    async def invite(self, request: InvitationRequest) -> None:
        if self._mock:
            return
        await self._post(
            "/api/v1/agent/invitations",
            request.model_dump(by_alias=True, mode="json"),
            allow_busy=True,
        )

    async def withdraw(self, request: WithdrawRequest) -> None:
        if self._mock:
            return
        await self._post(
            "/api/v1/agent/invitations/withdraw",
            request.model_dump(by_alias=True, mode="json"),
        )

    async def save_draft(self, request: ReferenceDraftRequest) -> None:
        if self._mock:
            return
        await self._post(
            "/api/v1/agent/drafts/reference",
            request.model_dump(by_alias=True, mode="json"),
        )

    async def post_action(
        self,
        action: dict[str, Any],
        *,
        idempotency_key: str | None = None,
    ) -> None:
        if self._mock:
            return
        headers: dict[str, str] = {}
        if idempotency_key:
            headers["Idempotency-Key"] = idempotency_key
        await self._post(
            "/api/v1/agent/actions",
            action,
            extra_headers=headers,
        )

    async def _get(self, path: str) -> Any:
        async with httpx.AsyncClient(
            base_url=self._base,
            headers=self._headers,
            timeout=self._settings.http_timeout_seconds,
        ) as client:
            response = await client.get(path)
        return self._handle(response)

    async def _post(
        self,
        path: str,
        body: dict[str, Any],
        allow_busy: bool = False,
        extra_headers: dict[str, str] | None = None,
    ) -> Any:
        headers = {**self._headers, **(extra_headers or {})}
        async with httpx.AsyncClient(
            base_url=self._base,
            headers=headers,
            timeout=self._settings.http_timeout_seconds,
        ) as client:
            response = await client.post(path, json=body)
        return self._handle(response, allow_busy=allow_busy)

    @staticmethod
    def _handle(response: httpx.Response, allow_busy: bool = False) -> Any:
        if response.is_success:
            if response.content:
                return response.json()
            return None
        code = ""
        message = response.text
        try:
            payload = response.json()
            code = payload.get("code", "")
            message = payload.get("message", message)
        except Exception:
            pass
        if response.status_code == 409 and code == "YOUTH_BUSY":
            raise YouthBusyError(message)
        if response.status_code == 409 and code == "NOT_ELIGIBLE":
            raise NotEligibleError(message)
        raise SpringError(response.status_code, code or "UNKNOWN", message)

    def _mock_eligible_youth(self, task_id: int) -> list[EligibleYouth]:
        return [
            EligibleYouth(
                id=4,
                skills=["inventory coordination", "customer service"],
                tier=1,
                availability="FREE",
                awaiting_verification_count=0,
                availability_window="weekday mornings",
                distance_bucket="near",
            ),
            EligibleYouth(
                id=7,
                skills=["data entry", "inventory coordination"],
                tier=1,
                availability="AWAITING_VERIFICATION",
                awaiting_verification_count=1,
                availability_window="weekday afternoons",
                distance_bucket="near",
            ),
            EligibleYouth(
                id=9,
                skills=["customer service", "stock taking"],
                tier=0,
                availability="FREE",
                awaiting_verification_count=0,
                availability_window="weekends",
                distance_bucket="far",
            ),
        ]

    def _mock_youth_status(self, youth_id: int) -> YouthStatusResponse:
        if youth_id == 4:
            return YouthStatusResponse(
                availability="FREE",
                active_task_id=None,
                awaiting_verification_task_ids=[],
                tier=1,
                verified_count=3,
            )
        if youth_id == 7:
            return YouthStatusResponse(
                availability="AWAITING_VERIFICATION",
                active_task_id=None,
                awaiting_verification_task_ids=[2],
                tier=1,
                verified_count=2,
            )
        return YouthStatusResponse(
            availability="FREE",
            active_task_id=None,
            awaiting_verification_task_ids=[],
            tier=0,
            verified_count=0,
        )

    def _mock_task(self, task_id: int) -> TaskPayload:
        return TaskPayload(
            id=task_id,
            title="Stock take at spaza shop",
            description="Count and reconcile inventory",
            tier=1,
            required_skills=["inventory coordination", "stock taking"],
            remote=False,
            est_hours=4.0,
            sme_id=9,
            reserved_until=None,
        )
