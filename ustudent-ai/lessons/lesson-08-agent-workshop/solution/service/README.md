# 作品二验收 #5:把 agent 接成 `/agent-chat` 服务

`../agent.py` 是 **CLI demo**(终端里跑三轮)。要满足验收第 5 条"FastAPI
`/agent-chat` 端点 + thread_id",把 agent **服务化** —— 这两个文件就是参考:

| 文件 | 复制到你的项目 | 作用 |
|---|---|---|
| `agent_service.py` | `app/agent_service.py` | agent + 3 工具, 放进 app/ 让路由能 import; `run()` 返回 `{answer, tool_calls}` |
| `agent_chat.py` | `app/routes/agent_chat.py` | 薄路由: `POST /agent-chat {message, thread_id}` → `{answer, tool_calls}` |

**和 `../agent.py` 的区别**:
- `agent.py` 的 `chat()` 返回**字符串**(CLI 打印够用)。
- `agent_service.py` 的 `run()` 返回 **`{answer, tool_calls}`** —— 因为前端 AI Chat 页要展示"调了哪些工具"的 trace。
- 放在 `app/` 下(不是 lessons/), 这样 `app/routes/agent_chat.py` 能 `from app.agent_service import run`。

## 接上去(3 步)

```bash
# 1. 复制两个文件到位
cp lessons/lesson-08-agent-workshop/solution/service/agent_service.py app/agent_service.py
cp lessons/lesson-08-agent-workshop/solution/service/agent_chat.py     app/routes/agent_chat.py

# 2. app/main.py 里挂路由(加两行)
#    from app.routes import agent_chat
#    app.include_router(agent_chat.router, tags=["agent"])

# 3. 起服务(后端要先起! 见 exercise 的"准备"段)
uvicorn app.main:app --reload --port 8000
```

## 验证(多轮记忆跨 HTTP 请求)

```bash
curl -s -X POST http://localhost:8000/agent-chat -H 'Content-Type: application/json' \
  -d '{"message":"Tell me about CS101.","thread_id":"t1"}' | python3 -m json.tool
# 再用【同一个 thread_id】问 "How many credits is it?" → 应答 3 credits(it=CS101)
curl -s -X POST http://localhost:8000/agent-chat -H 'Content-Type: application/json' \
  -d '{"message":"How many credits is it?","thread_id":"t1"}' | python3 -m json.tool
```

响应里有 `answer` + `tool_calls`(第 1 轮 `tool_calls` 含 `get_course`)。

## 前置条件(同 `../README.md`)

- 后端起着(`docker-compose -f student-pack/backend.docker-compose.yml up -d`)
- `.env` 的 `USTUDENT_BACKEND_URL=http://localhost:8080`(**别用容器名**, 否则死循环)
- `scripts/rag.py` + `chroma_db/`(作品一)+ `app/llm.py` + Groq key

## 接前端

前端 AI Chat 页(`/ai-chat`)POST 到 `/ai/agent-chat`(nginx 转发到 `:8000/agent-chat`),
展示 `answer` + 可折叠的 `tool_calls` trace。你这个端点起来后, 前端就能直接聊。
