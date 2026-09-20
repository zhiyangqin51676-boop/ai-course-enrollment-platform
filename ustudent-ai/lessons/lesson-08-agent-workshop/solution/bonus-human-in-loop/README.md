# AI-3 Bonus · 揭开 LangGraph:从预制件到人工审批

> **放在作品二提交后(72 分钟起)当进阶/bonus**,给做得快的学生 + 想深挖的。
> 也回答一个学生一定会有的疑惑:"我们那个 langgraph demo 好像只用了 LangChain,
> node/edge 在哪?"

## 为什么要这段

AI-2 用的 `create_react_agent` 是 LangGraph 的**预制件** —— 它把 node/edge/
StateGraph 全藏起来了。学生看完会以为 "LangGraph = LangChain + 一个魔法函数"。

这三步**逐层拆开黑盒**,让学生看到 LangGraph 真正的样子:**一个能暂停、能分支、
能等人的状态机**。而 human-in-the-loop 正是"为什么需要图,而不是一个 while 循环"
的教科书级理由。

## 三步渐进(每步一个小 delta)

| 步 | 文件 | 加了什么 | 第一次看到的 API |
|---|---|---|---|
| **0** | `step0_builder.py` | 把 create_react_agent 用 builder 拆开(纯 ReAct,无审批) | `StateGraph` / `add_node` / `add_conditional_edges` / `compile` |
| **A** | `step_a_interrupt.py` | 加中断:每个工具前都暂停,人点批准再继续 | `checkpointer` / `interrupt_before` / `invoke(None)` 恢复 |
| **B** | `step_b_approval.py` | 只在 **enrol(写)** 前暂停;读工具直通;批准/拒绝两分支 | 条件路由到人工门 / `update_state` 注入拒绝 |

**跑法**(需要 Groq key,不需要后端):
```bash
python step0_builder.py       # 纯 ReAct,和 create_react_agent 行为一致
echo "" | python step_a_interrupt.py   # 暂停 → 回车批准 → 恢复
python step_b_approval.py     # 三场景:读直通 / 批准选课 / 拒绝取消
```

---

## Step 0 · "魔法函数"拆开长这样

**口播**:
> "AI-2 我们写了 `agent = create_react_agent(llm, tools)` 一行就有了 agent。
> 但 node、edge 在哪?LangGraph 到底是什么?**我们把这一行拆开手写一遍。**"

**图**:
```
   START ──▶ agent ──(有 tool_calls?)──▶ tools ──┐
              ▲         │否                        │
              │         ▼                          │
              │        END                         │
              └────────────────────────────────────┘
```

**对照 AI-2 手搓版**(学生已经懂那个):
| 手搓 react_loop.py | Step 0 builder |
|---|---|
| `history.append(...)` | `add_messages` reducer |
| `run_react` 的 for 循环 | 图的 agent↔tools 循环边 |
| `if "final" in parsed` | `route()` 条件边 |
| `fn(**args)` | `ToolNode` |

**关键一句**:"**`create_react_agent` 就是这 8 行 builder 代码的预制套餐。**你现在看清引擎了。"

---

## Step A · 让图"暂停等人"

**口播**:
> "现在图能跑了。但生产里,有些动作**不能让 AI 自己决定就干** —— 比如真往数据库写。
> 我们要让图在工具执行**之前暂停**,等人点批准。"

**只加两处**(相比 step 0):
```python
graph = builder.compile(
    checkpointer=MemorySaver(),        # 暂停时把状态存下来
    interrupt_before=["tools"],        # 进 tools 节点前暂停
)
# 跑成两段:
graph.invoke({...}, config)   # 跑到暂停
# ... 人看一眼 snapshot, 点批准 ...
graph.invoke(None, config)    # None = 从暂停处恢复
```

**为什么普通 while 循环做不到**:
> "暂停 + 保存现场 + 等外部输入几分钟/几小时 + 精确恢复到那一步 —— 一个 for 循环没法'冻结'再'解冻'。**这就是 StateGraph + checkpointer 存在的理由。**"

**面试锚点**:"human-in-the-loop 靠 checkpointer 把状态持久化,所以能等人几小时甚至跨进程恢复。"

---

## Step B · 只拦高风险动作(生产级)

**口播**:
> "step A 每个工具前都暂停 —— 太烦,查个课也要批准。生产里只拦**高风险写操作**:
> 选课、付款、删数据。读操作直接放行。"

**图**(这才是一个真正有分支的图):
```
        START
          │
          ▼
       ┌─────┐
       │agent│──(条件边: LLM 想干嘛?)
       └──┬──┘
    ┌─────┼──────────────┬────────────┐
    ▼     ▼              ▼            ▼
(读工具)              (enrol 写)     (final)
safe_tools            ┌──────┐        END
    │              ⏸ │ enrol │ interrupt_before
    │                 └──┬───┘
    │            ┌───────┴───────┐
    │         批准             拒绝
    │       (真执行)      (update_state 注入取消)
    └───────────┴──────┬────────┘
                       ▼ 回到 agent
```

**三场景**(step_b 输出):
1. "What is CS201 about?" → 读工具 → **直通**,不打扰人
2. "Enrol me in CS201" → **暂停** → 批准 → "successfully enrolled"
3. "Enrol me in CS201" → **暂停** → 拒绝 → "enrolment has been cancelled"

**批准/拒绝两条分支的实现**:
- 批准 = `invoke(None, config)`:从暂停处恢复,真跑 enrol 节点。
- 拒绝 = `update_state(..., ToolMessage("cancelled"), as_node="enrol")`:**假装 enrol 节点返回了"已取消"**(不真执行),再恢复,agent 就告诉用户取消了。

**面试锚点**:
> "生产 agent 的高风险动作要有 human gate。实现 = 条件边把写操作路由到一个带
> interrupt 的节点,批准就恢复,拒绝就注入一条取消结果。**这是把'能跑的 demo'
> 变成'敢上线的系统'的关键一步。**"

---

## 接回真实的作品二

step_b 里 enrol 是 stub。**在真实 AI-3 workshop 里**,把 `enrol` 换成打后端那版:
```python
@tool
def enrol(student_id: int, course_code: str) -> str:
    r = httpx.post(f"{BACKEND}/api/courses/{course_id}/enroll", params={"studentId": student_id})
    ...
```
这时 human 门就成了**"往数据库写之前的最后一道闸"** —— 动机最强,最有说服力。

## 时间预算

- 这是 **bonus**,不占核心 90 分钟。作品二提交后(72 分钟起)有余力就上。
- 三步全讲约 15-20 分钟;时间紧只讲 step 0(拆开黑盒)+ step B 结论图。
- **核心收获一句话**:"`create_react_agent` 是自动挡,好开但看不见引擎;手写这个图是
  手动挡,让你看清 LangGraph = 一个能暂停、能分支、能等人的状态机。"

## 版本说明

三个文件都在 **langgraph 0.2.61** 上验证过(2026-07)。
- `interrupt_before` + `invoke(None)` 恢复是 0.2.x 的写法。
- 0.3.x 有新的动态 `interrupt()` 函数,升级时这三个文件要重验。
