from __future__ import annotations

from datetime import datetime
from enum import StrEnum
from typing import Any, Literal
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel


class CamelModel(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        extra="forbid",
    )


# ---------------------------------------------------------------------------
# Enums
# ---------------------------------------------------------------------------

class Availability(StrEnum):
    FREE = "FREE"
    AWAITING_VERIFICATION = "AWAITING_VERIFICATION"
    BUSY = "BUSY"
    UNKNOWN = "UNKNOWN"


class ConfidenceBand(StrEnum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"


class SkillSource(StrEnum):
    AI_EXTRACTED = "AI_EXTRACTED"
    SELF_REPORTED = "SELF_REPORTED"


class AgentMode(StrEnum):
    LLM = "LLM"
    FALLBACK = "FALLBACK"


class EventType(StrEnum):
    TASK_POSTED = "TASK_POSTED"
    TASK_ASSIGNED = "TASK_ASSIGNED"
    TASK_COMPLETED = "TASK_COMPLETED"
    TASK_CANCELLED = "TASK_CANCELLED"
    EXPERIENCE_VERIFIED = "EXPERIENCE_VERIFIED"
    APPLICATION_SUBMITTED = "APPLICATION_SUBMITTED"
    YOUTH_IDLE = "YOUTH_IDLE"


# ---------------------------------------------------------------------------
# Shared shapes
# ---------------------------------------------------------------------------

class SkillTag(CamelModel):
    label: str = Field(min_length=1, max_length=120)
    esco_uri: str | None = Field(default=None, max_length=500)
    ofo_code: str | None = Field(default=None, max_length=20)
    ofo_label: str | None = Field(default=None, max_length=200)
    evidence: str = Field(min_length=1, max_length=500)
    confidence: ConfidenceBand
    reasons: list[str] = Field(default_factory=list, max_length=10)
    source: SkillSource
    sme_confirmed: bool | None = None


class AgentAction(CamelModel):
    agent: str = Field(min_length=1, max_length=64)
    trigger_event_id: UUID
    inputs_summary: str = Field(min_length=1, max_length=1000)
    decision: str = Field(min_length=1, max_length=200)
    reasoning: str = Field(min_length=1, max_length=2000)
    outputs: dict[str, Any] = Field(default_factory=dict)
    counts: dict[str, int] = Field(default_factory=dict)
    mode: AgentMode
    used_fallback: bool
    model_version: str | None = None
    latency_ms: int = Field(ge=0)


# ---------------------------------------------------------------------------
# Events from Spring
# ---------------------------------------------------------------------------

class TaskPayload(CamelModel):
    id: int
    title: str = Field(min_length=1, max_length=200)
    description: str = Field(min_length=1, max_length=5000)
    tier: int = Field(ge=0)
    required_skills: list[str] = Field(default_factory=list)
    remote: bool
    est_hours: float = Field(gt=0)
    sme_id: int
    reserved_until: datetime | None = None
    reason: str | None = None


class _BaseEvent(CamelModel):
    event_id: UUID
    occurred_at: datetime
    type: EventType


class TaskPostedEvent(_BaseEvent):
    type: Literal[EventType.TASK_POSTED]
    task: TaskPayload


class TaskAssignedEvent(_BaseEvent):
    type: Literal[EventType.TASK_ASSIGNED]
    task_id: int
    youth_id: int
    sme_id: int


class TaskCompletedEvent(_BaseEvent):
    type: Literal[EventType.TASK_COMPLETED]
    task_id: int
    youth_id: int
    sme_id: int
    reflection: str = Field(default="", max_length=5000)


class TaskCancelledEvent(_BaseEvent):
    type: Literal[EventType.TASK_CANCELLED]
    task_id: int
    youth_id: int


class ExperienceVerifiedEvent(_BaseEvent):
    type: Literal[EventType.EXPERIENCE_VERIFIED]
    task_id: int
    youth_id: int
    sme_id: int
    rating: int = Field(ge=1, le=5)
    sme_feedback: str = Field(default="", max_length=5000)
    reference: str = Field(default="", max_length=5000)
    confirmed_skills: list[str] = Field(default_factory=list)


class ApplicationSubmittedEvent(_BaseEvent):
    type: Literal[EventType.APPLICATION_SUBMITTED]
    task_id: int
    youth_id: int
    application_id: int


class YouthIdleEvent(_BaseEvent):
    type: Literal[EventType.YOUTH_IDLE]
    youth_id: int
    reason: str = Field(default="", max_length=200)


Event = (
    TaskPostedEvent
    | TaskAssignedEvent
    | TaskCompletedEvent
    | TaskCancelledEvent
    | ExperienceVerifiedEvent
    | ApplicationSubmittedEvent
    | YouthIdleEvent
)


# ---------------------------------------------------------------------------
# Spring response shapes
# ---------------------------------------------------------------------------

class YouthStatusResponse(CamelModel):
    availability: Availability
    active_task_id: int | None
    awaiting_verification_task_ids: list[int] = Field(default_factory=list)
    tier: int = Field(ge=0)
    verified_count: int = Field(ge=0)


class EligibleYouth(CamelModel):
    id: int
    skills: list[str] = Field(default_factory=list)
    tier: int = Field(ge=0)
    availability: Availability
    awaiting_verification_count: int = Field(ge=0, default=0)
    availability_window: str | None = None
    distance_bucket: str | None = None


# ---------------------------------------------------------------------------
# Spring request shapes
# ---------------------------------------------------------------------------

class InvitationRequest(CamelModel):
    task_id: int
    youth_id: int
    note: str = Field(default="", max_length=1000)


class WithdrawRequest(CamelModel):
    youth_id: int
    except_task_id: int | None = None


class ReferenceDraftRequest(CamelModel):
    task_id: int
    text: str = Field(min_length=1, max_length=5000)
    skills: list[SkillTag]


# ---------------------------------------------------------------------------
# Service endpoints
# ---------------------------------------------------------------------------

class ElicitRequest(CamelModel):
    youth_id: int
    text: str = Field(min_length=1, max_length=5000)


class ElicitResponse(CamelModel):
    elicitation_id: UUID
    skill_tags: list[SkillTag]


class SimilarityRequest(CamelModel):
    a_tags: list[str] = Field(min_length=1)
    b_tags: list[str] = Field(min_length=1)


class SimilarityMatch(CamelModel):
    a: str
    b: str
    score: float = Field(ge=0.0, le=1.0)


class SimilarityResponse(CamelModel):
    similarity: float = Field(ge=0.0, le=1.0)
    matches: list[SimilarityMatch] = Field(default_factory=list)


class ScoreApplicant(CamelModel):
    youth_id: int
    skills: list[str] = Field(default_factory=list)
    tier: int = Field(ge=0)
    availability: Availability
    distance_bucket: str | None = None


class ScoreRequest(CamelModel):
    task: TaskPayload
    applicants: list[ScoreApplicant] = Field(min_length=1)


class ScoredApplicant(CamelModel):
    youth_id: int
    score: float
    reasons: list[str] = Field(default_factory=list)


class ScoreResponse(CamelModel):
    ranked: list[ScoredApplicant]


# ---------------------------------------------------------------------------
# Read routes for Slot 5
# ---------------------------------------------------------------------------

class ShortlistResponse(CamelModel):
    task_id: int
    free: list[EligibleYouth] = Field(default_factory=list)
    awaiting: list[EligibleYouth] = Field(default_factory=list)
    why: dict[int, str] = Field(default_factory=dict)
    counts: dict[str, int] = Field(default_factory=dict)


class DraftResponse(CamelModel):
    task_id: int
    reference: str = ""
    skill_tags: list[SkillTag] = Field(default_factory=list)


# ---------------------------------------------------------------------------
# Health
# ---------------------------------------------------------------------------

class HealthResponse(CamelModel):
    status: Literal["ok"] = "ok"
    llm_enabled: bool
    spring_mock: bool
    version: str
