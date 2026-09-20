# AI Course Enrollment Platform

**智能选课与学业规划平台**

A consolidated source repository for a course enrollment platform: a React frontend, Kotlin/Spring Boot backend, Python/FastAPI service, and AWS infrastructure managed with Terraform.

## Modules

| Directory | Purpose | Technology |
| --- | --- | --- |
| [`uplusstudent-frontend/`](uplusstudent-frontend/) | Login, student dashboard, course enrollment UI and AI chat UI | React 18, React Router, Axios, Nginx |
| [`uplusstudent/`](uplusstudent/) | Student profiles, courses, enrollment, prerequisite and schedule-conflict logic | Kotlin, Spring Boot 3, PostgreSQL, Flyway |
| [`ustudent-ai/`](ustudent-ai/) | FastAPI service and retained AI development materials | Python, FastAPI; RAG/Agent/MCP implementations in lesson directories |
| [`infrastructure/terraform/`](infrastructure/terraform/) | AWS infrastructure definitions | Terraform, ECS Fargate, RDS, ALB, ECR, VPC |

Original module directory names are retained so the relative build contexts in `ustudent-ai/docker-compose.yml` continue to point to the backend and frontend.

## Current source snapshot

All 218 files from the four supplied source archives are preserved byte-for-byte in their respective module directories. This repository adds only this overview and root ignore rules. Original module documentation, tests, deployment scripts, Bitbucket pipeline configurations, lesson tasks, starter code, reference solutions and the student runtime pack remain available for later deployment reconciliation.

The FastAPI entrypoint `ustudent-ai/app/main.py` currently registers `/health`, `/echo` and `/can-graduate`. The graduation endpoint uses a credits/GPA rule. RAG, Agent and MCP code is present in `ustudent-ai/lessons/`, but it is not registered by this entrypoint. The frontend includes an AI chat page; its presence does not establish that its expected AI endpoint is served by the current entrypoint. Previously deployed images may differ from these source archives.

## Local development

See the module documentation for details:

- [Frontend setup](uplusstudent-frontend/README.md)
- [Backend API requests](uplusstudent/http/README.md)
- [AI service documentation](ustudent-ai/README.md)
- [Infrastructure setup](infrastructure/terraform/README.md)

The source-based Compose configuration is in `ustudent-ai/`. To prepare a local configuration and attempt a build from this snapshot:

```bash
cd ustudent-ai
cp .env.example .env
# Fill only the provider settings required by the functionality you use.
docker compose up --build
```

The configuration exposes the frontend at `http://localhost:3000`, backend at `http://localhost:8080`, and FastAPI documentation at `http://localhost:8000/docs`. Container builds require network access for packages and model downloads. Full-stack startup was not tested during this repository import; AI chat/RAG/Agent integration should be reconciled before presenting a live demo.

For native backend development, the Gradle project requires JDK 21. Run module commands from their respective directories. There is no root-level package manager or unified build command.

## Deployment notes

Dockerfiles, deployment scripts and Terraform configuration are included as source. Existing Bitbucket pipeline files remain within each module and do not run automatically as GitHub Actions. Their original assumptions about repository roots must be adapted if used with this consolidated repository.

The source contains local demonstration database credentials and mock login-token generation. Use an isolated development environment; production authentication and credentials require separate configuration. Keep real `.env` files, Terraform variables/state and provider keys outside version control. Importing the repository does not deploy or modify AWS resources.

## Import verification

All original files were checked against the uploaded archives before import, including binary assets and the Gradle wrapper. No application tests, infrastructure deployment or complete application build was performed for this import. Original README claims about test or deployment results have not been independently revalidated here.
