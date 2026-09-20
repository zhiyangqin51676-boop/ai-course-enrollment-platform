#!/bin/bash

# Install aws-cli
yum -y install unzip
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
./aws/install

# Set aws key and secret
aws configure set aws_access_key_id "${AWS_KEY}"
aws configure set aws_secret_access_key "${AWS_SECRET}"