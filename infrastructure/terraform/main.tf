# AWS Provider Configuration
terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# Data sources
data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_caller_identity" "current" {}

# Variables
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "production"
}

variable "app_name" {
  description = "Application name"
  type        = string
  default     = "ustudent"
}

variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true
}

variable "domain_name" {
  description = "Domain name for the application (optional)"
  type        = string
  default     = ""
}

# LLM provider config for the AI service. We talk OpenAI-compatible APIs,
# so the bootcamp default is Groq (free, fast) and you can switch to OpenAI,
# OpenRouter, GitHub Models, etc. by changing only these three values.
variable "llm_api_key" {
  description = "API key for the LLM provider (Groq by default)"
  type        = string
  sensitive   = true
}

variable "llm_base_url" {
  description = "OpenAI-compatible API base URL for the LLM provider"
  type        = string
  default     = "https://api.groq.com/openai/v1"
}

variable "llm_model" {
  description = "Model identifier to call on the provider"
  type        = string
  default     = "llama-3.3-70b-versatile"
}

# Local values
locals {
  name_prefix = "${var.app_name}-${var.environment}"

  tags = {
    Environment = var.environment
    Application = var.app_name
    ManagedBy   = "terraform"
  }
}