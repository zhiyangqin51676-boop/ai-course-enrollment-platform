# 第 7 课(AI-2)· 参考答案

> 这是**概念课**,不交作品 —— solution 直接给你,方便对照 + 跑通看效果。
> 但**先自己填 `../starter/` 的 TODO** 再看这里,手搓一遍才懂 "Agent = while 循环 + LLM + 工具"。

## 里面是什么

- `react_loop.py` —— **手搓** ReAct agent(~60 行,不用任何框架)
- `langgraph_agent.py` —— **同一个 agent 用 LangGraph 重写**(几行)

**两个都是自洽的**(fake 课程/政策数据),**不需要后端、不需要 chroma**,只要 Groq key。

## 怎么跑

```bash
# 确认 .env 里有 LLM_API_KEY(第 4 课配的 Groq key)
python lessons/lesson-07-agent-mcp/solution/react_loop.py
```

**期望(手搓 ReAct)**:
```
=== Q: What is CS201 about?
  steps: 2                       ← get_course 一次 + 给最终答案一次
  A: CS201 is Data Structures and Algorithms, 4 credits, Dr. Wilson...

=== Q: Look up CS201 and then tell me when I can drop courses.
  steps: 3                       ← get_course → policy_qa → 最终答案(自己决定调 2 个工具!)
```

```bash
python lessons/lesson-07-agent-mcp/solution/langgraph_agent.py
```
**期望**:三题输出等价,但代码从 60 行缩到几行。

## 两个对照,理解这节的核心

| | 手搓 react_loop.py | langgraph_agent.py |
|---|---|---|
| 代码量 | ~60 行 | ~几行 |
| tool 协议 | 你手写 JSON parse | 框架代劳 |
| 你学到 | **Agent 不神秘 = while + LLM + 工具** | 框架帮你做了什么 |

## ⚠️ 一个坑(可靠复现)

`create_react_agent` 如果**工具匹配太脆 + 没有停止约束**,模型会反复调同一个工具(浪费 token,最坏 `GraphRecursionError`)。solution 已用三件套修好:
1. 工具匹配覆盖自然说法 `any(k in q for k in ("graduat","credit","gpa"))`
2. `state_modifier` 加停止约束
3. `recursion_limit` 安全网

细节看 `../pitfalls.md` 里 "Agent 无限循环" 那条。

## 跑通之后

自己去填 `../starter/react_loop.py` 的 TODO,手搓一遍 ReAct 循环 —— **这节的价值就是看清 agent 内部,别只会用框架。**
