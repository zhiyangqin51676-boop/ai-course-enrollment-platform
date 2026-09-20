export ECR_REPO="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/ustudent-backend"
docker build -t $ECR_REPO:$BITBUCKET_COMMIT .
docker tag $ECR_REPO:$BITBUCKET_COMMIT $ECR_REPO:latest
docker push $ECR_REPO:$BITBUCKET_COMMIT
