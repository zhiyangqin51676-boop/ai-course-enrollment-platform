# 第 4 课翻车点 cheatsheet

按"症状 → 原因 → 一句话解法"组织,便于现场快速判断。

---

## Groq 申请 / .env 段

### Groq signup 卡在 "verify email"
- **症状**:填了邮箱,一直没收到验证邮件。
- **原因**:企业邮箱、学校邮箱可能被过滤;垃圾邮件箱漏看。
- **解法**:换 Gmail 试;等 5 分钟仍无 → 用 Google 单点登录。

### 复制 key 时不小心跳走了页面 → key 丢了
- **症状**:key 只显示一次,复制前不小心关了对话框。
- **解法**:回 dashboard,**删掉那个 key** 重建。不要留悬空 key —— 有安全隐患。

### `.env` 建在错位置
- **症状**:`.env` 建在项目根,但 `LLMNotConfigured` 还在报。
- **原因**:从 `tests/` 或子目录起 uvicorn 时,pydantic settings 找不到 `.env`。
- **解法**:必须**从项目根**起服务:`cd ustudent-ai && uvicorn app.main:app --reload`。

### `.env` 里 key 有引号
- **症状**:`.env` 写成 `LLM_API_KEY="gsk_..."`,请求 401 unauthorized。
- **原因**:pydantic settings 会把引号也读进去,变成 key 的一部分。
- **解法**:`.env` 里**不要**加引号:`LLM_API_KEY=gsk_...`(裸字符串)。

### `.env` 被 commit 了
- **症状**:`git status` 显示 `.env` 是 modified/tracked。
- **解法**:马上从 git 里删:
  ```bash
  git rm --cached .env
  git commit -m "untrack .env"
  ```
  然后**去 Groq dashboard revoke 那个 key,重新生成一个**(因为 key 已进 git 历史了)。

### 学生"顺手"改了 `app/llm.py`
- **症状**:starter 的 llm.py 被学生改坏了(比如去掉了 LLMNotConfigured,或改了 base_url)。
- **解法**:课上明确说 "**不要改 app/llm.py,复制 starter 就用**"。这是 lesson 4 的边界,后面 lesson 才会让他们改。

---

## LLM 调用段

### `RateLimitError: 429`
- **症状**:调 5-10 次 `/ask` 后 429。
- **原因**:Groq 免费一分钟 30 次。学生 debug 时反复触发。
- **解法**:等 30 秒;`app/llm.py` 已有 429 exponential backoff 会自动等,别把它去掉。

### 调 `/ask` 卡 20+ 秒不动
- **症状**:请求发出去,uvicorn log 没动静。
- **原因**:网络到 Groq 慢/断。
- **解法**:先 `curl -v https://api.groq.com/openai/v1/models -H "Authorization: Bearer $LLM_API_KEY"` 排查网络;不通就换手机热点试。

### 500 Internal Server Error(uvicorn log 有 stack trace)
- **症状**:frontend 看不到 detail,uvicorn log 里是 exception。
- **原因**:`app/main.py` 里少了 `LLMNotConfigured` 的 exception handler。
- **解法**:加上(见 starter 的 main.py):
  ```python
  from app.llm import LLMNotConfigured
  @app.exception_handler(LLMNotConfigured)
  async def _handle_llm_not_configured(request, exc):
      return JSONResponse(status_code=503, content={"detail": str(exc), "code": "llm_not_configured"})
  ```

### `/ask` 返回但 answer 是空字符串
- **症状**:`{"answer": ""}`.
- **原因**:模型有时候(temperature=0 也会)返回 empty content,尤其 system prompt 太严格时。
- **解法**:检查 system prompt 是不是"过度约束";或加 fallback:`if not answer: answer = "I don't have enough context to answer that."`

### 换 model 到 llama-3.1-8b 后一切变差
- **症状**:同 prompt,8B 模型答得离谱。
- **解法**:课上说清 "**model 大小是 prompt engineering 的隐藏变量**"。作业默认用 llama-3.3-70b,除非 quota 用完了。

---

## Prompt Engineering 段

### handbook 塞进 system 后 prompt 太长报 4xx
- **症状**:`400 Bad Request: context length exceeded`。
- **原因**:handbook + 学生问题 + 输出 > 模型 context window。
- **解法**:llama-3.3-70b context 是 128k,不会撞。**如果撞了**,说明学生把 handbook 复制粘贴了多次 —— 检查代码。

### few-shot examples 让模型"忘记"约束
- **症状**:加了 few-shot 后,模型开始"聊天",不遵循 handbook 约束。
- **原因**:examples 里没体现"handbook 外要拒答"这一约束。
- **解法**:examples **必须**含一个 out-of-handbook 拒答案例(pitfalls 的 v2 build_messages 已示范)。

### v2 输出**总带** ```` ```json ```` fence
- **症状**:模型 JSON 前后加 markdown code fence。
- **原因**:训练数据里模型见惯了带 fence 的 JSON。
- **解法**:
  - 短期:`re.sub(r"^\`\`\`(?:json)?\s*|\s*\`\`\`$", "", raw, flags=re.MULTILINE)`
  - 长期:v3 用 `response_format={"type": "json_object"}`,provider 强制去掉 fence。

### v3 `pydantic.ValidationError: field required`
- **症状**:v3 endpoint 出 422/500,log 显示 pydantic 报字段缺失。
- **原因**:prompt 里的 JSON schema 跟 Pydantic model 字段名不一致(比如 prompt 说 `"citation"`,Pydantic 是 `source`)。
- **解法**:**prompt schema 一字一句**跟 Pydantic model 对齐。用 `AskV3Response.model_json_schema()` 拿到 schema 塞到 prompt 也可。

### response_format 报 provider not supported
- **症状**:`400 response_format is not supported`。
- **原因**:换到了不支持 JSON mode 的 provider(比如老的 Together AI endpoint)。
- **解法**:换回 Groq;或退回 few-shot + 手动 parse。

---

## 教学纪律

### 学生借用别人的 Groq key
- **症状**:全班共用 1-2 个 key,rate limit 全体炸。
- **解法**:课上强调"**每人自己的**"。检查:让每个学生 `head -1 .env | grep -o "gsk_...."` 报出前 4 字符,看是否有大量重复。

### 学生不写 `pytest`,只手工调 `/ask`
- **症状**:作业里没 `test_ask.py` 新测试,只有截图。
- **解法**:验收时明确 "**必须有测 build_v1/v2/v3_messages 的 pytest,LLM 调用可以不 test**"。

### 学生想直接用 LangChain
- **症状**:"我 Google 到 `ChatOpenAI` 更简单,为什么不用?"
- **解法**:"**LangChain 我们 lesson 8 讲 agent 时用**。今天用原生 SDK,是为了让你**看到 messages/temperature/response_format 这些参数**,LangChain 会把它们藏起来。"

### 学生把 handbook 塞 user message
- **症状**:`{"role": "user", "content": handbook + "\n" + question}`.
- **原因**:直觉上"给它信息"就用 user。
- **解法**:讲清 **约束/规则/知识放 system,用户输入放 user**。**面试常问 prompt injection**,答案就是这个分层。

---

## 时间管理

### 作业 3 回看拖到 25 分钟
- **解法**:严格 15 分钟卡表。剩下的问题让学生**下课单独找**,不占大课时间。

### Groq signup 段全班拖到 15 分钟
- **原因**:有人邮箱验证慢,有人网络抖。
- **解法**:**继续讲下一段**,让卡住的学生**跟同桌先看**;下课前再单独帮他们。**不为一个人耽误全班**。

### Prompt 3 招段拖到 30+ 分钟
- **原因**:demo 每一版都想让所有人跟着敲。
- **解法**:**你演学生看**,先跑通再让他们回家自己抄。不要求现场全部敲完。
