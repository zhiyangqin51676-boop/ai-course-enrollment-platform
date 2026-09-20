# 作业 5 · 建 handbook 语义索引 + CLI 查询

> 对应教案第 5 课。难度:中。预计用时:60–90 分钟。
>
> **今天课上, 我会带你敲完 `build` + 第一次 `query`**。回家把 `query` 补齐、写测试、跑你自己想的 3 条 query。
>
> 🆘 **跟不上 live-code?** `solution/` 里有能跑的完整版 —— 看 `solution/README.md`,
> 先跑通 demo 看到效果, 再回来自己填 `starter/` 的 TODO。**别直接抄**, 卡住看一眼就好。

## 学习目标

- **亲手把文字变成 384 维 float 向量**, 眼见"embedding = 数字"。
- **用 Chroma 建索引 + 查 top-K**, 掌握向量数据库最基本的 3 个 API。
- **对比 semantic vs `grep`**, 亲眼看到 embedding 的价值。

## 前置条件(课上已完成 / 你要先做)

1. `.venv` 已激活
2. `pip install -r requirements-dev.txt` 装了 `sentence-transformers` 和 `chromadb`
3. **提前把模型下下来**(30 秒 - 2 分钟, 别课后 debug 时才发现):
   ```bash
   python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('all-MiniLM-L6-v2')"
   ```

## 你要做的(6 步)

### Step 1 · 拉 starter, 建 branch

```bash
cd <你的 ustudent-ai>
git pull
git checkout -b feature/<你的名字>-lesson5
```

`lessons/lesson-05-embedding/starter/` 里有:
- `scripts/search_handbook.py` —— CLI 骨架, 3 个 TODO
- `tests/test_search.py` —— chunker + cosine 单测
- `README.md` —— 步骤提示

**把 starter 复制到项目位置**:

```bash
cp lessons/lesson-05-embedding/starter/scripts/search_handbook.py scripts/search_handbook.py
cp lessons/lesson-05-embedding/starter/tests/test_search.py       tests/test_search.py
```

### Step 2 · 填 `chunk_by_h2`(**课上完成**)

按 markdown `## ` 一级标题切段。返回 `[{"title": "## X", "content": "..."}]` 列表。

**跑测试** —— 应绿:

```bash
pytest tests/test_search.py::test_chunk_by_h2 -v
```

### Step 3 · 填 `build`(**课上完成**)

- 读 `data/handbook.md`
- 切段
- 每段用 `SentenceTransformer("all-MiniLM-L6-v2")` encode
- 用 `chromadb.PersistentClient(path="chroma_db")` 存
- **幂等**:每次 build 先 `delete_collection`, 避免重复

**跑一次**:
```bash
python scripts/search_handbook.py build
# 期望: "Indexed N chunks."
```

**验证 `chroma_db/` 目录已生成**。**注意**:`chroma_db/` 已加进 `.gitignore`, 不会被 commit。

### Step 4 · 填 `query`(**回家做**)

- 从命令行拿 query string
- encode
- Chroma `col.query(query_embeddings=[q_vec], n_results=3)`
- 打印 top-3, 每条含 distance + 前 80 char 预览

**跑课上讲过的对比**:
```bash
python scripts/search_handbook.py query "how do I stop taking a class?"
# 期望: top-1 命中 withdraw / drop 相关章节, distance < 1.5
```

### Step 5 · 跑 pytest 全绿

```bash
pytest tests/test_search.py -v
# 期望: 至少 4 条测试绿 (chunker 3 条 + cosine 2 条)
```

### Step 6 · 用**你自己的** 3 条 query 跑一遍 + 截图

用**你自己想的** 3 条问题(**不要重复课上讲的那 4 条**), 跑 `python scripts/search_handbook.py query "..."`, 截图 top-3 输出。

至少 1 条应该体现 semantic 优势(即 query 里没有 handbook 的原词)。

**提 PR**:
```bash
git add scripts/search_handbook.py tests/test_search.py
git commit -m "U5-XX: handbook semantic search CLI"
git push
```

**截图提交**:
1. `pytest -v` 全绿
2. `build` 输出
3. 3 条 query 的 top-3 输出
4. PR 链接贴 Jira

## 验收

- ✅ `python scripts/search_handbook.py build` 无报错生成 `chroma_db/`
- ✅ `python scripts/search_handbook.py query "..."` 返回 3 条含 distance + 内容预览
- ✅ pytest 全绿
- ✅ 3 条自选 query 中**至少 1 条**体现 semantic 优势(和 grep 对比时 grep 空手)
- ✅ PR 已提

## 提示 / 别走偏

- **别 commit `chroma_db/`**。它每次 build 会重建, 塞 git 是灾难。**`.gitignore` 已挡**。
- **cosine 和 Chroma 默认距离不是同一个** —— Chroma 用 L2, sentence-transformers 输出近似归一化, 所以差别小。**面试你要能说清区别**(见 pitfalls Q6)。
- **模型只加载一次**。如果你在同一个 script 里 `SentenceTransformer(...)` 多次调用, 加个 `@lru_cache` 或改成全局变量。**每次加载 200-500ms**。
- **query 前不需要重新 build**。Chroma 是持久化的, `build` 完文件在 disk 上, 再跑 `query` 会直接读。
- **改切段策略后必须 rebuild** —— 否则老 chunk 还留着。

## 常见问题

- **首次 build 特别慢** → 是 sentence-transformers 首次下载模型。**提前跑一次那句 import**(见前置条件 3)。
- **`chromadb.errors.NotFoundError: Collection ... does not exist`** → 你没先 `build`。或者你换了 collection 名, 但代码里还查老名字。
- **`RuntimeError: shape mismatch`** → build 用了模型 A, query 用了模型 B, 向量维度不同。**始终用同一个模型**。
- **`query` 返回的 distance 都很大(>1.5)** → 说明索引里根本没相关内容。检查 chunker 是否正确切段, 或者 handbook 里真的没有你问的东西。
- **切段函数 test 不过** → 检查你有没有把 `## `(带空格) 或 `##`(不带) 混着用。**统一用 `line.startswith("## ")`**。
