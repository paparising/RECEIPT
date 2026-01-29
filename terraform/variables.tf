# Terraform Variables for Receipt Application Deployment

# General Configuration
variable "project_name" {
  description = "Project name for resource naming and tagging"
  type        = string
  default     = "receipt-app"
}

variable "environment" {
  description = "Environment: dev, staging, or production"
  type        = string
  default     = "production"

  validation {
    condition     = contains(["dev", "staging", "production"], var.environment)
    error_message = "Environment must be dev, staging, or production."
  }
}

variable "aws_region" {
  description = "AWS region for resource deployment"
  type        = string
  default     = "us-east-1"
}

# Kubernetes Configuration
variable "kubernetes_host" {
  description = "Kubernetes cluster host/endpoint"
  type        = string
  sensitive   = true
}

variable "kubernetes_token" {
  description = "Kubernetes API token"
  type        = string
  sensitive   = true
}

variable "kubernetes_cluster_ca_certificate" {
  description = "Kubernetes cluster CA certificate"
  type        = string
  sensitive   = true
}

variable "kubeconfig_path" {
  description = "Path to kubeconfig file (alternative to explicit credentials)"
  type        = string
  default     = null
}

# Namespace Configuration
variable "create_namespaces" {
  description = "Create Kubernetes namespaces for different environments"
  type        = bool
  default     = true
}

variable "namespace_name" {
  description = "Kubernetes namespace for application deployment"
  type        = string
  default     = "production"
}

# Storage Configuration
variable "create_storage_classes" {
  description = "Create storage classes for application"
  type        = bool
  default     = true
}

variable "storage_class_standard" {
  description = "Storage class name for standard (gp2) storage"
  type        = string
  default     = "standard"
}

variable "storage_class_fast_ssd" {
  description = "Storage class name for fast SSD storage"
  type        = string
  default     = "fast-ssd"
}

variable "storage_iops" {
  description = "IOPS for fast-ssd storage class (gp3)"
  type        = number
  default     = 3000
}

variable "storage_throughput" {
  description = "Throughput in MB/s for fast-ssd storage class (gp3)"
  type        = number
  default     = 125
}

# Helm Chart Configuration
variable "enable_helm_chart" {
  description = "Deploy Helm chart for Receipt application"
  type        = bool
  default     = true
}

variable "helm_chart_path" {
  description = "Path to local Helm chart or Helm repository chart name"
  type        = string
  default     = "../helm/receipt-app"
}

variable "helm_release_name" {
  description = "Helm release name"
  type        = string
  default     = "receipt-app"
}

variable "helm_repository_url" {
  description = "Helm repository URL (if not using local chart)"
  type        = string
  default     = ""
}

variable "helm_repository_username" {
  description = "Helm repository username"
  type        = string
  default     = ""
  sensitive   = true
}

variable "helm_repository_password" {
  description = "Helm repository password"
  type        = string
  default     = ""
  sensitive   = true
}

# Application Configuration
variable "app_image" {
  description = "Docker image for Receipt application"
  type        = string
  default     = "receipt-app:1.0.0"
}

variable "app_replicas" {
  description = "Number of application replicas"
  type        = number
  default     = 3

  validation {
    condition     = var.app_replicas >= 1 && var.app_replicas <= 20
    error_message = "Replicas must be between 1 and 20."
  }
}

variable "app_port" {
  description = "Application port"
  type        = number
  default     = 8080
}

variable "app_cpu_request" {
  description = "CPU request for application pod"
  type        = string
  default     = "500m"
}

variable "app_memory_request" {
  description = "Memory request for application pod"
  type        = string
  default     = "512Mi"
}

variable "app_cpu_limit" {
  description = "CPU limit for application pod"
  type        = string
  default     = "1000m"
}

variable "app_memory_limit" {
  description = "Memory limit for application pod"
  type        = string
  default     = "1Gi"
}

# Database Configuration
variable "enable_mysql" {
  description = "Deploy MySQL database"
  type        = bool
  default     = true
}

variable "mysql_version" {
  description = "MySQL version"
  type        = string
  default     = "8.0"
}

variable "mysql_root_password" {
  description = "MySQL root password"
  type        = string
  sensitive   = true
}

variable "mysql_user" {
  description = "MySQL application user"
  type        = string
  default     = "appuser"
}

variable "mysql_password" {
  description = "MySQL application user password"
  type        = string
  sensitive   = true
}

variable "mysql_database" {
  description = "MySQL database name"
  type        = string
  default     = "appdb"
}

variable "mysql_storage_size" {
  description = "MySQL persistent volume size"
  type        = string
  default     = "10Gi"
}

# RabbitMQ Configuration
variable "enable_rabbitmq" {
  description = "Deploy RabbitMQ message queue"
  type        = bool
  default     = true
}

variable "rabbitmq_version" {
  description = "RabbitMQ version"
  type        = string
  default     = "3.12"
}

variable "rabbitmq_user" {
  description = "RabbitMQ default user"
  type        = string
  default     = "guest"
}

variable "rabbitmq_password" {
  description = "RabbitMQ default user password"
  type        = string
  sensitive   = true
}

variable "rabbitmq_storage_size" {
  description = "RabbitMQ persistent volume size"
  type        = string
  default     = "5Gi"
}

# Ingress Configuration
variable "enable_ingress" {
  description = "Deploy Ingress for external access"
  type        = bool
  default     = true
}

variable "ingress_class" {
  description = "Ingress class (e.g., nginx, alb, azure)"
  type        = string
  default     = "nginx"
}

variable "ingress_hostname" {
  description = "Ingress hostname/domain"
  type        = string
  default     = "receipt-app.example.com"
}

variable "ingress_tls_enabled" {
  description = "Enable TLS on ingress"
  type        = bool
  default     = true
}

variable "ingress_tls_issuer" {
  description = "cert-manager issuer for TLS (e.g., letsencrypt-prod)"
  type        = string
  default     = "letsencrypt-prod"
}

# Autoscaling Configuration
variable "enable_autoscaling" {
  description = "Enable Horizontal Pod Autoscaler"
  type        = bool
  default     = true
}

variable "hpa_min_replicas" {
  description = "Minimum replicas for HPA"
  type        = number
  default     = 3

  validation {
    condition     = var.hpa_min_replicas >= 1
    error_message = "Minimum replicas must be at least 1."
  }
}

variable "hpa_max_replicas" {
  description = "Maximum replicas for HPA"
  type        = number
  default     = 10

  validation {
    condition     = var.hpa_max_replicas >= var.hpa_min_replicas
    error_message = "Maximum replicas must be greater than or equal to minimum replicas."
  }
}

variable "hpa_target_cpu" {
  description = "Target CPU utilization percentage for HPA"
  type        = number
  default     = 70

  validation {
    condition     = var.hpa_target_cpu > 0 && var.hpa_target_cpu <= 100
    error_message = "Target CPU must be between 1 and 100."
  }
}

variable "hpa_target_memory" {
  description = "Target memory utilization percentage for HPA"
  type        = number
  default     = 80

  validation {
    condition     = var.hpa_target_memory > 0 && var.hpa_target_memory <= 100
    error_message = "Target memory must be between 1 and 100."
  }
}

# Security Configuration
variable "enable_rbac" {
  description = "Create RBAC resources (ServiceAccount, Role, RoleBinding)"
  type        = bool
  default     = true
}

variable "enable_network_policies" {
  description = "Create network policies for security"
  type        = bool
  default     = true
}

variable "enable_pod_security_policy" {
  description = "Enforce pod security policy"
  type        = bool
  default     = true
}

# Monitoring Configuration
variable "enable_prometheus_scraping" {
  description = "Enable Prometheus scraping annotations on pods"
  type        = bool
  default     = true
}

variable "prometheus_scrape_interval" {
  description = "Prometheus scrape interval"
  type        = string
  default     = "30s"
}

# Backup Configuration
variable "enable_backup" {
  description = "Enable backup for persistent volumes"
  type        = bool
  default     = true
}

variable "backup_retention_days" {
  description = "Number of days to retain backups"
  type        = number
  default     = 30
}

# Additional Labels and Tags
variable "additional_labels" {
  description = "Additional labels to apply to all resources"
  type        = map(string)
  default = {
    "app.kubernetes.io/name"       = "receipt-app"
    "app.kubernetes.io/component"  = "backend"
  }
}

variable "additional_tags" {
  description = "Additional AWS tags to apply to all resources"
  type        = map(string)
  default     = {}
}
