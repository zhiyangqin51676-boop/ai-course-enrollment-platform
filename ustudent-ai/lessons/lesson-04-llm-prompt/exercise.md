# 作业 4 · 让 `/ask` 变成 handbook 助手

> 对应教案第 4 课。难度:中。预计用时:60–90 分钟。
>
> **今天课上,我会带你敲完前 3 步(招 1)**。回家把招 2、3 补上,提 PR。

## 学习目标

- **真的调一次 LLM**(用你自己的 Groq key)。
- **同一个问题跑 3 版 prompt**,亲眼看到答案质量的进化。
- **让 LLM 只回答 handbook 相关问题** —— constrained assistant 的入门姿势。

## 前置条件(课上已完成)

- 你有自己的 Groq API key
- `.env` 里已填 `LLM_API_KEY=gsk_...`
- 跑 `uvicorn app.main:app --reload --port 8000`,看不到 "LLM_API_KEY is not set" 报错

如果这两条没做,回 `docs/runbook-groq-key.md`。

## 你要做的(6 步)

### Step 1 · 拉 starter,建 branch

```bash
cd <你的 ustudent-ai>
git pull
git checkout -b feature/<你的名字>-lesson4
```

`lessons/lesson-04-llm-prompt/starter/` 里有:
- `app/config.py` —— Pydantic Settings,读 `.env`(**已给,复制就用**)
- `app/llm.py` —— LLM 调用封装(**已给,复制就用**)
- `app/routes/ask.py` —— endpoint 骨架(**有 TODO,你写**)
- `tests/test_ask.py` —— prompt 拼接的单元测试(**已给,你让它绿**)

### Step 2 · 复制 starter 里的两个"已给"文件

```bash
cp lessons/lesson-04-llm-prompt/starter/app/config.py    app/config.py
cp lessons/lesson-04-llm-prompt/starter/app/llm.py       app/llm.py
```

打开这两个文件**读一遍** —— 你不需要改,但要知道:
- `config.py` 从 `.env` 读 `LLM_API_KEY / LLM_BASE_URL / LLM_MODEL`
- `llm.py` 提供两个函数:
  - `ask_llm(question, temperature)` —— 单条 user 消息
  - `chat(messages, temperature, response_format=None)` —— 多条 messages,支持 json_object 模式

### Step 3 · 写 `ask_v1`:System Message 定角色(**课上完成**)

打开 starter 的 `app/routes/ask.py`,填 `ask_v1` 的 TODO:

```python
def build_v1_messages(handbook: str, question: str) -> list[dict]:
    """招 1: system message 让 LLM 只按 handbook 回答。"""
    system = f"""You are a helpful student advisor at U+.
Answer ONLY using facts from the handbook below.
If the answer is not in the handbook, say "I don't know based on the handbook."

Handbook:
{handbook}
"""
    return [
        {"role": "system", "content": system},
        {"role": "user",   "content": question},
    ]

@router.post("/ask", response_model=AskV1Response)
def ask_v1_endpoint(req: AskRequest) -> AskV1Response:
    messages = build_v1_messages(HANDBOOK_TEXT, req.question)
    answer = chat(messages, temperature=0)
    return AskV1Response(answer=answer)
```

在 `app/main.py` 加两行:
```python
from app.routes import ask
app.include_router(ask.router, tags=["ask"])
```

**手工验证**:
```bash
# 起服务
uvicorn app.main:app --reload --port 8000

# 打开 http://localhost:8000/docs → POST /ask → 试
# handbook 内: "How many credit points do I need to graduate?" → 应有答案
# handbook 外: "What's the weather in Sydney today?" → 应拒答
```

**跑测试**:
```bash
pytest tests/test_ask.py::test_build_v1_messages -v
# 应绿
```

### Step 4 · 加 `ask_v2`:Few-shot 引导 JSON 格式

在 `ask.py` 里加 `build_v2_messages` + `ask_v2_endpoint`,挂在 `/ask/v2`:
- 在 system 里加 2 个 examples,展示期望输出格式
- 输出仍然是自由文本(可能是 JSON 字符串,可能不是)

```python
def build_v2_messages(handbook: str, question: str) -> list[dict]:
    """招 2: few-shot 引导模型吐 JSON。"""
    system = f"""You are a helpful student advisor at U+.
Answer using ONLY the handbook.
Return a JSON object: {{"answer": "...", "citation": "..."}}
If not in handbook: {{"answer": "unknown", "citation": ""}}

Handbook:
{handbook}

Examples:
User: How many credits do I need to graduate?
You: {{"answer": "120 credit points", "citation": "Undergraduate degree requires 120 credit points."}}

User: What's the capital of France?
You: {{"answer": "unknown", "citation": ""}}
"""
    return [
        {"role": "system", "content": system},
        {"role": "user",   "content": question},
    ]
```

**验证**:
- 手工调 `/ask/v2`,看输出**大概率**是 JSON 字符串,但可能带 ```json``` fence 或多余解释
- **这是 v3 要解决的问题**

跑 `pytest tests/test_ask.py::test_build_v2_messages -v` → 应绿。

### Step 5 · 加 `ask_v3`:强制 JSON + Pydantic 兜底

在 `ask.py` 里加 `build_v3_messages` + `ask_v3_endpoint`,挂在 `/ask/v3`:
- 用 `chat(..., response_format={"type": "json_object"})` 强制 JSON
- 用 `AskV3Response.model_validate_json(raw)` parse

```python
class AskV3Response(BaseModel):
    answer: str
    citation: str

@router.post("/ask/v3", response_model=AskV3Response)
def ask_v3_endpoint(req: AskRequest) -> AskV3Response:
    messages = build_v3_messages(HANDBOOK_TEXT, req.question)
    raw = chat(messages, temperature=0, response_format={"type": "json_object"})
    return AskV3Response.model_validate_json(raw)
```

**验证**:调 `/ask/v3`,输出**每次都是稳定 JSON**,直接可以塞进下游程序。

### Step 6 · 提 PR + 截图

```bash
git add app/ tests/
git commit -m "U4-XX: /ask handbook assistant, 3 prompt versions"
git push
```

**截图提交(3 张)**:
1. `pytest -v` 全绿
2. 打三次 `/ask` `/ask/v2` `/ask/v3`,**同一个问题**,3 个 endpoint **输出对比**
3. PR 链接贴 Jira

## 验收

- ✅ `/ask` `/ask/v2` `/ask/v3` 三个 endpoint 都在 `/docs`
- ✅ handbook 内问题(credits/GPA/enrolment)三个 endpoint 都有答
- ✅ handbook 外问题(weather/politics) v1 拒答,v3 返回 `{"answer": "unknown", "citation": ""}`
- ✅ v3 输出**每次都是有效 JSON**(不带 ```json``` fence)
- ✅ pytest 全绿

## 提示 / 别走偏

- **别把 handbook 塞 user message 里**。放 system,防 prompt injection。
- **temperature=0** 三版都用,方便对比。
- **不需要 mock LLM**。这节课我们直接真调,让你看到真答案。**下节课接 RAG 时再引入 test 姿势**。
- **每人自己 key,不要借同学的**。Groq 的 rate limit 是按 key 算的。
- **别 commit `.env`**。`git status` 确认它不在 staged 里。
- 你**不需要**改 `app/llm.py`,那是给你的封装,用就好。

## 常见问题

- **`LLMNotConfigured: LLM_API_KEY is not set`** → `.env` 没建/没填/位置错。看 `docs/runbook-groq-key.md`。
- **`RateLimitError 429`** → Groq 一分钟 30 次,你 debug 时跑太快。等 30 秒或看 pitfalls.md。
- **v2 输出偶尔不是 JSON** → 正常,这就是 v3 要解决的问题。别改 v2 去追求"稳定 JSON",那是 v3 的事。
- **v3 `JSONDecodeError`** → 你没加 `response_format={"type": "json_object"}`,或 Pydantic model 字段跟 prompt schema 对不上。
- **看不到 `/ask`** → `app/main.py` 没 `include_router`。
