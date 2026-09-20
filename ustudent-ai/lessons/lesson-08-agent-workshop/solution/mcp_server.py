"""Lesson 8 solution — bonus MCP server.

Same tools as agent.py, exposed via the MCP protocol so that Claude Desktop,
Cursor, or any other MCP client can pick them up without code changes.
"""
import os
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[3]))

from dotenv import load_dotenv
load_dotenv()

from mcp.server.fastmcp import FastMCP

from agent import get_course, handbook_qa

mcp = FastMCP("ustudent-bonus")


@mcp.tool()
def get_course_mcp(course_code: str) -> str:
    """Look up details for one course by code (CS101, MATH201, ...)."""
    return get_course.invoke({"course_code": course_code})


@mcp.tool()
def handbook_qa_mcp(question: str) -> str:
    """Answer policy / handbook questions (GPA, drops, prerequisites, etc)."""
    return handbook_qa.invoke({"question": question})


http_app = mcp.streamable_http_app()


if __name__ == "__main__":
    mcp.run()
