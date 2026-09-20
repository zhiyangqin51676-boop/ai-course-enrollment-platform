# ECS Cluster
resource "aws_ecs_cluster" "main" {
  name = "${local.name_prefix}-cluster"

  configuration {
    execute_command_configuration {
      logging = "OVERRIDE"
      log_configuration {
        cloud_watch_log_group_name = aws_cloudwatch_log_group.ecs.name
      }
    }
  }

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-ecs-cluster"
  })
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "ecs" {
  name              = "/ecs/${local.name_prefix}"
  retention_in_days = 7

  tags = local.tags
}

# ECS Task Execution Role
resource "aws_iam_role" "ecs_task_execution" {
  name = "${local.name_prefix}-ecs-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = local.tags
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# ECS Task Role
resource "aws_iam_role" "ecs_task" {
  name = "${local.name_prefix}-ecs-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = local.tags
}

# ----- Security Groups -----------------------------------------------------

resource "aws_security_group" "ecs_backend" {
  name_prefix = "${local.name_prefix}-ecs-backend-"
  vpc_id      = aws_vpc.main.id

  # From the ALB (public) and from the AI service (get_course/enrol calls).
  # Keep ALL ingress inline — do NOT also use separate aws_security_group_rule
  # resources for this SG, or terraform churns every apply (inline set vs. rule
  # resource fight) and can briefly drop the rule, 504-ing the frontend->AI path.
  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id, aws_security_group.ecs_ai.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-ecs-backend-sg"
  })

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_security_group" "ecs_frontend" {
  name_prefix = "${local.name_prefix}-ecs-frontend-"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 80
    to_port         = 80
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-ecs-frontend-sg"
  })

  lifecycle {
    create_before_destroy = true
  }
}

# AI service: single FastAPI process exposing /ask, /rag-ask, /agent-chat etc.
# Replaced the old two-service agent+mcp split — the AI bootcamp ships one
# unified app/main.py per the bootcamp's app/ layout.
resource "aws_security_group" "ecs_ai" {
  name_prefix = "${local.name_prefix}-ecs-ai-"
  vpc_id      = aws_vpc.main.id

  # From the ALB (public :8000) and from the frontend (nginx proxies /ai/* here).
  # ALL ingress inline — see the note on ecs_backend about not mixing inline
  # rules with separate aws_security_group_rule resources.
  ingress {
    from_port       = 8000
    to_port         = 8000
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id, aws_security_group.ecs_frontend.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.tags, {
    Name = "${local.name_prefix}-ecs-ai-sg"
  })

  lifecycle {
    create_before_destroy = true
  }
}

# NOTE: the "AI -> backend" and "frontend -> AI" ingress rules used to be
# separate aws_security_group_rule resources here. They were moved INLINE into
# the ecs_backend / ecs_ai security groups above — mixing inline ingress with
# separate rule resources on the same SG makes terraform churn every apply and
# can transiently drop the rule (frontend -> AI 504s). Don't reintroduce them.

# ----- Task Definitions ----------------------------------------------------

resource "aws_ecs_task_definition" "backend" {
  family                   = "${local.name_prefix}-backend"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name  = "backend"
      image = "${aws_ecr_repository.backend.repository_url}:latest"

      portMappings = [
        {
          containerPort = 8080
          protocol      = "tcp"
        }
      ]

      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "production" },
        { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://${aws_db_instance.main.endpoint}/ustudent" },
        { name = "SPRING_DATASOURCE_USERNAME", value = "ustudent" },
        { name = "SPRING_DATASOURCE_PASSWORD", value = var.db_password },
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.ecs.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "backend"
        }
      }

      healthCheck = {
        command     = ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
    }
  ])

  tags = local.tags
}

resource "aws_ecs_task_definition" "frontend" {
  family                   = "${local.name_prefix}-frontend"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name  = "frontend"
      image = "${aws_ecr_repository.frontend.repository_url}:latest"

      portMappings = [
        {
          containerPort = 80
          protocol      = "tcp"
        }
      ]

      # nginx template upstreams — service-discovery FQDNs so the frontend can
      # proxy /ai/* to the AI service (and /api/* as a fallback; in prod the ALB
      # rule forwards /api/* to the backend target group before nginx sees it).
      environment = [
        { name = "AI_UPSTREAM", value = "ai.${aws_service_discovery_private_dns_namespace.main.name}:8000" },
        { name = "BACKEND_UPSTREAM", value = "backend.${aws_service_discovery_private_dns_namespace.main.name}:8080" },
        # VPC DNS resolver so nginx re-resolves the service-discovery names per
        # TTL and follows a task's new IP after a restart (fixes /ai 504s).
        { name = "DNS_RESOLVER", value = "169.254.169.253" },
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.ecs.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "frontend"
        }
      }
    }
  ])

  tags = local.tags
}

# AI service task. Plain env-var secrets are good enough for the bootcamp;
# in prod, wire LLM_API_KEY through AWS Secrets Manager and reference it via
# `secrets = [{ name="LLM_API_KEY", valueFrom = "<secret-arn>" }]`.
resource "aws_ecs_task_definition" "ai" {
  family                   = "${local.name_prefix}-ai"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name  = "ai"
      image = "${aws_ecr_repository.ai.repository_url}:latest"

      portMappings = [
        {
          containerPort = 8000
          protocol      = "tcp"
        }
      ]

      environment = [
        { name = "LLM_API_KEY", value = var.llm_api_key },
        { name = "LLM_BASE_URL", value = var.llm_base_url },
        { name = "LLM_MODEL", value = var.llm_model },
        { name = "USTUDENT_BACKEND_URL", value = "http://backend.${local.name_prefix}.local:8080" },
        { name = "LOG_LEVEL", value = "INFO" },
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.ecs.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ai"
        }
      }

      healthCheck = {
        command  = ["CMD-SHELL", "curl -f http://localhost:8000/health || exit 1"]
        interval = 30
        timeout  = 5
        retries  = 3
        # FastAPI + Chroma model download on cold start needs ~60s.
        # Drop this lower and you'll watch ECS thrash — lesson 11 pitfall.
        startPeriod = 90
      }
    }
  ])

  tags = local.tags
}

# ----- Services ------------------------------------------------------------

resource "aws_ecs_service" "backend" {
  name            = "${local.name_prefix}-backend"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.backend.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  health_check_grace_period_seconds = 90

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_backend.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.backend.arn
    container_name   = "backend"
    container_port   = 8080
  }

  service_registries {
    registry_arn = aws_service_discovery_service.backend.arn
  }

  depends_on = [aws_lb_listener.backend]

  tags = local.tags
}

resource "aws_ecs_service" "frontend" {
  name            = "${local.name_prefix}-frontend"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.frontend.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_frontend.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.frontend.arn
    container_name   = "frontend"
    container_port   = 80
  }

  depends_on = [aws_lb_listener.frontend]

  tags = local.tags
}

resource "aws_ecs_service" "ai" {
  name            = "${local.name_prefix}-ai"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.ai.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  health_check_grace_period_seconds = 120

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_ai.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.ai.arn
    container_name   = "ai"
    container_port   = 8000
  }

  # Register in private DNS so the frontend nginx can proxy /ai/* to
  # ai.<namespace>.local:8000 (same-origin, avoids CORS in the browser).
  service_registries {
    registry_arn = aws_service_discovery_service.ai.arn
  }

  depends_on = [aws_lb_listener.ai, aws_ecs_service.backend]

  tags = local.tags
}

# ----- Service Discovery (private DNS for service-to-service) --------------

resource "aws_service_discovery_private_dns_namespace" "main" {
  name        = "${local.name_prefix}.local"
  description = "Private DNS namespace for service discovery"
  vpc         = aws_vpc.main.id

  tags = local.tags
}

resource "aws_service_discovery_service" "backend" {
  name = "backend"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.main.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }

  tags = local.tags
}

resource "aws_service_discovery_service" "ai" {
  name = "ai"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.main.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }

  tags = local.tags
}
