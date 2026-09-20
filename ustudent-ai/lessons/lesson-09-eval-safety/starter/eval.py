"""Lesson 9 starter — LLM-as-judge.

You will:
  1. Write a judge prompt that, given (question, reference, system_answer),
     returns JSON {"correct": bool, "reason": ...}.
  2. Run the lesson-6 RAG pipeline over data/golden/rag-eval.json,
     judge each answer, print a pass/fail table.

Re-running this is FREE — the judge runs at temperature=0 and our cache
makes second-pass calls hit the disk.
"""
import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[3]))

from app.llm import ask_llm
from scripts.rag import rag_answer   # 作品一(AI-1)你写的 RAG


# GIVEN — judges return JSON as text; strip any ```json fences before json.loads.
_FENCE = re.compile(r"^```(?:json)?\s*|\s*```$", re.MULTILINE)


def parse_json_safe(text: str) -> dict | None:
    cleaned = _FENCE.sub("", (text or "").strip())
    try:
        return json.loads(cleaned)
    except json.JSONDecodeError:
        return None


# TODO 1: write a judge prompt that:
#   - explains the task in 1-2 sentences
#   - REQUIRES JSON output {"correct": true|false, "reason": "..."}
#   - includes the rule: "if reference says <no answer in handbook> AND
#     the system answer is 'I don't know' -> correct=true"
JUDGE_PROMPT = """\
TODO
"""


def judge(question: str, reference: str, answer: str) -> dict:
    prompt = JUDGE_PROMPT.format(question=question, reference=reference, answer=answer)
    raw = ask_llm(prompt, temperature=0.0)
    # TODO 2: parse with parse_json_safe; default to {"correct": False, "reason": "judge returned garbage"}
    pass


def run():
    items = json.loads(Path("data/golden/rag-eval.json").read_text())["items"]
    correct = 0
    for item in items:
        # TODO 3: rag_answer(item['q']) -> result; judge it; print one row
        # then bump `correct` if needed
        pass
    print(f"\n=== {correct}/{len(items)} ===")


if __name__ == "__main__":
    run()
