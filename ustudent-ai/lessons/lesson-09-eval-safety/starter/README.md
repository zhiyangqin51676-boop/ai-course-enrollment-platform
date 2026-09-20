# Lesson 9 · Starter — Evaluation + Safety

Two short exercises. Each forces you to confront a question you wished you'd
thought of before lesson 6.

## 准备

```bash
source .venv/bin/activate
# 不需要装新东西
```

## Part 1 · LLM-as-judge (~25 分钟)

```bash
python eval.py
```

填完 `eval.py` 的 3 个 TODO。期望输出 8 条 golden eval 的 OK/XX 列表 + 总分。

**关键**：你的 judge prompt 必须能正确判定 g7/g8（handbook 里没答案、系统返回 "I don't know" 应判 correct=true）。

## Part 2 · Safety (~25 分钟)

```bash
python safety.py
```

填完 `safety.py` 的 5 个 TODO：
- PII regex 三件套（email / 学号 / 澳洲电话）
- 检测 3 种 prompt injection
- 跑 ATTACK_TEXT，确认能 BLOCKED

## 接进 RAG（选做但推荐）

修改 `app/routes/rag.py`：
- 在调 LLM **之前**，先 `detect_injection(retrieved_chunks)`，如果命中就返回安全错误
- 在返回给用户**之前**，先 `redact(answer)`（虽然 handbook 本身不该有 PII，演示用）

详细要求看 `../exercise.md`。
