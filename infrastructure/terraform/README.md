# UStudent AWS Infrastructure

This Terraform configuration deploys the UStudent application to AWS using ECS Fargate, RDS PostgreSQL, and Application Load Balancer.

## Architecture

- **VPC**: Custom VPC with public and private subnets across 2 AZs
- **Database**: RDS PostgreSQL in private subnets
- **Backend**: Spring Boot app running on ECS Fargate
- **Frontend**: React app running on ECS Fargate
- **Load Balancer**: Application Load Balancer for traffic routing
- **Container Registry**: ECR repositories for Docker images

## Prerequisites

1. **AWS CLI** configured with appropriate credentials
2. **Terraform** >= 1.0 installed
3. **Docker** for building images locally (optional)

## Quick Start

1. **Clone and navigate to infrastructure directory**:
   ```bash
   cd infrastructure/terraform
   ```

2. **Copy and configure variables**:
   ```bash
   cp terraform.tfvars.example terraform.tfvars
   # Edit terraform.tfvars with your values
   ```

3. **Initialize Terraform**:
   ```bash
   terraform init
   ```

4. **Plan the deployment**:
   ```bash
   terraform plan
   ```

5. **Apply the infrastructure**:
   ```bash
   terraform apply
   ```

6. **Get the application URL**:
   ```bash
   terraform output application_url
   ```

## Configuration

### Required Variables

- `aws_region`: AWS region for deployment (default: us-east-1)
- `db_password`: Secure password for PostgreSQL database

### Optional Variables

- `environment`: Environment name (default: production)
- `app_name`: Application name (default: ustudent)
- `domain_name`: Custom domain name (optional)

## Deployment Process

The infrastructure creates:

1. **Networking**: VPC, subnets, NAT gateways, route tables
2. **Database**: RDS PostgreSQL with security groups
3. **Container Registry**: ECR repositories for backend and frontend
4. **Compute**: ECS cluster with Fargate services
5. **Load Balancing**: ALB with target groups and listeners

## Security Features

- Private subnets for database and application containers
- Security groups with minimal required access
- RDS encryption at rest
- ECR image scanning enabled
- IAM roles with least privilege access

## Monitoring

- CloudWatch logs for ECS containers
- RDS enhanced monitoring
- ALB access logs (can be enabled)
- Health checks for all services

## Cost Optimization

- Uses t3.micro for RDS (suitable for development/small production)
- Fargate with minimal CPU/memory allocation
- ECR lifecycle policies to clean up old images
- NAT gateways only in required AZs

## Scaling

The infrastructure supports:
- Auto-scaling for ECS services (can be configured)
- RDS storage auto-scaling enabled
- Multi-AZ deployment for high availability

## Cleanup

To destroy all resources:
```bash
terraform destroy
```

**Warning**: This will delete all data including the database!

## Troubleshooting

### Common Issues

1. **ECR repositories empty**: Push Docker images before deploying ECS services
2. **Health check failures**: Ensure applications start correctly and health endpoints work
3. **Database connection issues**: Check security groups and network configuration

### Useful Commands

```bash
# Check ECS service status
aws ecs describe-services --cluster ustudent-production-cluster --services ustudent-production-backend

# View ECS logs
aws logs tail /ecs/ustudent-production --follow

# Check RDS status
aws rds describe-db-instances --db-instance-identifier ustudent-production-postgres
```

## Next Steps

After infrastructure deployment:

1. **Push Docker images** to ECR repositories
2. **Update ECS services** to use new images
3. **Configure domain** and SSL certificates (optional)
4. **Set up monitoring** and alerting
5. **Configure backup** strategies