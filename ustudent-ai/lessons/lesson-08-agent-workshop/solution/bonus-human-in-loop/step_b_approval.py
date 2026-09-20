"""AI-3 bonus · Step B — 选择性人工审批(只有 enrol 需要批准).

相比 step A 的升级:
  - step A 每个工具前都暂停(太烦, 查个课也要批准)。
  - step B 只在【写操作 enrol】前暂停; 读操作(get_course/policy_qa)直通。
  - 而且给出【批准】和【拒绝】两条真实分支。

这才是生产级 human-in-the-loop:高风险动作(往数据库写、付款、发邮件)
才拦, 低风险直接放行。用到的 LangGraph 招牌能力:
  条件边(把 enrol 路由去人工门) + interrupt + update_state(注入拒绝)。

演示里 enrol 用 stub。真实 AI-3 workshop 里, enrol 打后端
POST /api/courses/{id}/enroll —— human 门就成了"写数据库前的最后一道闸"。

验证过: langgraph 0.2.61。跑: python step_b_approval.py
"""
import os
from typing import Annotated, TypedDict

from dotenv import load_dotenv
load_dotenv()

from langchain_core.messages import SystemMessage, ToolMessage
from langchain_core.tools import tool
from langchain_openai import ChatOpenAI
from langgraph.graph import StateGraph, START, END
from langgraph.graph.message import add_messages
from langgraph.prebuilt import ToolNode
from langgraph.checkpoint.memory import MemorySaver


# ---- 工具:读(安全) + 写(高风险)------------------------------------------
@tool
def get_course(course_code: str) -> str:
    """Look up details for a single course by code (e.g. CS101). READ-ONLY."""
    return {
        "CS101": "CS101 — Introduction to Computer Science, 3 credits.",
        "CS201": "CS201 — Data Structures and Algorithms, 4 credits.",
    }.get(course_code.upper(), f"No course found with code {course_code}.")


@tool
def enrol(student_id: int, course_code: str) -> str:
    """Enrol a student in a course. HIGH-RISK write action.

    Demo stub. In the real AI-3 workshop this does:
        POST {BACKEND}/api/courses/{course_id}/enroll?studentId={student_id}
    """
    return f"[DB WRITE] enrolled student {student_id} in {course_code}."


SAFE_TOOLS = [get_course]        # 读:直通
ALL_TOOLS = [get_course, enrol]  # LLM 看得见全部

SYSTEM = SystemMessage(
    "You are the ustudent course-enrolment assistant. "
    "If an enrolment is cancelled or declined, tell the student in one sentence "
    "and do NOT try to enrol again."
)

llm = ChatOpenAI(
    api_key=os.environ["LLM_API_KEY"],
    base_url=os.environ.get("LLM_BASE_URL", "https://api.groq.com/openai/v1"),
    model=os.environ.get("LLM_MODEL", "llama-3.3-70b-versatile"),
    temperature=0.0,
).bind_tools(ALL_TOOLS)


class State(TypedDict):
    messages: Annotated[list, add_messages]


def agent_node(state: State) -> dict:
    return {"messages": [llm.invoke([SYSTEM] + state["messages"])]}


def route(state: State) -> str:
    """条件边:读工具直通, 写工具(enrol)去人工门, 无工具则结束。"""
    last = state["messages"][-1]
    if not last.tool_calls:
        return END
    if any(tc["name"] == "enrol" for tc in last.tool_calls):
        return "enrol"        # ← 高风险 → 人工门
    return "safe_tools"       # ← 只读 → 直通


builder = StateGraph(State)
builder.add_node("agent", agent_node)
builder.add_node("safe_tools", ToolNode(SAFE_TOOLS))
builder.add_node("enrol", ToolNode([enrol]))
builder.add_edge(START, "agent")
builder.add_conditional_edges(
    "agent", route,
    {"safe_tools": "safe_tools", "enrol": "enrol", END: END},
)
builder.add_edge("safe_tools", "agent")
builder.add_edge("enrol", "agent")

# interrupt_before=["enrol"] 只拦 enrol 节点; safe_tools 不拦。
graph = builder.compile(checkpointer=MemorySaver(), interrupt_before=["enrol"])


# ---- 辅助:跑到暂停, 返回待批准的 enrol 调用(None = 没触发人工门)-----------
def run_until_gate(user_msg: str, thread_id: str):
    config = {"configurable": {"thread_id": thread_id}}
    graph.invoke({"messages": [("user", user_msg)]}, config)
    snap = graph.get_state(config)
    if snap.next == ("enrol",):                       # 停在了人工门
        return config, snap.values["messages"][-1].tool_calls[0]
    return config, None                                # 直通(没写操作), 已出最终答案


def approve(config):
    """批准:invoke(None) 从暂停处恢复, 真执行 enrol。"""
    return graph.invoke(None, config)["messages"][-1].content


def reject(config, tool_call):
    """拒绝:注入一条'已取消'的工具结果(as_node='enrol' 跳过真执行), 再恢复。"""
    graph.update_state(
        config,
        {"messages": [ToolMessage(content="Enrolment cancelled by the user. Do not retry.",
                                  tool_call_id=tool_call["id"])]},
        as_node="enrol",
    )
    return graph.invoke(None, config)["messages"][-1].content


if __name__ == "__main__":
    print("=== 场景1:只读问题 → 不触发人工门 ===")
    cfg, gate = run_until_gate("What is CS201 about?", "read")
    print("人工门?", "是" if gate else "否(直通)")
    print("答案:", graph.get_state(cfg).values["messages"][-1].content, "\n")

    print("=== 场景2:选课 → 暂停 → 【批准】===")
    cfg, gate = run_until_gate("Enrol me in CS201, my id is 1.", "approve")
    print(f"⏸  agent 想执行高风险动作: {gate['name']}({gate['args']})")
    print("   [人类点批准]")                         # 生产里 = 前端弹窗点"确认"
    print("结果:", approve(cfg), "\n")

    print("=== 场景3:选课 → 暂停 → 【拒绝】===")
    cfg, gate = run_until_gate("Enrol me in CS201, my id is 1.", "reject")
    print(f"⏸  agent 想执行高风险动作: {gate['name']}({gate['args']})")
    print("   [人类点拒绝]")
    print("结果:", reject(cfg, gate))
