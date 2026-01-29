# Terraform Variables - Production Environment
# Usage: terraform apply -var-file="environments/prod.tfvars"

# General Configuration
project_name = "receipt-app"
environment  = "production"
aws_region   = "us-east-1"

# Kubernetes Configuration
# Note: These must be provided via environment variables or command line
# CRITICAL: Use strong authentication for production!

# Namespace Configuration
namespace_name          = "receipt-app-prod"
create_namespaces       = true

# Storage Configuration
create_storage_classes  = true
storage_class_standard  = "standard"
storage_class_fast_ssd  = "fast-ssd"
storage_iops            = 4000  # Higher IOPS for production
storage_throughput      = 200   # Higher throughput for production

# Helm Chart Configuration
enable_helm_chart       = true
helm_chart_path         = "../helm/receipt-app"
helm_release_name       = "receipt-app"

# Application Configuration
app_image               = "receipt-app:1.0.0"
app_replicas            = 3
app_port                = 8080
app_cpu_request         = "1000m"
app_memory_request      = "1Gi"
app_cpu_limit           = "2000m"
app_memory_limit        = "2Gi"

# Database Configuration
enable_mysql            = true
mysql_version           = "8.0"
# IMPORTANT: Use AWS Secrets Manager or similar for production passwords!
# terraform apply -var="mysql_root_password=..." -var="mysql_password=..."
mysql_root_password     = "CHANGE_ME_USE_SECRETS_MANAGER"
mysql_user              = "appuser"
mysql_password          = "CHANGE_ME_USE_SECRETS_MANAGER"
mysql_database          = "appdb"
mysql_storage_size      = "50Gi"

# RabbitMQ Configuration
enable_rabbitmq         = true
rabbitmq_version        = "3.12"
rabbitmq_user           = "guest"
# IMPORTANT: Use AWS Secrets Manager or similar for production passwords!
rabbitmq_password       = "CHANGE_ME_USE_SECRETS_MANAGER"
rabbitmq_storage_size   = "20Gi"

# Ingress Configuration
enable_ingress          = true
ingress_class           = "nginx"
ingress_hostname        = "receipt-app.example.com"
ingress_tls_enabled     = true
ingress_tls_issuer      = "letsencrypt-prod"

# Autoscaling Configuration
enable_autoscaling      = true
hpa_min_replicas        = 3
hpa_max_replicas        = 10
hpa_target_cpu          = 60    # Lower target for production stability
hpa_target_memory       = 75    # Lower target for production stability

# Security Configuration
enable_rbac             = true
enable_network_policies = true
enable_pod_security_policy = true

# Monitoring Configuration
enable_prometheus_scraping = true
prometheus_scrape_interval = "15s"  # More frequent scraping in production

# Backup Configuration
enable_backup           = true
backup_retention_days   = 30

# Additional Labels
additional_labels = {
  "app.kubernetes.io/name"       = "receipt-app"
  "app.kubernetes.io/component"  = "backend"
  "app.kubernetes.io/environment" = "production"
  "backup-enabled"               = "true"
}

additional_tags = {
  "Environment" = "production"
  "Team"        = "backend"
  "CostCenter"  = "operations"
  "Backup"      = "enabled"
  "Monitoring"  = "enabled"
}
