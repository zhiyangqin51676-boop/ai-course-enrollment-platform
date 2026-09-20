"""Lesson 8 solution — multi-tool Agent with memory."""
import os
import sys
from pathlib import Path

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


def _find_course(course_code):
    """Helper: hit GET /api/courses, filter locally by code."""
    r = httpx.get(f"{BACKEND}/api/courses", timeout=10.0)
    r.raise_for_status()
    needle = course_code.strip().upper()
    for c in (r.json().get("data") or []):
        if c.get("course_code", "").upper() == needle:
            return c
    return None


@tool
def handbook_qa(question: str) -> str:
    """Answer ustudent policy / handbook questions: GPA rules, drop deadlines,
    prerequisites, refunds, etc. Returns a grounded answer or a no-answer note.
    """
    result = rag_answer(question)
    if result.used_fallback:
        return f"(no handbook answer) {result.answer}"
    return result.answer


@tool
def get_course(course_code: str) -> str:
    """Look up details for one course by code like CS101 or MATH201.
    Returns: name, credits, current/max enrolment, teacher.
    """
    c = _find_course(course_code)
    if c is None:
        return f"No course found with code {course_code}."
    return (f"{c['course_code']} — {c['course_name']}: {c['credits']} credits, "
            f"{c['current_enrollments']}/{c['max_students']} enrolled, "
            f"teacher: {c.get('teacher', {}).get('full_name', 'TBA')}.")


@tool
def enrol(student_id: int, course_code: str) -> str:
    """Enrol a student in a course by code. Returns success or rejection reason."""
    c = _find_course(course_code)
    if c is None:
        return f"Cannot enrol — no course found with code {course_code}."
    try:
        r = httpx.post(f"{BACKEND}/api/courses/{c['id']}/enroll",
                       params={"studentId": student_id}, timeout=10.0)
        r.raise_for_status()
        body = r.json() if r.content else {"success": True}
    except Exception as e:
        return f"Enrolment failed: {e}"
    if body.get("success"):
        return f"Successfully enrolled student {student_id} in {course_code}."
    return f"Enrolment rejected: {body.get('message', 'unknown reason')}"


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
memory = MemorySaver()
agent = create_react_agent(
    llm, tools=[handbook_qa, get_course, enrol],
    # langgraph 0.2.x: state_modifier. 0.3.x renames to prompt.
    state_modifier=SYSTEM_PROMPT, checkpointer=memory,
)


def chat(message, thread_id):
    config = {"configurable": {"thread_id": thread_id}}
    result = agent.invoke({"messages": [("user", message)]}, config)
    return result["messages"][-1].content


if __name__ == "__main__":
    # NOTE: use CS101 (no prerequisite) for the enrol demo. CS201 requires
    # CS101 first, so enrolling a fresh student in CS201 gets rejected by the
    # backend — the memory/tool routing still works, but the enrol won't land
    # in the DB, which breaks the 验收 "enrol 真进数据库" checkpoint.
    tid = "demo-session-1"
    for turn in [
        "Tell me about CS101.",
        "How many credits is it?",
        "Sign me up for it. My student id is 1.",
    ]:
        print(f"\nUSER: {turn}")
        print(f"AGENT: {chat(turn, thread_id=tid)}")
