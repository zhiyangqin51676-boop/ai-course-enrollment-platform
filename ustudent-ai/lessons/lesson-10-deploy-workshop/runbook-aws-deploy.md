# Deploy the ustudent system to AWS (frontend + backend + AI)

> Student runbook. Deploys the **whole course-enrolment system** to AWS ECS:
> the React **frontend**, the Spring Boot **backend**, and your **AI service**
> (RAG + agent). The frontend's AI Chat page talks to your AI service once all
> three are live.
> **⚠️ This costs money — run `terraform destroy` when you're done.**

## Architecture (what you're deploying)

```
                     Internet
                        │
                 ┌──────────────┐
                 │     ALB       │   :80 → frontend   :8080 → backend   :8000 → AI
                 └──────────────┘   (ALB rule: /api/* → backend)
          ┌────────────┼─────────────┐
          ▼            ▼              ▼
     frontend       backend         AI service
     (React/nginx)  (Spring Boot)   (FastAPI: RAG + agent)
          │            │              │
   /ai/* proxied ──────┼──────────────┘   (frontend nginx → ai.<ns>.local:8000
   to AI (service      ▼                    via service discovery)
    discovery)        RDS (Postgres)   ← backend's database
```

- **RDS** is the **backend's** database (courses, enrolments, students). The AI
  service does NOT use RDS — it uses an embedded Chroma index + calls the backend API.
- The **frontend AI Chat page** POSTs to `/ai/agent-chat` (same origin, port 80).
  nginx in the frontend proxies `/ai/*` to your AI service. No CORS needed.

---

## 1. Set up AWS

1. **Install the AWS CLI.** See: <https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html>
2. **Sign in / sign up** for an AWS account: <https://aws.amazon.com/free>
3. **Create an IAM user for Terraform** (Console → IAM → Users → *Create user*):
   - Username e.g. `terraform-deploy`
   - *Attach policies directly* → attach **AdministratorAccess** (initial setup only)
4. **Create access keys**: the user → *Security credentials* → *Create access key* →
   *Command Line Interface (CLI)* → copy the key + secret.
5. **Configure the CLI:**
   ```bash
   aws configure          # paste key, secret, region = us-east-1, output = json
   aws sts get-caller-identity   # must return your account + arn
   ```
   > If this ever returns `InvalidClientTokenId`, your credentials expired —
   > re-run `aws configure` (or `aws sso login` if you use SSO).

## 2. Run Terraform

1. **Install Terraform CLI:** <https://developer.hashicorp.com/terraform/tutorials/aws-get-started/install-cli>
2. **Clone the infra repo:** <https://bitbucket.org/uplus-career/infrastructure/src/main/>
3. **Configure variables:** copy `terraform.tfvars.example` → `terraform.tfvars`, then edit:
   ```hcl
   db_password  = "a-strong-password"
   llm_api_key  = "gsk_...your_groq_key..."   # ★ REQUIRED — the AI service
                                              #   crashes on startup without it
   ```
   > **Don't skip `llm_api_key`.** An empty key = the AI container exits with
   > `OpenAIError: api_key must be set` and never becomes healthy.
   > `terraform.tfvars` is gitignored — never commit it.
4. **Apply:**
   ```bash
   cd terraform
   terraform init
   terraform plan     # review what will be created — don't skip
   terraform apply    # ~8 min (RDS is the slow part). Type "yes".
   ```
   This creates the VPC, RDS, ALB, 3 ECR repos, 3 ECS services, and service discovery.
   > If apply fails with `Cannot find version 17.x for postgres`, AWS retired that
   > minor version — check available ones and bump `engine_version` in `rds.tf`:
   > `aws rds describe-db-engine-versions --engine postgres --query 'DBEngineVersions[].EngineVersion'`

## 3. Manual Deploy (build + push all three images)

```bash
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export AWS_REGION=us-east-1
echo "$AWS_ACCOUNT_ID"

# ECR login (once)
aws ecr get-login-password --region us-east-1 \
  | docker login --username AWS --password-stdin \
    "$AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com"
```

### 3a. Frontend  (`uplusstudent-frontend` repo)
```bash
docker buildx build -f Dockerfile.production --platform linux/amd64 \
  -t "$AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/ustudent-frontend:latest" \
  --push .
```
> The image renders its nginx config from a template at startup, so `/api/*` and
> `/ai/*` proxy correctly on ECS (the AI Chat page depends on this).

### 3b. Backend  (`uplusstudent` repo)
```bash
# The Dockerfile is multi-stage — it runs ./gradlew bootJar inside the build,
# so you do NOT need to build the jar first.
docker buildx build --platform linux/amd64 \
  -t "$AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/ustudent-backend:latest" \
  --push .
```

### 3c. AI service  (`ustudent-ai` repo)  ← NEW this cohort
```bash
# The Dockerfile COPYs scripts/ (your portfolio-1/2 RAG) and bakes the Chroma
# index into the image; it installs CPU-only torch (image ~2.7 GB).
docker buildx build --platform linux/amd64 \
  -t "$AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/ustudent-ai:latest" \
  --push .

# (or just: cd ustudent-ai && bash scripts/deploy.sh — it does build+push+roll)
```
> If the AI container crashes with `ModuleNotFoundError: No module named 'scripts'`,
> your Dockerfile isn't `COPY scripts/ ./scripts/` — pull the latest ustudent-ai.

## 4. Deploy with ECS (roll all three services)

```bash
for svc in frontend backend ai; do
  aws ecs update-service --cluster ustudent-production-cluster \
    --service "ustudent-production-$svc" --force-new-deployment --region us-east-1 \
    --query 'service.serviceName' --output text
done
```
> First rollout: the AI task pulls a ~2.7 GB image + cold-starts (Chroma/torch),
> so give it ~2 min to pass health checks. 502/503 during that window is normal.

## 5. Verify (the whole system, via the ALB)

```bash
ALB=$(cd terraform && terraform output -raw load_balancer_dns_name)

# 1. Frontend loads
curl -s -o /dev/null -w '%{http_code}\n' "http://$ALB/"          # 200

# 2. Backend + RDS (course data via the ALB /api rule)
curl -s "http://$ALB/api/courses" | head -c 200                  # {"success":true,...}

# 3. ★ AI Chat wired through the frontend (nginx → AI service discovery) ★
curl -s -X POST "http://$ALB/ai/agent-chat" \
  -H 'Content-Type: application/json' \
  -d '{"message":"How many credits do I need to graduate?","thread_id":"t1"}'
# -> {"answer":"...120 credits...","tool_calls":[{"name":"handbook_qa",...}]}
```
Then open `http://<ALB>/` in a browser and use the **AI Chat** page — it should
answer using your RAG/agent.

## 6. Clean up  ⚠️

```bash
cd terraform
terraform destroy -auto-approve     # ~5-8 min; tears down everything
```
> Not destroying ≈ AUD $3-5/day. Screenshot your verification, then destroy.
> Confirm the console shows no ECS / RDS / ALB / VPC left.

---

## Quick troubleshooting

| Symptom | First check |
|---|---|
| `aws sts get-caller-identity` → InvalidClientTokenId | credentials expired → `aws configure` / `aws sso login` |
| `terraform apply` → `Cannot find version 17.x for postgres` | bump `engine_version` in `rds.tf` to an available one |
| AI task keeps restarting, logs `api_key must be set` | `llm_api_key` empty in `terraform.tfvars` |
| AI task exits, logs `No module named 'scripts'` | AI Dockerfile missing `COPY scripts/` — pull latest |
| `/ai/agent-chat` returns the React HTML, not JSON | frontend built without the nginx template (use `Dockerfile.production`) |
| ECS task PENDING forever | `aws ecs describe-services ... --query 'services[0].events'` |
| public curl → 502/503 right after deploy | task still pulling image / cold-starting — wait ~2 min |
