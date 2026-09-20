# Lesson 5 Starter · Semantic search over `data/handbook.md`

Follow `../exercise.md`. This README is a quick reference for where each
file goes and what "done" looks like at each TODO.

## Copy to your project

| Starter | Copy to |
|---|---|
| `starter/scripts/search_handbook.py` | `scripts/search_handbook.py` |
| `starter/tests/test_search.py` | `tests/test_search.py` |

## Pre-flight

Before running anything, warm the embedding model cache once:

```bash
python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('all-MiniLM-L6-v2')"
```

This downloads ~80 MB to `~/.cache/huggingface/`. Subsequent runs are ~0.5s.

## Order of work

1. **Fill TODO 1** — `chunk_by_h2` in `scripts/search_handbook.py`.
2. **Run** `pytest tests/test_search.py -v -k chunk` → 4 tests should GREEN.
3. **Fill TODO 2** — `build`. Then run `python scripts/search_handbook.py build`.
   - Should print `Indexed N chunks.` and create a `chroma_db/` directory.
4. **Fill TODO 3** — `query`. Then run
   `python scripts/search_handbook.py query "how do I stop taking a class?"`.
   - Top-1 distance should be < 1.5 and mention withdraw / drop.
5. **Try 3 of your own queries** (see exercise Step 6). Screenshot.

## Definition of done

- ✅ `pytest tests/test_search.py -v` → all green
- ✅ `build` creates `chroma_db/` and prints indexed count
- ✅ `query` returns 3 hits, each with distance + preview
- ✅ Screenshots of 3 self-picked queries showing semantic advantage
- ✅ `.gitignore` protects `chroma_db/` — verify `git status` doesn't list it

## Reset the index

Whenever chunking or embedding changes, wipe and rebuild:

```bash
rm -rf chroma_db/
python scripts/search_handbook.py build
```
