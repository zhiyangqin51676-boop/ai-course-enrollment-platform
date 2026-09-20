# 作业 9 · 给你的 RAG/Agent 做评估 + 一处安全加固

> 对应教案第 9 课。难度：中高（本营 AI 部分压轴）。预计 50–80 分钟。

## Part 1 · 评估（25 分钟）

1. 完成 `starter/eval.py`
2. 跑 `python eval.py`，对作品一的 RAG 给出 8/8 评分（或解释为什么不满分）
3. 找一条**不及格的 case**（如果都过了，故意把 chunk_size 改成 100 制造一条），用第 6 课讲的诊断方法分析：是**检索错**还是**生成错**？
4. 改 1 处（chunk_size / threshold / prompt），再跑一遍，让那条过
5. 写 100 字总结：你做了什么改动，为什么？通过率涨了多少？

## Part 2 · 安全加固（25 分钟）

至少做 1 项，写进 PR：

### 选项 A · PII redaction
- 完成 `starter/safety.py` 的 PII regex
- 写一段 middleware：所有进入 `/agent-chat` 的请求 message **先 redact 再交给 agent**
- 写一段 logging：把 redacted_text 写日志，原文不写
- 截图：日志里看不到学号 / 邮箱

### 选项 B · Prompt injection 防护
- 完成 `starter/safety.py` 的 injection 检测(`detect_injection`)
- 改 `scripts/rag.py`(作品一你写的)：检索回来的 chunks **先过 detect_injection**
- 命中的话：**不调 LLM**，直接返回安全错误 + 记日志
- 截图：故意问一个包含 "ignore previous instructions" 的问题，被拦截

## 提交（Jira）

- PR 链接
- eval 输出截图（含改动前后对比）
- 安全选项的 demo 截图
- 100 字总结

## 验收

- eval 通过率 ≥ 7/8（不及格的那条必须**分析过**且尝试修过）
- 安全选项至少 1 项落地 + 截图证明
- **`temperature=0`** 跑 judge —— 否则评估不可复现

## 别踩的雷

- ❌ Judge 用高温度 → 同一答案两次评分不一致
- ❌ 把 PII 当成"前端的事" → 是 backend / AI 服务的事。第三方 API 是不可信外部
- ❌ Injection 只检测用户输入，不查 RAG chunks → 真实攻击 90% 来自被污染的文档/网页
- ❌ "我们 handbook 是内部的不需要担心" → 现实里数据源会被加进去（学生上传、爬虫、第三方）

## 简历素材

> Implemented LLM-as-judge evaluation harness for a RAG system, with
> deterministic judge prompts (temperature=0 + disk cache) for reproducible
> scoring. Built two production safety layers: regex-based PII redaction on
> all log paths, and prompt-injection heuristics that short-circuit
> compromised retrieval contexts before any LLM call.
