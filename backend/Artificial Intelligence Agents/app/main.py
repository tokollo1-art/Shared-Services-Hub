from __future__ import annotations

import logging
from contextlib import asynccontextmanager
from typing import AsyncIterator

from fastapi import FastAPI, Header, HTTPException, Request, status
from fastapi.responses import JSONResponse
from pydantic import TypeAdapter, ValidationError

from app.agents.elicitation import ElicitationAgent
from app.agents.evidence import EvidenceAgent
from app.agents.invitation_agent import InvitationAgent
from app.agents.matchmaker import MatchmakerAgent
from app.audit import AuditLogger
from app.availability import AvailabilityGuard
from app.config import Settings, get_settings
from app.event_store import build_event_store
from app.llm_client import LLMClient
from app.schemas import (
    AgentAction,
    ApplicationSubmittedEvent,
    DraftResponse,
    ElicitRequest,
    ElicitResponse,
    Event,
    EventType,
    ExperienceVerifiedEvent,
    HealthResponse,
    ShortlistResponse,
    TaskAssignedEvent,
    TaskCancelledEvent,
    TaskCompletedEvent,
    TaskPostedEvent,
    YouthIdleEvent,
)
from app.spring_client import SpringClient
from app.skills.similarity import skill_similarity
from app.schemas import (
    ScoreRequest,
    ScoreResponse,
    ScoredApplicant,
    SimilarityRequest,
    SimilarityResponse,
)

logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    settings = get_settings()
    app.state.settings = settings
    app.state.spring = SpringClient(settings)
    app.state.event_store = build_event_store(
        settings.event_store_backend,
        settings.event_store_path,
    )
    app.state.audit = AuditLogger(app.state.spring)
    app.state.llm = LLMClient(settings)
    app.state.guard = AvailabilityGuard(app.state.spring)
    app.state.shortlists: dict[int, ShortlistResponse] = {}
    app.state.drafts: dict[int, DraftResponse] = {}

    app.state.matchmaker = MatchmakerAgent(
        event_store=app.state.event_store,
        audit=app.state.audit,
        spring=app.state.spring,
        guard=app.state.guard,
        llm=app.state.llm,
        store=app.state.shortlists,
    )
    app.state.evidence = EvidenceAgent(
        event_store=app.state.event_store,
        audit=app.state.audit,
        spring=app.state.spring,
        llm=app.state.llm,
        store=app.state.drafts,
    )
    app.state.invitation = InvitationAgent(
        event_store=app.state.event_store,
        audit=app.state.audit,
        spring=app.state.spring,
    )
    app.state.elicitation = ElicitationAgent(
        event_store=app.state.event_store,
        audit=app.state.audit,
        llm=app.state.llm,
    )

    logger.info(
        "SSH AI service starting: spring_mock=%s llm_enabled=%s event_store=%s",
        settings.mock_spring,
        settings.llm_enabled,
        settings.event_store_backend,
    )
    yield
    app.state.event_store.close()
    logger.info("SSH AI service shutting down")


app = FastAPI(
    title="SSH AI Service",
    version="0.1.0",
    lifespan=lifespan,
)


def _check_webhook_token(authorization: str | None, settings: Settings) -> None:
    if authorization is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing Authorization header",
        )
    expected = f"Bearer {settings.webhook_token.get_secret_value()}"
    if authorization != expected:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Invalid webhook token",
        )


def _parse_event(raw: object) -> Event:
    if not isinstance(raw, dict):
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Event body must be a JSON object",
        )
    event_type = raw.get("type")
    if event_type not in {e.value for e in EventType}:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=f"Unknown event type: {event_type!r}",
        )
    adapter = TypeAdapter(Event)
    try:
        return adapter.validate_python(raw)
    except ValidationError as exc:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=exc.errors(),
        )


async def _dispatch(event: Event) -> None:
    if isinstance(event, TaskPostedEvent):
        logger.info(
            "dispatch.TASK_POSTED",
            extra={
                "event_id": str(event.event_id),
                "task_id": event.task.id,
                "title": event.task.title,
            },
        )
        await app.state.matchmaker.run(event)
    elif isinstance(event, TaskAssignedEvent):
        logger.info(
            "dispatch.TASK_ASSIGNED",
            extra={
                "event_id": str(event.event_id),
                "task_id": event.task_id,
                "youth_id": event.youth_id,
            },
        )
        await app.state.invitation.run(event)
    elif isinstance(event, TaskCompletedEvent):
        logger.info(
            "dispatch.TASK_COMPLETED",
            extra={
                "event_id": str(event.event_id),
                "task_id": event.task_id,
                "youth_id": event.youth_id,
                "reflection_len": len(event.reflection),
            },
        )
        await app.state.evidence.run(event)
    elif isinstance(event, TaskCancelledEvent):
        logger.info(
            "dispatch.TASK_CANCELLED",
            extra={
                "event_id": str(event.event_id),
                "task_id": event.task_id,
                "youth_id": event.youth_id,
            },
        )
    elif isinstance(event, ExperienceVerifiedEvent):
        logger.info(
            "dispatch.EXPERIENCE_VERIFIED",
            extra={
                "event_id": str(event.event_id),
                "task_id": event.task_id,
                "youth_id": event.youth_id,
                "rating": event.rating,
            },
        )
    elif isinstance(event, ApplicationSubmittedEvent):
        logger.info(
            "dispatch.APPLICATION_SUBMITTED",
            extra={
                "event_id": str(event.event_id),
                "task_id": event.task_id,
                "youth_id": event.youth_id,
                "application_id": event.application_id,
            },
        )
    elif isinstance(event, YouthIdleEvent):
        logger.info(
            "dispatch.YOUTH_IDLE",
            extra={
                "event_id": str(event.event_id),
                "youth_id": event.youth_id,
                "reason": event.reason,
            },
        )


@app.get("/health", response_model=HealthResponse)
async def health() -> HealthResponse:
    settings: Settings = app.state.settings
    return HealthResponse(
        llm_enabled=settings.llm_enabled,
        spring_mock=settings.mock_spring,
        version=app.version,
    )


@app.post("/events", status_code=status.HTTP_200_OK)
async def receive_event(
    request: Request,
    authorization: str | None = Header(default=None),
) -> JSONResponse:
    settings: Settings = app.state.settings
    _check_webhook_token(authorization, settings)

    raw = await request.json()
    event = _parse_event(raw)

    await _dispatch(event)

    return JSONResponse(
        status_code=status.HTTP_200_OK,
        content={"eventId": str(event.event_id), "type": event.type.value},
    )


@app.get("/agent/activity", response_model=list[AgentAction])
async def agent_activity(limit: int = 50) -> list[AgentAction]:
    return app.state.audit.recent(limit=limit)


@app.get("/agent/shortlist/{task_id}", response_model=ShortlistResponse)
async def agent_shortlist(task_id: int) -> ShortlistResponse:
    stored = app.state.shortlists.get(task_id)
    if stored is None:
        return ShortlistResponse(task_id=task_id)
    return stored


@app.get("/agent/draft/{task_id}", response_model=DraftResponse)
async def agent_draft(task_id: int) -> DraftResponse:
    stored = app.state.drafts.get(task_id)
    if stored is None:
        return DraftResponse(task_id=task_id)
    return stored


@app.post("/elicit", response_model=ElicitResponse)
async def elicit(body: ElicitRequest) -> ElicitResponse:
    return await app.state.elicitation.elicit(
        youth_id=body.youth_id,
        text=body.text,
    )


@app.post("/skills/similarity", response_model=SimilarityResponse)
async def skills_similarity(body: SimilarityRequest) -> SimilarityResponse:
    return skill_similarity(body.a_tags, body.b_tags)


@app.post("/score", response_model=ScoreResponse)
async def score_applicants(body: ScoreRequest) -> ScoreResponse:
    ranked: list[ScoredApplicant] = []
    for applicant in body.applicants:
        result = skill_similarity(
            body.task.required_skills,
            applicant.skills,
        )
        reasons: list[str] = []
        if result.matches:
            best = max(result.matches, key=lambda m: m.score)
            reasons.append(
                f"matched '{best.a}' to '{best.b}' at {best.score:.2f}"
            )
        reasons.append(
            f"availability: {applicant.availability.value}, tier: {applicant.tier}"
        )
        ranked.append(
            ScoredApplicant(
                youth_id=applicant.youth_id,
                score=result.similarity,
                reasons=reasons,
            )
        )
    ranked.sort(key=lambda x: x.score, reverse=True)
    return ScoreResponse(ranked=ranked)
