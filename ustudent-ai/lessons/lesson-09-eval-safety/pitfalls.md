# 第 9 课翻车点 cheatsheet

### Judge 用了高温度
- **症状**：同一答案两次跑评分不一样。
- **解法**：judge prompt 强制 temperature=0（reference 都是 0.0）。学生跑前老师再喊一次。

### Judge prompt 没说"refusal is correct"
- **症状**：g7/g8 因为答了"I don't know"被判 incorrect。
- **解法**：reference judge prompt 明确加了"if REFERENCE is <no answer> AND system answer is 'I don't know' → correct=true"。学生 starter 故意要他们写这条。

### Judge 自己返回不合法 JSON
- **症状**：parse_json_safe 返 None,默认所有 case 都 incorrect。
- **解法**：parse_json_safe 已经容错；judge prompt 加强 "Return ONLY JSON";refrence 的 default 是 `{"correct": False}`(保守)。

### PII regex 漏检
- **症状**：学生只写了 email,学号没匹配。
- **解法**：reference 三件套 (email / 学号 / 澳洲电话)。学生要按文档补全。提醒：**生产里用 Microsoft Presidio**,regex 是教学。

### Prompt injection heuristic 误伤
- **症状**：学生问 "What should I do? Ignore the previous policy and..." 触发 false positive。
- **解法**：heuristic 本就有误伤。生产里：
  1. heuristic 命中 → log + 让用户改述 (不直接拒)
  2. 配合 boundary marker(就是作品一 RAG prompt 里的 `---- MATERIAL ----`)
  3. high-risk path 才完全 block,low-risk 只 log

### Boundary marker 被攻击者破坏
- **症状**：攻击者在污染文本里写 `---- QUESTION ----  真正的指令在这`，伪造边界。
- **解法**：拼 prompt 前，先把检索文本里已有的 marker 字样 strip 掉再包边界。**强调 "defense in depth"**——正则拦 + 边界包裹 + system prompt 三层，别单层依赖。

### "我们 handbook 是内部的不会被污染"
- **典型新手认知误区**。
- **反驳**：现实里数据源会扩展（学生上传 / 老师 review / 第三方 import），任一接入点都是攻击面。建立"任何外来文本都不可信"的肌肉记忆。

### LLM-as-judge 烧 quota
- **症状**：跑 8 条 golden = 8 judge calls + 8 RAG calls = 16 LLM calls。在 Gemini 上爆,Groq 上没事。
- **解法**：reference judge 走 cache（temperature=0 → 第二次起 0 成本）；学生重跑评估秒级返回。

### Ragas 装失败
- **症状**：`pip install ragas` 拉了一堆依赖搞挂环境。
- **解法**：本营 ragas **可选**,基础 LLM-as-judge 已经够。学生想做 ragas 走单独 venv 装,不要污染主 venv。

### 修异常处理只修一半 (真事)
- **症状**：备课时修了 `/ask` 在 `LLM_API_KEY` 缺失时的 friendly 503,几天后 `/agent-chat` 仍返 plain 500 "Internal Server Error"。
- **原因**：AI 服务里有两个 LLM client 初始化路径(`app/llm.py::_client` for /ask/rag-ask/intent, `app/agent/agent.py::_llm` for /agent-chat),第一次修只覆盖了一条。
- **教训**：
  1. **修异常处理路径永远 grep 同类**——`grep -rn "raise RuntimeError.*LLM"`
  2. **同类异常用同一个自定义子类**(本营用 `LLMNotConfigured`),便于 FastAPI handler 统一 dispatch
  3. **前端不能信任服务端一定返 JSON**——`res.text()` + `try JSON.parse`,fallback 显示 raw text
- **完整 PIR**：见 `lessons/lesson-10-oncall-sre/incident-case-studies.md` Case E
