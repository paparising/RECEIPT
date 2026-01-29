# Terraform Providers Configuration
# Supports multiple cloud platforms and Kubernetes

terraform {
  required_version = ">= 1.0"

  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.23"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.11"
    }
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Uncomment to use remote state backend
  # backend "s3" {
  #   bucket         = "receipt-app-terraform-state"
  #   key            = "prod/terraform.tfstate"
  #   region         = "us-east-1"
  #   encrypt        = true
  #   dynamodb_table = "terraform-locks"
  # }
}

# AWS Provider Configuration
provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Environment = var.environment
      Project     = var.project_name
      ManagedBy   = "Terraform"
      CreatedAt   = timestamp()
    }
  }
}

# Kubernetes Provider Configuration
provider "kubernetes" {
  host                   = var.kubernetes_host
  token                  = var.kubernetes_token
  cluster_ca_certificate = var.kubernetes_cluster_ca_certificate

  # Alternative: Use kubeconfig
  # config_path = var.kubeconfig_path
}

# Helm Provider Configuration
provider "helm" {
  kubernetes {
    host                   = var.kubernetes_host
    token                  = var.kubernetes_token
    cluster_ca_certificate = var.kubernetes_cluster_ca_certificate
  }
}
