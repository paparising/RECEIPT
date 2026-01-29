# Terraform Best Practices & Troubleshooting - Receipt Application

## 🏆 Best Practices

### 1. State Management

#### Store State Remotely
```hcl
# terraform/providers.tf
terraform {
  backend "s3" {
    bucket         = "receipt-app-terraform-state"
    key            = "terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "terraform-locks"
  }
}
```

#### Enable State Locking
```bash
# Create DynamoDB table for locks
aws dynamodb create-table \
  --table-name terraform-locks \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
```

#### Backup State Regularly
```bash
# Daily backup
0 2 * * * cp /path/to/terraform.tfstate /backups/terraform.tfstate.$(date +\%Y\%m\%d)

# Or for S3 backend
aws s3 sync s3://receipt-app-terraform-state /backups/
```

#### Never Commit Sensitive Data
```bash
# .gitignore
*.tfstate
*.tfstate.backup
.terraform/
.terraform.lock.hcl
override.tf
override.tf.json
*_override.tf
*_override.tf.json
*.tfvars
*.tfvars.json
!example.tfvars
crash.log
crash.*.log
override.tf
override.tf.json
*_override.tf
*_override.tf.json
.DS_Store
*.swp
*.swo
*~
.idea/
*.iml
```

---

### 2. Variable Management

#### Use Defaults for Non-Sensitive Values
```hcl
variable "app_replicas" {
  type        = number
  default     = 1
  description = "Number of application replicas"
}
```

#### Mark Sensitive Data
```hcl
variable "mysql_root_password" {
  type        = string
  sensitive   = true  # Won't appear in logs/output
  description = "MySQL root password"
}
```

#### Use Environment-Specific Files
```bash
# Structure
terraform/
├── environments/
│   ├── dev.tfvars
│   ├── staging.tfvars
│   └── prod.tfvars
```

#### Use AWS Secrets Manager for Secrets
```bash
# Store secret
aws secretsmanager create-secret \
  --name receipt-app/prod/mysql-password \
  --secret-string "YourSecretPassword123!"

# Retrieve in Terraform
data "aws_secretsmanager_secret_version" "mysql_password" {
  secret_id = "receipt-app/prod/mysql-password"
}

# Use in configuration
mysql_root_password = data.aws_secretsmanager_secret_version.mysql_password.secret_string
```

#### Use Environment Variables for CI/CD
```bash
# In CI/CD pipeline
export TF_VAR_mysql_root_password="$MYSQL_ROOT_PASSWORD"
export TF_VAR_mysql_password="$MYSQL_PASSWORD"
export TF_VAR_rabbitmq_password="$RABBITMQ_PASSWORD"

terraform apply -var-file="environments/prod.tfvars"
```

---

### 3. Code Organization

#### Use Modules for Reusability
```hcl
# terraform/modules/helm-chart/main.tf
resource "helm_release" "app" {
  name      = var.release_name
  namespace = var.namespace
  # ... rest of configuration
}

# terraform/main.tf
module "app_helm" {
  source = "./modules/helm-chart"
  
  release_name = local.release_name
  namespace    = kubernetes_namespace.receipt_app.metadata[0].name
}
```

#### Use Locals for Computed Values
```hcl
locals {
  release_name = "${var.project_name}-${var.environment}"
  namespace    = "${var.project_name}-${var.environment}"
  common_labels = merge(
    var.additional_labels,
    {
      app         = var.project_name
      environment = var.environment
      managed_by  = "terraform"
    }
  )
}
```

#### Keep Files Organized by Function
```bash
terraform/
├── main.tf              # Core resources
├── providers.tf         # Provider configuration
├── variables.tf         # Input variables
├── outputs.tf           # Output values
├── locals.tf            # Local values (optional)
├── data.tf              # Data sources (optional)
└── environments/        # Environment configurations
```

---

### 4. Testing and Validation

#### Validate Configuration
```bash
# Check syntax
terraform validate

# Format check
terraform fmt -check -recursive

# Full validation
terraform validate && terraform fmt -check
```

#### Plan Before Apply
```bash
# Always review plan
terraform plan -var-file="environments/prod.tfvars" -out=tfplan

# Review plan file
terraform show tfplan

# Check for dangerous changes
terraform plan -var-file="environments/prod.tfvars" | grep -E "^\s*[-+]"
```

#### Use Terraform Cloud/Enterprise
```hcl
terraform {
  cloud {
    organization = "my-organization"
    
    workspaces {
      name = "receipt-app-prod"
    }
  }
}
```

---

### 5. Documentation

#### Document Variables
```hcl
variable "app_replicas" {
  type        = number
  default     = 1
  description = "Number of application replicas to deploy"
  
  validation {
    condition     = var.app_replicas >= 1 && var.app_replicas <= 20
    error_message = "Replicas must be between 1 and 20."
  }
}
```

#### Document Outputs
```hcl
output "application_url" {
  description = "URL to access the application"
  value       = "http://${local.ingress_hostname}"
}
```

#### Add Inline Comments
```hcl
# Configure Kubernetes storage
resource "kubernetes_storage_class" "standard" {
  metadata {
    name = var.storage_class_standard
  }
  
  storage_provisioner = "ebs.csi.aws.com"
  # ... rest of configuration
}
```

---

### 6. Security

#### Use RBAC Properly
```bash
# Create minimal IAM policy for Terraform
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ec2:CreateVolume",
        "ec2:DeleteVolume",
        "ec2:DescribeVolumes"
      ],
      "Resource": "arn:aws:ec2:*:*:volume/*"
    }
  ]
}
```

#### Encrypt State
```hcl
terraform {
  backend "s3" {
    encrypt = true
    sse_kms_key_id = "arn:aws:kms:us-east-1:123456789012:key/12345678-1234-1234-1234-123456789012"
  }
}
```

#### Use AWS KMS for Encryption
```bash
# Create KMS key
aws kms create-key --description "Terraform state encryption"

# Get key ID
aws kms list-keys
```

---

## 🔧 Troubleshooting

### Common Issues and Solutions

### Issue 1: Kubernetes Connection Failed

**Error**:
```
Error: Unable to connect to Kubernetes API Server: dial tcp: lookup on ...: no such host
```

**Causes & Solutions**:

1. **Wrong kubeconfig path**
```bash
# Check kubeconfig
kubectl config view

# Set correct path
export KUBECONFIG=/path/to/kubeconfig.yaml

# Or specify in variables
kubeconfig_path = "/Users/username/.kube/config"
```

2. **Invalid credentials**
```bash
# Verify cluster connection
kubectl cluster-info

# Get cluster details
kubectl config get-contexts

# Switch context
kubectl config use-context your-context
```

3. **Network connectivity**
```bash
# Test API server endpoint
curl -k https://your-cluster.com/api/v1

# Check firewall
telnet your-cluster.com 443
```

---

### Issue 2: Helm Release Not Found

**Error**:
```
Error: chart path "/path/to/helm/receipt-app" not found
```

**Solutions**:

1. **Chart path is wrong**
```bash
# Check chart location
ls -la terraform/
ls -la helm/

# Use correct relative path
helm_chart_path = "../../helm/receipt-app"

# Or absolute path
helm_chart_path = "/full/path/to/helm/receipt-app"
```

2. **Chart files missing**
```bash
# Verify Chart.yaml exists
ls helm/receipt-app/Chart.yaml

# Check chart structure
ls helm/receipt-app/
# Should show: Chart.yaml, values.yaml, templates/
```

---

### Issue 3: Storage Class Not Available

**Error**:
```
Error: PersistentVolumeClaim not provisioning
Error: no persistent volumes available
```

**Solutions**:

1. **Storage provisioner not installed**
```bash
# Check available storage classes
kubectl get storageclass

# For AWS EBS, install CSI driver
helm repo add aws-ebs-csi-driver https://kubernetes-sigs.github.io/aws-ebs-csi-driver
helm install aws-ebs-csi-driver aws-ebs-csi-driver/aws-ebs-csi-driver -n kube-system
```

2. **Use existing storage class**
```hcl
storage_class_standard = "gp2"  # Use default AWS storage class
```

---

### Issue 4: Insufficient Resources

**Error**:
```
Error: Pods pending
Error: CrashLoopBackOff
```

**Solutions**:

1. **Reduce resource requests**
```bash
terraform apply \
  -var="app_request_cpu=100m" \
  -var="app_request_memory=128Mi" \
  -var-file="environments/dev.tfvars"
```

2. **Check node capacity**
```bash
# Check available resources
kubectl describe nodes

# Check pod requests
kubectl top pods -n receipt-app-prod
```

3. **Add more nodes**
```bash
# Scale cluster
# (Procedure depends on your Kubernetes provider)
aws eks update-nodegroup-config --cluster-name myCluster --nodegroup-name myNodeGroup --scaling-config minSize=3,maxSize=10,desiredSize=5
```

---

### Issue 5: Terraform State Lock

**Error**:
```
Error: Resource instance already exists in state
Error: Error releasing the lock
```

**Solutions**:

1. **Force unlock** (use carefully!)
```bash
terraform force-unlock <LOCK_ID>
```

2. **Check lock**
```bash
# For DynamoDB locks
aws dynamodb scan --table-name terraform-locks
```

3. **Prevent concurrent runs**
```bash
# Use backend locking
# (Already configured in providers.tf with DynamoDB)
```

---

### Issue 6: Plan Shows Unexpected Changes

**Error**:
```
Terraform will perform the following actions...
```

**Solutions**:

1. **Refresh state**
```bash
terraform refresh -var-file="environments/prod.tfvars"
```

2. **Check for manual changes**
```bash
# What was changed manually in cluster?
kubectl get all -n receipt-app-prod

# Check if state is out of sync
terraform plan -var-file="environments/prod.tfvars" | grep "~"
```

3. **Ignore changes** (if needed)
```hcl
lifecycle {
  ignore_changes = [
    helm_release.metadata[0].labels["version"]
  ]
}
```

---

### Issue 7: Helm Repository Issues

**Error**:
```
Error: chart not found in repository
Error: failed to update repository
```

**Solutions**:

1. **Update Helm repos**
```bash
helm repo update
```

2. **Check repository access**
```bash
helm repo list
helm search repo bitnami/mysql

# Or in Terraform
terraform apply -var="enable_helm_repository=true"
```

3. **Use local chart path**
```hcl
helm_chart_path = "../../helm/receipt-app"
```

---

### Issue 8: Ingress Not Working

**Error**:
```
Error: ingress not assigned IP/hostname
Error: can't reach application via hostname
```

**Solutions**:

1. **Check ingress controller**
```bash
kubectl get ingress -n receipt-app-prod
kubectl describe ingress -n receipt-app-prod

# Check if controller is running
kubectl get pods -n ingress-nginx
```

2. **Install ingress controller**
```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm install ingress-nginx ingress-nginx/ingress-nginx -n ingress-nginx --create-namespace
```

3. **Configure DNS**
```bash
# Get ingress IP
kubectl get svc -n ingress-nginx

# Add DNS record
# receipt-app.example.com -> <EXTERNAL-IP>
```

---

### Issue 9: Secret or ConfigMap Not Found

**Error**:
```
Error: secret "mysql-password" not found
Error: configmap not found
```

**Solutions**:

1. **Create secrets manually**
```bash
kubectl create secret generic mysql-password \
  --from-literal=password='your-password' \
  -n receipt-app-prod
```

2. **Check Helm values**
```yaml
# Check if Helm is creating secrets
mysql:
  auth:
    rootPassword: "value"
    password: "value"
```

3. **Verify secret exists**
```bash
kubectl get secrets -n receipt-app-prod
kubectl describe secret mysql-password -n receipt-app-prod
```

---

### Issue 10: RBAC Permission Denied

**Error**:
```
Error: forbidden: User "system:serviceaccount:..." cannot ...
```

**Solutions**:

1. **Check RBAC configuration**
```bash
terraform apply \
  -var="enable_rbac=true" \
  -var-file="environments/prod.tfvars"
```

2. **Verify service account**
```bash
kubectl get serviceaccount -n receipt-app-prod
kubectl describe sa receipt-app-sa -n receipt-app-prod
```

3. **Check role bindings**
```bash
kubectl get rolebindings -n receipt-app-prod
kubectl get clusterrolebindings | grep receipt
```

---

## 🔍 Debugging Commands

### Terraform Debug
```bash
# Enable verbose logging
TF_LOG=DEBUG terraform plan -var-file="environments/prod.tfvars"

# Log to file
TF_LOG_PATH=terraform.log TF_LOG=DEBUG terraform apply

# JSON logging
TF_LOG_JSON=true terraform apply
```

### Kubernetes Debug
```bash
# Check resource status
kubectl get all -n receipt-app-prod

# Check events
kubectl get events -n receipt-app-prod --sort-by='.lastTimestamp'

# Pod logs
kubectl logs -n receipt-app-prod -l app=receipt-app

# Describe problematic resource
kubectl describe pod <pod-name> -n receipt-app-prod

# Check resource usage
kubectl top pods -n receipt-app-prod
kubectl top nodes
```

### Helm Debug
```bash
# Check Helm release status
helm list -n receipt-app-prod
helm status receipt-app-prod -n receipt-app-prod

# Helm values
helm get values receipt-app-prod -n receipt-app-prod

# Helm history
helm history receipt-app-prod -n receipt-app-prod

# Debug template rendering
helm template receipt-app-prod -n receipt-app-prod
```

---

## 📊 Monitoring Terraform Execution

### Track Terraform Changes
```bash
# View complete plan
terraform plan -var-file="environments/prod.tfvars" -json | jq

# Count changes
terraform plan -var-file="environments/prod.tfvars" | grep -c "~"

# See what will be replaced
terraform plan -var-file="environments/prod.tfvars" | grep -E "^-/\+"
```

### Verify Deployment
```bash
# Get outputs
terraform output

# Get specific output
terraform output kubernetes_namespace

# Export for scripts
NS=$(terraform output -raw kubernetes_namespace)
kubectl get all -n $NS
```

---

## 🚨 Critical Warnings

### ⚠️ Changing Critical Variables
Avoid changing these in production:
- `kubernetes_host`, `kubernetes_token` (cluster connection)
- `project_name` (affects all resource names)
- `environment` (separates environments)
- `namespace_name` (existing workloads)

To change, destroy and recreate:
```bash
terraform destroy -var-file="environments/prod.tfvars"
terraform apply -var-file="environments/prod.tfvars"
```

### ⚠️ State File Loss
Always backup state:
```bash
# Daily backup
0 2 * * * cp terraform.tfstate terraform.tfstate.backup

# Or use remote backend (recommended)
terraform {
  backend "s3" {
    # AWS S3 is more reliable than local files
  }
}
```

### ⚠️ Credentials in Logs
Never log sensitive values:
```bash
# Don't do this:
terraform apply -var="mysql_password=secret123"

# Do this instead:
export TF_VAR_mysql_password="secret123"
terraform apply
```

---

## ✅ Health Checks

### Post-Deployment Verification

```bash
#!/bin/bash
# Verify Terraform deployment

NAMESPACE=$(terraform output -raw kubernetes_namespace)

echo "✓ Checking namespace..."
kubectl get namespace $NAMESPACE

echo "✓ Checking pods..."
kubectl get pods -n $NAMESPACE

echo "✓ Checking services..."
kubectl get svc -n $NAMESPACE

echo "✓ Checking deployments..."
kubectl rollout status deployment -n $NAMESPACE

echo "✓ Checking persistent volumes..."
kubectl get pvc -n $NAMESPACE

echo "✓ Checking ingress..."
kubectl get ingress -n $NAMESPACE

echo "✓ Checking events..."
kubectl get events -n $NAMESPACE --sort-by='.lastTimestamp' | tail -20

echo "✓ All checks complete!"
```

---

**Last Updated**: 2024
**Status**: ✅ Production-Ready

