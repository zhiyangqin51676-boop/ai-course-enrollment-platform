# 第 8 课工作坊翻车点 cheatsheet

### ⚠️ [最高频] `.env` 的 BACKEND URL 指向容器名 → agent 死循环撞 recursion limit
- **症状**:第一轮 "Tell me about CS101" 就抛 `GraphRecursionError: Recursion limit of 25 reached`。
- **根因**:`.env` 里 `USTUDENT_BACKEND_URL=http://ustudent-backend:8080`(**docker 容器名**)。
  学生在 venv(宿主机)跑 agent 时解析不了这个名字 → `get_course` 的 httpx 连不上 →
  工具报错 → LLM **反复重试 get_course** → 撞 25 步崩。**是 AI-2 那个循环坑的"后端连不上"变种。**
- **解法**:`.env` 改成 `USTUDENT_BACKEND_URL=http://localhost:8080`(venv 跑用这个)。
  `.env.example` 默认已是 localhost;老师提醒学生**别照旧 .env 里的容器名**。
  只有 AI 服务本身也在 docker-compose 里跑时才用容器名(那时 compose 会覆盖)。
- **快速判断**:`python -c "import os;from dotenv import load_dotenv;load_dotenv();print(os.environ.get('USTUDENT_BACKEND_URL'))"` 看是不是 localhost。

### 后端没起,enrol 全失败
- **症状**:`httpx.ConnectError`,或(同上)agent 反复重试撞 recursion limit。
- **解法**:`docker-compose -f student-pack/backend.docker-compose.yml up -d`;`curl localhost:8080/api/courses` 返 5 门课再开工。

### enrol 被拒:"already enrolled" 或 "prerequisite not met"
- **症状**:enrol 调了但返回 rejected。
- **原因 1**:同一 student 重复选同一门 → "already enrolled"。重置:`docker-compose -f student-pack/backend.docker-compose.yml down -v && ... up -d`。
- **原因 2**:选了 **CS201**(需先修 CS101)→ 被拒。demo 用 **CS101**(无前置)。
- **注意**:这两种"被拒"其实是 agent **正确**处理后端业务规则, 不是 bug —— 可当加分教学点。

### "it" 指代不准
- **症状**：第二轮"How many credits is it"，模型问 "what course?"。
- **原因**：
  1. thread_id 不一致 (每轮一个新 id → 没历史)
  2. MemorySaver 是函数内变量 (每次调用新建一个，等于没记忆)
- **解法**：MemorySaver 必须 **module-level** 或 dependency injection。thread_id 必须**跨轮一致**。

### Agent 反复调用同一个工具
- **症状**：trace 里 get_course("CS201") 调了 5 次。
- **原因**：system prompt 没说"already looked up X, use that result"。
- **解法**：reference SYSTEM_PROMPT 写了"resolve pronouns to the most recent course discussed"——这句是关键。

### Tool description 写得糙,agent 选错
- **症状**：政策问题 (handbook_qa) 被错路由到 get_course。
- **诊断**：看你的 tool description 第一句话写了什么——LLM 主要靠它判断。
- **解法**：handbook_qa description 明确说"policy / handbook questions: GPA, drops, prerequisites, refunds"。**列具体场景比抽象描述管用 10 倍**。

### Enrol 工具返回 200 但实际没生效
- **症状**：agent 回"Successfully enrolled",但 `/api/me/courses?studentId=1` 没看到。
- **诊断**：检查 enrol 返回的 body —— 后端可能返 `{success: false, message: "Already enrolled"}` 但 HTTP 200。
- **解法**：reference 检查 `body.get("success")`，不能只看 HTTP code。

### Llama 3.3 偶尔不调工具直接编
- **症状**：问 "What is CS999"（不存在的课），模型不调 get_course 直接编一个描述。
- **原因**：小型化 Llama 对工具调用的"激进度"不如 GPT-4。
- **解法**：
  1. system prompt 加 "Never invent course info — always look it up"（reference 有这句）
  2. 极端情况换大一点的模型 (`llama-3.3-70b-versatile` 是当下 Groq 上最好的免费选项)

### LangGraph recursion limit hit
- **症状**：`GraphRecursionError`。
- **解法**：通常是工具死循环（调 A→ 调 B → 调 A）。先 print trace 看；再加 MAX_STEPS / `recursion_limit`。

### MCP server 起不来
- **症状**：`mcp.run()` 卡住或报错。
- **原因**：stdio 在终端里跑会等 stdin。`python mcp_server.py` 期望被 Claude Desktop 之类的客户端 spawn，单独跑会"挂"是正常的。
- **解法**：要单独测就跑 `uvicorn mcp_server:http_app --port 9000`，然后用 `mcp dev mcp_server.py` 或 inspector 工具。

### docker-compose 后端用了不同端口
- **症状**：`USTUDENT_BACKEND_URL` 没改对。
- **解法**：本机直跑用 `http://localhost:8080`，docker-compose 内服务间用 `http://ustudent-backend:8080`。**学生从 host 上跑 agent 用前者**。

### 微调讲稿超时
- **症状**：讲 LoRA 数学细节,15 分钟变 30 分钟。
- **解法**：**只讲 demo.md 的 15 分钟讲稿**。深入留课后；想做微调的学生指 HuggingFace TRL + Unsloth。
