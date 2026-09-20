# 作品三 · 把整个选课系统部署上线（含你的 AI 服务）

> 对应教案第 10 课工作坊。难度：中高（真实 AWS）。预计课内+课后：120-180 分钟。
> 详细命令见同目录 **`runbook-aws-deploy.md`**(英文,可直接照做)。

## 目标

把 **完整的 ustudent 选课系统** 部署到 AWS ECS：**前端(React)+ 后端(Spring Boot)+
你的 AI 服务(RAG/agent)**,三个服务在一个 ALB 后面协同工作。最终:**打开前端网页,
在 AI Chat 页里聊天,能收到你 RAG/agent 的回答。** 这是"我能独立把一个多服务系统上线"
的完整证明——比"部署一个服务"强得多的简历句。

## 架构一句话

- ALB `:80`→前端 / `:8080`→后端 / `:8000`→AI;`/api/*` 由 ALB 规则转后端。
- **RDS 是后端的库**;AI 用镜像内 Chroma + 调后端 API,不碰 RDS。
- 前端 AI Chat 页打 `/ai/agent-chat`(同源),nginx 代理到你的 AI 服务(服务发现)。

## 验收（必须全过）

1. ✅ `terraform apply` 成功(VPC/RDS/ALB/ECR×3/ECS×3/服务发现)。**tfvars 填了 `db_password` + `llm_api_key`**。
2. ✅ 三个镜像都 build+push 到各自 ECR(frontend / backend / **ai**)。
3. ✅ 三个 ECS 服务都 healthy(`running=1`)。
4. ✅ 前端首页:`curl http://<alb>/` 返 200。
5. ✅ 后端:`curl http://<alb>/api/courses` 返回课程数据(证明 backend + RDS + ALB)。
6. ✅ **★ AI Chat 打通 ★**:`curl -X POST http://<alb>/ai/agent-chat -d '{"message":"...","thread_id":"t1"}'` 返回你 RAG/agent 的答案(含 `tool_calls`);浏览器打开前端 AI Chat 页能真聊。
7. ✅ `terraform destroy` 拆干净。

## 提交（Jira）

- 三个 repo 的 PR/commit 链接(尤其你 AI 服务的 Dockerfile)。
- 截图:ECS 控制台三个 service 都 healthy。
- 截图:浏览器打开 `http://<alb>/`,在 **AI Chat 页**聊天并收到回答(**作品三灵魂**)。
- 截图:`curl http://<alb>/api/courses` 成功。
- **一段英文简历句**(见下方模板)。
- ⚠ 确认 `terraform destroy`(截图控制台无 ECS/RDS/ALB)。

## ⚠ 成本警告

- ECS×3 + RDS + ALB + NAT 跑 **一天 ~ AUD $5-8**。
- **截完图立刻 `terraform destroy`**。老师课后 24h 群里 ping"destroy 了吗"。

## 简历句模板（参考）

> Deployed a complete multi-service application to **AWS ECS Fargate** with
> Terraform: a React frontend, a Spring Boot backend (RDS Postgres), and a
> Python **RAG/agent AI service** — all behind an ALB, wired together with
> service discovery so the frontend's chat page reaches the AI service. Built
> and shipped all three container images to ECR.

## 别踩的雷（详见 pitfalls.md）

- ❌ `terraform.tfvars` 的 `llm_api_key` 留空 → AI 容器崩(`api_key must be set`)。
- ❌ ARM Mac build 不加 `--platform linux/amd64` → ECS 起不来。
- ❌ 前端用错 Dockerfile / 没带 nginx 模板 → chat 打不通 AI。
- ❌ RDS `postgres` 版本被 AWS 淘汰 → 查可用版本改 `rds.tf`。
- ❌ 忘了 `terraform destroy` → 持续扣费。
