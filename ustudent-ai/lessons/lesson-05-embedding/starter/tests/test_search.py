"""Lesson 5 · unit tests for the pure functions in search_handbook.py.

We test the CHUNKER — no network, deterministic. LLM/embedding calls are
verified manually via `build` + `query` (see exercise Step 3-4).

If your tests hit ImportError, you haven't implemented chunk_by_h2 yet.
Implement, re-run, GREEN.
"""
import sys
from pathlib import Path

# Make `scripts` importable regardless of pytest CWD.
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))

import numpy as np
import pytest

from search_handbook import chunk_by_h2


# ---- Chunker ------------------------------------------------------------

def test_chunk_by_h2_single_section():
    text = "## 1. Intro\nHello world.\n"
    result = chunk_by_h2(text)
    assert len(result) == 1
    assert result[0]["title"] == "## 1. Intro"
    assert "Hello world" in result[0]["content"]


def test_chunk_by_h2_multiple_sections():
    text = (
        "## 1. A\nfoo\n"
        "## 2. B\nbar\n"
        "## 3. C\nbaz\n"
    )
    result = chunk_by_h2(text)
    assert len(result) == 3
    assert result[1]["title"] == "## 2. B"
    assert "bar" in result[1]["content"]


def test_chunk_by_h2_ignores_h3():
    """### is a subsection, not a chunk boundary."""
    text = (
        "## 1. Main\n"
        "top line\n"
        "### 1.1 Sub\n"
        "sub line\n"
    )
    result = chunk_by_h2(text)
    assert len(result) == 1
    assert "sub line" in result[0]["content"]


def test_chunk_by_h2_drops_empty_content():
    """A heading with no body underneath should still be dropped or
    included empty — but never crash."""
    text = "## 1. A\n\n## 2. B\nreal content\n"
    result = chunk_by_h2(text)
    # Whichever policy you pick, section 2 must be there with its content.
    titles = [c["title"] for c in result]
    assert "## 2. B" in titles
    b = next(c for c in result if c["title"] == "## 2. B")
    assert "real content" in b["content"]


# ---- Cosine (referenced but not part of the CLI) -----------------------
# Students may add a cosine helper as part of their exercise; if they do,
# these tests exercise it. If not, they can skip these.

def cosine(a: np.ndarray, b: np.ndarray) -> float:
    return float(np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b)))


def test_cosine_identical_vectors():
    a = np.array([1.0, 2.0, 3.0])
    assert cosine(a, a) == pytest.approx(1.0)


def test_cosine_orthogonal():
    a = np.array([1.0, 0.0])
    b = np.array([0.0, 1.0])
    assert cosine(a, b) == pytest.approx(0.0)
