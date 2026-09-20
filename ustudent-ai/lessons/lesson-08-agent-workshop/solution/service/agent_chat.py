"""作品二 · POST /agent-chat — the portfolio-2 endpoint.

Wire into app/main.py:
    from app.routes import agent_chat
    app.include_router(agent_chat.router, tags=["agent"])

The React AI Chat page posts here (via nginx /ai/agent-chat) and shows the
answer + a collapsible tool_calls trace.
"""
from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.agent_service import run

router = APIRouter()


class AgentChatRequest(BaseModel):
    message: str = Field(..., min_length=1)
    thread_id: str = Field(..., min_length=1,
                           description="Same id -> same memory thread.")


class ToolCall(BaseModel):
    name: str
    args: dict
    result: str


class AgentChatResponse(BaseModel):
    answer: str
    tool_calls: list[ToolCall] = []


@router.post("/agent-chat", response_model=AgentChatResponse)
def agent_chat(req: AgentChatRequest) -> AgentChatResponse:
    result = run(req.message, thread_id=req.thread_id)
    return AgentChatResponse(
        answer=result["answer"],
        tool_calls=[ToolCall(**tc) for tc in result["tool_calls"]],
    )
