#!/bin/bash
set -e 

# --- Configuration ---
# Bitbucket Pipelines automatically provides $BITBUCKET_COMMIT
# AWS_REGION and AWS_ACCOUNT_ID should be set in your Repository Variables

CLUSTER_NAME="ustudent-production-cluster"
SERVICE_NAME="ustudent-production-frontend"
IMAGE_REPO_URL="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/ustudent-frontend"

# The specific image tag we want to deploy
NEW_IMAGE="${IMAGE_REPO_URL}:${BITBUCKET_COMMIT}"

echo "🚀 Starting Safe Deployment..."
echo "Target Image: $NEW_IMAGE"

# 1. Get the current Task Definition ARN being used by the service
CURRENT_TASK_DEF_ARN=$(aws ecs describe-services \
    --cluster $CLUSTER_NAME \
    --services $SERVICE_NAME \
    --region $AWS_REGION \
    --query 'services[0].taskDefinition' \
    --output text)

echo "Current Task Definition: $CURRENT_TASK_DEF_ARN"

# 2. Fetch configuration, update image, and clean up for registration
# We use jq to swap the image URL and remove read-only fields
aws ecs describe-task-definition \
    --task-definition $CURRENT_TASK_DEF_ARN \
    --region $AWS_REGION \
    | jq --arg IMAGE "$NEW_IMAGE" '
        .taskDefinition 
        | .containerDefinitions[0].image = $IMAGE 
        | del(.taskDefinitionArn, .revision, .status, .requiresAttributes, .compatibilities, .registeredAt, .registeredBy)' \
    > new-task-def.json

# 3. Register the new revision
NEW_TASK_DEF_ARN=$(aws ecs register-task-definition \
    --cli-input-json file://new-task-def.json \
    --region $AWS_REGION \
    --query 'taskDefinition.taskDefinitionArn' \
    --output text)

echo "Registered new revision: $NEW_TASK_DEF_ARN"

# 4. Update the service to use the new revision
aws ecs update-service \
    --cluster $CLUSTER_NAME \
    --service $SERVICE_NAME \
    --task-definition $NEW_TASK_DEF_ARN \
    --force-new-deployment \
    --region $AWS_REGION

echo "✅ Deployment initiated successfully!"
