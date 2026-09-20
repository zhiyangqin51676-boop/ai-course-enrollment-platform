"""Lesson 5 starter — chunk the handbook, embed, store, retrieve.

Two parts:

PART 1 (toy):
  Write a cosine_similarity that takes two word-count dicts and returns a
  number in [0, 1]. Verify "drop the course" and "withdraw from the class"
  come out closer than "drop the course" and "today's weather".

PART 2 (real):
  Use Chroma to index data/handbook.md + data/faq.md + data/courses-catalog.md.
  Query for three real student questions and print the top-3 chunks.

By the end you'll have a runnable index living in ./chroma_db that lesson 6
will reuse for the actual RAG service.
"""
from __future__ import annotations

import sys
from collections import Counter
from math import sqrt
from pathlib import Path

import chromadb


# ============================================================================
# PART 1 — toy bag-of-words cosine similarity
# ============================================================================

def bag_of_words(text: str) -> dict[str, int]:
    """Lower-case, split on whitespace, count tokens."""
    # TODO: return Counter of lowercase whitespace-split tokens, as a plain dict
    pass


def cosine_similarity(a: dict[str, float], b: dict[str, float]) -> float:
    """cos(a, b) = dot(a, b) / (||a|| * ||b||). Returns 0 if either is empty."""
    # TODO: implement.
    # Hint:
    #   common = set(a) & set(b)
    #   dot = sum(a[k]*b[k] for k in common)
    #   norm = sqrt(sum(v*v for v in vec.values()))
    pass


def demo_part1():
    a = "drop the course before week two"
    b = "withdraw from the class by week two"
    c = "today's weather is wonderful"
    print(f"sim(a, b) = {cosine_similarity(bag_of_words(a), bag_of_words(b)):.3f}")
    print(f"sim(a, c) = {cosine_similarity(bag_of_words(a), bag_of_words(c)):.3f}")
    # If your implementation is right, a-b should be MUCH higher than a-c.


# ============================================================================
# PART 2 — real embeddings via Chroma
# ============================================================================

# Naive chunker. You can swap this for a markdown-heading-aware one later.
def chunk_text(text: str, size: int = 600, overlap: int = 100) -> list[str]:
    # TODO: slide a window of `size` characters with `overlap` carry-over,
    # return non-empty chunks.
    pass


CORPUS = ["data/handbook.md", "data/faq.md", "data/courses-catalog.md"]
ROOT = Path(__file__).resolve().parents[3]


def build_index():
    client = chromadb.PersistentClient(path=str(ROOT / "lessons/lesson-05-embedding/starter/chroma_db"))
    # TODO: get_or_create_collection("handbook")
    col = ...

    # TODO: for each file, read, chunk, then col.add(documents=..., ids=...)
    # IDs must be globally unique within the collection. Use f"{path}::{i}".
    pass


def query_index():
    client = chromadb.PersistentClient(path=str(ROOT / "lessons/lesson-05-embedding/starter/chroma_db"))
    col = client.get_collection("handbook")

    for q in [
        "How many credits do I need to graduate?",
        "Can I take CS101 and MATH101 together?",
        "How is GPA calculated?",
    ]:
        # TODO: res = col.query(query_texts=[q], n_results=3)
        # Print q + top-3 (distance, source-id, first 80 chars of chunk)
        pass


if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "demo":
        demo_part1()
    elif len(sys.argv) > 1 and sys.argv[1] == "build":
        build_index()
        print("Index built.")
    elif len(sys.argv) > 1 and sys.argv[1] == "query":
        query_index()
    else:
        print("Usage: python index_handbook.py [demo|build|query]")
