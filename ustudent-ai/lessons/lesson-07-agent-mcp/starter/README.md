# Lesson 7 · Starter — Hand-rolled ReAct, then LangGraph

The first half makes you write an Agent **from scratch** — no framework. By the
time you finish, "agent" should no longer feel like magic.

The second half throws that code away and uses LangGraph. You'll see exactly
which lines the framework owns.

## 准备

```bash
source .venv/bin/activate
pip install -r requirements.txt   # need langchain-openai, langgraph
```

## Part 1 · 手搓 ReAct (~30 分钟)

```bash
python react_loop.py
```

填完 `react_loop.py` 的 6 个 TODO。期望最后跑出来三组结果：
- "What is CS201 about?" → 1 个 tool 调用 (`get_course`) + final
- "How many credits to graduate?" → 1 个 tool 调用 (`policy_qa`) + final
- "Look up CS201 and then tell me when I can drop" → **2 个** tool 调用 + final

每条 trace 应该至少有 `raw` 字段；命中工具的应有 `tool` / `args` / `result`。

## Part 2 · LangGraph 重写 (~15 分钟)

```bash
python langgraph_agent.py
```

填完 3 个 TODO。**应该比 Part 1 少 ~40 行代码**，但同样行为。

## 比较 + 提交

- 写一段话（在 PR 描述里）回答：**LangGraph 帮你做了哪些事情？** 至少列 3 件。
- 如果第三个问题（需要 2 个 tool）在 Part 2 也正确触发了 2 个工具，说明 LangGraph 的 prompt 默认设置已经能引导多轮 — 这是 prebuilt agent 的隐藏价值。
