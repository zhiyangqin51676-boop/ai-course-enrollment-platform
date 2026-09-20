"""Lesson 5 solution tests · chunker + cosine sanity.

We deliberately don't test build/query end-to-end — those touch disk +
embedding model. Manual verification via CLI (see exercise Step 3-4).
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))

import numpy as np
import pytest

from search_handbook import chunk_by_h2


# ---- chunker ------------------------------------------------------------

class TestChunkByH2:
    def test_single_section(self):
        text = "## 1. Intro\nHello world.\n"
        result = chunk_by_h2(text)
        assert len(result) == 1
        assert result[0]["title"] == "## 1. Intro"
        assert "Hello world" in result[0]["content"]

    def test_multiple_sections(self):
        text = "## 1. A\nfoo\n## 2. B\nbar\n## 3. C\nbaz\n"
        result = chunk_by_h2(text)
        assert len(result) == 3
        assert result[1]["title"] == "## 2. B"
        assert "bar" in result[1]["content"]

    def test_h3_stays_within_h2(self):
        text = (
            "## 1. Main\n"
            "top line\n"
            "### 1.1 Sub\n"
            "sub line\n"
        )
        result = chunk_by_h2(text)
        assert len(result) == 1
        content = result[0]["content"]
        assert "top line" in content
        assert "sub line" in content
        assert "### 1.1 Sub" in content

    def test_drops_empty_content(self):
        text = "## 1. A\n\n## 2. B\nreal content\n"
        result = chunk_by_h2(text)
        titles = [c["title"] for c in result]
        assert "## 2. B" in titles
        # §1 A had only whitespace under it — dropped
        assert "## 1. A" not in titles

    def test_content_before_first_h2_ignored(self):
        text = "# Preamble\nignore me\n## 1. First\nkeep me\n"
        result = chunk_by_h2(text)
        assert len(result) == 1
        assert "keep me" in result[0]["content"]
        assert "ignore me" not in result[0]["content"]


# ---- cosine sanity ------------------------------------------------------

def cosine(a: np.ndarray, b: np.ndarray) -> float:
    return float(np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b)))


class TestCosine:
    def test_identical(self):
        a = np.array([1.0, 2.0, 3.0])
        assert cosine(a, a) == pytest.approx(1.0)

    def test_orthogonal(self):
        a = np.array([1.0, 0.0])
        b = np.array([0.0, 1.0])
        assert cosine(a, b) == pytest.approx(0.0)

    def test_opposite(self):
        a = np.array([1.0, 2.0])
        b = np.array([-1.0, -2.0])
        assert cosine(a, b) == pytest.approx(-1.0)
