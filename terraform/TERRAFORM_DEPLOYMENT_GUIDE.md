# Terraform Deployment Guide - Receipt Application

## Overview

This guide explains how to deploy the Receipt Management Application to Kubernetes using Terraform as Infrastructure-as-Code (IaC).

---

## 📋 Prerequisites

### Required Software
- **Terraform** >= 1.0
  ```bash
  # Install Terraform
  # macOS: brew install terraform
  # Windows: choco install terraform
  # Linux: https://www.terraform.io/downloads
  
  terraform version  # Verify installation
  ```

- **kubectl** >= 1.20
  ```bash
  kubectl version --client
  ```

- **Helm** >= 3.0
  ```bash
  helm version
  ```

### Kubernetes Cluster
- Kubernetes 1.20+
- Access to cluster API (kubeconfig or service account token)
- Storage provisioner installed (AWS EBS CSI, Azure Disks, etc.)
- Ingress controller (nginx or other)

### AWS Account (if using AWS)
- IAM credentials configured
- EC2 for EBS volumes
- Route53 for DNS (optional)

---

## 🚀 Quick Start (5 minutes)

### 1. Initialize Terraform
```bash
cd terraform

# Initialize Terraform (downloads providers)
terraform init
```

### 2. Configure Kubernetes Access
```bash
# Option A: Use kubeconfig
export KUBECONFIG=/path/to/kubeconfig.yaml

# Option B: Set environment variables
export TF_VAR_kubernetes_host="https://your-cluster.com"
export TF_VAR_kubernetes_token="your-token"
export TF_VAR_kubernetes_cluster_ca_certificate="your-cert"
```

### 3. Select Environment
```bash
# Choose environment: dev, staging, or prod
cd environments

# Review variables
cat dev.tfvars  # or staging.tfvars, prod.tfvars
```

### 4. Plan Deployment
```bash
cd ..

terraform plan -var-file="environments/dev.tfvars" -out=tfplan
```

### 5. Apply Configuration
```bash
terraform apply tfplan
```

### 6. Verify Deployment
```bash
# View outputs
terraform output

# Check Kubernetes deployment
kubectl get pods -n receipt-app-dev
kubectl rollout status deployment/receipt-app-dev -n receipt-app-dev
```

---

## 📁 Directory Structure

```
terraform/
├── main.tf                      # Main configuration (Kubernetes resources)
├── providers.tf                 # Provider configuration
├── variables.tf                 # Input variables (50+ parameters)
├── outputs.tf                   # Output values
├── terraform.tfvars             # Default variable values (optional)
├── environments/
│   ├── dev.tfvars              # Development environment config
│   ├── staging.tfvars          # Staging environment config
│   └── prod.tfvars             # Production environment config
├── modules/                     # (Optional) Terraform modules for reuse
│   ├── namespace/
│   ├── helm-chart/
│   └── networking/
├── .terraform/                  # (Auto-created) Terraform working directory
├── .gitignore                   # Git ignore rules
├── README.md                    # This file
└── TERRAFORM_REFERENCE.md       # Detailed variable reference
```

---

## 🔧 Configuration Files Explained

### main.tf
Core infrastructure configuration:
- Kubernetes namespaces
- Storage classes
- Service accounts and RBAC
- Network policies
- Helm release deployment

### providers.tf
Provider setup:
- AWS provider (region, tags)
- Kubernetes provider (cluster connection)
- Helm provider (chart repository)

### variables.tf
All input variables with defaults:
- 50+ configurable parameters
- Validation rules
- Sensitive variables (passwords, tokens)
- Type constraints

### environments/*.tfvars
Environment-specific values:
- **dev.tfvars** - 1 replica, minimal resources, no TLS
- **staging.tfvars** - 2 replicas, moderate resources, staging TLS
- **prod.tfvars** - 3 replicas, high resources, production TLS

---

## 💻 Common Commands

### Initialize Workspace
```bash
terraform init

# Re-initialize (e.g., after changing providers)
terraform init -upgrade
```

### Plan Changes
```bash
# View what will be created
terraform plan -var-file="environments/dev.tfvars"

# Save plan to file
terraform plan -var-file="environments/dev.tfvars" -out=tfplan

# Show detailed changes
terraform plan -var-file="environments/dev.tfvars" -detailed-exitcode
```

### Apply Configuration
```bash
# Apply with plan file
terraform apply tfplan

# Apply directly (asks for confirmation)
terraform apply -var-file="environments/dev.tfvars"

# Apply without confirmation (use with caution!)
terraform apply -var-file="environments/dev.tfvars" -auto-approve
```

### View State and Outputs
```bash
# List all resources
terraform state list

# Show resource details
terraform state show kubernetes_namespace.receipt_app

# View all outputs
terraform output

# View specific output
terraform output application_access_info

# Output as JSON
terraform output -json
```

### Modify Configuration
```bash
# Update variables
terraform apply -var="app_replicas=5" -var-file="environments/dev.tfvars"

# Change environment
terraform apply -var-file="environments/staging.tfvars"

# Scale up
terraform apply -var="hpa_max_replicas=15" -var-file="environments/prod.tfvars"
```

### Destroy Resources
```bash
# Review what will be destroyed
terraform plan -destroy -var-file="environments/dev.tfvars"

# Destroy infrastructure
terraform destroy -var-file="environments/dev.tfvars"

# Destroy without confirmation
terraform destroy -var-file="environments/dev.tfvars" -auto-approve
```

### Debugging
```bash
# Verbose logging
TF_LOG=DEBUG terraform apply

# JSON logging
TF_LOG_PATH=terraform.log terraform apply

# Validate configuration
terraform validate

# Format code
terraform fmt -recursive

# Check code quality
terraform plan -json | jq
```

---

## 🔐 Sensitive Variables & Secrets

### Production Passwords
**NEVER** commit passwords to version control!

#### Option 1: Environment Variables
```bash
export TF_VAR_mysql_root_password="your-secure-password"
export TF_VAR_mysql_password="your-secure-password"
export TF_VAR_rabbitmq_password="your-secure-password"

terraform apply -var-file="environments/prod.tfvars"
```

#### Option 2: AWS Secrets Manager
```bash
# Create secret
aws secretsmanager create-secret \
  --name receipt-app/prod/mysql-password \
  --secret-string "your-password"

# Retrieve in Terraform
data "aws_secretsmanager_secret_version" "mysql_password" {
  secret_id = "receipt-app/prod/mysql-password"
}
```

#### Option 3: Terraform Cloud/Enterprise
Use variable sets with sensitive flag set.

### Rotate Secrets
```bash
# Update MySQL password
terraform apply \
  -var="mysql_password=new-password" \
  -var-file="environments/prod.tfvars"

# Update all secrets
terraform apply \
  -var="mysql_password=new-mysql-pwd" \
  -var="rabbitmq_password=new-rabbitmq-pwd" \
  -var-file="environments/prod.tfvars"
```

---

## 📊 Deployment Scenarios

### Scenario 1: Deploy to Development
```bash
# Initialize
terraform init

# Plan
terraform plan -var-file="environments/dev.tfvars" -out=dev.plan

# Apply
terraform apply dev.plan
```

### Scenario 2: Promote from Staging to Production
```bash
# Create backup of staging state
cp terraform.tfstate terraform.tfstate.staging.backup

# Plan production changes
terraform plan -var-file="environments/prod.tfvars" -out=prod.plan

# Review plan carefully
terraform show prod.plan

# Apply
terraform apply prod.plan
```

### Scenario 3: Scale Application
```bash
# Scale to 5 replicas
terraform apply \
  -var="app_replicas=5" \
  -var="hpa_max_replicas=15" \
  -var-file="environments/prod.tfvars"

# Or update tfvars and apply
# Edit environments/prod.tfvars: app_replicas = 5, hpa_max_replicas = 15
terraform apply -var-file="environments/prod.tfvars"
```

### Scenario 4: Change Storage Class
```bash
# Use faster storage for production
terraform apply \
  -var="storage_iops=5000" \
  -var="storage_throughput=250" \
  -var-file="environments/prod.tfvars"
```

### Scenario 5: Enable/Disable Features
```bash
# Disable autoscaling
terraform apply \
  -var="enable_autoscaling=false" \
  -var-file="environments/dev.tfvars"

# Enable network policies
terraform apply \
  -var="enable_network_policies=true" \
  -var-file="environments/staging.tfvars"
```

---

## 🔄 Managing Terraform State

### Local State (Default)
State stored in `terraform.tfstate`:
```bash
# View state
cat terraform.tfstate | jq

# Backup state
cp terraform.tfstate terraform.tfstate.backup

# Restore from backup
cp terraform.tfstate.backup terraform.tfstate
```

### Remote State (Recommended for Production)
Use S3 + DynamoDB for remote state:

1. **Create S3 Bucket**
```bash
aws s3 mb s3://receipt-app-terraform-state
aws s3api put-bucket-versioning \
  --bucket receipt-app-terraform-state \
  --versioning-configuration Status=Enabled
```

2. **Create DynamoDB Table**
```bash
aws dynamodb create-table \
  --table-name terraform-locks \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
```

3. **Enable in providers.tf**
```hcl
terraform {
  backend "s3" {
    bucket         = "receipt-app-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "terraform-locks"
  }
}
```

4. **Migrate State**
```bash
terraform init  # Will ask to copy state to backend
```

---

## 🏗️ Managing Multiple Environments

### Workspace Approach
```bash
# Create workspaces
terraform workspace new dev
terraform workspace new staging
terraform workspace new prod

# List workspaces
terraform workspace list

# Switch workspace
terraform workspace select dev

# Apply to current workspace
terraform apply -var-file="environments/dev.tfvars"

# Use in code
variable "environment" {
  default = terraform.workspace
}
```

### Separate State Files Approach (Recommended)
```bash
# Development
terraform apply -var-file="environments/dev.tfvars"

# Staging (separate directory)
cd terraform-staging
terraform apply -var-file="../environments/staging.tfvars"

# Production (separate directory)
cd terraform-prod
terraform apply -var-file="../environments/prod.tfvars"
```

---

## 📈 Monitoring & Troubleshooting

### Check Deployment Status
```bash
# View Terraform outputs
terraform output

# Get application access info
terraform output application_access_info

# Check service endpoints
kubectl get svc -n $(terraform output -raw kubernetes_namespace)
```

### Verify Resources
```bash
# List created resources
terraform state list

# Show specific resource
terraform state show kubernetes_namespace.receipt_app

# Check Kubernetes resources
kubectl get all -n receipt-app-prod
```

### Troubleshoot Issues
```bash
# Validate configuration
terraform validate

# Format check
terraform fmt -check -recursive

# Provider info
terraform providers

# Debug mode
TF_LOG=DEBUG terraform apply -var-file="environments/dev.tfvars"
```

### Refresh State
```bash
# Update state with actual cluster state
terraform refresh -var-file="environments/prod.tfvars"

# Re-plan after refresh
terraform plan -var-file="environments/prod.tfvars"
```

---

## 🔒 Security Best Practices

### 1. Protect State Files
```bash
# Don't commit state files
echo "*.tfstate" >> .gitignore
echo "*.tfstate.backup" >> .gitignore
```

### 2. Use Secrets Manager
```bash
# Avoid hardcoding passwords
# Use AWS Secrets Manager, HashiCorp Vault, or similar
```

### 3. Limit Terraform Permissions
```bash
# Create IAM policy for Terraform
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ec2:*",
        "s3:*",
        "kms:*"
      ],
      "Resource": "*"
    }
  ]
}
```

### 4. Enable State Locking
```hcl
# Prevents concurrent modifications
backend "s3" {
  dynamodb_table = "terraform-locks"
}
```

### 5. Audit & Logging
```bash
# Enable CloudTrail for AWS changes
# Review Kubernetes audit logs
# Monitor Terraform logs
```

---

## 🔄 CI/CD Integration

### GitHub Actions Example
```yaml
name: Terraform Apply

on:
  push:
    branches: [main]
    paths: [terraform/**]

jobs:
  terraform:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: hashicorp/setup-terraform@v2
      
      - name: Terraform Init
        run: cd terraform && terraform init
      
      - name: Terraform Plan
        run: terraform plan -var-file="environments/prod.tfvars"
      
      - name: Terraform Apply
        if: github.ref == 'refs/heads/main'
        run: terraform apply -auto-approve
```

### GitLab CI Example
```yaml
stages:
  - validate
  - plan
  - apply

terraform:validate:
  stage: validate
  script:
    - cd terraform
    - terraform init
    - terraform validate

terraform:plan:
  stage: plan
  script:
    - terraform plan -var-file="environments/prod.tfvars" -out=tfplan

terraform:apply:
  stage: apply
  script:
    - terraform apply -auto-approve tfplan
  only:
    - main
```

---

## 📚 Additional Resources

- [Terraform Documentation](https://www.terraform.io/docs)
- [Kubernetes Provider](https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs)
- [Helm Provider](https://registry.terraform.io/providers/hashicorp/helm/latest/docs)
- [AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [Terraform Best Practices](https://www.terraform.io/docs/language/settings/backends/index.html)

---

## 🆘 Getting Help

### Check Logs
```bash
terraform plan -var-file="environments/dev.tfvars" 2>&1 | tee plan.log
```

### Validate Configuration
```bash
terraform validate
terraform fmt -recursive
```

### Test Before Applying
```bash
terraform plan -var-file="environments/dev.tfvars" -detailed-exitcode
echo $?  # 0 = no changes, 1 = error, 2 = changes needed
```

---

**Status**: ✅ **Terraform deployment ready**

See also: [TERRAFORM_REFERENCE.md](TERRAFORM_REFERENCE.md) for detailed variable documentation.

