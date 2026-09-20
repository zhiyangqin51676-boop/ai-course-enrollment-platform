#!/usr/bin/env bash
# Log in to ECR. Sourced from CI or run manually before docker push.
#
# Requires:
#   AWS_ACCOUNT_ID    e.g. 123456789012
#   AWS_REGION        e.g. us-east-1
#   AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY (or aws sso login already done)

set -euo pipefail

: "${AWS_ACCOUNT_ID:?Set AWS_ACCOUNT_ID}"
: "${AWS_REGION:?Set AWS_REGION (e.g. us-east-1)}"

REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

echo "==> Logging in to ECR registry: $REGISTRY"
aws ecr get-login-password --region "$AWS_REGION" \
    | docker login --username AWS --password-stdin "$REGISTRY"

echo "==> Done. Registry available at: $REGISTRY"
