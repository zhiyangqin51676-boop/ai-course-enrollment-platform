"""AI-1 starter · POST /rag-ask — RAG over the handbook, returns sources.

Fill in the TODOs, then wire into app/main.py:
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
    # TODO: call rag_answer(req.question) and map the RagResult into a
    #       RagResponse. Remember to convert each source dict into a Source.
    raise NotImplementedError
