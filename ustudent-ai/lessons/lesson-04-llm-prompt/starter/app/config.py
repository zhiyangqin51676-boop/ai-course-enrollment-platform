"""Central env-var access. Read once, cached via lru_cache.

Provider-agnostic by design: we talk OpenAI-compatible APIs. Switching
between Groq / OpenAI / OpenRouter only changes .env, never the code.
"""
from __future__ import annotations

from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    # LLM provider — defaults to Groq (free tier, 14,400 RPD).
    llm_api_key: str = ""
    llm_base_url: str = "https://api.groq.com/openai/v1"
    llm_model: str = "llama-3.3-70b-versatile"

    # Service
    ai_service_port: int = 8000
    log_level: str = "INFO"


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    return Settings()
