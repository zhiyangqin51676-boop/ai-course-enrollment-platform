"""Lesson 2 — students copy this pattern to write their own endpoint + test."""
from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter()


class EchoRequest(BaseModel):
    message: str


class EchoResponse(BaseModel):
    echoed: str


@router.post("/echo", response_model=EchoResponse)
def echo(req: EchoRequest) -> EchoResponse:
    return EchoResponse(echoed=req.message)
