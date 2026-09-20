# AI-1 Starter · RAG over the handbook (作品一)

Turn lesson-5's retrieval into a full RAG assistant. Follow `../exercise.md`.

## Prerequisite

You must have lesson-5's index built. From project root:

```bash
python scripts/search_handbook.py build   # creates chroma_db/, collection "handbook"
```

## Copy to your project

| Starter | Copy to |
|---|---|
| `starter/scripts/rag.py` | `scripts/rag.py` |
| `starter/app/routes/rag.py` | `app/routes/rag.py` |

## Order of work

1. **Fill `retrieve` (TODO 1)** — copy the query logic from your lesson-5
   `search_handbook.py`, but return `{"text", "distance"}` dicts.
2. **Fill `rag_answer` (TODO 2, 3)** — threshold fallback, then prompt →
   `chat()` → assemble. Run `python scripts/rag.py`; g1 answers, g3 falls back.
3. **Fill `/rag-ask` route** — map `RagResult` → `RagResponse`.
4. **Wire main.py** — `app.include_router(rag.router, tags=["rag"])`.
5. **Test via /docs** — POST /rag-ask with a handbook question and a
   non-handbook question.

## Definition of done (作品一 验收)

- ✅ handbook 有的 → 正确回答
- ✅ handbook 没有的 → 兜底, 不编
- ✅ 返回带 sources (text + distance)
- ✅ temperature ≤ 0.2
- ✅ /rag-ask 端点 + /docs 可调
- ✅ golden set 4 题全过 (g1 credits / g2 CS201 prereq / g3 AWS→fallback / g4 GPA<1.0)
