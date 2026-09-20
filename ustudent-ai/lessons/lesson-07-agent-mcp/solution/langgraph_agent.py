"""Lesson 7 solution — same agent with LangGraph."""
import os
from dotenv import load_dotenv
load_dotenv()

from langchain_core.tools import tool
from langchain_openai import ChatOpenAI
from langgraph.prebuilt import create_react_agent

API_KEY = os.environ["LLM_API_KEY"]
BASE_URL = os.environ.get("LLM_BASE_URL", "https://api.groq.com/openai/v1")
MODEL = os.environ.get("LLM_MODEL", "llama-3.3-70b-versatile")


FAKE_COURSES = {
    "CS101": "CS101 — Introduction to Computer Science, 3 credits, Dr. Wilson.",
    "CS201": "CS201 — Data Structures and Algorithms, 4 credits, Dr. Wilson.",
    "MATH101": "MATH101 — Calculus I, 4 credits, Dr. Brown.",
}


@tool
def get_course(course_code: str) -> str:
    """Look up details for a single course by code (e.g. CS101)."""
    return FAKE_COURSES.get(course_code.upper(), f"No course found with code {course_code}.")


@tool
def policy_qa(question: str) -> str:
    """Answer a policy question about graduation, drops, GPA, etc."""
    # Match natural phrasings ("graduate", "credit") not just one keyword —
    # a brittle match makes the agent loop by rewording + retrying.
    q = question.lower()
    if any(k in q for k in ("graduat", "credit", "gpa")):
        return "You need 120 credits and GPA >= 2.0."
    if any(k in q for k in ("drop", "withdraw", "refund")):
        return "Drop with full refund: end of Week 2."
    return "I don't know based on the handbook."


# A system prompt matters even with prebuilt agents: without a "stop" nudge,
# weaker models keep re-calling tools with reworded questions and never
# finalize (they hit the recursion limit). This one line fixes that.
SYSTEM_PROMPT = (
    "You are the ustudent course-enrolment assistant. "
    "Use a tool only when you need information you don't already have. "
    "As soon as a tool result answers the question, STOP calling tools and "
    "reply to the student directly. Never call the same tool twice for the "
    "same question."
)

llm = ChatOpenAI(api_key=API_KEY, base_url=BASE_URL, model=MODEL, temperature=0.0)
agent = create_react_agent(llm, tools=[get_course, policy_qa], state_modifier=SYSTEM_PROMPT)


if __name__ == "__main__":
    for q in [
        "What is CS201 about?",
        "How many credits do I need to graduate?",
        "Look up CS201 and then tell me when I can drop courses.",
    ]:
        print(f"\n=== Q: {q}")
        # recursion_limit is a safety net: if a model ever loops, fail with a
        # clear message instead of a scary 25-step traceback.
        result = agent.invoke(
            {"messages": [("user", q)]},
            {"recursion_limit": 10},
        )
        print(f"  A: {result['messages'][-1].content}")
