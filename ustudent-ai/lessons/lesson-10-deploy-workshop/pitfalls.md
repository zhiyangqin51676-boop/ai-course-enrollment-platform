# 第 10 课工作坊翻车点 cheatsheet

> 本节聚焦"容器化 + 部署到公网"。CI/CD pipeline、Splunk/SignalFX 的坑在第 11 课。

### Image architecture 错（ARM Mac 出 arm64,ECS 跑 amd64）
- **症状**：ECS tasks 一直 STOPPED，event 里说 "exec format error"。
- **解法**：`docker buildx build --platform linux/amd64 ...` —— 永远加 `--platform`。reference 的 `scripts/deploy.sh` 都已经写死。**本节最贵的坑,反复强调。**

### 容器一起就 `ModuleNotFoundError: No module named 'scripts'`
- **症状**：镜像 build 成功、`docker run` 一起就崩,日志 `from scripts.rag import rag_answer` → ModuleNotFoundError。
- **原因**：作品一/二的 RAG 放在 `scripts/rag.py`，`app/` 下的路由 import 它。但 Dockerfile 若只 `COPY app/ data/`，镜像里没有 `scripts/`。
- **解法**：Dockerfile 加 `COPY scripts/ ./scripts/`；并 `RUN python scripts/search_handbook.py build` 把 chroma 索引烘进镜像（否则 `/rag-ask` 运行时没索引）。reference Dockerfile 已修好。
- **根源**：老师 reference 的 app 全在 `app/`（`app.rag.pipeline`），学生的是 `scripts/rag.py`——teacher/student 结构分歧，学生一部署才暴露。

### 镜像 ~2.7GB，ECR 推送 / ECS 首次拉取慢
- **症状**：`deploy.sh` build+push 很久；ECS 首个 task 拉镜像慢，`startPeriod` 内没起来被 kill。
- **原因**：RAG 依赖 `sentence-transformers` → `torch`。默认 `pip install torch` 拉 **CUDA 版**（~6GB nvidia 库），镜像能到 9GB+。
- **解法**：Dockerfile 里**先装 CPU-only torch**：`pip install --index-url https://download.pytorch.org/whl/cpu torch`，再装 requirements。镜像 9GB → ~2.7GB。Fargate 无 GPU，CUDA 库纯浪费。
- 即便 2.7GB 也比纯 API 服务大，首次拉取慢是正常的；`health_check_grace_period_seconds=120` 要留够。

### ECS health check startPeriod 太短，tasks 反复重启
- **症状**：CloudWatch 看任务每 60-90 秒被 kill 重建。FastAPI + Chroma 冷启动要 60-90 秒。
- **解法**：reference task definition 设 `startPeriod = 90`，ECS service 设 `health_check_grace_period_seconds = 120`。**别低于这个**。
- **教案直接点名**：上一期 Spring Boot 同样的坑。

### 镜像 push 后 ECS 不滚
- **症状**：`docker push :latest` 完了 ECS 还跑老镜像。
- **原因**：ECS 不主动检测镜像变化；新 task definition revision 或 `--force-new-deployment` 才会拉新。
- **解法**：`aws ecs update-service --force-new-deployment ...`（`deploy.sh` 已经这样做）。**面试常问,回扣段 5 原理。**

### 忘了 `terraform destroy` 持续扣费
- **症状**：账单上每天 AUD ~$3-5 不停。
- **解法**：
  1. 课后 24h 内老师群里 ping
  2. 学生 Jira 卡的"DONE"必须含 destroy 截图
  3. **极端**：设个 AWS Budget Alert($5 阈值),发邮件警告

### `.env` 进了 git
- **症状**：PR review 时看到 `.env` 在 diff。
- **解法**：**立刻 revoke key** + git history 抹掉（用 git-filter-repo）+ 重新申请 key + commit `.env.example` 替代。**`.gitignore` 早写好**。

### 在 ARM 跑 buildx 没装 qemu
- **症状**：`docker buildx build --platform linux/amd64` 报 "exec /bin/sh: exec format error"。
- **解法**：装 binfmt：`docker run --privileged --rm tonistiigi/binfmt --install all`。Mac Docker Desktop 默认有。

### Secrets 走环境变量被 CloudTrail 看到
- **症状**：审计抓出来 task definition 里有 LLM_API_KEY 明文。
- **教案要求短期可用 env vars，正式上线必须 Secrets Manager**：
  ```hcl
  secrets = [{
    name      = "LLM_API_KEY"
    valueFrom = aws_secretsmanager_secret_version.llm_key.arn
  }]
  ```
- 课上当甜点讲一句，作业不强制。

### AWS 账号没装 AWS CLI / SSO 未登录
- **症状**：`aws sts get-caller-identity` 报 NoCredentialsError。
- **解法**：先 `aws configure` 或 `aws sso login`。这一步**课前 1 天**就群里发 runbook。

### terraform apply 报 `Cannot find version 17.x for postgres`
- **症状**：apply 建 RDS 时 `InvalidParameterCombination: Cannot find version 17.4 for postgres`。
- **原因**：AWS 定期淘汰 postgres 旧小版本;`rds.tf` 钉的版本过期了。
- **解法**：查可用版本换掉:`aws rds describe-db-engine-versions --engine postgres --query 'DBEngineVersions[].EngineVersion'`,改 `rds.tf` 的 `engine_version`。**每期 cohort 前复验**(2026-07:17.4 已退,17.5-17.10 可用)。

### AI 任务反复重启,日志 `OpenAIError: api_key must be set`
- **症状**：ECS AI 服务 running 在 0/1 抖,日志 `The api_key client option must be set`。
- **原因**：`terraform.tfvars` 的 `llm_api_key` **空**(空串会走到 OpenAIError,不是 KeyError)。
- **解法**：tfvars 填上真 Groq key,`terraform apply` 更新 task def。**学生最容易漏这步**——白板强调。

### 前端部署后 chat 打不通 AI(`/ai/agent-chat` 返回 HTML)
- **症状**：浏览器 chat 无响应;curl `/ai/agent-chat` 返回 React 的 index.html 而非 JSON。
- **原因**：前端镜像没带正确的 nginx 代理配置——要么 Dockerfile 没 `COPY nginx.conf`(走默认 nginx),要么 nginx 上游是 docker-compose 名(`ustudent-ai`)ECS 里不解析。
- **解法**：用 `Dockerfile.production`(已改成 env 模板化 nginx);ecs.tf 给 AI 注册服务发现 + 前端 task 传 `AI_UPSTREAM=ai.<ns>.local:8000`。**同源 /ai 代理避免 CORS**,是 chat 能连 AI 的关键。

### chat 半路开始 504(前端好、直连 AI 也好,唯独前端→AI 504)
- **症状**：前端网页正常、直连 `curl <alb>:8000/agent-chat` 正常,但浏览器 chat / `curl <alb>/ai/agent-chat` 返回 **504,固定 ~10s**(= nginx `proxy_connect_timeout`)。
- **两个叠加根因**(实测踩过):
  1. **nginx 缓存了 AI 的旧 IP**:`proxy_pass http://字面域名` 只在**启动时解析一次**,AI 的 Fargate task 一重启换新 IP,nginx 还发往死 IP → 连不上。**修**:加 `resolver`(VPC DNS `169.254.169.253`)+ `proxy_pass` 用**变量**(`set $ai_upstream ...`)强制按 TTL 重解析 + `rewrite` 剥 `/ai` 前缀。
  2. **安全组规则漂移**:`ecs_ai`/`ecs_backend` 若**同时用内联 ingress + 独立 `aws_security_group_rule`**,terraform **每次 apply 都 churn**、会短暂删掉 frontend→AI 规则 → 前端被挡在 8000 外。**修**:所有 ingress **改内联**,删独立 rule 资源(切换时注意:同一条 AWS 规则被内联和独立资源同时管,一次 apply 里"加内联+删独立"会互相抵消,要**再 apply 一次**才干净;`terraform plan` 显示 `No changes` 才算稳)。
- **判断哪个**:`curl <alb>:8000/...`(直连,走 ALB)好 = AI 服务没问题;那 504 就出在前端 nginx 或 sg。查 AI sg 的 8000 入站有没有包含**当前**前端 sg。

### 前端首页能开但 /api 502
- **症状**：`http://<alb>/` 200,但 `/api/courses` 502。
- **原因**：后端 ECS 任务没健康(镜像没推 / RDS 连不上 / task 还在起)。
- **解法**：`aws ecs describe-services ... --query 'services[0].events'` 看后端事件;确认后端镜像已推 ECR、RDS 起好。

### terraform apply 卡在 RDS 创建
- **症状**：apply 半天不动，卡在 `aws_db_instance`。
- **解法**：RDS 创建本来就要 4-6 分钟，**正常**。所以老师课前预跑、课上不从 0 apply。
