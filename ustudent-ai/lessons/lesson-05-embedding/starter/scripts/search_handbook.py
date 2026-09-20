"""Lesson 5 · CLI · semantic search over data/handbook.md.

Usage (from project root):
    python scripts/search_handbook.py build
    python scripts/search_handbook.py query "how do I stop taking a class?"

Fill in the TODOs. Then wire your project's data/handbook.md.
"""
from __future__ import annotations

import sys
from pathlib import Path
from typing import TypedDict

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


# ---------------------------------------------------------------------------
# TODO 1 · chunk_by_h2
# ---------------------------------------------------------------------------

def chunk_by_h2(text: str) -> list[Chunk]:
    """Split markdown text on lines starting with '## '.

    Return a list like:
        [
            {"title": "## 1. Academic Calendar", "content": "The academic year..."},
            {"title": "## 2. Credits and Graduation Requirements", "content": "..."},
            ...
        ]

    Rules:
      - A new chunk starts at every line that begins with "## "
        (NOTE the space — do NOT split on "### ").
      - Content of a chunk is everything after its title, up to the next "## ".
      - Whitespace-only chunks should be dropped.
    """
    # TODO: implement.
    raise NotImplementedError


# ---------------------------------------------------------------------------
# TODO 2 · build
# ---------------------------------------------------------------------------

def build() -> None:
    """Read handbook, chunk it, embed each chunk, write to Chroma.

    Steps:
      1. Read HANDBOOK_PATH.
      2. Call chunk_by_h2 → list of Chunks.
      3. Load SentenceTransformer(MODEL_NAME).
      4. Encode every chunk (as `title + "\\n" + content`).
      5. Open chromadb.PersistentClient(path=str(DB_PATH)).
      6. Drop the old collection if it exists (idempotent rebuild), then create it fresh.
      7. col.add(documents=..., embeddings=..., ids=...)  (ids are just str(i)).
      8. Print "Indexed N chunks."
    """
    # TODO: implement.
    raise NotImplementedError


# ---------------------------------------------------------------------------
# TODO 3 · query
# ---------------------------------------------------------------------------

def query(question: str, n: int = 3) -> None:
    """Encode question, ask Chroma for top-n, print each hit.

    Print format for each hit:
        [d=<distance>] <first 80 chars of document>...
    """
    # TODO: implement.
    raise NotImplementedError


# ---------------------------------------------------------------------------
# CLI dispatch — done for you
# ---------------------------------------------------------------------------

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
