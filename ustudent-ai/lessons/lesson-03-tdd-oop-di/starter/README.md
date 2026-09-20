# Lesson 3 Starter · `/can-graduate` with TDD

> 这个 starter 里的文件是**你的作业起点**。跟着 `exercise.md` 一步步做,
> 每步都跑 `pytest -v` 观察 red → green。

## 文件在哪里

Starter 假设你的项目已经有:
- `app/` 目录(routes, main.py)
- `tests/` 目录(conftest, existing tests)

**你要新建的两个文件**在这个 starter 里已经给出**骨架 + TODO**,复制到你项目对应位置:

| Starter 位置 | 复制到你的项目 |
|---|---|
| `starter/app/routes/can_graduate.py` | `app/routes/can_graduate.py` |
| `starter/tests/test_can_graduate.py` | `tests/test_can_graduate.py` |

`app/main.py` 你自己改 —— 加两行:

```python
from app.routes import can_graduate
app.include_router(can_graduate.router, tags=["lesson-3"])
```

## 一步一步做

**Step 1**:先把 `test_can_graduate.py` 的第一条测试(标记 `# TODO 1`)**取消注释**,`pytest` 应看到 **RED**。

**Step 2**:在 `can_graduate.py` 里填 TODO,让第一条测试变 **GREEN**。

**Step 3**:改 `app/main.py`,`include_router`。再跑 pytest。

**Step 4**:把 `test_can_graduate.py` 的其他 TODO(负面 case + 边界 case)取消注释、填完。全绿。

**Step 5**:选一个重构(见 exercise.md Step 5)。全绿。

**Step 6**:提 PR。

## 常见坑

看 `../pitfalls.md` "FastAPI 作业段"。
