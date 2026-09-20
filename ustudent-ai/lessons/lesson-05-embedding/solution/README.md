# Lesson 5 · 参考答案(跟不上时用)

> ⚠️ **先自己做 `../exercise.md`**。这个文件夹是"安全网" —— 课上跟不上 live-code、
> 或想对照检查时再看。**直接抄不会让你学会;卡住时看一眼再回去自己写才有用。**

## 里面是什么

- `scripts/search_handbook.py` —— 完整的语义检索 CLI(build + query)
- `tests/test_search.py` —— chunker + cosine 的单元测试

## 怎么跑(2 步)

### 1. 复制到你的项目

```bash
# 在你的 ustudent-ai 项目根目录
cp lessons/lesson-05-embedding/solution/scripts/search_handbook.py scripts/search_handbook.py
cp lessons/lesson-05-embedding/solution/tests/test_search.py       tests/test_search.py
```

### 2. 建索引 + 查询

```bash
# 首次会下载 ~80MB 模型(要 wifi), 之后秒开
python scripts/search_handbook.py build
# → Indexed 10 chunks into chroma_db

python scripts/search_handbook.py query "how do I stop taking a class?"
# → 会返回 top-3 相关章节 + distance
```

跑测试:
```bash
pytest tests/test_search.py -v   # 应全绿
```

## 前置条件

- `.venv` 激活, 装了 `sentence-transformers` + `chromadb`(`pip install -r requirements-dev.txt`)
- 从**项目根目录**跑(不是从 `scripts/` 里跑)

## 跑通之后

**回去把 `starter/scripts/search_handbook.py` 的 3 个 TODO 自己填一遍。** 你已经看过
答案怎么工作, 自己写一遍才会记住。作业验收看的是你自己的实现。
