# Terraform Variables - Staging Environment
# Usage: terraform apply -var-file="environments/staging.tfvars"

# General Configuration
project_name = "receipt-app"
environment  = "staging"
aws_region   = "us-east-1"

# Kubernetes Configuration
# Note: These must be provided via environment variables or command line

# Namespace Configuration
namespace_name          = "receipt-app-staging"
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
app_replicas            = 2
app_port                = 8080
app_cpu_request         = "500m"
app_memory_request      = "512Mi"
app_cpu_limit           = "750m"
app_memory_limit        = "768Mi"

# Database Configuration
enable_mysql            = true
mysql_version           = "8.0"
mysql_root_password     = "staging-root-password-change-me"
mysql_user              = "appuser"
mysql_password          = "staging-app-password-change-me"
mysql_database          = "appdb"
mysql_storage_size      = "20Gi"

# RabbitMQ Configuration
enable_rabbitmq         = true
rabbitmq_version        = "3.12"
rabbitmq_user           = "guest"
rabbitmq_password       = "staging-rabbitmq-password-change-me"
rabbitmq_storage_size   = "5Gi"

# Ingress Configuration
enable_ingress          = true
ingress_class           = "nginx"
ingress_hostname        = "receipt-app.staging.example.com"
ingress_tls_enabled     = true
ingress_tls_issuer      = "letsencrypt-staging"  # Use staging issuer for non-prod

# Autoscaling Configuration
enable_autoscaling      = true
hpa_min_replicas        = 2
hpa_max_replicas        = 3
hpa_target_cpu          = 70
hpa_target_memory       = 80

# Security Configuration
enable_rbac             = true
enable_network_policies = true
enable_pod_security_policy = true

# Monitoring Configuration
enable_prometheus_scraping = true
prometheus_scrape_interval = "30s"

# Backup Configuration
enable_backup           = true
backup_retention_days   = 14

# Additional Labels
additional_labels = {
  "app.kubernetes.io/name"       = "receipt-app"
  "app.kubernetes.io/component"  = "backend"
  "app.kubernetes.io/environment" = "staging"
}

additional_tags = {
  "Environment" = "staging"
  "Team"        = "backend"
  "CostCenter"  = "engineering"
}
