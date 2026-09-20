"""Lesson 9 solution — LLM-as-judge."""
import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[3]))

from app.llm import ask_llm
from scripts.rag import rag_answer   # 作品一(AI-1)你写的 RAG


# Judges return JSON as text; strip any ```json fences before json.loads.
_FENCE = re.compile(r"^```(?:json)?\s*|\s*```$", re.MULTILINE)


def parse_json_safe(text: str) -> dict | None:
    cleaned = _FENCE.sub("", (text or "").strip())
    # Judges sometimes wrap the JSON in prose — grab the first {...} block.
    block = re.search(r"\{.*\}", cleaned, re.DOTALL)
    if block:
        cleaned = block.group(0)
    try:
        return json.loads(cleaned)
    except json.JSONDecodeError:
        pass
    # Lenient fallback: Llama occasionally emits an unquoted "reason" value,
    # which is invalid JSON. Recover the verdict rather than showing "garbage".
    verdict = re.search(r'"correct"\s*:\s*(true|false)', cleaned, re.IGNORECASE)
    if not verdict:
        return None
    reason = re.search(r'"reason"\s*:\s*"?(.+?)"?\s*\}?\s*$', cleaned, re.DOTALL)
    return {
        "correct": verdict.group(1).lower() == "true",
        "reason": reason.group(1).strip() if reason else "",
    }

JUDGE_PROMPT = """\
You are an evaluator. Decide if SYSTEM_ANSWER correctly answers QUESTION
when judged against REFERENCE_ANSWER.

Return ONLY JSON: {{"correct": true|false, "reason": "<one short sentence>"}}

Rules:
- "correct" means the system answer conveys the same key facts as the
  reference. Wording can differ.
- If REFERENCE is "<no answer in handbook>" AND the system answer is
  "I don't know" or similar refusal -> correct=true (refusing to invent
  is the right behaviour).

QUESTION: {question}
REFERENCE: {reference}
SYSTEM_ANSWER: {answer}
"""


def judge(question, reference, answer):
    prompt = JUDGE_PROMPT.format(question=question, reference=reference, answer=answer)
    raw = ask_llm(prompt, temperature=0.0)
    return parse_json_safe(raw) or {"correct": False, "reason": "judge returned garbage"}


def run():
    items = json.loads(Path("data/golden/rag-eval.json").read_text())["items"]
    correct = 0
    print(f"{'id':>4} {'verdict':<8}  question / answer")
    print("-" * 100)
    for item in items:
        result = rag_answer(item["q"])
        v = judge(item["q"], item["ref"], result.answer)
        mark = "OK" if v["correct"] else "XX"
        if v["correct"]:
            correct += 1
        print(f"{item['id']:>4} {mark:<8}  Q: {item['q']}")
        print(f"     answer:  {result.answer[:120]}")
        if not v["correct"]:
            print(f"     reason:  {v.get('reason','')}")
        print()
    print(f"\n=== {correct}/{len(items)} ({correct/len(items)*100:.0f}%) ===")


if __name__ == "__main__":
    run()
