#!/bin/bash

# Install aws-cli
yum -y install unzip
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
./aws/install

# Set aws key and secret
aws configure set aws_access_key_id "${AWS_KEY}"
aws configure set aws_secret_access_key "${AWS_SECRET}"

# ECR login 
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com
