# 第 7 课翻车点 cheatsheet

### 信息量过载，学生集体懵
- **症状**：讲到 LangChain 时台下表情已经空了。
- **解法**：本节的 _One Thing You Must Remember_ = "**Agent = while 循环 + LLM 决策 + 工具执行**"。其他都是细节。手搓 demo 是锚点,失焦时回到那 60 行代码。

### 学生分不清 function call vs MCP vs Agent vs Framework
- **解法**：黑板写下四件事的一句话定义（见 theory-cheatsheet §1），每提到一个就指那一行。

### LangGraph/LangChain 版本不匹配 demo 跑不通
- **症状**：`ImportError` 或 `AttributeError`。这两个库 API 半年大改一次。
- **解法**：reference 锁了 `langgraph==0.2.61`、`langchain-core==0.3.28`、`langchain-openai==0.2.14`。开课前一周用 `pip install -r requirements.txt` 在干净 venv 跑一遍 lesson 7/8 solution 确认通畅。

### 模型不按 `{"tool":...}` 格式输出
- **症状**：手搓 ReAct 第一步就 parse_json_safe 返回 None。
- **常见原因**：模型在 JSON 前面加了 "Here is the action:" 之类的礼貌话。
- **解法**：
  1. system prompt 加 "Reply with EXACTLY the JSON, no prose before or after"
  2. parse_json_safe 容错：找第一个 `{` 和最后一个 `}`，切出来再 loads
  3. starter 的"nudge then continue" 分支正是为这个

### ⚠ Agent 无限循环 / 反复调同一个工具(**2026-07 verify 时踩到, 会炸 demo**)
- **症状**:`create_react_agent` 跑 "How many credits do I need to graduate?" 时, 模型拿到正确答案后**不停手**, 用改写的问题("minimum GPA?", "with honors?")反复调 policy_qa, 直到 `GraphRecursionError: Recursion limit of 25 reached`。
- **根因有两层**:
  1. **工具匹配太脆**:`policy_qa` 原本只查 `"graduation"`, 但问题是 `"graduate"` → 首次返回 "I don't know" → 模型被逼改写重试。**flaky**:有时改对了收敛, 有时越改越发散死循环。
  2. **没有停止约束**:prebuilt `create_react_agent` 默认 prompt 不告诉模型"够了就停"。弱模型(Llama 3.3)会一直"再挖一点"。
- **三件套修法(reference 已修)**:
  1. **工具匹配覆盖自然说法**:`any(k in q for k in ("graduat","credit","gpa"))`, 保证首次命中。
  2. **加 system prompt 停止约束**:`create_react_agent(..., state_modifier="...As soon as a tool result answers the question, STOP calling tools...Never call the same tool twice.")`。
  3. **recursion_limit 安全网**:`agent.invoke(..., {"recursion_limit": 10})` —— 即使循环也优雅退出("need more steps"), 不是 25 步吓人 traceback。
- **教学用法(注意 flaky!)**:这是最好的"agent 失败模式"活教材, **但 bug 是非确定性的** —— 有时崩到 recursion limit, 有时侥幸 2 步收敛。**别赌它现场崩**。可靠信号是 **stream 里"多了一次浪费的调用"**(brittle 2 工具版实测 3/3 遍都是 2 次调用收敛)。演示时用 `stream_mode="values"` 打印每次 tool_call, 指着那次浪费的调用讲, 崩溃只作最坏情况口头带过。呼应 theory 的"Agent 成本是 RAG 3-5 倍"—— 一半烧在这种重试上。
- **可靠复现脚本**(上课前跑一遍):
  ```python
  # brittle policy_qa 只匹配 "graduation"; 用 stream 数 policy_qa 调用次数
  # 问 "How many credits do I need to graduate?" → 稳定 2 次调用(1 次浪费)
  for chunk in agent.stream({"messages":[("user", q)]}, {"recursion_limit":10}, stream_mode="values"):
      m = chunk["messages"][-1]
      if getattr(m, "tool_calls", None): print("调用:", m.tool_calls[0]["args"])
  ```

### 多步推理只执行了一步
- **症状**：第三个问题"Look up CS201 then tell me when I can drop"只调了 get_course，没调 policy_qa。
- **原因**：MAX_STEPS 太小 / system prompt 没说"after each tool result, decide whether to call another"
- **解法**：reference 的 prompt 明确写了"After you see a TOOL_RESULT, decide whether to call another tool"。学生没这句的就加上。

### `create_react_agent` API 变了
- **症状**：`TypeError: create_react_agent() got an unexpected keyword argument 'prompt'`。
- **根因**：langgraph **0.2.x** 用 `state_modifier=...`；**0.3.x** 重命名为 `prompt=...`。**踩过**——stage C verify 时第一次跑就崩。
- **解法**：reference 锁 langgraph 0.2.61 + `state_modifier`。如果之后升级，全仓 grep `state_modifier` 改 `prompt`，再跑 lesson 7/8 solution 验一遍。

### MCP server 跑不起来
- **症状**：`mcp.server.fastmcp` 找不到 FastMCP。
- **解法**：必须 `mcp>=1.16`。reference 锁 1.28.0。

### 学生想"顺手"实现真 MCP server
- **教学纪律**：本节只讲 MCP 概念。**真实 MCP server 在 lesson 8 实操**。否则时间不够。

### Groq tool calling 偶尔抽风
- **症状**：在某些复杂 prompt 下，Llama 3.3 70B 会把 tool_calls 字段塞错位置。
- **应对**：reference `app.agent.agent` 用 LangGraph 默认设置——LangGraph 已经做了不少容错。`Llama-3.3-70b-versatile` 实测可靠；如果切到更小模型（`llama-3.1-8b-instant`）翻车率明显上升。**坚持用 70b**。
