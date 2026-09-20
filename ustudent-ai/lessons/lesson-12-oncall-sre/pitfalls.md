# 第 12 课翻车点 cheatsheet（讲授课）

### 概念多易枯燥
- **症状**：讲到 SLO/SLI/SLA 时台下发呆。
- **解法**：每个概念都配 ustudent 真实例子（demo.md 已经全配了）。讲 SLO 不要讲 Google 那本书的抽象,直接用 "ai-service /rag-ask p95 < 3s" 这种具体的。

### 学生没值过班体会不深
- **症状**：讲 "凌晨 4 点被 PagerDuty 叫醒" 学生没共鸣。
- **解法**：让某个学生**当场说**他最近一次 "看不懂的报错折腾几个小时" 的经历——那就是 incident 的精神体验。

### 时间到了求职收尾被压缩
- **症状**：实战话题 (70-85 段) 容易超时,结营变 1 分钟仓促结束。
- **解法**：实战段每个 case 控 5 分钟,讲完一个就看表。**留足 5 分钟收尾** —— 这是毕业课的仪式感。

### 学生问 "我没值过班怎么写简历"
- **解法**：教他**翻译你做过的事**：
  - 第 10 课 deploy → "Designed deployment rollback strategy"
  - 第 6 课 RAG threshold tuning → "Tuned distance threshold based on production-like data distribution"
  - 第 9 课 eval → "Implemented LLM-as-judge harness for continuous quality monitoring"
- 没**真实**事故经历不要硬编;**用学过的概念回答"如果是你怎么处理"**的面试题。

### 学生问"那 AI Engineer 也要值班吗"
- **答**：**会**。Production AI 服务的 SLI/SLO 跟传统后端一样, 而且 AI 模型 silently 退化(模型升级、上游 quality drift) 需要专门监控。
- 这是 AI Engineer **区别于纯数据科学家**的工程素养。
