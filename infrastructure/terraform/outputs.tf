# ---- Network ---------------------------------------------------------------

output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.main.id
}

output "public_subnet_ids" {
  description = "Public subnet IDs"
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "Private subnet IDs"
  value       = aws_subnet.private[*].id
}

# ---- Data ------------------------------------------------------------------

output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.main.endpoint
}

output "rds_port" {
  description = "RDS instance port"
  value       = aws_db_instance.main.port
}

# ---- ECR -------------------------------------------------------------------

output "ecr_backend_repository_url" {
  value = aws_ecr_repository.backend.repository_url
}

output "ecr_frontend_repository_url" {
  value = aws_ecr_repository.frontend.repository_url
}

output "ecr_ai_repository_url" {
  description = "ECR repo for the unified ustudent-ai FastAPI service"
  value       = aws_ecr_repository.ai.repository_url
}

# ---- ECS -------------------------------------------------------------------

output "ecs_cluster_name" {
  value = aws_ecs_cluster.main.name
}

output "ecs_backend_service_name" {
  value = aws_ecs_service.backend.name
}

output "ecs_frontend_service_name" {
  value = aws_ecs_service.frontend.name
}

output "ecs_ai_service_name" {
  description = "ECS service for the unified ustudent-ai container"
  value       = aws_ecs_service.ai.name
}

# ---- ALB URLs --------------------------------------------------------------

output "load_balancer_dns_name" {
  value = aws_lb.main.dns_name
}

output "load_balancer_zone_id" {
  value = aws_lb.main.zone_id
}

output "application_url" {
  description = "Frontend (React) URL"
  value       = "http://${aws_lb.main.dns_name}"
}

output "backend_api_url" {
  description = "Spring Boot backend URL"
  value       = "http://${aws_lb.main.dns_name}:8080"
}

output "ai_service_url" {
  description = "FastAPI AI service URL (/health, /rag-ask, /agent-chat, ...)"
  value       = "http://${aws_lb.main.dns_name}:8000"
}

# ---- Account ---------------------------------------------------------------

output "aws_account_id" {
  value = data.aws_caller_identity.current.account_id
}

output "aws_region" {
  value = var.aws_region
}
