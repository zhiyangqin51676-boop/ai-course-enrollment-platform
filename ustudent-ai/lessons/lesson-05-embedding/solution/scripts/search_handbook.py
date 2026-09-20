"""Lesson 5 solution · semantic search over data/handbook.md.

Usage (from project root):
    python scripts/search_handbook.py build
    python scripts/search_handbook.py query "how do I stop taking a class?"
"""
from __future__ import annotations

import os
import sys
from functools import lru_cache
from pathlib import Path
from typing import TypedDict

# Silence harmless noise before importing the heavy libs: the tokenizers fork
# warning, and chromadb 0.5.x telemetry (a posthog capture() signature mismatch
# spams "Failed to send telemetry event ..." on every query — and the
# ANONYMIZED_TELEMETRY env var does NOT suppress it in this version).
os.environ.setdefault("TOKENIZERS_PARALLELISM", "false")
import logging
logging.getLogger("chromadb.telemetry").setLevel(logging.CRITICAL)

import chromadb
from sentence_transformers import SentenceTransformer


ROOT = Path(__file__).resolve().parents[1]
HANDBOOK_PATH = ROOT / "data" / "handbook.md"
DB_PATH = ROOT / "chroma_db"
COLLECTION_NAME = "handbook"
MODEL_NAME = "all-MiniLM-L6-v2"


class Chunk(TypedDict):
    title: str
    content: str


@lru_cache(maxsize=1)
def _model() -> SentenceTransformer:
    """Load the embedding model once per process (200-500ms cost)."""
    return SentenceTransformer(MODEL_NAME)


# ---- pure function: chunker ---------------------------------------------

def chunk_by_h2(text: str) -> list[Chunk]:
    """Split markdown text on lines starting with '## ' (H2), not '### '."""
    chunks: list[Chunk] = []
    current: Chunk | None = None
    for line in text.splitlines():
        # A single '## ' line starts a new chunk. '### ' (H3) does not.
        if line.startswith("## ") and not line.startswith("### "):
            if current is not None:
                chunks.append(current)
            current = {"title": line.rstrip(), "content": ""}
        elif current is not None:
            current["content"] += line + "\n"
    if current is not None:
        chunks.append(current)
    # Drop chunks whose content is whitespace-only.
    return [c for c in chunks if c["content"].strip()]


# ---- build --------------------------------------------------------------

def build() -> None:
    text = HANDBOOK_PATH.read_text(encoding="utf-8")
    sections = chunk_by_h2(text)
    if not sections:
        raise SystemExit(f"No sections found in {HANDBOOK_PATH}")

    docs = [f"{s['title']}\n{s['content']}" for s in sections]
    embeddings = _model().encode(docs).tolist()

    client = chromadb.PersistentClient(path=str(DB_PATH))
    # Idempotent rebuild: drop and recreate so repeated runs don't duplicate.
    try:
        client.delete_collection(COLLECTION_NAME)
    except Exception:
        pass
    col = client.create_collection(COLLECTION_NAME)
    col.add(
        documents=docs,
        embeddings=embeddings,
        ids=[str(i) for i in range(len(docs))],
    )
    print(f"Indexed {col.count()} chunks into {DB_PATH}")


# ---- query --------------------------------------------------------------

def query(question: str, n: int = 3) -> None:
    q_vec = _model().encode(question).tolist()

    client = chromadb.PersistentClient(path=str(DB_PATH))
    col = client.get_collection(COLLECTION_NAME)

    result = col.query(query_embeddings=[q_vec], n_results=n)
    print(f"\n=== Q: {question}")
    for doc, dist in zip(result["documents"][0], result["distances"][0]):
        preview = doc.replace("\n", " ").strip()[:80]
        print(f"  [d={dist:.3f}] {preview}...")


# ---- CLI dispatch -------------------------------------------------------

def main(argv: list[str]) -> int:
    if len(argv) < 2:
        print(__doc__, file=sys.stderr)
        return 2
    cmd = argv[1]
    if cmd == "build":
        build()
        return 0
    if cmd == "query":
        if len(argv) < 3:
            print("Usage: python scripts/search_handbook.py query \"<question>\"", file=sys.stderr)
            return 2
        query(argv[2])
        return 0
    print(f"Unknown command: {cmd}", file=sys.stderr)
    return 2


if __name__ == "__main__":
    sys.exit(main(sys.argv))
