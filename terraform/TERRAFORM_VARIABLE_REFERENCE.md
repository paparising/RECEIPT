# Terraform Variable Reference - Receipt Application

## Overview
This document provides detailed information about all Terraform variables used in the Receipt Application deployment.

---

## 📋 Variable Categories

### General Variables

#### `project_name`
- **Type**: `string`
- **Default**: `"receipt-app"`
- **Description**: Name of the project, used as prefix for resource names
- **Example**: `project_name = "receipt-app"`

#### `environment`
- **Type**: `string`
- **Default**: `"dev"`
- **Validation**: Must be one of: `dev`, `staging`, `production`
- **Description**: Deployment environment
- **Usage**: Differentiates dev/staging/production resources
- **Example**: `environment = "production"`

#### `aws_region`
- **Type**: `string`
- **Default**: `"us-east-1"`
- **Description**: AWS region for cloud resources
- **Example**: `aws_region = "us-west-2"`

#### `additional_labels`
- **Type**: `map(string)`
- **Default**: `{}`
- **Description**: Additional Kubernetes labels for all resources
- **Example**: 
  ```hcl
  additional_labels = {
    team      = "platform"
    cost-code = "12345"
  }
  ```

#### `additional_tags`
- **Type**: `map(string)`
- **Default**: `{}`
- **Description**: Additional AWS tags for all resources
- **Example**: 
  ```hcl
  additional_tags = {
    owner       = "devops"
    environment = "production"
  }
  ```

---

## 🔌 Kubernetes Variables

#### `kubernetes_host`
- **Type**: `string`
- **Required**: Yes (unless using kubeconfig)
- **Description**: Kubernetes API server endpoint
- **Format**: `https://your-cluster.com`
- **Example**: `kubernetes_host = "https://kubernetes.docker.internal"`
- **Alternative**: Set `KUBECONFIG` environment variable

#### `kubernetes_token`
- **Type**: `string`
- **Required**: Yes (unless using kubeconfig)
- **Sensitive**: Yes (won't appear in logs)
- **Description**: Authentication token for Kubernetes API
- **Get Token**:
  ```bash
  kubectl get secret $(kubectl get secret -n default -o name | grep default) -o jsonpath='{.data.token}' | base64 -d
  ```

#### `kubernetes_cluster_ca_certificate`
- **Type**: `string`
- **Required**: Yes (unless using kubeconfig)
- **Sensitive**: Yes
- **Description**: Base64-encoded CA certificate for cluster
- **Get Certificate**:
  ```bash
  kubectl get secret $(kubectl get secret -n default -o name | grep default) -o jsonpath='{.data.ca\.crt}' | base64 -d
  ```

#### `kubeconfig_path`
- **Type**: `string`
- **Default**: `"~/.kube/config"`
- **Description**: Path to kubeconfig file (alternative to token auth)
- **Example**: `kubeconfig_path = "/home/user/.kube/config"`

---

## 🏗️ Namespace Variables

#### `namespace_name`
- **Type**: `string`
- **Default**: `"${var.project_name}-${var.environment}"`
- **Description**: Kubernetes namespace name
- **Example**: `namespace_name = "receipt-app-prod"`
- **Auto-computed**: Yes, unless explicitly set

#### `create_namespace`
- **Type**: `bool`
- **Default**: `true`
- **Description**: Whether to create the namespace
- **Example**: `create_namespace = true`

---

## 💾 Storage Variables

#### `storage_class_standard`
- **Type**: `string`
- **Default**: `"standard"`
- **Description**: Name for standard storage class (gp2)
- **Example**: `storage_class_standard = "gp2"`

#### `storage_class_fast`
- **Type**: `string`
- **Default**: `"fast-ssd"`
- **Description**: Name for fast storage class (gp3)
- **Example**: `storage_class_fast = "gp3"`

#### `create_storage_classes`
- **Type**: `bool`
- **Default**: `true`
- **Description**: Whether to create storage classes
- **Example**: `create_storage_classes = true`

#### `storage_iops`
- **Type**: `number`
- **Default**: `3000`
- **Range**: 3000-16000
- **Description**: IOPS for gp3 storage
- **Dev**: 3000 (minimal)
- **Prod**: 4000-5000 (high performance)
- **Example**: `storage_iops = 4000`

#### `storage_throughput`
- **Type**: `number`
- **Default**: `125`
- **Range**: 125-1000 MB/s
- **Description**: Throughput for gp3 storage
- **Dev**: 125 MB/s
- **Prod**: 200-250 MB/s
- **Example**: `storage_throughput = 200`

---

## 🎯 Helm Release Variables

#### `helm_chart_path`
- **Type**: `string`
- **Default**: `"../../helm/receipt-app"`
- **Description**: Local path to Helm chart
- **Example**: `helm_chart_path = "../helm/receipt-app"`

#### `helm_chart_repository`
- **Type**: `string`
- **Default**: `""`
- **Description**: Helm chart repository URL (if not using local path)
- **Example**: `helm_chart_repository = "https://charts.example.com"`

#### `helm_release_name`
- **Type**: `string`
- **Default**: `"${var.project_name}-${var.environment}"`
- **Description**: Helm release name
- **Example**: `helm_release_name = "receipt-app-prod"`

#### `helm_namespace`
- **Type**: `string`
- **Default**: `kubernetes_namespace.receipt_app.metadata[0].name`
- **Description**: Kubernetes namespace for Helm release
- **Example**: `helm_namespace = "receipt-app-prod"`

#### `helm_create_namespace`
- **Type**: `bool`
- **Default**: `false`
- **Description**: Whether Helm creates namespace (we manage separately)
- **Example**: `helm_create_namespace = false`

#### `helm_chart_version`
- **Type**: `string`
- **Default**: `""`
- **Description**: Specific Helm chart version to deploy
- **Example**: `helm_chart_version = "1.0.0"`

#### `enable_helm_repository`
- **Type**: `bool`
- **Default**: `false`
- **Description**: Whether to register Helm repository
- **Example**: `enable_helm_repository = false`

#### `helm_repository_url`
- **Type**: `string`
- **Default**: `"https://charts.bitnami.com/bitnami"`
- **Description**: Helm repository URL for Bitnami charts
- **Example**: `helm_repository_url = "https://charts.example.com"`

---

## 🐳 Application Variables

#### `app_image`
- **Type**: `string`
- **Default**: `"receipt-app"`
- **Description**: Docker image name for application
- **Example**: `app_image = "myregistry.azurecr.io/receipt-app"`

#### `app_image_tag`
- **Type**: `string`
- **Default**: `"latest"`
- **Description**: Docker image tag
- **Example**: `app_image_tag = "v1.2.3"`
- **Per-Environment**: `dev` → "latest", `staging` → "staging", `prod` → specific version

#### `app_replicas`
- **Type**: `number`
- **Default**: `1`
- **Range**: 1-20
- **Validation**: Must be between 1 and 20
- **Dev**: 1
- **Staging**: 2
- **Prod**: 3-5
- **Example**: `app_replicas = 3`

#### `app_port`
- **Type**: `number`
- **Default**: `8080`
- **Description**: Application container port
- **Example**: `app_port = 8080`

#### `app_request_cpu`
- **Type**: `string`
- **Default**: `"250m"`
- **Description**: CPU resource request
- **Format**: `"100m"`, `"500m"`, `"1"`, `"2"`, etc.
- **Dev**: "250m"
- **Staging**: "500m"
- **Prod**: "1000m" or "1"
- **Example**: `app_request_cpu = "1000m"`

#### `app_request_memory`
- **Type**: `string`
- **Default**: `"256Mi"`
- **Description**: Memory resource request
- **Format**: `"128Mi"`, `"512Mi"`, `"1Gi"`, etc.
- **Dev**: "256Mi"
- **Staging**: "512Mi"
- **Prod**: "1Gi" or "2Gi"
- **Example**: `app_request_memory = "1Gi"`

#### `app_limit_cpu`
- **Type**: `string`
- **Default**: `"500m"`
- **Description**: CPU resource limit
- **Dev**: "500m"
- **Staging**: "750m"
- **Prod**: "2000m" or "2"
- **Example**: `app_limit_cpu = "2000m"`

#### `app_limit_memory`
- **Type**: `string`
- **Default**: `"512Mi"`
- **Description**: Memory resource limit
- **Dev**: "512Mi"
- **Staging**: "768Mi"
- **Prod**: "2Gi"
- **Example**: `app_limit_memory = "2Gi"`

#### `app_image_pull_policy`
- **Type**: `string`
- **Default**: `"IfNotPresent"` (prod), `"Always"` (dev)
- **Options**: `"Always"`, `"Never"`, `"IfNotPresent"`
- **Example**: `app_image_pull_policy = "Always"`

---

## 🗄️ MySQL Variables

#### `mysql_version`
- **Type**: `string`
- **Default**: `"8.0"`
- **Description**: MySQL version
- **Example**: `mysql_version = "8.0.35"`

#### `mysql_root_password`
- **Type**: `string`
- **Sensitive**: Yes (won't appear in logs)
- **Required**: Yes (no default)
- **Description**: Root password for MySQL
- **Security**: Use AWS Secrets Manager in production
- **Example**: `mysql_root_password = "SecureRootPassword123!"`
- **Generate**: `openssl rand -base64 32`

#### `mysql_user`
- **Type**: `string`
- **Default**: `"receipt_user"`
- **Description**: MySQL database user
- **Example**: `mysql_user = "receipt_app"`

#### `mysql_password`
- **Type**: `string`
- **Sensitive**: Yes
- **Required**: Yes (no default)
- **Description**: MySQL user password
- **Security**: Use AWS Secrets Manager in production
- **Example**: `mysql_password = "SecureUserPassword123!"`

#### `mysql_database`
- **Type**: `string`
- **Default**: `"receipt_db"`
- **Description**: Default MySQL database name
- **Example**: `mysql_database = "receipt_db_prod"`

#### `mysql_storage_size`
- **Type**: `string`
- **Default**: `"10Gi"`
- **Description**: MySQL persistent volume size
- **Dev**: "2Gi"
- **Staging**: "20Gi"
- **Prod**: "50Gi" or more
- **Example**: `mysql_storage_size = "50Gi"`

#### `mysql_storage_class`
- **Type**: `string`
- **Default**: `"standard"`
- **Description**: MySQL storage class name
- **Options**: `"standard"`, `"fast-ssd"`
- **Prod**: `"fast-ssd"`
- **Example**: `mysql_storage_class = "fast-ssd"`

---

## 🐰 RabbitMQ Variables

#### `rabbitmq_version`
- **Type**: `string`
- **Default**: `"3.12"`
- **Description**: RabbitMQ version
- **Example**: `rabbitmq_version = "3.12.8"`

#### `rabbitmq_user`
- **Type**: `string`
- **Default**: `"receipt_user"`
- **Description**: RabbitMQ administrative user
- **Example**: `rabbitmq_user = "receipt_app"`

#### `rabbitmq_password`
- **Type**: `string`
- **Sensitive**: Yes
- **Required**: Yes (no default)
- **Description**: RabbitMQ user password
- **Security**: Use AWS Secrets Manager in production
- **Example**: `rabbitmq_password = "SecureRabbitPassword123!"`

#### `rabbitmq_storage_size`
- **Type**: `string`
- **Default**: `"5Gi"`
- **Description**: RabbitMQ persistent volume size
- **Dev**: "2Gi"
- **Staging**: "5Gi"
- **Prod**: "20Gi"
- **Example**: `rabbitmq_storage_size = "20Gi"`

#### `rabbitmq_storage_class`
- **Type**: `string`
- **Default**: `"standard"`
- **Description**: RabbitMQ storage class
- **Prod**: `"fast-ssd"`
- **Example**: `rabbitmq_storage_class = "standard"`

---

## 🌐 Ingress Variables

#### `enable_ingress`
- **Type**: `bool`
- **Default**: `false` (dev), `true` (staging/prod)
- **Description**: Whether to create Ingress resource
- **Example**: `enable_ingress = true`

#### `ingress_class`
- **Type**: `string`
- **Default**: `"nginx"`
- **Description**: Ingress controller class
- **Options**: `"nginx"`, `"istio"`, `"alb"`, etc.
- **Example**: `ingress_class = "nginx"`

#### `ingress_hostname`
- **Type**: `string`
- **Default**: `"receipt-app.example.com"`
- **Description**: Application hostname for Ingress
- **Dev**: `"receipt-app.dev.local"` (no TLS)
- **Staging**: `"receipt-app.staging.example.com"` (TLS enabled)
- **Prod**: `"receipt-app.example.com"` (TLS enabled)
- **Example**: `ingress_hostname = "receipt-app.example.com"`

#### `enable_ingress_tls`
- **Type**: `bool`
- **Default**: `false` (dev), `true` (staging/prod)
- **Description**: Whether to enable TLS on Ingress
- **Example**: `enable_ingress_tls = true`

#### `ingress_tls_issuer`
- **Type**: `string`
- **Default**: `"letsencrypt-staging"`
- **Description**: Certificate issuer for TLS
- **Options**: `"letsencrypt-staging"`, `"letsencrypt-prod"`, custom issuer
- **Dev**: Not used (no TLS)
- **Staging**: `"letsencrypt-staging"`
- **Prod**: `"letsencrypt-prod"`
- **Example**: `ingress_tls_issuer = "letsencrypt-prod"`

---

## 🔄 Autoscaling Variables

#### `enable_autoscaling`
- **Type**: `bool`
- **Default**: `false` (dev), `true` (staging/prod)
- **Description**: Whether to enable HPA (Horizontal Pod Autoscaler)
- **Example**: `enable_autoscaling = true`

#### `hpa_min_replicas`
- **Type**: `number`
- **Default**: `1`
- **Range**: 1+
- **Validation**: Must be >= 1
- **Dev**: 1
- **Staging**: 2
- **Prod**: 3
- **Example**: `hpa_min_replicas = 3`

#### `hpa_max_replicas`
- **Type**: `number`
- **Default**: `3`
- **Range**: hpa_min_replicas+
- **Validation**: Must be >= hpa_min_replicas
- **Dev**: 1
- **Staging**: 3
- **Prod**: 10-20
- **Example**: `hpa_max_replicas = 10`

#### `hpa_cpu_target`
- **Type**: `number`
- **Default**: `70`
- **Range**: 1-100 (percent)
- **Validation**: Must be between 1 and 100
- **Description**: Target CPU utilization percentage
- **Dev**: N/A (no autoscaling)
- **Staging**: 70%
- **Prod**: 60-70%
- **Example**: `hpa_cpu_target = 60`

#### `hpa_memory_target`
- **Type**: `number`
- **Default**: `80`
- **Range**: 1-100 (percent)
- **Validation**: Must be between 1 and 100
- **Description**: Target memory utilization percentage
- **Dev**: N/A (no autoscaling)
- **Staging**: 80%
- **Prod**: 75-80%
- **Example**: `hpa_memory_target = 75`

---

## 🔐 Security Variables

#### `enable_rbac`
- **Type**: `bool`
- **Default**: `true`
- **Description**: Whether to create RBAC resources
- **Example**: `enable_rbac = true`

#### `enable_network_policies`
- **Type**: `bool`
- **Default**: `false` (dev), `true` (staging/prod)
- **Description**: Whether to create NetworkPolicy resources
- **Example**: `enable_network_policies = true`

#### `enable_pod_security_policy`
- **Type**: `bool`
- **Default**: `false` (dev), `true` (staging/prod)
- **Description**: Whether to enforce pod security policies
- **Example**: `enable_pod_security_policy = true`

#### `service_account_name`
- **Type**: `string`
- **Default**: `"${var.project_name}-sa"`
- **Description**: Kubernetes service account name
- **Example**: `service_account_name = "receipt-app-sa"`

---

## 📊 Monitoring Variables

#### `enable_prometheus_scraping`
- **Type**: `bool`
- **Default**: `false` (dev), `true` (staging/prod)
- **Description**: Whether to add Prometheus scraping annotations
- **Example**: `enable_prometheus_scraping = true`

#### `prometheus_scrape_interval`
- **Type**: `string`
- **Default**: `"30s"`
- **Description**: Prometheus scrape interval
- **Dev**: "30s"
- **Prod**: "15s" (more frequent)
- **Example**: `prometheus_scrape_interval = "15s"`

#### `prometheus_scrape_path`
- **Type**: `string`
- **Default**: `"/actuator/prometheus"`
- **Description**: Prometheus metrics endpoint path
- **Example**: `prometheus_scrape_path = "/metrics"`

#### `prometheus_scrape_port`
- **Type**: `string`
- **Default**: `"8080"`
- **Description**: Prometheus scrape port
- **Example**: `prometheus_scrape_port = "8080"`

---

## 💾 Backup Variables

#### `enable_backup`
- **Type**: `bool`
- **Default**: `false` (dev), `true` (staging/prod)
- **Description**: Whether to enable backups
- **Example**: `enable_backup = true`

#### `backup_retention_days`
- **Type**: `number`
- **Default**: `7`
- **Range**: 1-365
- **Description**: How many days to retain backups
- **Dev**: N/A
- **Staging**: 14
- **Prod**: 30-90
- **Example**: `backup_retention_days = 30`

---

## 🎯 Variable Precedence (Lowest to Highest)

1. **defaults.tfvars** (if exists)
2. **environments/*.tfvars** (dev, staging, prod)
3. **Command line**: `-var="key=value"`
4. **Environment variables**: `TF_VAR_key=value`

Example application order:
```bash
# Uses values from: prod.tfvars, then overrides with CLI arg
terraform apply \
  -var-file="environments/prod.tfvars" \
  -var="app_replicas=5"
```

---

## 🔍 Variable Validation Examples

### Invalid Configurations

❌ **Environment not allowed**
```hcl
environment = "production-2"  # Must be: dev, staging, production
```

❌ **Replicas out of range**
```hcl
app_replicas = 0      # Must be 1-20
app_replicas = 25     # Must be 1-20
```

❌ **HPA limits invalid**
```hcl
hpa_max_replicas = 2  # Must be >= hpa_min_replicas (3)
```

❌ **CPU target invalid**
```hcl
hpa_cpu_target = 150  # Must be 1-100
```

### Valid Configurations

✅ **Development**
```hcl
environment = "dev"
app_replicas = 1
enable_autoscaling = false
enable_ingress = false
```

✅ **Production**
```hcl
environment = "production"
app_replicas = 3
hpa_min_replicas = 3
hpa_max_replicas = 10
enable_autoscaling = true
enable_ingress = true
enable_network_policies = true
```

---

## 🚀 Quick Configuration Templates

### Development Setup
```hcl
environment                  = "dev"
app_replicas                = 1
enable_autoscaling         = false
enable_ingress             = false
enable_network_policies    = false
app_request_cpu            = "250m"
app_request_memory         = "256Mi"
app_limit_cpu              = "500m"
app_limit_memory           = "512Mi"
mysql_storage_size         = "2Gi"
rabbitmq_storage_size      = "2Gi"
```

### Staging Setup
```hcl
environment                = "staging"
app_replicas              = 2
hpa_min_replicas          = 2
hpa_max_replicas          = 3
enable_autoscaling        = true
enable_ingress            = true
enable_network_policies   = true
app_request_cpu           = "500m"
app_request_memory        = "512Mi"
mysql_storage_size        = "20Gi"
rabbitmq_storage_size     = "5Gi"
backup_retention_days     = 14
```

### Production Setup
```hcl
environment                = "production"
app_replicas              = 3
hpa_min_replicas          = 3
hpa_max_replicas          = 10
enable_autoscaling        = true
enable_ingress            = true
enable_ingress_tls        = true
enable_network_policies   = true
enable_pod_security_policy = true
app_request_cpu           = "1000m"
app_request_memory        = "1Gi"
app_limit_cpu             = "2000m"
app_limit_memory          = "2Gi"
storage_iops              = 4000
storage_throughput        = 200
mysql_storage_size        = "50Gi"
rabbitmq_storage_size     = "20Gi"
enable_prometheus_scraping = true
enable_backup             = true
backup_retention_days     = 30
```

---

**See also**: [TERRAFORM_DEPLOYMENT_GUIDE.md](TERRAFORM_DEPLOYMENT_GUIDE.md) for operational procedures.

