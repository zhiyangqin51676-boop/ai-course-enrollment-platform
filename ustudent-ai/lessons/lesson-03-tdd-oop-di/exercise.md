# 作业 3 · 用 TDD 写一个 `/can-graduate` 端点

> 对应教案第 3 课。

## 学习目标

- **走一遍完整的 TDD 循环**:红 → 绿 → 加更多 case → 重构。
- **用 Pydantic 定义业务对象**(顺便用上今天讲的 OOP —— 把 `is_eligible()` 挂在 request 上)。
- **知道 pytest 里怎么测 FastAPI endpoint**(TestClient 姿势)。

## 你要做的(6 步)

### Step 1 · 拉 starter,建 branch

```bash
cd <你的 ustudent-ai 仓库>
git pull                                # 拿到 lesson-03 的 starter
git checkout -b feature/<你的名字>-lesson3
```

`lessons/lesson-03-tdd-oop-di/starter/` 里有:
- `main.py` —— 端点骨架(有 TODO)
- `test_can_graduate.py` —— 测试骨架(有 TODO)
- `README.md` —— 步骤提示

### Step 2 · 先写 1 个测试(RED)

在 `tests/test_can_graduate.py` 里,写这条:

```python
def test_120_credits_and_gpa_2_can_graduate():
    r = client.post("/can-graduate", json={"credits": 120, "gpa": 2.0})
    assert r.status_code == 200
    assert r.json() == {"can_graduate": True}
```

跑:`pytest tests/test_can_graduate.py`

**预期**:❌ 404 (端点还不存在) —— 这就是 **RED**。

### Step 3 · 写最小代码让它 GREEN

在 `app/routes/can_graduate.py` 里,填端点(见 starter 的 TODO)。规则:
- `credits >= 120` **且** `gpa >= 2.0` → `{"can_graduate": true}`
- 否则 → `{"can_graduate": false}`

**必须**:
- 用 Pydantic 定义 `GraduationRequest`(有 `credits`、`gpa` 两个字段)。
- **把 `is_eligible()` 方法挂到 request 上**(今天讲的 OOP —— 数据 + 方法打包)。
- 在 `app/main.py` 里加两行:
  ```python
  from app.routes import can_graduate
  app.include_router(can_graduate.router, tags=["graduation"])
  ```

跑:`pytest tests/test_can_graduate.py` → ✅ **GREEN**。

### Step 4 · 加更多 case(负面 + 边界)

**至少加以下 4 条测试**:

| 测试名 | 输入 | 期望 |
|---|---|---|
| `test_not_enough_credits` | credits=100, gpa=3.5 | can_graduate=False |
| `test_gpa_too_low` | credits=150, gpa=1.8 | can_graduate=False |
| `test_exactly_120_and_2_passes` | credits=120, gpa=2.0 | can_graduate=True |
| `test_119_credits_fails` | credits=119, gpa=4.0 | can_graduate=False |

跑:`pytest -v` → **全绿**。

### Step 5 · 重构一次(可选,但推荐)

**只有全绿的代码才能重构**。选一件做:

- (A) 把 magic number 120 和 2.0 抽到 `app/config.py` 里(常量或 Pydantic Settings 字段)。
- (B) 用 pytest fixture 消除重复的 `client` 创建。
- (C) 给 `is_eligible()` 写文档字符串,解释为什么是 120 / 2.0。

重构完再跑一次 `pytest -v`,**必须仍然全绿**。

### Step 6 · 推 branch,提 PR,截图提交

```bash
git add app/routes/can_graduate.py app/main.py tests/test_can_graduate.py
git commit -m "U3-XX: add /can-graduate endpoint with TDD"
git push
```

Bitbucket 输出里有创建 PR 的链接。**截图提交**:

1. `pytest -v` 全绿的终端截图。
2. http://localhost:8000/docs 里看到 `/can-graduate` 端点。

## 验收

- ✅ pytest 全绿,至少 5 条测试(1 正面 + 2 负面 + 2 边界)。
- ✅ `/can-graduate` 出现在 `/docs`。
- ✅ `GraduationRequest` 是 Pydantic class,`is_eligible()` 是它的方法(**不是**独立函数)。
- ✅ PR 已提。

## 提示 / 别走偏

- **别把逻辑塞进端点函数**。业务(`>= 120 and >= 2.0`)在 `GraduationRequest.is_eligible()` 里 —— 这样你**测的是这个方法**,而不是 http 层。
- **一定要先跑一次 RED**。跳过 RED 直接写代码,你就没体验 TDD。
- **不要 `import` 一堆不用的东西**。starter 有 TODO 注释指路。
- **端口用 8000**,别和后端 8080 撞。
- **不需要 LLM API key** —— 这个作业没调 LLM。
- 如果你想额外挑战:**把 `Student` class 独立出来**(而不是塞在 request 里),endpoint 内部 `Student(**req.dict()).can_graduate()` —— 更靠近今天讲的 OOP 直觉。

## 常见问题

- **`ImportError: no module named app`** → 检查 `pytest.ini` 是否有 `pythonpath = .`,或从项目根目录跑 pytest。
- **`AttributeError: 'GraduationRequest' object has no attribute 'is_eligible'`** → 你在 `class GraduationRequest(BaseModel):` **同层缩进**加方法,不是嵌套在 `__init__`。
- **`422 Unprocessable Entity`** → 请求 JSON key 拼错(比如 `credit` vs `credits`)。看 `/docs` 的 schema 对照。
- **本地 `pytest` 绿但 CI 红** → CI 用 clean venv;你本地可能残留旧 dependency。跑 `pip install -r requirements-dev.txt` 再本地跑一次。
