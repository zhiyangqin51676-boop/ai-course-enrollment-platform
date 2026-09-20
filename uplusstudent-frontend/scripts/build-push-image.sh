export ECR_REPO="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/ustudent-frontend"
docker build -f Dockerfile.production -t $ECR_REPO:$BITBUCKET_COMMIT .
docker push $ECR_REPO:$BITBUCKET_COMMIT
