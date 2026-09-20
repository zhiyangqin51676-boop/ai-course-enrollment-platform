# 第 5 课翻车点 cheatsheet

按"症状 → 原因 → 一句话解法"组织, 便于现场快速判断。

---

## 环境 / 依赖段

### `sentence-transformers` 首次下载慢
- **症状**:`SentenceTransformer('all-MiniLM-L6-v2')` 卡 30 秒 - 3 分钟。
- **原因**:模型 ~80MB, 首次从 HuggingFace 下。
- **解法**:上课前一天群里让每个学生跑:
  ```bash
  python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('all-MiniLM-L6-v2')"
  ```
  下完 cache 在 `~/.cache/huggingface/`, 后续 0.5 秒加载。

### `pip install sentence-transformers` 装了 30 分钟
- **原因**:依赖 `torch` (~1GB)。
- **解法**:等。若真的很慢, 换镜像源(墙内): `pip install -i https://pypi.tuna.tsinghua.edu.cn/simple sentence-transformers`。

### `chromadb` 版本冲突
- **症状**:import chromadb 报 `pydantic v1/v2 mismatch` 或类似。
- **原因**:chromadb 依赖 pydantic v2, 但学生的 venv 有 v1 老库。
- **解法**:`pip install --upgrade chromadb pydantic` —— 让 chromadb 决定版本。

### Mac M 系列 CPU 卡
- **症状**:build handbook (10 段) 耗时 30+ 秒。
- **原因**:torch on Apple Silicon 需要 MPS 后端。
- **解法**:一般 `sentence-transformers` 自动用 MPS。若真慢, 显式 `SentenceTransformer('all-MiniLM-L6-v2', device='cpu')` 也没差多少 —— **10 段一次 encode 5 秒内**。若超, 换 device。

---

## Chroma 段

### `chromadb.errors.NotFoundError: Collection ... does not exist`
- **原因**:query 前没先 build; 或换了 collection 名。
- **解法**:先跑 `python scripts/search_handbook.py build`, 或改回原名。

### 重复 build 后 count 越来越大
- **症状**:`col.count()` 每次跑 build 都 x2。
- **原因**:忘了 `delete_collection` 就 add 新的, 老数据留着。
- **解法**:build 开头统一:
  ```python
  try: client.delete_collection("handbook")
  except: pass
  ```

### 磁盘不清理, `chroma_db/` 越长越大
- **症状**:反复重建后 disk 用几 GB。
- **原因**:Chroma 内部日志 + segment 累积。
- **解法**:定期或每次实验后:
  ```bash
  rm -rf chroma_db/
  ```
  下次 build 会重建。**上课演示前老师要 `rm -rf` 一次**, 才是干净的"从零"演示。

### 换了 embedding 模型后 query 报维度不匹配
- **症状**:`shape mismatch: expected 384, got 1536`。
- **原因**:build 用 MiniLM (384 dim), query 用了 OpenAI (1536 dim)。
- **解法**:整套流程用**同一个模型**。换模型必须完整重建索引。

### 距离数看起来不像 cosine
- **症状**:top-1 distance ≈ 0.5, top-3 ≈ 0.9, **不像 cosine 的 -1 到 1 范围**。
- **原因**:Chroma **默认用 L2 距离**, 不是 cosine。
- **解法**:知道即可, 不用改。若真要用 cosine, 创建 collection 时:
  ```python
  client.create_collection("handbook", metadata={"hnsw:space": "cosine"})
  ```

---

## Embedding / 语义段

### 手算 cosine 报 `divide by zero`
- **症状**:某个向量 norm=0, cos 除零。
- **原因**:encode 传了空字符串。
- **解法**:encode 前 `assert text.strip()`, 或在 cosine 里保护:
  ```python
  na, nb = np.linalg.norm(a), np.linalg.norm(b)
  return np.dot(a,b) / (na * nb) if na and nb else 0.0
  ```

### 反义词 embedding 距离很近
- **症状**:"I love it" vs "I hate it" cos ≈ 0.8, 学生震惊。
- **原因**:embedding 抓的是**主题**(都在说"it"), **不太抓情感极性**。
- **解法**:这不是 bug, 是特性。**告诉学生**:embedding 有边界, 情感/否定/逻辑关系它抓不好。lesson 9 讲 evaluation 时再详细展开。

### 中文查英文 handbook 命中率低
- **症状**:学生用中文问 "怎么退课", top-K 相似度低。
- **原因**:MiniLM 是英文模型, 中英跨语言检索差。
- **解法**:告诉学生, 中文场景要换 multilingual:
  ```python
  SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')
  ```
  或 `BAAI/bge-large-zh`。

### query 前忘 encode
- **症状**:`col.query(query_texts=[q])` vs `col.query(query_embeddings=[q_vec])` 混用。
- **原因**:Chroma 支持两种签名。**`query_texts` 会用 collection 默认 embed model**, 若没配置则报错。
- **解法**:**统一用 `query_embeddings`, 手动 encode 后传** —— 代码更清晰, 也强制学生记得 embedding 是显式操作。

---

## 切段段

### `## ` 混用 `##`(不带空格)
- **症状**:chunk_by_h2 只切了一半的段。
- **原因**:handbook 有些标题写 `##Section` 无空格。
- **解法**:统一用 `if line.startswith("##"):` 但要保证不误伤 `###`。安全写法:
  ```python
  if line.startswith("## ") and not line.startswith("### "):
      # start new section
  ```

### 第一段没被抓到
- **症状**:handbook 开头有 title `# Handbook`, chunker 从第一个 `##` 开始, 前面丢了。
- **原因**:preamble 逻辑没处理。
- **解法**:对我们的 handbook 结构没问题(第一个 `##` 就是 §1)。若你的语料前面有内容, 加 fallback 收集器。

### 每段 title 没塞进 content
- **症状**:build 后 query "how are credits calculated?" 找不到, 因为 title 里的 "Credits" 关键词丢了。
- **解法**:document 里包含 title:
  ```python
  docs = [f"{s['title']}\n{s['content']}" for s in sections]
  ```
  已在 demo.md 段 4.3 强调。

### handbook 内容重复到多个 chunk
- **症状**:top-3 都来自同一个 section。
- **原因**:某段特别长, sentence-transformers 内部会截断 (max 256 tokens)。
- **解法**:若段真的太长, 手动再切一次:
  ```python
  if len(section["content"]) > 2000:
      sub_chunks = [content[i:i+1500] for i in range(0, len(content), 1200)]
  ```

---

## 教学纪律

### 学生想装 `torch-gpu`
- **症状**:"老师我想用 GPU 加速"。
- **解法**:"**10 段 handbook, CPU 5 秒 encode 完**。GPU 是 lesson 6+ 大语料时才有意义, 现在别装, 环境会更乱。"

### 学生把 handbook 塞给 LLM 而不是搜索
- **症状**:交作业时 `/ask` 还是全 handbook 塞进 prompt。
- **原因**:没意识到今天讲的是**为下节课准备的搜索层**。
- **解法**:验收时明确 —— **本节作业不改 `/ask` endpoint**。语义搜索先跑通, 下节接 RAG 再融合。

### 学生跑 semantic 觉得"没那么神奇"
- **症状**:自选 3 条 query, 每条都关键词命中, semantic 和 grep 并列。
- **原因**:学生想不到"用户真实说法" vs "手册用词"的差异。
- **解法**:提示"**装作你是刚入学、没读过 handbook 的新生, 你会怎么问?**"。

### 学生 debug 时反复 build, quota 心疼
- **症状**:"老师我一直在 build, 是不是烧了很多 quota?"
- **解法**:告诉学生 `sentence-transformers` **完全本地**, 不打 API, **零 quota**。Chroma 也本地。**这节课不烧 Groq**。

---

## 时间管理

### 段 3 手算 cosine 拖到 25 分钟
- **原因**:学生问 "为什么用 dot 不用别的", 深挖数学。
- **解法**:**深挖留到 pitfalls / cheatsheet**。当场只讲 "**方向近 → 意思近**", 数学证明课后。

### 段 4 建索引超时
- **原因**:chroma import 慢, 或首次 build 触发下载。
- **解法**:**提前 5 分钟让学生 warm up** —— import + `SentenceTransformer(...)` 跑一遍。

### 段 5 grep 对比只跑得完 2 条
- **原因**:每条都停下来讨论。
- **解法**:**保 Q2 + Q4** 两条(最强 semantic 优势), Q1 (关键词) 和 Q3 (change major) 可省。

### 作业 5 讲解占用 15+ 分钟
- **解法**:严格 10 分钟, 剩下让学生课后看 exercise.md。
