# U+ AI Bootcamp · Local Stack (Student Pack)

This folder is all you need to run the entire course selection system + AI
assistant on your laptop. **No source code required** — everything runs from
pre-built Docker images.

## Prerequisites

- **Docker Desktop** (Windows / macOS) with at least **4 GB memory**
  (Settings → Resources → Memory). Or Docker Engine on Linux.
- ~2 GB free disk space (first-run image download).
- A network that can reach `docker.io` (Docker Hub).

## Setup (3 commands)

```bash
# 1. copy env template
cp .env.example .env
# 2. (optional for lesson 1) edit .env and paste your Groq API key
#    — you only need this from lesson 3 onwards
# 3. start everything
docker-compose up -d
```

First run: pulls **~1.3 GB** of images. On a decent broadband connection:
**5-10 minutes** — this is normal, not a failure. Subsequent runs: 5-10 seconds.

> **Do this the day BEFORE lesson 1** so you're not sitting through a
> download while everyone else is watching. Once the images are cached,
> starting the stack is instant.

Watch progress:
```bash
docker-compose ps
docker-compose logs -f ustudent-backend   # backend takes ~30s to init Flyway + Spring Boot
```

## Verify

Once all four containers report `healthy` (`docker-compose ps`), open:

| URL | Expected |
|---|---|
| http://localhost:3000 | React frontend login page |
| http://localhost:8080/actuator/health | `{"status":"UP"}` |
| http://localhost:8000/health | `{"status":"ok"}` |
| http://localhost:8000/docs | FastAPI Swagger UI |

## Log in

Sample students that ship with the stack:

| Username | Role |
|---|---|
| `john_student` | Student (pre-enrolled in CS101) |
| `jane_student` | Student |
| `prof_wilson` | Teacher |
| `prof_brown` | Teacher |

(See the handbook `runbooks/How to set up your local stack` for how to log in.)

After logging in you'll see a **💬 AI Chat** link in the top navbar — that's
the /agent-chat endpoint fronted by a chat UI. Try:

1. "What is CS101 about?"
2. "How many credits is it?" (the agent should remember "it" = CS101)
3. "Sign me up for it."
4. Switch back to Dashboard → My Courses tab → refresh — CS101 should appear.

**Fun aside**: if you try `Sign me up for CS201.` instead, the agent will
tell you `CS201 requires the prerequisite CS101`. That's a real business rule
enforced by the backend, not by the AI — which nicely demonstrates that the
agent is really talking to the system, not making things up.

## Common commands

```bash
docker-compose ps                        # 状态
docker-compose logs -f ustudent-ai       # 实时看某服务日志
docker-compose restart ustudent-ai       # 重启某服务
docker-compose pull                      # 拉最新 image（老师发布新版后）
docker-compose down                      # 全部停(保留数据库数据)
docker-compose down -v                   # 全部停 + 删数据库(慎用)
```

## Troubleshooting

**"port already in use" on 3000/5432/8000/8080**
→ 找出占用进程:`lsof -iTCP -sTCP:LISTEN -n -P | grep <port>`,或临时改 docker-compose 里 host 端口(如 `"3001:80"`)。

**Docker OOMKilled**
→ Docker Desktop → Settings → Resources → Memory ≥ 4 GB(6 GB 更稳)。

**Backend `unhealthy`**
→ 后端启动要 30-60 秒,先等等。持续 unhealthy 看 `docker-compose logs ustudent-backend`。

**Frontend `/dashboard` 或 `/ai-chat` 报 404**
→ 你可能拉了旧 image。跑 `docker-compose pull && docker-compose up -d`。

**AI Chat 说 "Sorry, the agent returned an error"**
→ `.env` 里没配 `LLM_API_KEY`。第 3 课之前用不上 AI Chat,可以忽略。

问题解决不了群里 @ 助教。
