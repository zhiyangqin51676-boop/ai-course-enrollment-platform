"""Call any OpenAI-compatible LLM provider.

Default: Groq (free tier, 14,400 RPD, fast). Swap to OpenAI / OpenRouter
by changing only .env. Code stays the same.

Two entry points:
  - ask_llm(question, temperature)  → single-turn convenience
  - chat(messages, temperature, response_format=None)  → multi-message,
    system + few-shot + JSON mode

Both share:
  - 429 retry with exponential backoff (4, 8, 16, 32, 64 sec)
  - On-disk cache keyed by (base_url, model, prompt, temperature) for
    deterministic calls (temperature=0). Cheap re-runs on the free tier.

Raises `LLMNotConfigured` (not RuntimeError) so the FastAPI handler can
turn a missing key into a 503 with a friendly message instead of a 500.
"""
from __future__ import annotations

import hashlib
import json
import logging
import time
from functools import lru_cache
from pathlib import Path
from typing import Any

from openai import APIStatusError, OpenAI, RateLimitError

from app.config import get_settings

log = logging.getLogger(__name__)

_CACHE_DIR = Path("data/.llm_cache")


class LLMNotConfigured(RuntimeError):
    """LLM_API_KEY missing. Caught by app/main.py and turned into 503."""


@lru_cache(maxsize=1)
def _client() -> OpenAI:
    s = get_settings()
    if not s.llm_api_key:
        raise LLMNotConfigured(
            "LLM_API_KEY is not set. Copy .env.example to .env and add your "
            "Groq key (see docs/runbook-groq-key.md)."
        )
    return OpenAI(api_key=s.llm_api_key, base_url=s.llm_base_url)


def _cache_key(base_url: str, model: str, payload: str, temperature: float) -> str:
    raw = f"{base_url}|{model}|{temperature}|{payload}".encode("utf-8")
    return hashlib.sha256(raw).hexdigest()


def _cache_get(key: str) -> str | None:
    path = _CACHE_DIR / f"{key}.json"
    if not path.exists():
        return None
    try:
        return json.loads(path.read_text())["answer"]
    except (json.JSONDecodeError, KeyError):
        return None


def _cache_put(key: str, answer: str) -> None:
    _CACHE_DIR.mkdir(parents=True, exist_ok=True)
    (_CACHE_DIR / f"{key}.json").write_text(json.dumps({"answer": answer}))


def _call_with_retry(**kwargs: Any) -> str:
    """Send the request with 429 exponential backoff."""
    max_retries = 5
    last_exc: Exception | None = None
    for attempt in range(max_retries):
        try:
            response = _client().chat.completions.create(**kwargs)
            return response.choices[0].message.content or ""
        except (RateLimitError, APIStatusError) as e:
            status = getattr(e, "status_code", None) or getattr(
                getattr(e, "response", None), "status_code", None
            )
            if status is not None and status != 429:
                raise
            last_exc = e
            if attempt == max_retries - 1:
                raise
            wait = 4 * (2 ** attempt)
            log.warning(
                "LLM 429; sleeping %ss before retry %d/%d",
                wait, attempt + 2, max_retries,
            )
            time.sleep(wait)
    assert last_exc is not None
    raise last_exc


def ask_llm(
    question: str,
    temperature: float = 0.2,
    *,
    cache: bool | None = None,
) -> str:
    """Single-turn ask. Just wraps chat() with a one-message list."""
    return chat(
        messages=[{"role": "user", "content": question}],
        temperature=temperature,
        cache=cache,
    )


def chat(
    messages: list[dict],
    temperature: float = 0.0,
    *,
    response_format: dict | None = None,
    cache: bool | None = None,
) -> str:
    """Multi-message chat. Use for system+user+few-shot or JSON mode.

    - `response_format={"type": "json_object"}` forces Groq/OpenAI JSON mode.
    - Cache only kicks in at temperature=0 by default.
    """
    s = get_settings()
    use_cache = cache if cache is not None else (temperature == 0.0)

    cache_id: str | None = None
    if use_cache:
        # Include full messages + response_format in the cache key so
        # v1/v2/v3 don't collide.
        payload = json.dumps({"messages": messages, "response_format": response_format})
        cache_id = _cache_key(s.llm_base_url, s.llm_model, payload, temperature)
        cached = _cache_get(cache_id)
        if cached is not None:
            return cached

    kwargs: dict[str, Any] = {
        "model": s.llm_model,
        "messages": messages,
        "temperature": temperature,
    }
    if response_format is not None:
        kwargs["response_format"] = response_format

    answer = _call_with_retry(**kwargs)

    if use_cache and cache_id is not None:
        _cache_put(cache_id, answer)
    return answer
