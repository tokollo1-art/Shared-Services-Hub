from __future__ import annotations

import json
import logging
from pathlib import Path
from typing import Any

from app.llm_client import LLMClient
from app.schemas import ConfidenceBand, SkillSource, SkillTag

logger = logging.getLogger(__name__)

_SKILLS_PATH = Path(__file__).parent / "esco_subset.json"


def _load_skills() -> dict[str, dict[str, Any]]:
    data = json.loads(_SKILLS_PATH.read_text(encoding="utf-8"))
    index: dict[str, dict[str, Any]] = {}
    for entry in data:
        label = str(entry.get("label", "")).strip().lower()
        if not label:
            continue
        index[label] = {
            "esco_uri": entry.get("esco_uri"),
            "ofo_code": entry.get("ofo_code"),
            "ofo_label": entry.get("ofo_label"),
        }
    return index


_SKILL_INDEX: dict[str, dict[str, Any]] = _load_skills()
_SKILL_LABELS: list[str] = sorted(_SKILL_INDEX.keys())


def known_labels() -> list[str]:
    return list(_SKILL_LABELS)


def is_known(label: str) -> bool:
    return label.strip().lower() in _SKILL_INDEX


def meta_for(label: str) -> dict[str, Any] | None:
    """Return the ESCO/OFO metadata for a known label, or None."""
    return _SKILL_INDEX.get(label.strip().lower())


def make_tag(
    label: str,
    *,
    evidence: str,
    source: SkillSource,
    confidence: ConfidenceBand = ConfidenceBand.MEDIUM,
    reasons: list[str] | None = None,
) -> SkillTag | None:
    """
    Build a fully-populated SkillTag from a known label.

    Returns None if the label is not in the curated list. This is the
    single construction point for tags that originate from
    required_skills rather than from LLM extraction — it keeps the
    _SKILL_INDEX private and makes the evidence agent's fallback path
    one line instead of five.
    """
    meta = _SKILL_INDEX.get(label.strip().lower())
    if meta is None:
        return None
    return SkillTag(
        label=label.strip().lower(),
        esco_uri=meta.get("esco_uri"),
        ofo_code=meta.get("ofo_code"),
        ofo_label=meta.get("ofo_label"),
        evidence=evidence[:500],
        confidence=confidence,
        reasons=reasons or [f"built from curated skill '{label}'"],
        source=source,
        sme_confirmed=None,
    )


async def map_text_to_skills(
    text: str,
    *,
    llm: LLMClient,
    source: SkillSource = SkillSource.AI_EXTRACTED,
    max_skills: int = 5,
) -> list[SkillTag]:
    cleaned = text.strip()
    if not cleaned:
        return []

    prompt = _build_prompt(cleaned, max_skills)
    result = await llm.call(
        prompt=prompt,
        fallback=lambda: {"skills": []},
        schema_hint=(
            '{"skills": [{"label": "<one of the curated labels>", '
            '"evidence": "<short quote from the text>"}]}'
        ),
    )

    raw_skills = result.data.get("skills", [])
    if not isinstance(raw_skills, list):
        logger.warning(
            "skill_mapper.bad_shape",
            extra={"type": type(raw_skills).__name__},
        )
        return []

    tags: list[SkillTag] = []
    seen: set[str] = set()
    for item in raw_skills:
        if not isinstance(item, dict):
            continue
        label = str(item.get("label", "")).strip().lower()
        evidence = str(item.get("evidence", "")).strip()
        if not label or not evidence:
            continue
        if label in seen:
            continue
        tag = make_tag(
            label,
            evidence=evidence,
            source=source,
            reasons=[f"matched curated skill '{label}' from text"],
        )
        if tag is None:
            logger.info("skill_mapper.drop_unknown", extra={"label": label})
            continue
        seen.add(label)
        tags.append(tag)
        if len(tags) >= max_skills:
            break

    return tags


def _build_prompt(text: str, max_skills: int) -> str:
    labels_block = ", ".join(_SKILL_LABELS)
    return (
        f"Extract up to {max_skills} skills from the text below. "
        f"Choose ONLY from this list: {labels_block}.\n"
        f"For each chosen skill, quote a short phrase from the text as evidence.\n"
        f"Return a JSON object (not an array) with exactly one key 'skills'. "
        f"Example: {{\"skills\": [{{\"label\": \"stock taking\", "
        f"\"evidence\": \"counted the stock\"}}]}}.\n"
        f"If no skill applies, return {{\"skills\": []}}.\n\n"
        f"Text:\n{text}"
    )
