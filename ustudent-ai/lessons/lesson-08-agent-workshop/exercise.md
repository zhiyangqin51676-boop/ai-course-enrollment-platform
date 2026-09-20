# 作品二 · 多工具 Agent + MCP

> 对应教案第 8 课工作坊。难度：中高。预计课内 + 课后用时：90-120 分钟。

## 目标

把 lesson 7 设计 + lesson 6 RAG + lesson 7 学的 LangGraph 串成一个能选工具、有记忆、操作真后端的 Agent。这是**简历的核心 AI 项目**。

## 准备（开工前，缺一不可）

### 1. 启动后端(这节起 agent 要真打后端)

从 AI-3 起,你的 agent 有 `get_course` / `enrol` 两个工具会真连 `localhost:8080`。
用预建镜像秒起(**不用源码、不用 build**):

```bash
docker-compose -f student-pack/backend.docker-compose.yml up -d
# 等 ~45s 后端跑完数据库 migration, 验证:
curl http://localhost:8080/api/courses        # 应返 5 门课
curl http://localhost:8080/actuator/health    # 应 {"status":"UP"}
```
不关就一直跑;关掉:`docker-compose -f student-pack/backend.docker-compose.yml down`。
**没起后端 → agent 一调 enrol 就 `connection refused`。**

### 2. 作品一的 RAG 就绪

agent 的 `handbook_qa` 工具会 `from scripts.rag import rag_answer`,所以你要有:
- `scripts/rag.py`(作品一你写的)
- `chroma_db/`(lesson 5 建的;没有就 `python scripts/search_handbook.py build`)

### 3. LLM 配好 + 检查 `.env` 的后端地址

- `app/llm.py`(lesson 4 复制的)+ `.env` 里的 Groq key。
- ⚠️ **如果你的 `.env` 是早先建的**,里面可能有 `USTUDENT_BACKEND_URL=http://ustudent-backend:8080`
  (docker 容器名)。你在 venv 里跑 agent 解析不了它 → **get_course/enrol 连不上 → agent 死循环撞
  `GraphRecursionError`**。改成 localhost:
  ```bash
  # Mac:
  sed -i '' 's|USTUDENT_BACKEND_URL=.*|USTUDENT_BACKEND_URL=http://localhost:8080|' .env
  # Linux/WSL: sed -i 's|...|...|' .env  (-i 后不带 '')
  grep USTUDENT_BACKEND_URL .env    # 确认是 localhost:8080
  ```
  (新的 `.env.example` 默认已是 localhost;这步只针对已有旧 `.env` 的同学。)

## 验收（必须全过）

1. ✅ Agent 能根据问题**自己**选对工具（policy → handbook_qa, 课程详情 → get_course, 选课 → enrol）
2. ✅ 多轮对话里"它/这门课"等指代正确延续（memory 生效）
3. ✅ `enrol` 工具真打了 ustudent 后端（不是 mock）
4. ✅ 跑通三轮 demo：
   - "Tell me about CS101" → 调 `get_course`
   - "How many credits is it?" → 不重调，直接答（关键是知道"it" = CS101）
   - "Sign me up for it. My student id is 1." → 调 `enrol(1, "CS101")`，**真后端真返回、真进数据库**
   - ⚠️ 用 **CS101**(无前置)。CS201 需先修 CS101，fresh student 选 CS201 会被后端拒绝，enrol 进不了库。
5. ✅ FastAPI `/agent-chat` 端点 + `thread_id` 支持
   - 把 CLI 版 agent 服务化:agent 逻辑放 `app/agent_service.py`(`run()` 返回 `{answer, tool_calls}`),薄路由放 `app/routes/agent_chat.py`,挂进 `app/main.py`。参考 `solution/service/`。
   - 响应带 `tool_calls`(前端 AI Chat 页要展示 trace)。同 `thread_id` → 跨请求记忆生效。

## 进阶（给做得快的，作品集亮点）

- 🌟 把同样工具用 MCP server 暴露（`mcp_server.py`）
- 🌟 配进 Claude Desktop 里，截图它能用你的工具
- 🌟 加 `drop_course` 工具
- 🌟 接 lesson 9 的 prompt-injection 防护（提前预习）

## 提交（Jira）

- PR 链接
- 三轮 demo 的截图（或 GIF：USER 问→AGENT 答→tool_calls trace）
- 后端 `localhost:8080/api/me/courses?studentId=1` 截图，证明 enrol 真生效了
- 50-100 字"作品二 elevator pitch"，供简历

## 简历句子模板（参考）

> Designed and implemented a 3-tool LangGraph agent on top of Llama 3.3 (via Groq's
> OpenAI-compatible API). The agent routes between a RAG-backed handbook QA tool, a
> backend lookup tool, and a real enrolment action, with multi-turn memory via
> MemorySaver. Exposed the same tool set as an MCP server so the agent is
> consumable from Claude Desktop and other MCP clients.

## 别踩的雷

- ❌ **没启动后端**就跑 enrol → connection refused。先 `docker-compose up -d`
- ❌ thread_id 写死成 `"default"` → 跨用户记忆串流，隐私问题。线上一定是 per-user
- ❌ 工具 description 写得糙 → agent 选错工具。description 是 LLM 唯一的依据
- ❌ 把 student_id 写进 system prompt → 多用户串流。**永远从入参拿**
- ❌ enrol 调用没 try/except → 后端 down 整个 agent 崩
