from functools import lru_cache
from typing import Literal

from pydantic import Field, SecretStr
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )

    spring_base_url: str = Field(
        default="http://localhost:8080",
        description="Base URL of the Spring Boot backend (no trailing slash).",
    )
    agent_token: SecretStr = Field(
        description="Bearer token shared with Spring for AGENT-role calls.",
    )
    webhook_token: SecretStr = Field(
        description="Bearer token Spring uses when POSTing /events to this service.",
    )

    gemini_api_key: SecretStr | None = Field(
        default=None,
        description="Google Gemini API key. None forces template fallback mode.",
    )
    llm_model: str = Field(default="gemini-3.5-flash-lite",
                           description="Gemini model identifier.",)
    llm_timeout_seconds: float = Field(default=12.0, gt=0)

    mock_spring: bool = Field(
        default=False,
        description="If true, SpringClient returns canned responses instead of HTTP.",
    )
    mock_llm: bool = Field(
        default=False,
        description="If true, LLMClient skips the API and uses deterministic fallbacks.",
    )

    http_timeout_seconds: float = Field(default=5.0, gt=0)
    http_max_retries: int = Field(default=2, ge=0, le=5)
    cache_ttl_seconds: int = Field(default=3600, gt=0)

    event_store_backend: Literal["memory", "sqlite"] = Field(
    default="memory",
    description="Event dedupe backend. 'memory' for demo, 'sqlite' for pilot.",
)
    event_store_path: str = Field(
    default="events.sqlite",
    description="SQLite file path. Ignored when backend is 'memory'.",
)

    log_level: Literal["DEBUG", "INFO", "WARNING", "ERROR"] = Field(default="INFO")

    @property
    def llm_enabled(self) -> bool:
        return not self.mock_llm and self.gemini_api_key is not None


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    return Settings()
