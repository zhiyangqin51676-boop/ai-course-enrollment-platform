# 课后（可选）· 写一份 PIR + 简历定稿

> 对应教案第 12 课"无强制作业"。鼓励但不强制。

## 选做 1 · PIR 复盘你最得意的作品

挑你最得意的那个作品 (一/二/三)，按下面模板写一份 PIR 式的"工作日记"：

```markdown
# Project <N>: <Name> — Retrospective

## What I built (3 sentences)
- ...

## What worked
- ...

## What surprised me
- ...

## What I'd do differently next time
- ...

## What I learned that I can talk about in interviews
- 至少 3 条具体的 (不是"我学了 LangGraph",而是"我学到 LangGraph 的 state_modifier 在 0.2 vs 0.3 改了名,反映出 LangGraph API 还不稳定,生产里要锁死版本")
```

## 选做 2 · 简历定稿

把你的简历更新成包含两条经历：

```
# Full Stack / Backend Engineer — ustudent
- Built ustudent course enrolment system (Spring Boot Kotlin, PostgreSQL,
  React). Deployed to AWS ECS Fargate via Terraform with Bitbucket CI/CD.
  ...

# AI Engineer — ustudent AI service
- Built RAG-based course Q&A service (FastAPI, Chroma, Llama 3.3 via Groq).
  8/8 golden eval pass with distance-threshold fallback + grounded prompt.
- Designed 3-tool LangGraph agent with multi-turn memory (MemorySaver) +
  exposed via standard MCP protocol — usable from Claude Desktop or custom
  clients without code changes.
- Implemented LLM-as-judge evaluation harness + safety layers (PII
  redaction, prompt-injection detection) + provider-agnostic LLM client
  validated against Groq and OpenAI.
- Containerised + shipped to AWS ECS Fargate (Terraform IaC, Bitbucket
  CI/CD, Splunk/SignalFX observability).
```

提交：把更新后的简历贴 Jira；可选发助教 review。

## 选做 3 · 给你的项目设计一个 SLO

为作品三的 ai-service 写出三件事：
- 一个 SLI（你能真实测的指标）
- 一个 SLO（业务可承受的目标）
- 配套的 error budget 策略（用完了你打算停什么）

例子：
- SLI: ai-service `/rag-ask` p95 latency
- SLO: 99% of calls < 3s over rolling 30 days
- Error budget: 1% (~7h/month). 烧掉 50% → feature freeze;烧光 → 全员投稳定性

## 结营心态叮嘱

- **简历每周改 2 次,投 5 个职位** —— 比"等准备好了再投"有效 10 倍
- **面试讲故事比讲术语强** —— "我做的 RAG 系统在 handbook 没答案的 case 上用 distance threshold 短路兜底, 8/8 通过"
- **拒信不是评价** —— 是匹配度问题，下一个
- **作品集 > 文凭** —— 你今天就有这个作品集
- **GitHub 公开三个作品的代码** —— PR 链接 / 部署截图 / 简历句子全放 README

毕业快乐 🎓
