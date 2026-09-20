"""Lesson 8 starter — bonus: expose your tools as a real MCP server.

If you finish the LangGraph agent fast, do this. Result: your ustudent tools
become consumable by Claude Desktop, Cursor, and any other MCP client.

Run two ways:

  STDIO (Claude Desktop config will spawn this):
      python mcp_server.py

  HTTP / Streamable:
      uvicorn mcp_server:http_app --port 9000
"""
import os
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[3]))

from dotenv import load_dotenv
load_dotenv()

from mcp.server.fastmcp import FastMCP

# Reuse the same tool implementations from your LangGraph agent. The DRY win.
from agent import get_course, handbook_qa, enrol   # the .invoke() forms

mcp = FastMCP("ustudent-bonus")


@mcp.tool()
def get_course_mcp(course_code: str) -> str:
    """Look up details for one course by code (CS101, MATH201, ...)."""
    # TODO 1: call your get_course tool. LangChain tools have .invoke({...})
    # for direct calling outside of an agent.
    pass


@mcp.tool()
def handbook_qa_mcp(question: str) -> str:
    """Answer policy / handbook questions (GPA, drops, prerequisites, etc)."""
    # TODO 2: same pattern
    pass


# Streamable HTTP app for uvicorn
http_app = mcp.streamable_http_app()


if __name__ == "__main__":
    mcp.run()
