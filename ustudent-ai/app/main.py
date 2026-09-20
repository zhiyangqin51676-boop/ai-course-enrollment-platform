"""FastAPI entrypoint for the ustudent-ai service.

Every new endpoint you write goes into a module under `app/routes/`
and is wired in below with `app.include_router(...)`.
"""
from fastapi import FastAPI

from app.routes import echo, health

from app.routes import can_graduate

app = FastAPI(
    title="ustudent AI service",
    version="0.1.0",
)

app.include_router(health.router, tags=["health"])
app.include_router(echo.router, tags=["echo"])
app.include_router(can_graduate.router, tags=["lesson-3"])
