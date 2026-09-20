"""Lesson 7 starter — hand-roll a ReAct loop.

By the end of this file you will have an Agent — a real one. Not a framework
wrapping it. ~50 lines that you wrote yourself.

Then lesson 8 will replace this whole file with `create_react_agent(...)` from
LangGraph and you'll see exactly what the framework was doing for you.

THE PATTERN

  1. Tell the LLM the tools it can call.
  2. Ask it what to do.
  3. If it returned `{"tool": ..., "args": ...}` — run the tool, append the
     result to the chat, ask again.
  4. If it returned `{"final": "..."}` — return that to the user.
  5. Cap the loop so a confused model can't burn forever.

THE TWO TOOLS YOU GET

  get_course(course_code) -> str         look up one course
  policy_qa(question)     -> str         answer a policy question (fake here)
"""
import json
import os
import re
from pathlib import Path

from dotenv import load_dotenv
load_dotenv()

from openai import OpenAI

API_KEY = os.environ["LLM_API_KEY"]
BASE_URL = os.environ.get("LLM_BASE_URL", "https://api.groq.com/openai/v1")
MODEL = os.environ.get("LLM_MODEL", "llama-3.3-70b-versatile")
client = OpenAI(api_key=API_KEY, base_url=BASE_URL)


# ---- Tools (the agent's hands) --------------------------------------------

FAKE_COURSES = {
    "CS101": "CS101 — Introduction to Computer Science, 3 credits, Dr. Wilson, MWF 09:00-10:30.",
    "CS201": "CS201 — Data Structures and Algorithms, 4 credits, Dr. Wilson, TTh 11:00-12:30.",
    "MATH101": "MATH101 — Calculus I, 4 credits, Dr. Brown, MWF 09:30-11:00.",
}

# Match natural phrasings, not one keyword — a brittle match makes the agent
# loop (it gets "I don't know", rewords the question, retries).
POLICY_RULES = [
    (("graduat", "credit", "gpa"), "You need 120 credits and GPA >= 2.0 to graduate."),
    (("drop", "withdraw", "refund"), "Drop with full refund: end of Week 2. After that, see refund policy."),
]


def get_course(course_code: str) -> str:
    """Look up details for a single course by code (e.g. CS101)."""
    return FAKE_COURSES.get(course_code.upper(), f"No course found with code {course_code}.")


def policy_qa(question: str) -> str:
    """Answer a policy question about graduation, drops, GPA, etc."""
    q = question.lower()
    for keywords, ans in POLICY_RULES:
        if any(k in q for k in keywords):
            return ans
    return "I don't know based on the handbook."


TOOLS = {
    "get_course": get_course,
    "policy_qa":  policy_qa,
}


# ---- The ReAct loop -------------------------------------------------------

MAX_STEPS = 5

SYSTEM_PROMPT = """\
You are the ustudent course-enrolment assistant.

You have these tools:
  - get_course(course_code) -> details for one course
  - policy_qa(question)     -> answer to a policy / handbook question

OUTPUT FORMAT — reply with EXACTLY ONE of these JSON shapes, nothing else:

  {"tool": "<tool_name>", "args": {...}}
  {"final": "<your reply to the student>"}

After you see a TOOL_RESULT, decide whether to call another tool or to
produce the final answer.
"""


def call_llm(prompt: str) -> str:
    resp = client.chat.completions.create(
        model=MODEL,
        messages=[{"role": "user", "content": prompt}],
        temperature=0.0,
    )
    return (resp.choices[0].message.content or "").strip()


def parse_json_safe(text: str):
    cleaned = re.sub(r"^```(?:json)?\s*|\s*```$", "", text.strip(), flags=re.MULTILINE)
    try:
        return json.loads(cleaned)
    except json.JSONDecodeError:
        return None


def run_react(user_message: str) -> dict:
    history = [SYSTEM_PROMPT, f"USER: {user_message}"]
    trace = []

    for step in range(MAX_STEPS):
        # TODO 1: call the LLM with "\n\n".join(history)
        raw = ...
        trace.append({"step": step, "raw": raw})

        # TODO 2: parse the raw JSON
        parsed = ...

        # TODO 3: if parsed has "final" -> return {"answer": ..., "trace": trace}
        ...

        # TODO 4: extract tool + args from parsed.
        # If no valid tool call, nudge the model and continue:
        #   history.append(f"ASSISTANT: {raw}")
        #   history.append('SYSTEM: Reply only with {"tool":...} or {"final":...}.')
        #   continue
        ...

        # TODO 5: look up the tool function in TOOLS; if not found, set tool_result
        # to "ERROR: no such tool". Otherwise call it with **args (wrap in try/except).
        tool_result = ...

        # TODO 6: append both the assistant turn and the tool result to history:
        #   history.append(f"ASSISTANT: {raw}")
        #   history.append(f"TOOL_RESULT[{name}]: {tool_result}")
        ...

    return {"answer": "I'm stuck — try again or contact advising@uplus.edu.",
            "trace": trace}


if __name__ == "__main__":
    for q in [
        "What is CS201 about?",
        "How many credits do I need to graduate?",
        "Look up CS201 and then tell me when I can drop courses.",
    ]:
        print(f"\n=== Q: {q}")
        out = run_react(q)
        print(f"  steps: {len(out['trace'])}")
        print(f"  A: {out['answer']}")
