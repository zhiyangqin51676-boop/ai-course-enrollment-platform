"""作品二 agent, service-ready.

把 workshop 的独立 agent.py "服务化" —— 同样 3 工具 + memory, 但:
  - 放在 app/ 下, 能被 FastAPI 路由 import
  - run() 返回 {"answer", "tool_calls"}(前端 AI Chat 页要 tool_calls trace)

handbook_qa 复用作品一的 scripts/rag.py; get_course/enrol 打真后端。
"""
from __future__ import annotations

import os
from typing import Any

from dotenv import load_dotenv
load_dotenv()   # 读 .env, 让下面的 os.environ["LLM_API_KEY"] 拿得到

import httpx
from langchain_core.tools import tool
from langchain_openai import ChatOpenAI
from langgraph.checkpoint.memory import MemorySaver
from langgraph.prebuilt import create_react_agent

from scripts.rag import rag_answer   # 作品一(AI-1)的 RAG

BACKEND = os.environ.get("USTUDENT_BACKEND_URL", "http://localhost:8080")


def _find_course(course_code: str) -> dict | None:
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
    prerequisites, refunds, etc. Returns a grounded answer or a no-answer note."""
    result = rag_answer(question)
    return f"(no handbook answer) {result.answer}" if result.used_fallback else result.answer


@tool
def get_course(course_code: str) -> str:
    """Look up details for one course by code like CS101 or MATH201.
    Returns: name, credits, current/max enrolment, teacher."""
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
  * Policy / handbook questions (GPA, drop deadlines, prerequisites, refunds)
    -> handbook_qa
  * "What is X about" / "how many credits is X" -> get_course
  * "Sign me up for X" / "enrol me in X" -> enrol (resolve pronouns like 'it'
    or 'that course' to the most recently discussed course)

As soon as a tool result answers the question, STOP calling tools and reply.
Never claim an enrolment succeeded unless you actually called enrol().
"""

_llm = ChatOpenAI(
    api_key=os.environ["LLM_API_KEY"],
    base_url=os.environ.get("LLM_BASE_URL", "https://api.groq.com/openai/v1"),
    model=os.environ.get("LLM_MODEL", "llama-3.3-70b-versatile"),
    temperature=0.1,
)
# module-level so memory persists across requests within one process
_memory = MemorySaver()
_agent = create_react_agent(
    _llm, tools=[handbook_qa, get_course, enrol],
    state_modifier=SYSTEM_PROMPT, checkpointer=_memory,
)


def run(message: str, thread_id: str) -> dict[str, Any]:
    """Send `message` on the conversation `thread_id`.
    Returns {"answer": str, "tool_calls": [{"name","args","result"}, ...]}."""
    config = {"configurable": {"thread_id": thread_id}}
    result = _agent.invoke({"messages": [("user", message)]},
                           {**config, "recursion_limit": 12})
    messages = result["messages"]
    answer = messages[-1].content if messages else ""

    # Pull this turn's tool calls + results out of the message trace.
    tool_calls: list[dict] = []
    pending: dict[str, dict] = {}
    for m in messages:
        for tc in getattr(m, "tool_calls", []) or []:
            pending[tc["id"]] = {"name": tc["name"], "args": tc["args"]}
        if getattr(m, "type", None) == "tool":
            entry = pending.pop(m.tool_call_id, {"name": "?", "args": {}})
            entry["result"] = str(m.content)
            tool_calls.append(entry)
    return {"answer": answer, "tool_calls": tool_calls}
