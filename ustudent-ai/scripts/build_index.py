#!/usr/bin/env python3
"""Build the Chroma index from the markdown corpus under data/.

Run once before starting the AI service if you want /rag-ask to work:
    python scripts/build_index.py

Re-run after editing handbook.md / faq.md / courses-catalog.md.
"""
from __future__ import annotations

import sys
from pathlib import Path

# Allow running this script directly without `python -m`.
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app.rag.index import get_client, index_documents  # noqa: E402

CORPUS_FILES = [
    "data/handbook.md",
    "data/courses-catalog.md",
    "data/faq.md",
]


def main() -> int:
    root = Path(__file__).resolve().parent.parent
    documents: dict[str, str] = {}
    for rel in CORPUS_FILES:
        path = root / rel
        if not path.exists():
            print(f"  ! missing: {path}", file=sys.stderr)
            continue
        documents[rel] = path.read_text(encoding="utf-8")
        print(f"  + loaded {rel} ({len(documents[rel])} chars)")

    if not documents:
        print("No corpus files found. Nothing to index.", file=sys.stderr)
        return 1

    client = get_client(root / "data" / "chroma_db")
    n = index_documents(client, documents)
    print(f"\nIndexed {n} chunks into Chroma at data/chroma_db/")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
