"""Lesson 7 starter — same agent, written with LangGraph.

Side-by-side with react_loop.py (your hand-rolled version), this is what the
framework saves you. Should be ~15 lines once filled in.
"""
import os
from dotenv import load_dotenv
load_dotenv()

from langchain_core.tools import tool
from langchain_openai import ChatOpenAI
from langgraph.prebuilt import create_react_agent

API_KEY = os.environ["LLM_API_KEY"]
BASE_URL = os.environ.get("LLM_BASE_URL", "https://api.groq.com/openai/v1")
MODEL = os.environ.get("LLM_MODEL", "llama-3.3-70b-versatile")


# Same fake data as react_loop.py — apples to apples.
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
    # Match natural phrasings, not one keyword — a brittle match makes the
    # agent loop (it gets "I don't know", rewords, retries forever).
    q = question.lower()
    if any(k in q for k in ("graduat", "credit", "gpa")):
        return "You need 120 credits and GPA >= 2.0."
    if any(k in q for k in ("drop", "withdraw", "refund")):
        return "Drop with full refund: end of Week 2."
    return "I don't know based on the handbook."


# A system prompt that tells the model to STOP once a tool has answered.
# Without it, weaker models keep re-calling tools and hit the recursion limit.
SYSTEM_PROMPT = (
    "You are the ustudent course-enrolment assistant. "
    "As soon as a tool result answers the question, STOP calling tools and "
    "reply to the student directly. Never call the same tool twice."
)

# TODO 1: build a ChatOpenAI with api_key/base_url/model from above (temperature=0)
llm = ...

# TODO 2: build an agent:
#   create_react_agent(llm, tools=[get_course, policy_qa], state_modifier=SYSTEM_PROMPT)
agent = ...


if __name__ == "__main__":
    for q in [
        "What is CS201 about?",
        "How many credits do I need to graduate?",
        "Look up CS201 and then tell me when I can drop courses.",
    ]:
        print(f"\n=== Q: {q}")
        # TODO 3: call agent.invoke({"messages": [("user", q)]}, {"recursion_limit": 10})
        # Print result["messages"][-1].content
        ...
