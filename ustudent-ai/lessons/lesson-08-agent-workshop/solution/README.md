# 第 8 课(AI-3 / 作品二)· 参考答案

> ⚠️ **作品二是你的核心简历项目 —— 先自己填 `../starter/agent.py` 的 5 个 TODO**。
> 这份 solution 是"跟不上时的安全网 + 对照检查",别直接抄。验收看你自己的实现。

## 里面是什么

- `agent.py` —— 完整的多工具 Agent(handbook_qa + get_course + enrol + 多轮记忆)· **CLI 版**,终端跑三轮
- `service/` —— **验收 #5:接成 `/agent-chat` 端点**(`agent_service.py` + 路由 + README)
- `mcp_server.py` —— bonus:把同样工具暴露成 MCP server
- `bonus-human-in-loop/` —— **进阶自学**:揭开 LangGraph(见下)

## 前置条件(缺一不可)

1. **作品一(AI-1)的 RAG 就绪**:`scripts/rag.py` + `chroma_db/`
   (agent 的 `handbook_qa` 工具会 `from scripts.rag import rag_answer`)
2. **后端在跑**:`docker-compose up -d`,`curl localhost:8080/api/courses` 能返课程
   —— `get_course` / `enrol` 真打后端,不起后端会 connection refused
3. `.env` 里有 Groq key

## 怎么跑

```bash
python lessons/lesson-08-agent-workshop/solution/agent.py
```

**期望**(三轮对话,证明多轮记忆):
```
USER: Tell me about CS101.
AGENT: CS101, Introduction to Computer Science, 3 credits...     ← get_course

USER: How many credits is it?
AGENT: It's 3 credits.                                           ← "it" 记忆 = CS101

USER: Sign me up for it. My student id is 1.
AGENT: You have been successfully enrolled in CS101.            ← enrol 真打后端
```

**核心看点**:agent **自己**决定调哪个工具、"it" 跨轮指代 CS101、enrol 真写数据库。你没写一行 if-else。

> 为什么用 CS101 不用 CS201:CS201 需要先修 CS101(后端的前置课规则),fresh student
> 选 CS201 会被拒绝。想看 agent 怎么优雅处理"被拒绝",可以另开一轮问它选 CS201。

## 🌟 进阶:`bonus-human-in-loop/` —— 揭开 LangGraph

如果你好奇 "我们的 langgraph agent 好像只用了 LangChain,node/edge 在哪?",
这个文件夹三步拆开黑盒(**只要 Groq key,不用后端**):

```bash
cd lessons/lesson-08-agent-workshop/solution/bonus-human-in-loop
python step0_builder.py       # 把 create_react_agent 用 builder 拆开(看到 StateGraph/node/edge)
echo "" | python step_a_interrupt.py   # 加中断:工具前暂停 → 回车批准 → 恢复
python step_b_approval.py     # 只在 enrol(写)前暂停:读直通 / 批准 / 拒绝
```

看 `bonus-human-in-loop/README.md` 有完整走查 + 图。

**一句话**:`create_react_agent` 是自动挡好开但看不见引擎;手写这个图 = 手动挡,
看清 LangGraph 是**一个能暂停、能分支、能等人的状态机**。生产里高风险动作
(选课、付款、删数据)都要这种"人工审批门"。

## 跑通之后

回去把 `../starter/agent.py` 的 TODO 自己填一遍,提作品二的 PR。**验收看你自己的实现 + 三轮记忆 demo + enrol 真进数据库。**
