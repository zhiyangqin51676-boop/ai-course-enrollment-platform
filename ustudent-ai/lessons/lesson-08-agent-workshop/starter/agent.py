"""Lesson 8 starter — Project 2: multi-tool Agent with memory.

You inherit:
  - 作品一 (AI-1) RAG: scripts/rag.py's rag_answer
  - AI-2 LangGraph create_react_agent pattern
  - lesson 5 vector index (already built in chroma_db/ via search_handbook.py build)
  - the real ustudent Spring Boot backend running at $USTUDENT_BACKEND_URL

You build:
  - 3 tools: handbook_qa, get_course, enrol
  - LangGraph ReAct agent with MemorySaver
  - A demo that proves multi-turn pronoun resolution works
"""
import os
import sys
from pathlib import Path

# Allow `from app...` imports from the repo root.
sys.path.insert(0, str(Path(__file__).resolve().parents[3]))

from dotenv import load_dotenv
load_dotenv()

import httpx
from langchain_core.tools import tool
from langchain_openai import ChatOpenAI
from langgraph.checkpoint.memory import MemorySaver
from langgraph.prebuilt import create_react_agent

from scripts.rag import rag_answer   # 作品一(AI-1)你写的 RAG

BACKEND = os.environ.get("USTUDENT_BACKEND_URL", "http://localhost:8080")


# ---- Tools ----------------------------------------------------------------

@tool
def handbook_qa(question: str) -> str:
    """Answer ustudent policy / handbook questions: GPA rules, drop deadlines,
    prerequisites, refunds, etc. Returns a grounded answer or a no-answer note.
    """
    # TODO 1: call rag_answer(question). If result.used_fallback, prefix
    # the returned string with "(no handbook answer)". Otherwise return
    # result.answer as-is.
    pass


@tool
def get_course(course_code: str) -> str:
    """Look up details for one course by code like CS101 or MATH201.
    Returns: name, credits, current/max enrolment, teacher.
    """
    # TODO 2:
    #   1. GET {BACKEND}/api/courses  (no filter)
    #   2. find the course whose course_code equals course_code (case-insensitive)
    #   3. format a one-line summary, or return "No course found with code X"
    pass


@tool
def enrol(student_id: int, course_code: str) -> str:
    """Enrol a student in a course by code. Returns success or rejection reason."""
    # TODO 3:
    #   1. resolve course_code -> numeric course id (reuse get_course logic)
    #   2. POST {BACKEND}/api/courses/{course_id}/enroll?studentId={student_id}
    #   3. return "Successfully enrolled..." or "Enrolment rejected: <reason>"
    pass


# ---- Agent ----------------------------------------------------------------

SYSTEM_PROMPT = """\
You are the ustudent course-enrolment assistant.

Tool choice rule:
  * Policy / handbook questions (GPA, drop deadlines, prerequisites,
    refunds, etc.) -> handbook_qa
  * "What is X about" / "how many credits is X" -> get_course
  * "Sign me up for X" / "enrol me in X" -> enrol (also resolve pronouns
    like 'it' or 'that course' to the most recent course discussed)

Never claim an enrolment succeeded unless you actually called enrol().
Never invent course info — always look it up.
"""

llm = ChatOpenAI(
    api_key=os.environ["LLM_API_KEY"],
    base_url=os.environ.get("LLM_BASE_URL", "https://api.groq.com/openai/v1"),
    model=os.environ.get("LLM_MODEL", "llama-3.3-70b-versatile"),
    temperature=0.1,
)

# TODO 4: create_react_agent with the tools above + a MemorySaver checkpointer
# memory = MemorySaver()
# agent = create_react_agent(
#     llm,
#     tools=[handbook_qa, get_course, enrol],
#     state_modifier=SYSTEM_PROMPT,   # langgraph 0.2.x — 0.3.x renames to `prompt`
#     checkpointer=memory,
# )
agent = ...


def chat(message: str, thread_id: str) -> str:
    # TODO 5: invoke the agent with configurable={"thread_id": thread_id}
    # Return the final assistant message content.
    pass


if __name__ == "__main__":
    # Three-turn conversation that REQUIRES memory to work.
    # Use CS101 (no prerequisite) — enrolling a fresh student in CS201 is
    # rejected by the backend (CS201 requires CS101 first), which would break
    # the "enrol 真进数据库" acceptance check.
    tid = "demo-session-1"
    for turn in [
        "Tell me about CS101.",
        "How many credits is it?",        # 'it' must resolve to CS101
        "Sign me up for it. My student id is 1.",
    ]:
        print(f"\nUSER: {turn}")
        print(f"AGENT: {chat(turn, thread_id=tid)}")
