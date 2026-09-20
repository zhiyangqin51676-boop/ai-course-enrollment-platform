# ustudent-ai

U+ AI 训练营 · AI 服务层 · **FastAPI + Python 3.11+**

后端(Spring Boot Kotlin)、前端(React)、AI 服务(本仓库)三件套并存。
你的 AI 代码全都写在这里,不要动其他仓库。

## 🚀 本地跑起来

### macOS / Linux

```bash
# 1. 建虚拟环境并激活
python -m venv .venv
source .venv/bin/activate

# 2. 装依赖
pip install -r requirements-dev.txt

# 3. 起服务 (--reload 改代码自动重启)
uvicorn app.main:app --reload --port 8000

# 4. 浏览器打开 http://localhost:8000/docs 试端点
# 5. 另开一个终端: 跑测试
pytest -v
```

### Windows (PowerShell)

```powershell
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements-dev.txt
uvicorn app.main:app --reload --port 8000
```

### 验证服务起来了

```bash
curl http://localhost:8000/health
# 应返回: {"status":"ok"}
```

## 📁 仓库结构

```
ustudent-ai/
├── app/
│   ├── main.py              ← FastAPI 入口。挂 router 的地方
│   ├── config.py            ← 环境变量 / Pydantic Settings
│   └── routes/              ← 每个端点一个模块
│       ├── health.py
│       └── echo.py
├── tests/                   ← pytest 测试
├── data/                    ← RAG 语料 / 评估集
├── docs/                    ← 后端 API 契约
├── docker-compose.yml       ← 从源码 build + 起整栈
├── student-pack/            ← 用 Docker Hub 预建镜像的极简 compose
├── lessons/                 ← 每节课的作业说明 + starter 代码
├── requirements.txt         ← 生产依赖
├── requirements-dev.txt     ← 生产 + pytest
├── Dockerfile
└── bitbucket-pipelines.yml
```

## 🧭 每节课作业

- **作业说明**:`lessons/lesson-XX/exercise.md`
- **常见坑**:`lessons/lesson-XX/pitfalls.md`
- **起点代码**:`lessons/lesson-XX/starter/`
- **提交流程**:见 Confluence Runbook「Assignment Instruction」

## 🐳 想不 clone,直接跑整栈?

回到 Confluence Runbook「How to set up your local stack」——
用老师打好的 Docker Hub 镜像,3 分钟起整套四件套。

## ❓ 遇到问题

按顺序:

1. 看当前作业的 `lessons/lesson-XX/pitfalls.md`
2. 看 Confluence Runbooks
3. 群里 @ 助教
