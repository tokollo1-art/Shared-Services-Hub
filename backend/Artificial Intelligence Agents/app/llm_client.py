from __future__ import annotations

import hashlib
import json
import logging
import time
from dataclasses import dataclass
from typing import Any, Callable

from google import genai
from google.genai import types

from app.config import Settings

logger = logging.getLogger(__name__)


@dataclass(frozen=True)
class LLMResult:
    data: dict[str, Any]
    mode: str
    latency_ms: int
    cached: bool = False
    used_fallback: bool = False


@dataclass
class _CacheEntry:
    data: dict[str, Any]
    expires_at: float


class LLMClient:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings
        self._cache: dict[str, _CacheEntry] = {}
        self._client: genai.Client | None = None
        if settings.llm_enabled:
            self._client = genai.Client(
                api_key=settings.gemini_api_key.get_secret_value(),
                http_options=types.HttpOptions(
                    timeout=int(settings.llm_timeout_seconds * 1000),
                ),
            )
            logger.info(
                "LLMClient ready: model=%s cache_ttl=%ss timeout=%ss",
                settings.llm_model,
                settings.cache_ttl_seconds,
                settings.llm_timeout_seconds,
            )
        else:
            logger.info("LLMClient in fallback-only mode")

    @property
    def model_name(self) -> str | None:
        """
        The configured model identifier, or None when the LLM is disabled.
        Agents use this to set model_version on their audit records.
        """
        if not self._settings.llm_enabled:
            return None
        return self._settings.llm_model

    async def call(
        self,
        prompt: str,
        fallback: Callable[[], dict[str, Any]],
        *,
        schema_hint: str = "",
    ) -> LLMResult:
        started = time.monotonic()
        cache_key = self._cache_key(prompt)
        cached = self._get_cached(cache_key)
        if cached is not None:
            return LLMResult(
                data=cached,
                mode="LLM",
                latency_ms=0,
                cached=True,
            )

        if self._client is None:
            data = fallback()
            return LLMResult(
                data=data,
                mode="FALLBACK",
                latency_ms=_elapsed_ms(started),
                used_fallback=True,
            )

        try:
            parsed = await self._generate(prompt, schema_hint)
            self._put_cached(cache_key, parsed)
            return LLMResult(
                data=parsed,
                mode="LLM",
                latency_ms=_elapsed_ms(started),
            )
        except Exception as exc:
            logger.warning(
                "LLM call failed (%s); using fallback",
                exc,
                extra={"prompt_hash": cache_key[:8]},
            )
            data = fallback()
            return LLMResult(
                data=data,
                mode="FALLBACK",
                latency_ms=_elapsed_ms(started),
                used_fallback=True,
            )

    async def _generate(self, prompt: str, schema_hint: str) -> dict[str, Any]:
        assert self._client is not None
        full_prompt = (
            f"{prompt}\n\n"
            f"Reply with JSON only, no prose, no markdown fences.\n"
            f"{schema_hint}"
        ).strip()

        response = await self._client.aio.models.generate_content(
            model=self._settings.llm_model,
            contents=full_prompt,
            config=types.GenerateContentConfig(
                response_mime_type="application/json",
                temperature=0.2,
                max_output_tokens=1024,
            ),
        )
        text = (response.text or "").strip()
        if not text:
            raise ValueError("Empty LLM response")
        return _parse_json(text)

    def _cache_key(self, prompt: str) -> str:
        model = self._settings.llm_model
        return hashlib.sha256(f"{model}::{prompt}".encode()).hexdigest()

    def _get_cached(self, key: str) -> dict[str, Any] | None:
        entry = self._cache.get(key)
        if entry is None:
            return None
        if entry.expires_at < time.time():
            self._cache.pop(key, None)
            return None
        return entry.data

    def _put_cached(self, key: str, data: dict[str, Any]) -> None:
        self._cache[key] = _CacheEntry(
            data=data,
            expires_at=time.time() + self._settings.cache_ttl_seconds,
        )
        if len(self._cache) > 1000:
            self._prune()

    def _prune(self) -> None:
        now = time.time()
        self._cache = {
            k: v for k, v in self._cache.items() if v.expires_at >= now
        }


def _elapsed_ms(started: float) -> int:
    return int((time.monotonic() - started) * 1000)


def _parse_json(text: str) -> dict[str, Any]:
    cleaned = text.strip()
    if cleaned.startswith("```"):
        cleaned = cleaned.split("\n", 1)[1] if "\n" in cleaned else cleaned
        if cleaned.endswith("```"):
            cleaned = cleaned[:-3]
        cleaned = cleaned.strip()
    try:
        parsed = json.loads(cleaned)
    except json.JSONDecodeError as exc:
        raise ValueError(f"LLM returned invalid JSON: {exc}") from exc
    if not isinstance(parsed, dict):
        raise ValueError("LLM returned non-object JSON")
    return parsed
