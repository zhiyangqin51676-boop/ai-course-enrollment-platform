"""AI-1 solution · RAG pipeline continuing from lesson 5's search_handbook.py.

Uses the SAME index students built in lesson 5:
  - chroma_db/ directory
  - collection "handbook"
  - explicit SentenceTransformer('all-MiniLM-L6-v2') + query_embeddings

Run from project root (after lesson-5 `build`):
    python scripts/rag.py
"""
from __future__ import annotations

import os
import sys

# Silence a harmless "tokenizers forked after parallelism" warning.
os.environ.setdefault("TOKENIZERS_PARALLELISM", "false")

# Silence chromadb 0.5.x telemetry noise: a posthog capture() signature mismatch
# spams "Failed to send telemetry event ..." on every query. Harmless, and the
# ANONYMIZED_TELEMETRY env var does NOT suppress it in this version — mute the log.
import logging
logging.getLogger("chromadb.telemetry").setLevel(logging.CRITICAL)

from dataclasses import dataclass, field
from functools import lru_cache
from pathlib import Path

import chromadb
from sentence_transformers import SentenceTransformer

ROOT = Path(__file__).resolve().parents[1]
# Make `app` importable when run as `python scripts/rag.py` from project root.
sys.path.insert(0, str(ROOT))

DB_PATH = ROOT / "chroma_db"
COLLECTION = "handbook"
MODEL_NAME = "all-MiniLM-L6-v2"

DISTANCE_THRESHOLD = 1.5   # > this = too far = no relevant material → fallback
FALLBACK = (
    "I couldn't find anything in the student handbook that answers that. "
    "Please contact Academic Advising at advising@uplus.edu."
)

RAG_PROMPT = """\
You are the ustudent course-enrolment assistant. Answer ONLY using the
material below. If the material does not answer the question, reply with
exactly: "I don't know based on the handbook."

Do not invent facts. Do not use outside knowledge. Be concise.

---- MATERIAL ----
{context}

---- QUESTION ----
{question}
"""


@lru_cache(maxsize=1)
def _model() -> SentenceTransformer:
    return SentenceTransformer(MODEL_NAME)


def retrieve(question: str, k: int = 3) -> list[dict]:
    q_vec = _model().encode(question).tolist()
    client = chromadb.PersistentClient(path=str(DB_PATH))
    col = client.get_collection(COLLECTION)
    res = col.query(query_embeddings=[q_vec], n_results=k)
    out = []
    for doc, dist in zip(res["documents"][0], res["distances"][0]):
        out.append({"text": doc, "distance": float(dist)})
    return out


@dataclass
class RagResult:
    answer: str
    sources: list[dict] = field(default_factory=list)
    used_fallback: bool = False


def rag_answer(question: str, k: int = 3) -> RagResult:
    sources = retrieve(question, k=k)

    if not sources or sources[0]["distance"] > DISTANCE_THRESHOLD:
        return RagResult(answer=FALLBACK, sources=sources, used_fallback=True)

    context = "\n\n---\n\n".join(s["text"] for s in sources)
    prompt = RAG_PROMPT.format(context=context, question=question)

    from app.llm import chat
    answer = chat([{"role": "user", "content": prompt}], temperature=0.1)

    used_fallback = "i don't know" in answer.lower()
    return RagResult(answer=answer, sources=sources, used_fallback=used_fallback)


if __name__ == "__main__":
    GOLDEN = [
        "How many credits do I need to graduate?",
        "Can a freshman take CS201?",
        "What AWS region does the system run in?",
        "What happens if my GPA drops below 1.0?",
    ]
    for q in GOLDEN:
        r = rag_answer(q)
        print(f"\n=== Q: {q}")
        print(f"  fallback? {r.used_fallback}")
        print(f"  A: {r.answer}")
        if r.sources:
            print(f"  sources: {len(r.sources)} chunks, top distance {r.sources[0]['distance']:.3f}")
