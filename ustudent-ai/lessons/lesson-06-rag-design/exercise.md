# 作品一 · RAG 选课问答助手

> 对应 AI-1 课(RAG 设计+搭建)。难度:中。预计课内完成 + 课后打磨 1 小时。
>
> **这是你的第一个简历项目**。课内我会带你把核心搭通, 你回家打磨到 golden set 全过。
>
> 🆘 **跟不上 live-code?** `solution/` 里有能跑的完整版 —— 看 `solution/README.md`,
> 先跑通 demo 看到 RAG 怎么答题 + 兜底, 再回来自己填 `starter/` 的 TODO。作品一验收看你
> 自己的实现, **别直接抄**。

## 学习目标

- **把 lesson-5 的检索接上 prompt → LLM → return**, 变成能回答问题的 RAG。
- **实现阈值兜底** —— handbook 里没有的问题, 不硬答、不编。
- **返回 sources** —— 可追溯 + 自己调 bug 的唯一线索。

## 前置条件

lesson-5 的索引已建好(从项目根):

```bash
python scripts/search_handbook.py build   # 生成 chroma_db/, collection "handbook"
```

`.env` 里有有效的 Groq key(lesson 4 配好的)。

## 你要做的(6 步)

### Step 1 · 拉 starter, 建 branch

```bash
cd <你的 ustudent-ai>
git pull
git checkout -b feature/<你的名字>-rag
cp lessons/lesson-06-rag-design/starter/scripts/rag.py        scripts/rag.py
cp lessons/lesson-06-rag-design/starter/app/routes/rag.py     app/routes/rag.py
```

### Step 2 · 填 `retrieve`(TODO 1, **课上完成**)

把 lesson-5 `search_handbook.py` 的 query 逻辑搬过来, 但返回
`{"text": ..., "distance": ...}` 的列表。

### Step 3 · 填 `rag_answer`(TODO 2+3, **课上完成**)

- **TODO 2 兜底**:检索为空, 或最近距离 > `DISTANCE_THRESHOLD` → 返回 `FALLBACK`, 不喊 LLM。
- **TODO 3 正常路径**:拼 context → format `RAG_PROMPT` → `chat(...)` temperature=0.1 → 组装 `RagResult`。

跑一次:
```bash
python scripts/rag.py
```
**期望**:g1 正常答 120, g3(AWS region)兜底。

### Step 4 · 接 `/rag-ask` 端点(**课上完成**)

填 `app/routes/rag.py` 的 TODO, 然后 `app/main.py` 加:
```python
from app.routes import rag
app.include_router(rag.router, tags=["rag"])
```
Swagger UI http://localhost:8000/docs → POST /rag-ask 试一发。

### Step 5 · 跑 golden set 4 题(**回家做**)

| 题 | 问题 | 期望 |
|---|---|---|
| g1 | How many credits do I need to graduate? | 答 120 |
| g2 | Can a freshman take CS201? | No(需 sophomore) |
| g3 | What AWS region does the system run in? | **兜底**(handbook 无) |
| g4 | What happens if my GPA drops below 1.0? | suspension(§8) |

**4 题全过才算作品一达标**。没过的, 判断是**检索错**还是**生成错**(见 demo 段 2.4), 分别去调检索层 or prompt。

### Step 6 · 提 PR + 截图

```bash
git add scripts/rag.py app/routes/rag.py app/main.py
git commit -m "作品一: RAG /rag-ask with sources + fallback"
git push
```

**截图提交**:
1. `python scripts/rag.py` 跑完 4 题的输出(看到 g3 fallback=True)
2. `/docs` 里 `/rag-ask` 调用 g1 的响应(含 sources)
3. PR 链接贴 Jira

## 验收(作品一)

- ✅ handbook 有的 → 正确回答
- ✅ handbook 没有的 → 兜底, 不编
- ✅ 返回带 `sources`(text + distance)
- ✅ `temperature ≤ 0.2`
- ✅ `/rag-ask` 端点 + `/docs` 可调
- ✅ golden set 4 题全过

## 提示 / 别走偏

- **别删 sources 字段** —— 就算前端不显示, 它是你调 bug 的唯一线索, 也是 Responsible AI 的可追溯性。
- **prompt 一定要有负面约束** —— "Answer ONLY using the material"。没有它, g3 会被编出一个假 AWS region。
- **阈值别设太低** —— 设成 0.1 会导致所有问题都兜底(因为真实距离都 > 0.1)。1.5 是经验值, 可以自己 sweep。
- **温度必须低**(0.1)—— RAG 要事实性, 不要创意。
- **不需要改 lesson-5 的 `search_handbook.py`** —— 索引复用, `rag.py` 是新加的一层。

## 常见问题

- **`Collection handbook does not exist`** → 没先跑 lesson-5 的 `build`。
- **`ModuleNotFoundError: No module named 'app'`** → 从项目根跑, 或确认 `rag.py` 顶部有 `sys.path.insert(0, str(ROOT))`。
- **g3 没兜底反而编了 AWS region** → prompt 缺负面约束, 或阈值设太高。
- **所有问题都兜底** → 阈值设太低(比如 0.1), 调回 1.5。
- **`tokenizers ... forked` 警告** → 无害, starter 已 `TOKENIZERS_PARALLELISM=false` 抑制。
