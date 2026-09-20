"""AI-1 solution · POST /rag-ask — RAG over the handbook, returns sources.

Wire into app/main.py:
    from app.routes import rag
    app.include_router(rag.router, tags=["rag"])
"""
from fastapi import APIRouter
from pydantic import BaseModel, Field

from scripts.rag import rag_answer

router = APIRouter()


class RagRequest(BaseModel):
    question: str = Field(..., min_length=1)


class Source(BaseModel):
    text: str
    distance: float


class RagResponse(BaseModel):
    answer: str
    sources: list[Source]
    used_fallback: bool


@router.post("/rag-ask", response_model=RagResponse)
def rag_ask(req: RagRequest) -> RagResponse:
    result = rag_answer(req.question)
    return RagResponse(
        answer=result.answer,
        sources=[Source(**s) for s in result.sources],
        used_fallback=result.used_fallback,
    )
