# Receipt Application - Terraform Infrastructure as Code

Complete Infrastructure-as-Code (IaC) solution for deploying the Receipt Management Application to Kubernetes using Terraform.

---

## 📚 Quick Navigation

- **[Getting Started](#-getting-started)** - 5-minute quickstart
- **[Deployment Guide](TERRAFORM_DEPLOYMENT_GUIDE.md)** - Comprehensive deployment procedures
- **[Variable Reference](TERRAFORM_VARIABLE_REFERENCE.md)** - All 50+ variables documented
- **[Best Practices](TERRAFORM_BEST_PRACTICES.md)** - Best practices and troubleshooting
- **[Architecture](#-architecture)** - Infrastructure design

---

## 🚀 Getting Started

### Prerequisites
```bash
# Install required tools
terraform version      # Should be ≥ 1.0
kubectl version        # Should be ≥ 1.20
helm version           # Should be ≥ 3.0
```

### 1. Initialize Terraform
```bash
cd terraform
terraform init
```

### 2. Configure Kubernetes Access
```bash
# Option A: Use kubeconfig (simplest)
export KUBECONFIG=~/.kube/config

# Option B: Set credentials via environment variables
export TF_VAR_kubernetes_host="https://your-cluster.com"
export TF_VAR_kubernetes_token="your-sa-token"
export TF_VAR_kubernetes_cluster_ca_certificate="your-ca-cert"
```

### 3. Choose Environment & Apply
```bash
# Development
terraform plan -var-file="environments/dev.tfvars"
terraform apply -var-file="environments/dev.tfvars"

# Staging
terraform plan -var-file="environments/staging.tfvars"
terraform apply -var-file="environments/staging.tfvars"

# Production
terraform plan -var-file="environments/prod.tfvars"
terraform apply -var-file="environments/prod.tfvars"
```

### 4. Verify Deployment
```bash
# View outputs
terraform output

# Check Kubernetes
NAMESPACE=$(terraform output -raw kubernetes_namespace)
kubectl get all -n $NAMESPACE
```

---

## 📁 Project Structure

```
terraform/
│
├── 📄 README.md                        ← You are here
├── 📄 TERRAFORM_DEPLOYMENT_GUIDE.md   ← How to deploy
├── 📄 TERRAFORM_VARIABLE_REFERENCE.md ← Variable docs
├── 📄 TERRAFORM_BEST_PRACTICES.md     ← Best practices & troubleshooting
│
├── 📋 providers.tf                     ← AWS, Kubernetes, Helm providers
├── 📋 variables.tf                     ← 50+ input variables with validation
├── 📋 outputs.tf                       ← Infrastructure output values
├── 📋 main.tf                          ← Core Kubernetes resources
│
├── 📁 environments/                    ← Environment-specific configurations
│   ├── dev.tfvars                      ← Development: 1 replica, minimal resources
│   ├── staging.tfvars                  ← Staging: 2 replicas, HA setup
│   └── prod.tfvars                     ← Production: 3 replicas, full HA
│
└── 📁 .terraform/                      ← Auto-generated Terraform working directory
    ├── providers/
    └── modules/
```

---

## 🏗️ Architecture Overview

### Deployment Flow
```
┌─────────────────────────────────────────────────────────────┐
│                    AWS Account                              │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         EKS Kubernetes Cluster                       │  │
│  │                                                      │  │
│  │  ┌────────────────────────────────────────────────┐ │  │
│  │  │  Namespace: receipt-app-prod                   │ │  │
│  │  │                                               │ │  │
│  │  │  ┌──────────────┐  ┌──────────────┐         │ │  │
│  │  │  │ Receipt App  │  │ Autoscaler   │         │ │  │
│  │  │  │ Deployment   │  │ (HPA)        │         │ │  │
│  │  │  │ (3 replicas) │  │ (3-10 pods)  │         │ │  │
│  │  │  └──────┬───────┘  └──────────────┘         │ │  │
│  │  │         │                                   │ │  │
│  │  │  ┌──────────────┐  ┌──────────────┐         │ │  │
│  │  │  │ MySQL DB     │  │ RabbitMQ     │         │ │  │
│  │  │  │ StatefulSet  │  │ StatefulSet  │         │ │  │
│  │  │  │ w/ 50Gi PVC  │  │ w/ 20Gi PVC  │         │ │  │
│  │  │  └──────────────┘  └──────────────┘         │ │  │
│  │  │         │                                   │ │  │
│  │  │  ┌──────────────────────────────────────┐  │ │  │
│  │  │  │ Ingress (NGINX)                      │  │ │  │
│  │  │  │ TLS: letsencrypt-prod                │  │ │  │
│  │  │  │ Host: receipt-app.example.com        │  │ │  │
│  │  │  └──────────────────────────────────────┘  │ │  │
│  │  │                                            │ │  │
│  │  │  ┌──────────────────────────────────────┐  │ │  │
│  │  │  │ Storage Classes (EBS gp3)            │  │ │  │
│  │  │  │ - Standard: gp2                      │  │ │  │
│  │  │  │ - Fast: gp3 (4000 IOPS, 200 MB/s)   │  │ │  │
│  │  │  └──────────────────────────────────────┘  │ │  │
│  │  │                                            │ │  │
│  │  │  ┌──────────────────────────────────────┐  │ │  │
│  │  │  │ RBAC (Service Account + Roles)       │  │ │  │
│  │  │  │ Network Policies                     │  │ │  │
│  │  │  └──────────────────────────────────────┘  │ │  │
│  │  └────────────────────────────────────────────┘ │  │
│  │                                                 │  │
│  └────────────────────────────────────────────────────┘  │
│                                                         │  │
│  ┌────────────────────────────────────────────────────┐  │
│  │         EBS Volumes (AWS)                         │  │
│  │ - MySQL Data: 50Gi gp3 with encryption            │  │
│  │ - RabbitMQ Data: 20Gi gp3 with encryption         │  │
│  └────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### Resource Types Created
1. **Kubernetes Namespace** - Application isolation
2. **Storage Classes** - Standard (gp2) and Fast (gp3)
3. **Helm Release** - Receipt application deployment
4. **Service Account** - RBAC identity
5. **Cluster Role & Binding** - Permission management
6. **Network Policies** - Network security
7. **AWS EBS Volumes** - Persistent data storage

---

## 🔧 Environment Configurations

### Development (`dev.tfvars`)
```
Environment: development
Namespace: receipt-app-dev
Replicas: 1
Autoscaling: disabled
Resources: 250m CPU / 256Mi memory
Storage: 2Gi MySQL, 2Gi RabbitMQ
TLS: disabled
Network Policies: disabled (loose security)
Ingress: receipt-app.dev.local
Use Case: Local development, testing
```

### Staging (`staging.tfvars`)
```
Environment: staging
Namespace: receipt-app-staging
Replicas: 2
Autoscaling: enabled (2-3 replicas)
Resources: 500m CPU / 512Mi memory
Storage: 20Gi MySQL, 5Gi RabbitMQ
TLS: enabled (staging issuer)
Network Policies: enabled
Ingress: receipt-app.staging.example.com
Use Case: Integration testing, pre-production
```

### Production (`prod.tfvars`)
```
Environment: production
Namespace: receipt-app-prod
Replicas: 3
Autoscaling: enabled (3-10 replicas)
Resources: 1000m CPU / 1Gi memory
Storage: 50Gi MySQL (gp3), 20Gi RabbitMQ
Storage IOPS: 4000, Throughput: 200 MB/s
TLS: enabled (production issuer - letsencrypt-prod)
Network Policies: strictly enabled
Ingress: receipt-app.example.com
Backup: enabled (30-day retention)
Monitoring: Prometheus scraping enabled
Use Case: Production workloads
```

---

## 📊 Configuration Examples

### Minimal Development Setup
```bash
terraform apply -var-file="environments/dev.tfvars"
```

### Scale to 5 Replicas
```bash
terraform apply \
  -var="app_replicas=5" \
  -var="hpa_max_replicas=15" \
  -var-file="environments/prod.tfvars"
```

### Use Different Storage Size
```bash
terraform apply \
  -var="mysql_storage_size=100Gi" \
  -var="storage_iops=5000" \
  -var-file="environments/prod.tfvars"
```

### Enable Feature Flags
```bash
terraform apply \
  -var="enable_autoscaling=true" \
  -var="enable_network_policies=true" \
  -var="enable_prometheus_scraping=true" \
  -var-file="environments/prod.tfvars"
```

---

## 🔐 Managing Secrets

### ❌ Never Do This
```bash
# DON'T commit passwords to git!
terraform apply -var="mysql_password=secret123"

# DON'T hardcode in tfvars!
cat environments/prod.tfvars
# mysql_password = "secret123"  ← BAD!
```

### ✅ Recommended Approaches

#### 1. Environment Variables (Simplest)
```bash
export TF_VAR_mysql_root_password="your-secure-password"
export TF_VAR_mysql_password="your-secure-password"
export TF_VAR_rabbitmq_password="your-secure-password"

terraform apply -var-file="environments/prod.tfvars"
```

#### 2. AWS Secrets Manager (Recommended)
```bash
# Store secret
aws secretsmanager create-secret \
  --name receipt-app/prod/mysql-password \
  --secret-string "$(openssl rand -base64 32)"

# Reference in Terraform
# (See TERRAFORM_VARIABLE_REFERENCE.md for details)
```

#### 3. Terraform Cloud (Enterprise)
Use sensitive variable sets in Terraform Cloud/Enterprise console.

---

## 📈 Common Operations

### View Current Infrastructure
```bash
# List all resources
terraform state list

# Show specific resource details
terraform state show kubernetes_namespace.receipt_app

# View all outputs
terraform output

# Get application URL
terraform output -raw application_access_info
```

### Update Configuration
```bash
# Change number of replicas
terraform apply \
  -var="app_replicas=5" \
  -var-file="environments/prod.tfvars"

# Update from staging to production
terraform apply -var-file="environments/prod.tfvars"

# Change storage size
terraform apply \
  -var="mysql_storage_size=100Gi" \
  -var-file="environments/prod.tfvars"
```

### Monitor Deployment
```bash
# Get deployment status
NAMESPACE=$(terraform output -raw kubernetes_namespace)

kubectl rollout status deployment/receipt-app-$(terraform output -raw environment) -n $NAMESPACE
kubectl get pods -n $NAMESPACE
kubectl logs -n $NAMESPACE -l app=receipt-app --tail=50 -f
```

### Destroy Infrastructure
```bash
# Destroy development
terraform destroy -var-file="environments/dev.tfvars"

# Destroy specific resources
terraform destroy -target=kubernetes_namespace.receipt_app

# Destroy without confirmation (dangerous!)
terraform destroy -auto-approve -var-file="environments/prod.tfvars"
```

---

## 🔒 Security Features

### Built-in Security
✅ **RBAC** - Role-based access control  
✅ **Network Policies** - Pod-to-pod network isolation  
✅ **Pod Security Policy** - Pod hardening  
✅ **Secrets Management** - Sensitive variable encryption  
✅ **TLS/HTTPS** - HTTPS ingress with Let's Encrypt  
✅ **Service Accounts** - Kubernetes service account for app  
✅ **Backup** - 30-day backup retention (production)  

### Security Best Practices
1. **Use AWS Secrets Manager** for production secrets
2. **Enable remote state locking** with DynamoDB
3. **Encrypt state files** with KMS
4. **Enable network policies** for production
5. **Use TLS certificates** from Let's Encrypt
6. **Regularly rotate secrets** and update passwords
7. **Monitor and audit** all infrastructure changes

---

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| [README.md](README.md) | This file - overview |
| [TERRAFORM_DEPLOYMENT_GUIDE.md](TERRAFORM_DEPLOYMENT_GUIDE.md) | Step-by-step deployment procedures |
| [TERRAFORM_VARIABLE_REFERENCE.md](TERRAFORM_VARIABLE_REFERENCE.md) | Complete variable documentation |
| [TERRAFORM_BEST_PRACTICES.md](TERRAFORM_BEST_PRACTICES.md) | Best practices and troubleshooting |

---

## 🆘 Troubleshooting Quick Reference

| Issue | Solution |
|-------|----------|
| Can't connect to cluster | Check kubeconfig or set kubernetes_host/token |
| Helm chart not found | Verify path or use absolute path |
| Storage not provisioning | Install EBS CSI driver or use existing storage class |
| Pod won't start | Check resource limits, check node capacity |
| Ingress not working | Install ingress controller, configure DNS |
| State lock stuck | Run `terraform force-unlock <LOCK_ID>` |

See [TERRAFORM_BEST_PRACTICES.md](TERRAFORM_BEST_PRACTICES.md) for detailed troubleshooting.

---

## 🚀 Advanced Topics

### Workspaces for Multiple Clusters
```bash
terraform workspace new dev
terraform workspace new staging
terraform workspace new prod

terraform workspace select dev
terraform apply -var-file="environments/dev.tfvars"

terraform workspace select prod
terraform apply -var-file="environments/prod.tfvars"
```

### Remote State Management
```hcl
terraform {
  backend "s3" {
    bucket         = "receipt-app-terraform"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "terraform-locks"
  }
}
```

### Using Modules for Code Reuse
```hcl
module "app_helm" {
  source = "./modules/helm-chart"
  
  release_name = "receipt-app"
  namespace    = kubernetes_namespace.receipt_app.metadata[0].name
  # ... other variables
}
```

### CI/CD Integration
See [TERRAFORM_BEST_PRACTICES.md](TERRAFORM_BEST_PRACTICES.md) for GitHub Actions and GitLab CI examples.

---

## 📊 Typical Deployment Workflow

```bash
# 1. Clone repository
git clone <repo>
cd terraform

# 2. Initialize Terraform
terraform init

# 3. Configure credentials
export TF_VAR_kubernetes_host="..."
export TF_VAR_kubernetes_token="..."
export TF_VAR_mysql_password="..." # Use AWS Secrets Manager!

# 4. Plan changes
terraform plan -var-file="environments/prod.tfvars" -out=tfplan

# 5. Review plan
terraform show tfplan

# 6. Apply changes
terraform apply tfplan

# 7. Verify deployment
terraform output
kubectl get all -n receipt-app-prod

# 8. Monitor application
kubectl logs -n receipt-app-prod -l app=receipt-app -f
```

---

## 🔄 Update Workflow

```bash
# 1. Make changes to configuration
# Edit main.tf, variables.tf, or environments/*.tfvars

# 2. Plan changes
terraform plan -var-file="environments/prod.tfvars" -out=tfplan

# 3. Review and test
terraform show tfplan
terraform validate

# 4. Apply when ready
terraform apply tfplan

# 5. Monitor changes
kubectl rollout status deployment -n $(terraform output -raw kubernetes_namespace)
```

---

## 📞 Support & Resources

- **Terraform Docs**: https://www.terraform.io/docs
- **Kubernetes Provider**: https://registry.terraform.io/providers/hashicorp/kubernetes/latest
- **Helm Provider**: https://registry.terraform.io/providers/hashicorp/helm/latest
- **AWS Provider**: https://registry.terraform.io/providers/hashicorp/aws/latest

---

## ✅ Quality Checklist

Before deploying to production:

- [ ] Reviewed all variables in `environments/prod.tfvars`
- [ ] Set secure passwords for MySQL and RabbitMQ
- [ ] Configured AWS credentials
- [ ] Configured Kubernetes credentials
- [ ] Ran `terraform validate` successfully
- [ ] Reviewed `terraform plan` output
- [ ] Tested in staging environment first
- [ ] Enabled remote state locking (DynamoDB)
- [ ] Enabled state encryption (KMS)
- [ ] Set up backup procedures
- [ ] Configured monitoring (Prometheus)
- [ ] Set up log aggregation
- [ ] Documented any customizations
- [ ] Tested disaster recovery procedures

---

## 📝 File Manifesto

### Core Configuration Files
- **providers.tf** (60 lines)
  - Terraform, AWS, Kubernetes, Helm provider configuration
  - AWS tags, Kubernetes authentication, backend example

- **variables.tf** (400+ lines)
  - 50+ input variables
  - Validation rules
  - Comprehensive descriptions
  - Sensitive variable flagging

- **outputs.tf** (300+ lines)
  - 20+ output values
  - Connection strings
  - Useful commands
  - Infrastructure information

- **main.tf** (350+ lines)
  - Kubernetes namespace
  - Storage classes (standard, fast-ssd)
  - Helm release (with MySQL and RabbitMQ)
  - RBAC (service account, cluster role, binding)
  - Network policies

### Environment Configurations
- **environments/dev.tfvars** (60 lines)
  - 1 replica, minimal resources
  - No autoscaling, no TLS
  - Test credentials

- **environments/staging.tfvars** (60 lines)
  - 2 replicas, moderate resources
  - Autoscaling enabled (2-3)
  - Staging TLS certificate

- **environments/prod.tfvars** (70 lines)
  - 3 replicas, generous resources
  - Autoscaling enabled (3-10)
  - Production TLS, all security enabled
  - Secret warnings for safety

### Documentation
- **README.md** (this file)
  - Overview and quickstart

- **TERRAFORM_DEPLOYMENT_GUIDE.md**
  - Comprehensive deployment procedures
  - Command reference
  - Scenarios and examples

- **TERRAFORM_VARIABLE_REFERENCE.md**
  - All 50+ variables documented
  - Default values
  - Usage examples
  - Validation rules

- **TERRAFORM_BEST_PRACTICES.md**
  - Best practices
  - Troubleshooting guide
  - Debugging commands
  - Security checklist

---

## 🎯 Next Steps

1. **Read**: [TERRAFORM_DEPLOYMENT_GUIDE.md](TERRAFORM_DEPLOYMENT_GUIDE.md)
2. **Review**: `environments/prod.tfvars` for your environment
3. **Configure**: Kubernetes and AWS credentials
4. **Plan**: `terraform plan -var-file="environments/prod.tfvars"`
5. **Deploy**: `terraform apply -var-file="environments/prod.tfvars"`
6. **Monitor**: Check deployment status with kubectl

---

**Status**: ✅ **Production Ready**

**Version**: 1.0  
**Last Updated**: 2024  
**Maintained By**: DevOps Team

