"""Lesson 4 · unit tests for prompt builders.

We test the PROMPTS, not the LLM output. Prompt-builder functions are
pure — no network, no non-determinism. The LLM call itself we verify
manually via /docs (see exercise Step 3-5).

If your tests hit ImportError, you haven't implemented the builder yet
in app/routes/ask.py — that's TDD's RED. Implement, re-run, GREEN.
"""
from app.routes.ask import (
    build_v1_messages,
    build_v2_messages,
    build_v3_messages,
)


HANDBOOK_STUB = """\
Undergraduate degree requires 120 credit points.
Minimum GPA of 2.0 required to graduate.
"""


# ---- v1 ------------------------------------------------------------------

def test_build_v1_messages_has_two_messages():
    messages = build_v1_messages(HANDBOOK_STUB, "How many credits?")
    assert len(messages) == 2


def test_build_v1_messages_uses_system_and_user_roles():
    messages = build_v1_messages(HANDBOOK_STUB, "How many credits?")
    assert messages[0]["role"] == "system"
    assert messages[1]["role"] == "user"


def test_build_v1_messages_puts_question_in_user_role():
    question = "How many credits do I need to graduate?"
    messages = build_v1_messages(HANDBOOK_STUB, question)
    assert messages[1]["content"] == question


def test_build_v1_messages_includes_handbook_in_system():
    messages = build_v1_messages(HANDBOOK_STUB, "irrelevant")
    assert "120 credit points" in messages[0]["content"]


def test_build_v1_messages_instructs_refuse_when_out_of_handbook():
    """Prevents the model from making up answers for non-handbook questions."""
    messages = build_v1_messages(HANDBOOK_STUB, "irrelevant")
    system = messages[0]["content"].lower()
    # We're lenient on exact wording, but SOME refusal instruction must exist.
    assert "handbook" in system
    assert any(word in system for word in ["only", "must", "cannot", "don't know"])


# ---- v2 ------------------------------------------------------------------

def test_build_v2_messages_includes_json_shape():
    messages = build_v2_messages(HANDBOOK_STUB, "How many credits?")
    system = messages[0]["content"]
    assert "answer" in system
    assert "citation" in system


def test_build_v2_messages_shows_examples():
    messages = build_v2_messages(HANDBOOK_STUB, "How many credits?")
    system = messages[0]["content"]
    # We want at least 2 examples visible in the prompt (few-shot signature)
    assert system.count("User:") >= 2 or system.count("Example") >= 1


def test_build_v2_messages_example_covers_out_of_handbook():
    messages = build_v2_messages(HANDBOOK_STUB, "How many credits?")
    system = messages[0]["content"]
    assert '"answer": "unknown"' in system or "'answer': 'unknown'" in system


# ---- v3 ------------------------------------------------------------------

def test_build_v3_messages_still_carries_schema_hint():
    """v3 uses response_format at endpoint level, but the model still
    needs to know the field names via the prompt."""
    messages = build_v3_messages(HANDBOOK_STUB, "How many credits?")
    system = messages[0]["content"]
    assert "answer" in system
    assert "citation" in system
