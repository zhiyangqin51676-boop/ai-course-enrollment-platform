# Lesson 8 · Workshop — Project 2: Multi-tool Agent + MCP

This is **Portfolio Project 2**. End of class, you should have:

  * A 3-tool LangGraph agent that picks the right tool per question
  * Multi-turn memory — "Sign me up for it" resolves "it" from earlier
  * (Bonus) An MCP server exposing the same tools

## 准备

```bash
source .venv/bin/activate
pip install -r requirements.txt   # langgraph + mcp already pinned
```

确认你已经有：
- `chroma_db/` (作品一 AI-1 建的，`python scripts/search_handbook.py build`)
- `scripts/rag.py` (作品一你写的 RAG，agent 的 handbook_qa 工具会 import 它)
- `app/llm.py` + `.env` 里的 Groq key
- ustudent backend 在 `localhost:8080` 跑 —— **enrol/get_course 真打后端，不起后端会 connection refused**。
  用预建镜像秒起(不用源码、不用 build):
  ```bash
  docker-compose -f student-pack/backend.docker-compose.yml up -d
  # 等 ~45s 后端跑完 migration, 验证:
  curl http://localhost:8080/api/courses     # 应返 5 门课
  ```

## 任务

1. 填完 `agent.py` 的 5 个 TODO，跑：`python agent.py`
2. 期望看到三轮对话，**"it" 在第 2/3 轮指代 CS101**(用 CS101 因为它无前置课；CS201 需先修 CS101，选课会被后端拒）
3. （可选）填完 `mcp_server.py`，跑：`python mcp_server.py`，用 inspector 工具试调

## 验收

- 三轮 demo 通过：tool 选对、记忆生效、enrol 真打了后端
- 看 trace（agent 返回里有 tool_calls）确认是 LLM 自己选的工具，不是 if-else

详细要求看 `../exercise.md`。
