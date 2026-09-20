"""AI-1 starter · turn lesson-5's retrieve into a full RAG pipeline.

You already built retrieval in lesson 5 (search_handbook.py). This file
adds the 3 missing steps: threshold fallback, prompt assembly, LLM call.

Reuses the SAME index from lesson 5:
  - chroma_db/ directory, collection "handbook"
  - explicit SentenceTransformer('all-MiniLM-L6-v2') + query_embeddings

Run from project root (after lesson-5 `build`):
    python scripts/rag.py
"""
from __future__ import annotations

import os
import sys

# Silence a harmless "tokenizers forked after parallelism" warning.
os.environ.setdefault("TOKENIZERS_PARALLELISM", "false")

from dataclasses import dataclass, field
from functools import lru_cache
from pathlib import Path

import chromadb
from sentence_transformers import SentenceTransformer

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))  # make `app` importable

DB_PATH = ROOT / "chroma_db"
COLLECTION = "handbook"
MODEL_NAME = "all-MiniLM-L6-v2"

DISTANCE_THRESHOLD = 1.5
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
    """Same as lesson 5's query, returning structured hits.

    TODO(1): encode the question, query Chroma, and return a list of
    {"text": <document>, "distance": <float>} for the top-k hits.
    """
    raise NotImplementedError("Implement retrieve (copy from lesson 5's query)")


@dataclass
class RagResult:
    answer: str
    sources: list[dict] = field(default_factory=list)
    used_fallback: bool = False


def rag_answer(question: str, k: int = 3) -> RagResult:
    """The RAG pipeline: retrieve → threshold → prompt → LLM → assemble.

    TODO(2): if there are no sources, OR the closest distance is greater
             than DISTANCE_THRESHOLD, return the FALLBACK (used_fallback=True)
             WITHOUT calling the LLM.

    TODO(3): otherwise, join the source texts into `context`, format
             RAG_PROMPT, call the LLM, and return a RagResult. Use
             `from app.llm import chat` and temperature=0.1.
             Set used_fallback = ("i don't know" in answer.lower()).
    """
    raise NotImplementedError("Implement rag_answer")


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
