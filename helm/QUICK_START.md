# Quick Start - Helm Deployment

## Prerequisites

```bash
# Check Kubernetes cluster
kubectl cluster-info
kubectl get nodes

# Install/Verify Helm
helm version
which helm
```

## Step 1: Update Container Image

Update your Docker image in the registry:

```bash
# Build and push
docker build -t myregistry/receipt-app:1.0.0 .
docker push myregistry/receipt-app:1.0.0

# Update in values.yaml or use --set
```

## Step 2: Add Helm Chart Repository

```bash
# Add Bitnami repo for MySQL and RabbitMQ
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
```

## Step 3: Download Chart Dependencies

```bash
cd helm/receipt-app
helm dependency update
```

## Step 4: Deploy to Kubernetes

### Development Environment

```bash
helm install receipt-app . \
  --namespace dev \
  --create-namespace \
  --values values-dev.yaml
```

### Production Environment

```bash
helm install receipt-app . \
  --namespace production \
  --create-namespace \
  --values values-prod.yaml
```

## Step 5: Verify Deployment

```bash
# Check pod status
kubectl rollout status deployment/receipt-app -n production

# View pods
kubectl get pods -n production

# View services
kubectl get svc -n production

# View ingress
kubectl get ingress -n production

# View all resources
kubectl get all -n production
```

## Step 6: Access Application

### Via Ingress (Production)

```bash
# Access via configured hostname
curl https://receipt-app.example.com
```

### Via Port-Forward (Development)

```bash
# Forward local port to service
kubectl port-forward svc/receipt-app 8080:8080 -n dev &

# Access at http://localhost:8080
curl http://localhost:8080
```

## Common Operations

### Update Application

```bash
# Upgrade to new version
helm upgrade receipt-app . \
  --namespace production \
  --values values-prod.yaml \
  --set image.tag=2.0.0

# Monitor upgrade
kubectl rollout status deployment/receipt-app -n production -w
```

### Rollback Deployment

```bash
# View history
helm history receipt-app -n production

# Rollback to previous
helm rollback receipt-app -n production

# Rollback to specific revision
helm rollback receipt-app 2 -n production
```

### Scale Application

```bash
# Manual scale
kubectl scale deployment receipt-app --replicas=5 -n production

# Or update HPA
helm upgrade receipt-app . \
  --set autoscaling.maxReplicas=10 \
  -n production
```

### View Logs

```bash
# Real-time logs
kubectl logs -f deployment/receipt-app -n production

# Logs from specific pod
kubectl logs -f pod/receipt-app-xxxxx -n production
```

### Database Access

```bash
# Forward MySQL port
kubectl port-forward svc/mysql 3306:3306 -n production &

# Connect
mysql -h localhost -u appuser -p"apppassword" appdb

# Or use exec
kubectl exec -it pod/mysql-xxxxx -n production -- \
  mysql -u appuser -p"apppassword" appdb
```

### RabbitMQ Management UI

```bash
# Forward RabbitMQ port
kubectl port-forward svc/rabbitmq 15672:15672 -n production &

# Access http://localhost:15672
# Username: guest
# Password: guest
```

## Cleanup

```bash
# Delete release
helm uninstall receipt-app -n production

# Delete namespace
kubectl delete namespace production

# Delete all release history
helm delete receipt-app -n production --no-hooks
```

## Troubleshooting

### Pod Issues

```bash
# Describe pod
kubectl describe pod <pod-name> -n production

# View events
kubectl get events -n production --sort-by='.lastTimestamp'

# Check previous logs (crashed container)
kubectl logs <pod-name> -n production --previous
```

### Database Connection

```bash
# Test connectivity
kubectl exec pod/receipt-app-xxxxx -n production -- \
  nc -zv mysql 3306

# Check connection from pod
kubectl exec pod/receipt-app-xxxxx -n production -- \
  curl -v http://mysql:3306
```

### Resource Issues

```bash
# Check node resources
kubectl top nodes

# Check pod resource usage
kubectl top pod -n production

# Increase limits
helm upgrade receipt-app . \
  --set resources.limits.memory=2Gi \
  -n production
```

## Environment-Specific Values

All values can be overridden at deployment time:

```bash
helm install receipt-app . \
  --namespace production \
  --values values-prod.yaml \
  --set replicaCount=5 \
  --set image.tag=1.0.1 \
  --set logging.level.root=DEBUG \
  --set ingress.hosts[0].host=myapp.example.com
```

## Useful Links

- Chart Documentation: `helm/receipt-app/`
- Kubernetes Guide: `helm/KUBERNETES_DEPLOYMENT_GUIDE.md`
- Helm Configuration: `helm/HELM_CHART_CONFIGURATION_GUIDE.md`
- RBAC Setup: `helm/rbac.yaml`
