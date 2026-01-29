# Terraform Variables - Development Environment
# Usage: terraform apply -var-file="environments/dev.tfvars"

# General Configuration
project_name = "receipt-app"
environment  = "dev"
aws_region   = "us-east-1"

# Kubernetes Configuration
# Note: These must be provided via environment variables or command line
# Example: export TF_VAR_kubernetes_host="https://your-cluster.com"
# Or: terraform apply -var "kubernetes_host=https://your-cluster.com" ...

# Namespace Configuration
namespace_name          = "receipt-app-dev"
create_namespaces       = true

# Storage Configuration
create_storage_classes  = true
storage_class_standard  = "standard"
storage_class_fast_ssd  = "fast-ssd"
storage_iops            = 3000
storage_throughput      = 125

# Helm Chart Configuration
enable_helm_chart       = true
helm_chart_path         = "../helm/receipt-app"
helm_release_name       = "receipt-app"

# Application Configuration
app_image               = "receipt-app:1.0.0"
app_replicas            = 1
app_port                = 8080
app_cpu_request         = "250m"
app_memory_request      = "256Mi"
app_cpu_limit           = "500m"
app_memory_limit        = "512Mi"

# Database Configuration
enable_mysql            = true
mysql_version           = "8.0"
mysql_root_password     = "rootpassword123"  # Use secrets management in production!
mysql_user              = "appuser"
mysql_password          = "apppassword123"   # Use secrets management in production!
mysql_database          = "appdb"
mysql_storage_size      = "2Gi"

# RabbitMQ Configuration
enable_rabbitmq         = true
rabbitmq_version        = "3.12"
rabbitmq_user           = "guest"
rabbitmq_password       = "guest"            # Use secrets management in production!
rabbitmq_storage_size   = "2Gi"

# Ingress Configuration
enable_ingress          = true
ingress_class           = "nginx"
ingress_hostname        = "receipt-app.dev.local"
ingress_tls_enabled     = false
ingress_tls_issuer      = "letsencrypt-staging"

# Autoscaling Configuration
enable_autoscaling      = false  # Disabled for dev
hpa_min_replicas        = 1
hpa_max_replicas        = 3
hpa_target_cpu          = 70
hpa_target_memory       = 80

# Security Configuration
enable_rbac             = true
enable_network_policies = false  # Less strict for dev
enable_pod_security_policy = false

# Monitoring Configuration
enable_prometheus_scraping = true
prometheus_scrape_interval = "30s"

# Backup Configuration
enable_backup           = false  # Not needed for dev
backup_retention_days   = 7

# Additional Labels
additional_labels = {
  "app.kubernetes.io/name"       = "receipt-app"
  "app.kubernetes.io/component"  = "backend"
  "app.kubernetes.io/environment" = "development"
}

additional_tags = {
  "Environment" = "development"
  "Team"        = "backend"
  "CostCenter"  = "engineering"
}
