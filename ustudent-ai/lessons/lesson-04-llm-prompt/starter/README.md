# Lesson 4 Starter · `/ask` handbook assistant with 3 prompt versions

Follow `../exercise.md` end-to-end. This README is a quick reference for
where each file goes and what "done" looks like at each step.

## Copy to your project

| Starter | Copy to |
|---|---|
| `starter/app/config.py` | `app/config.py` |
| `starter/app/llm.py` | `app/llm.py` |
| `starter/app/routes/ask.py` | `app/routes/ask.py` |
| `starter/tests/test_ask.py` | `tests/test_ask.py` |

Then edit `app/main.py` to add:

```python
from fastapi.responses import JSONResponse
from app.llm import LLMNotConfigured

@app.exception_handler(LLMNotConfigured)
async def _handle_llm_not_configured(request, exc: LLMNotConfigured):
    return JSONResponse(
        status_code=503,
        content={"detail": str(exc), "code": "llm_not_configured"},
    )

from app.routes import ask
app.include_router(ask.router, tags=["ask"])
```

## Order of work

1. **Copy** `config.py` + `llm.py` (don't edit).
2. **Fill TODO** in `ask.py` for `build_v1_messages`.
3. **Run** `pytest tests/test_ask.py -v -k v1` → should be all green.
4. **Wire** `include_router` in `main.py`, add the `LLMNotConfigured` handler.
5. **Try** `/ask` via `/docs`. Both a handbook question AND a non-handbook question.
6. Repeat 2-5 for **v2** (few-shot) and **v3** (JSON mode).

## Definition of done

- ✅ `pytest tests/test_ask.py -v` → all green
- ✅ `/ask` (v1) refuses out-of-handbook questions
- ✅ `/ask/v2` returns JSON-shaped text (may need light cleaning)
- ✅ `/ask/v3` returns valid Pydantic-parsed JSON every time
- ✅ Screenshots of the 3 endpoints against the SAME question
