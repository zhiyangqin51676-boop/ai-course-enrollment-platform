# AI-1 (RAG) 翻车点 cheatsheet

按"症状 → 原因 → 一句话解法"组织。

---

## 环境 / 索引段

### `Collection handbook does not exist`
- **原因**:没先跑 lesson-5 的 `build`, 或换了 collection 名。
- **解法**:`python scripts/search_handbook.py build`。确认 `chroma_db/` 出现。

### `ModuleNotFoundError: No module named 'app'`
- **原因**:`python scripts/rag.py` 时项目根不在 sys.path。
- **解法**:确认 `rag.py` 顶部有 `sys.path.insert(0, str(ROOT))`, 且从项目根跑。

### `tokenizers ... forked after parallelism` 警告刷屏
- **原因**:sentence-transformers + fork 的已知警告。
- **解法**:无害。starter 已 `os.environ.setdefault("TOKENIZERS_PARALLELISM","false")` 抑制。

### 依赖版本打架(huggingface-hub / transformers)
- **症状**:`huggingface-hub>=... ,<1.0 is required ... but found 1.22.0`。
- **原因**:`sentence-transformers 5.x` 拉了太新的 hub, 和 chromadb 0.5.23 卡的 `tokenizers<=0.20.3` → transformers 4.46 冲突。
- **解法**:钉版本 `sentence-transformers==3.3.1` + `huggingface-hub<1.0`。requirements-dev 已收窄到 `<4.0`。

---

## RAG 逻辑段

### g3(AWS region)没兜底, 反而编了一个假 region
- **原因 A**:prompt 缺负面约束("Answer ONLY using the material")。
- **原因 B**:阈值设太高(比如 3.0), 太远的片段也被当"有材料"喂进去。
- **解法**:两个都查。负面约束必须在; 阈值 1.5 左右。

### 所有问题都兜底(包括 g1)
- **原因**:阈值设太低(比如 0.1)。真实距离 g1≈0.5、g4≈1.0, 都 > 0.1 → 全兜底。
- **解法**:阈值调回 1.5。用 golden set sweep 一下不同阈值的兜底命中率。

### g2(freshman CS201)答错
- **原因**:这是隐式多跳题 —— 要同时命中 §4(CS201 需 sophomore)和常识(freshman ≠ sophomore)。top-3 可能没同时捞到。
- **解法**:top-K 调到 5; 或确认 §4 prereq 段被正确索引。**这题本身就是教"检索质量决定一切"的好案例。**

### 答案里混入检索片段的原文噪声
- **症状**:LLM 把 `---- MATERIAL ----` 边界符也回显出来。
- **解法**:prompt 里明确 "Be concise. Answer in your own words." 一般 llama-3.3 不会犯。

### `sources` 字段被学生删了
- **原因**:觉得"答案干净点"。
- **解法**:**强制留下**。这是验收硬指标, 也是调 bug 唯一线索。

---

## FastAPI 段

### `/rag-ask` 返回 500
- **原因**:`scripts.rag` import 失败(路径), 或 `rag_answer` 抛异常。
- **解法**:看 uvicorn log。多半是 chroma_db 没建 or app 路径问题。

### `/rag-ask` 返回 422
- **原因**:请求 JSON 字段名不对(应为 `question`)。
- **解法**:看 `/docs` schema 对照。

### `sources` 序列化报错
- **症状**:`ValidationError: Source ... text field required`。
- **原因**:`retrieve` 返回的 dict key 和 `Source` model 字段对不上。
- **解法**:确认 retrieve 返回 `{"text", "distance"}`, Source 也是这俩字段。

---

## 教学纪律

### 学生想用 Chroma 内置 embedding(query_texts)
- **症状**:学生写 `col.query(query_texts=[q])` 而不是显式 encode。
- **原因**:看到 Chroma 文档的简写。
- **解法**:**统一用 lesson-5 的显式 `SentenceTransformer` + `query_embeddings`** —— 索引是那样建的, 混用会维度/模型不一致。

### 学生想现在就加 rerank / hybrid
- **解法**:"**先把 80 分的基础 RAG 交了**。rerank 是 90 分的进阶, 作品一达标后再加。"

### 学生纠结阈值精确值
- **解法**:"**阈值是经验值, 不是算出来的**。1.5 能过 golden set 就行。真实项目用 golden set sweep。别卡在这。"

---

## 时间管理

### 段 2 理论讲太久, 没时间搭
- **解法**:段 2 严格 20 分钟。失败模式表只讲 3 行, 其余留"90 分之外弹药"。

### 学生搭 rag_answer 卡壳, 全班进度拖慢
- **解法**:老师带搭段(段 4)已经完整演示过。卡的学生**直接抄 demo 的代码**, 理解留课后。**先跑通闭环, 再理解细节。**

### 快的学生 40 分钟就交了
- **解法**:进阶题伺候 —— 元数据过滤 / rerank / 阈值 sweep / 接前端。见 demo "90 分之外弹药"。
