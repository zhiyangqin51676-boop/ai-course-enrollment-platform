#!/usr/bin/env bash
# Local "push and roll" — builds the AI image, pushes to ECR, forces ECS to
# pick up the new latest. Use after `terraform apply` has created the
# cluster/service/repo.
#
# Required env:
#   AWS_ACCOUNT_ID
#   AWS_REGION
#   ECR_REPOSITORY     default ustudent-ai
#   ECS_CLUSTER        default ustudent-production-cluster
#   ECS_SERVICE        default ustudent-production-ai

set -euo pipefail

: "${AWS_ACCOUNT_ID:?Set AWS_ACCOUNT_ID}"
: "${AWS_REGION:?Set AWS_REGION}"

ECR_REPOSITORY="${ECR_REPOSITORY:-ustudent-ai}"
ECS_CLUSTER="${ECS_CLUSTER:-ustudent-production-cluster}"
ECS_SERVICE="${ECS_SERVICE:-ustudent-production-ai}"

REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
IMAGE_URI="${REGISTRY}/${ECR_REPOSITORY}"
TAG="$(git rev-parse --short HEAD 2>/dev/null || date +%Y%m%d%H%M%S)"

cd "$(dirname "$0")/.."

echo "==> ECR login"
bash scripts/aws-ecr-setup.sh

echo "==> Build & push  $IMAGE_URI:$TAG  (also tagged :latest)"
# linux/amd64 — Fargate's default. Without --platform, an ARM Mac would push
# arm64 and ECS would silently fail to start tasks. Worth dying on.
docker buildx build \
    --platform linux/amd64 \
    --push \
    -t "$IMAGE_URI:latest" \
    -t "$IMAGE_URI:$TAG" \
    .

echo "==> Force ECS to roll service: $ECS_CLUSTER / $ECS_SERVICE"
aws ecs update-service \
    --cluster "$ECS_CLUSTER" \
    --service "$ECS_SERVICE" \
    --force-new-deployment \
    --region "$AWS_REGION" \
    >/dev/null

echo "==> Waiting for service to stabilise (this can take 2-5 minutes)..."
aws ecs wait services-stable \
    --cluster "$ECS_CLUSTER" \
    --services "$ECS_SERVICE" \
    --region "$AWS_REGION"

echo "==> Done. Image tagged $TAG is live."
