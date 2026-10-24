from __future__ import annotations

import logging

from app.schemas import ConfidenceBand, SkillTag

logger = logging.getLogger(__name__)


def score_tag(
    tag: SkillTag,
    required_skills: list[str],
) -> SkillTag:
    """
    Refine a SkillTag's provisional confidence band.

    Two signals:
    1. Requirement overlap — does the tag's label appear in the task's
       required_skills?
    2. Evidence strength — how directly does the tag's evidence quote
       support the label?

    High only when both signals agree. Otherwise the lower of the two.
    The SME's rating is NOT used here; it arrives at verification and is
    stored separately on the tag as sme_confirmed.
    """
    required_lower = {s.strip().lower() for s in required_skills}
    label_lower = tag.label.strip().lower()

    overlap_band = _overlap_band(label_lower, required_lower)
    evidence_band = _evidence_band(tag.evidence, label_lower)

    final_band = _combine(overlap_band, evidence_band)

    updated = tag.model_copy(
        update={
            "confidence": final_band,
            "reasons": _build_reasons(
                tag.reasons,
                overlap_band,
                evidence_band,
                label_lower,
                required_lower,
            ),
        }
    )
    logger.debug(
        "confidence.score",
        extra={
            "label": label_lower,
            "overlap": overlap_band.value,
            "evidence": evidence_band.value,
            "final": final_band.value,
        },
    )
    return updated


def _overlap_band(label: str, required_lower: set[str]) -> ConfidenceBand:
    if label in required_lower:
        return ConfidenceBand.HIGH
    if any(label in req or req in label for req in required_lower):
        return ConfidenceBand.MEDIUM
    return ConfidenceBand.LOW


def _evidence_band(evidence: str, label: str) -> ConfidenceBand:
    """
    Heuristic evidence strength from the evidence quote alone.

    This is deliberately simple: the LLM already judged the evidence
    when it produced it. This layer catches obvious weak cases — empty
    evidence, evidence that doesn't mention any word from the label, or
    evidence that is a single word.

    A future upgrade replaces this with an embedding similarity between
    the evidence and the label. The function signature does not change.
    """
    text = evidence.strip().lower()
    if not text:
        return ConfidenceBand.LOW
    if len(text.split()) < 3:
        return ConfidenceBand.LOW

    label_words = {w for w in label.split() if len(w) > 3}
    if not label_words:
        return ConfidenceBand.MEDIUM
    if label_words & set(text.split()):
        return ConfidenceBand.HIGH
    return ConfidenceBand.MEDIUM


def _combine(a: ConfidenceBand, b: ConfidenceBand) -> ConfidenceBand:
    """
    High only when both agree. Otherwise the lower band.

    Order: LOW < MEDIUM < HIGH.
    """
    order = {
        ConfidenceBand.LOW: 0,
        ConfidenceBand.MEDIUM: 1,
        ConfidenceBand.HIGH: 2,
    }
    if a == ConfidenceBand.HIGH and b == ConfidenceBand.HIGH:
        return ConfidenceBand.HIGH
    return min((a, b), key=lambda x: order[x])


def _build_reasons(
    existing: list[str],
    overlap_band: ConfidenceBand,
    evidence_band: ConfidenceBand,
    label: str,
    required_lower: set[str],
) -> list[str]:
    reasons = list(existing)
    if not required_lower:
        reasons.append(f"'{label}' is self-reported; pending verification")
    elif overlap_band == ConfidenceBand.HIGH:
        reasons.append(f"'{label}' is a required skill for this task")
    elif overlap_band == ConfidenceBand.MEDIUM:
        reasons.append(f"'{label}' partially overlaps with task requirements")
    else:
        reasons.append(f"'{label}' is not in the task's required skills")
    if evidence_band == ConfidenceBand.HIGH:
        reasons.append("evidence quote directly supports the skill")
    elif evidence_band == ConfidenceBand.MEDIUM:
        reasons.append("evidence quote is related but indirect")
    else:
        reasons.append("evidence quote is weak or missing")

    return reasons[:10]
