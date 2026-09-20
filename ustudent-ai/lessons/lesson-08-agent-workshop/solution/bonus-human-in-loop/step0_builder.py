"""AI-3 bonus · Step 0 — 把 create_react_agent 用 builder 拆开重写.

create_react_agent 把整个图藏起来了。这一步不加任何新功能, 只是
【用 LangGraph 的 builder API 手写出同一个 ReAct 循环】, 让你看清
那个"魔法函数"内部到底是什么 —— 就是一个 agent 节点 + tools 节点 +
一条"要不要继续调工具"的条件边。

验证过: langgraph 0.2.61。跑: python step0_builder.py
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


# ---- 工具(和 AI-2 一样的 fake data)---------------------------------------
@tool
def get_course(course_code: str) -> str:
    """Look up details for a single course by code (e.g. CS101)."""
    return {
        "CS101": "CS101 — Introduction to Computer Science, 3 credits, Dr. Wilson.",
        "CS201": "CS201 — Data Structures and Algorithms, 4 credits, Dr. Wilson.",
    }.get(course_code.upper(), f"No course found with code {course_code}.")


@tool
def policy_qa(question: str) -> str:
    """Answer a policy question about graduation, drops, GPA, etc."""
    q = question.lower()
    if any(k in q for k in ("graduat", "credit", "gpa")):
        return "You need 120 credits and GPA >= 2.0 to graduate."
    if any(k in q for k in ("drop", "withdraw", "refund")):
        return "Drop with full refund by end of Week 2."
    return "I don't know based on the handbook."


TOOLS = [get_course, policy_qa]
llm = ChatOpenAI(
    api_key=os.environ["LLM_API_KEY"],
    base_url=os.environ.get("LLM_BASE_URL", "https://api.groq.com/openai/v1"),
    model=os.environ.get("LLM_MODEL", "llama-3.3-70b-versatile"),
    temperature=0.0,
).bind_tools(TOOLS)   # ← bind_tools: 把工具"告诉"LLM(create_react_agent 内部也这么干)


# ---- 状态:图在各节点之间传递的东西 ---------------------------------------
class State(TypedDict):
    # add_messages 是个 reducer: 新消息【追加】到列表, 不是覆盖。
    # 这就是 AI-2 手搓版里 history.append(...) 的框架版。
    messages: Annotated[list, add_messages]


# ---- 节点:每个节点就是一个 (state) -> partial_state 的函数 -----------------
def agent_node(state: State) -> dict:
    """调 LLM, 让它决定:调工具? 还是给最终答案?"""
    return {"messages": [llm.invoke(state["messages"])]}


def route(state: State) -> str:
    """条件边:看 LLM 最后一条消息有没有 tool_calls。
    有 → 去 tools 节点执行; 没有 → 结束(它给了最终答案)。
    这就是 AI-2 手搓版里 `if "final" in parsed` 那个判断。"""
    return "tools" if state["messages"][-1].tool_calls else END


# ---- 组装图:node + edge + conditional_edge + compile ----------------------
builder = StateGraph(State)
builder.add_node("agent", agent_node)
builder.add_node("tools", ToolNode(TOOLS))     # ToolNode: 自动执行 tool_calls, 相当于手搓的 fn(**args)

builder.add_edge(START, "agent")               # 入口 → agent
builder.add_conditional_edges(                 # agent 之后走哪 —— 由 route 决定
    "agent", route, {"tools": "tools", END: END},
)
builder.add_edge("tools", "agent")             # tools 执行完 → 回到 agent(这就是"循环")

graph = builder.compile()


if __name__ == "__main__":
    for q in [
        "What is CS201 about?",
        "Look up CS201 and then tell me when I can drop courses.",
    ]:
        print(f"\n=== Q: {q}")
        result = graph.invoke({"messages": [("user", q)]}, {"recursion_limit": 10})
        print(f"  A: {result['messages'][-1].content}")

# 对照:这个图和 `create_react_agent(llm, tools)` 行为完全一样。
# create_react_agent 就是上面这 8 行 builder 代码的"预制套餐"。
