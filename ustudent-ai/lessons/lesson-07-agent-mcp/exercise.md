# 作业 7 · 设计你的 Agent

> 对应教案第 7 课。难度：中。预计用时：40–60 分钟。这是**第 8 课工作坊的施工蓝图**——设计不清楚,工作坊一定翻车。

## Part 1 · 跑通两份 starter（30 分钟）

1. 填完 `starter/react_loop.py` 的 6 个 TODO，跑通三组测试
2. 填完 `starter/langgraph_agent.py` 的 3 个 TODO，跑通同样三组
3. 对照两份代码，**用一句话回答**：
   - LangGraph 替我做了哪三件事？
   - 多步推理（第 3 个问题）两份代码都对吗？为什么？

## Part 2 · 设计你的 Agent（30 分钟）

按下面模板写出**你**的多工具 Agent 设计，发到 PR 描述里或 Confluence。

```markdown
# 我的 ustudent Agent 设计

## 1. 角色 (system prompt 草稿)
- 它是谁？(选课助手 / 教务助手 / ...)
- 它能做什么？哪些不该做？(边界很重要)
- 它的语气？(正式 / 友好 / 简洁)

## 2. 工具清单 (至少 3 个,其中至少 1 个真打 ustudent backend)
| 工具名 | 入参 | 干什么 | 数据源 |
|---|---|---|---|
| get_course | course_code | 查课程详情 | 后端 GET /api/courses |
| enrol | student_id, course_code | 选课 | 后端 POST /enroll |
| handbook_qa | question | 政策问题 | 复用作品一 RAG |
| ??? | | | |

## 3. 决策流程草图 (ReAct 循环或 LangGraph 状态图)
- 文字描述 OR ASCII 图
- 重点:模型怎么判断"这个问题用哪个工具"?
- 如果工具调用失败怎么办?(retry? fallback?)

## 4. 记忆策略
- 多轮对话时怎么处理指代("它"、"那门课")?
- thread_id 怎么定?(per-user-session? per-conversation?)
- 记忆放哪儿?(内存? Redis? Postgres?)

## 5. 安全边界
- 哪些动作要二次确认?(选课?退课?改成绩当然不能让 agent 做)
- 用户身份怎么验?(JWT? session?)
- prompt injection 怎么防?(第 9 课正式讲,这里先有意识)

## 6. 失败模式
- 工具选错了怎么办?(常见:agent 应该 RAG 时去查后端)
- 模型陷入死循环怎么办?(MAX_STEPS)
- 后端 down 了怎么办?(返回友好错误,不要让 agent 重试 10 次)
```

## 验收

- 两份 starter 都跑通(截图三组结果)
- 设计文档八节都不能空填或写"待定"
- "失败模式"这节要明确——这是第 8 课工作坊踩的最多坑

## 别踩的雷

- ❌ 工具描述写得模糊。description 是 agent 选工具的**唯一**依据
- ❌ 不写失败模式。"happy path 能跑"在产线上等于"会上线就垮"
- ❌ 把后端 ID 硬编进 prompt。课程代码 (CS201) 是 string,后端 id 是 long——agent 自己映射
