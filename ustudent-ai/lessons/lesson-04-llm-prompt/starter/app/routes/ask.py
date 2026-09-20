"""Lesson 4 · POST /ask, /ask/v2, /ask/v3 — handbook assistant, 3 prompt versions.

Fill in the TODOs. Each version is a step up in prompt engineering:
    v1: system message defines role + handbook constraint
    v2: v1 + few-shot examples showing desired JSON shape
    v3: v2 + response_format={"type": "json_object"} + Pydantic parse

Also wire this router into app/main.py:
    from app.routes import ask
    app.include_router(ask.router, tags=["ask"])
"""
from pathlib import Path

from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.llm import chat

router = APIRouter()


# Load handbook once at import time. Kept small: pass the full text into
# the prompt. Lesson 6+ (RAG) will replace this with retrieval.
_HANDBOOK_PATH = Path("data/handbook.md")
HANDBOOK_TEXT = _HANDBOOK_PATH.read_text()


# ---- request / response models --------------------------------------------

class AskRequest(BaseModel):
    question: str = Field(..., min_length=1)


class AskV1Response(BaseModel):
    answer: str


class AskV2Response(BaseModel):
    answer: str


class AskV3Response(BaseModel):
    answer: str
    citation: str


# ---- prompt builders (pure functions, unit-testable) ----------------------

def build_v1_messages(handbook: str, question: str) -> list[dict]:
    """招 1: system message defines the assistant's role + handbook constraint."""
    # TODO(v1):
    #   Return a list of two messages:
    #     - {"role": "system", "content": "<system prompt with handbook baked in>"}
    #     - {"role": "user",   "content": question}
    #
    #   The system prompt MUST:
    #     - Say the assistant is a helpful student advisor at U+.
    #     - Say it must ONLY answer using facts from the handbook.
    #     - Say if not in handbook, reply "I don't know based on the handbook."
    #     - Include the full handbook text (fine at this size).
    raise NotImplementedError("Implement build_v1_messages")


def build_v2_messages(handbook: str, question: str) -> list[dict]:
    """招 2: few-shot examples of the desired JSON format."""
    # TODO(v2):
    #   Same shape as v1, but the system prompt should:
    #     - Ask for a JSON object: {"answer": "...", "citation": "..."}
    #     - Include TWO example turns:
    #         1. A handbook question with a proper answer + citation
    #         2. An out-of-handbook question with {"answer": "unknown", "citation": ""}
    raise NotImplementedError("Implement build_v2_messages")


def build_v3_messages(handbook: str, question: str) -> list[dict]:
    """招 3: keep few-shot; the endpoint will also use response_format."""
    # TODO(v3):
    #   You can reuse build_v2_messages here — the "encourage JSON" job is
    #   done by response_format at the endpoint level. But keep the schema
    #   description in the system prompt so the model knows the field names.
    raise NotImplementedError("Implement build_v3_messages")


# ---- endpoints ------------------------------------------------------------

@router.post("/ask", response_model=AskV1Response)
def ask_v1_endpoint(req: AskRequest) -> AskV1Response:
    messages = build_v1_messages(HANDBOOK_TEXT, req.question)
    answer = chat(messages, temperature=0)
    return AskV1Response(answer=answer)


@router.post("/ask/v2", response_model=AskV2Response)
def ask_v2_endpoint(req: AskRequest) -> AskV2Response:
    messages = build_v2_messages(HANDBOOK_TEXT, req.question)
    answer = chat(messages, temperature=0)
    return AskV2Response(answer=answer)


@router.post("/ask/v3", response_model=AskV3Response)
def ask_v3_endpoint(req: AskRequest) -> AskV3Response:
    messages = build_v3_messages(HANDBOOK_TEXT, req.question)
    raw = chat(
        messages,
        temperature=0,
        response_format={"type": "json_object"},
    )
    return AskV3Response.model_validate_json(raw)
