"""AI-3 bonus · Step A — 加人工中断(最简版:每个工具前都暂停).

相比 step0 只加了两处(见 ★):
  1. compile 时加 checkpointer + interrupt_before=["tools"]
  2. 跑的时候分两段:先 invoke 到暂停 → 人看一眼 → invoke(None) 恢复

这是 human-in-the-loop 的最小骨架:图能【暂停、存状态、等外部、再恢复】。
一个普通 while 循环做不到这件事 —— 这就是为什么要 StateGraph。

验证过: langgraph 0.2.61。跑: python step_a_interrupt.py
"""
import os
from typing import Annotated, TypedDict

from dotenv import load_dotenv
load_dotenv()

from langchain_core.tools import tool
from langchain_openai import ChatOpenAI
from langgraph.graph import StateGraph, START, END
from langgraph.graph.message import add_messages
from langgraph.prebuilt import ToolNode
from langgraph.checkpoint.memory import MemorySaver   # ★ 存状态用


@tool
def get_course(course_code: str) -> str:
    """Look up details for a single course by code (e.g. CS101)."""
    return {
        "CS101": "CS101 — Introduction to Computer Science, 3 credits.",
        "CS201": "CS201 — Data Structures and Algorithms, 4 credits.",
    }.get(course_code.upper(), f"No course found with code {course_code}.")


TOOLS = [get_course]
llm = ChatOpenAI(
    api_key=os.environ["LLM_API_KEY"],
    base_url=os.environ.get("LLM_BASE_URL", "https://api.groq.com/openai/v1"),
    model=os.environ.get("LLM_MODEL", "llama-3.3-70b-versatile"),
    temperature=0.0,
).bind_tools(TOOLS)


class State(TypedDict):
    messages: Annotated[list, add_messages]


def agent_node(state: State) -> dict:
    return {"messages": [llm.invoke(state["messages"])]}


def route(state: State) -> str:
    return "tools" if state["messages"][-1].tool_calls else END


builder = StateGraph(State)
builder.add_node("agent", agent_node)
builder.add_node("tools", ToolNode(TOOLS))
builder.add_edge(START, "agent")
builder.add_conditional_edges("agent", route, {"tools": "tools", END: END})
builder.add_edge("tools", "agent")

# ★ 关键两处:
#   - checkpointer: 暂停时把图的状态存下来(内存版; 生产用 Redis/Postgres)
#   - interrupt_before=["tools"]: 每次要进 tools 节点【之前】暂停
graph = builder.compile(
    checkpointer=MemorySaver(),
    interrupt_before=["tools"],
)


if __name__ == "__main__":
    # thread_id 标识一段会话; 恢复时要用同一个, 才能找回暂停的状态。
    config = {"configurable": {"thread_id": "demo-1"}}

    # 第一段:跑到"要执行工具"之前, 停下来。
    graph.invoke({"messages": [("user", "What is CS201 about?")]}, config)

    snapshot = graph.get_state(config)
    print("图暂停在:", snapshot.next, "  (('tools',) = 停在工具节点前)")
    pending = snapshot.values["messages"][-1].tool_calls
    print("待执行的工具:", [(t["name"], t["args"]) for t in pending])

    # === 这里就是人类介入点 ===
    input(">>> 按回车批准执行工具(生产里这是前端的'批准'按钮)... ")

    # 第二段:invoke(None) = 从暂停处【恢复】, 不给新输入。
    result = graph.invoke(None, config)
    print("恢复后最终答案:", result["messages"][-1].content)
