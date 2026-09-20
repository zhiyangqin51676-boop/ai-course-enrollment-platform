# AI-1 (RAG) · 参考答案(跟不上时用)

> ⚠️ **先自己做 `../exercise.md`(作品一)**。这是"安全网" —— 课上跟不上 live-code,
> 或想对照检查时再看。**作品一是简历项目, 自己写一遍才算你的。**

## 里面是什么

- `rag.py` —— 完整的 RAG pipeline(retrieve → 阈值兜底 → prompt → LLM → 组装 sources)
- `app/routes/rag.py` —— `/rag-ask` FastAPI 端点

## 前置条件(缺一不可)

1. **lesson-5 的索引已建好**:
   ```bash
   python scripts/search_handbook.py build   # 生成 chroma_db/, collection "handbook"
   ```
2. **`app/llm.py` 存在**(lesson 4 你复制过的那个, 提供 `chat()`)
3. **`.env` 里有 Groq key**(`LLM_API_KEY=gsk_...`)

## 怎么跑(2 步)

### 1. 复制到你的项目

```bash
# 在你的 ustudent-ai 项目根目录
cp lessons/lesson-06-rag-design/solution/rag.py            scripts/rag.py
cp lessons/lesson-06-rag-design/solution/app/routes/rag.py app/routes/rag.py
```

### 2. 跑 RAG demo

```bash
python scripts/rag.py
```

**期望**(golden set 4 题):
```
=== Q: How many credits do I need to graduate?   → 答 120,  fallback False
=== Q: Can a freshman take CS201?                 → 需 sophomore, fallback False
=== Q: What AWS region does the system run in?    → 兜底!  fallback True
=== Q: What happens if my GPA drops below 1.0?    → suspension, fallback False
```

**看 g3 那个 `fallback True`** —— handbook 里没有 AWS region, RAG 主动兜底不编。这就是作品一的核心。

### 3. (可选)起端点

在 `app/main.py` 加两行:
```python
from app.routes import rag
app.include_router(rag.router, tags=["rag"])
```
然后 `uvicorn app.main:app --reload --port 8000`, 打开 http://localhost:8000/docs 试 `/rag-ask`。

## 跑通之后

**回去把 `starter/scripts/rag.py` 的 TODO 自己填一遍**, 再提作品一的 PR。看过答案怎么
工作, 自己写才记得住 —— 而且验收看的是你自己的实现 + golden set 4 题全过。

## 常见问题

- **`Collection handbook does not exist`** → 没先跑 lesson-5 的 `build`。
- **`ModuleNotFoundError: No module named 'app'`** → 从项目根跑; solution 顶部已 `sys.path.insert`。
- **`No module named app.llm`** → 你还没从 lesson 4 复制 `app/llm.py`。
- 更多见 `../pitfalls.md`。
