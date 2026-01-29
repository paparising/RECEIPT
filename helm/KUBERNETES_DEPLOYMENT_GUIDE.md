# Kubernetes Deployment Guide for Receipt Application

## Prerequisites

1. **Kubernetes Cluster** (1.20+)
   - kubectl configured and connected to cluster
   - Helm 3.x installed
   - Sufficient resources (at least 3 nodes for production)

2. **Required Tools**
   ```bash
   # Install Helm
   curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
   
   # Verify installation
   helm version
   kubectl version --client
   ```

3. **Container Registry**
   - Push your Docker image to a registry (Docker Hub, ECR, GCR, etc.)
   - Example: `docker tag receipt-app:1.0.0 myregistry/receipt-app:1.0.0`
   - Example: `docker push myregistry/receipt-app:1.0.0`

4. **Update values.yaml**
   - Change `image.repository` to your registry URL
   - Update ingress hostnames
   - Configure storage classes if needed

## Quick Start - Development

```bash
# Navigate to helm chart directory
cd helm/receipt-app

# Install Helm dependencies
helm dependency update

# Deploy to development cluster
helm install receipt-app . \
  --namespace default \
  --values values-dev.yaml \
  --set image.repository=myregistry/receipt-app \
  --set image.tag=1.0.0

# Verify deployment
kubectl rollout status deployment/receipt-app -n default
kubectl get pods -n default -l app=receipt-app
kubectl get svc -n default
```

## Staging Deployment

```bash
# Create staging namespace
kubectl create namespace staging

# Deploy to staging
helm install receipt-app . \
  --namespace staging \
  --values values-staging.yaml \
  --set image.repository=myregistry/receipt-app \
  --set image.tag=1.0.0

# Verify
kubectl get all -n staging
```

## Production Deployment

```bash
# Create production namespace
kubectl create namespace production

# Create secret for image pull (if using private registry)
kubectl create secret docker-registry regcred \
  --docker-server=myregistry \
  --docker-username=<username> \
  --docker-password=<password> \
  --docker-email=<email> \
  -n production

# Deploy to production
helm install receipt-app . \
  --namespace production \
  --values values-prod.yaml \
  --set image.repository=myregistry/receipt-app \
  --set image.tag=1.0.0 \
  --set imagePullSecrets[0].name=regcred

# Verify production deployment
kubectl get all -n production
kubectl describe pod -n production -l app=receipt-app
```

## Helm Commands

### Installation

```bash
# Install release
helm install receipt-app ./helm/receipt-app \
  -n production \
  -f values-prod.yaml

# Install with custom values
helm install receipt-app ./helm/receipt-app \
  -n production \
  -f values-prod.yaml \
  --set replicaCount=5 \
  --set image.tag=2.0.0
```

### Upgrade

```bash
# Update Helm dependencies
helm dependency update ./helm/receipt-app

# Upgrade existing release
helm upgrade receipt-app ./helm/receipt-app \
  -n production \
  -f values-prod.yaml

# Upgrade with rollback on failure
helm upgrade receipt-app ./helm/receipt-app \
  -n production \
  -f values-prod.yaml \
  --atomic
```

### Rollback

```bash
# View release history
helm history receipt-app -n production

# Rollback to previous version
helm rollback receipt-app 1 -n production

# Rollback to specific revision
helm rollback receipt-app 3 -n production
```

### Uninstall

```bash
# Delete release but keep namespace
helm uninstall receipt-app -n production

# Delete release and namespace
kubectl delete namespace production
```

### Inspection

```bash
# View rendered templates
helm template receipt-app ./helm/receipt-app \
  -n production \
  -f values-prod.yaml

# Dry-run install
helm install receipt-app ./helm/receipt-app \
  -n production \
  -f values-prod.yaml \
  --dry-run --debug

# List releases
helm list -n production

# Get release values
helm get values receipt-app -n production

# Get release manifest
helm get manifest receipt-app -n production
```

## Monitoring Deployment

```bash
# Watch deployment progress
kubectl rollout status deployment/receipt-app -n production -w

# View deployment events
kubectl describe deployment receipt-app -n production

# View pod logs
kubectl logs -f deployment/receipt-app -n production --tail=100

# View logs from specific pod
kubectl logs -f pod/receipt-app-xxxxx -n production

# Get into pod (debugging)
kubectl exec -it pod/receipt-app-xxxxx -n production -- /bin/sh
```

## Database & RabbitMQ Access

### MySQL

```bash
# Port-forward to local machine
kubectl port-forward svc/mysql 3306:3306 -n production &

# Connect from local
mysql -h localhost -u appuser -p"apppassword" appdb
```

### RabbitMQ

```bash
# Port-forward management UI
kubectl port-forward svc/rabbitmq 15672:15672 -n production &

# Access at http://localhost:15672
# Username: guest, Password: guest
```

### Application

```bash
# Port-forward application
kubectl port-forward svc/receipt-app 8080:8080 -n production &

# Access at http://localhost:8080
```

## Health Checks

```bash
# Check health endpoint
kubectl exec -it pod/receipt-app-xxxxx -n production -- \
  curl -s http://localhost:8080/actuator/health

# Check readiness
kubectl exec -it pod/receipt-app-xxxxx -n production -- \
  curl -s http://localhost:8080/actuator/health/readiness

# Check liveness
kubectl exec -it pod/receipt-app-xxxxx -n production -- \
  curl -s http://localhost:8080/actuator/health/liveness
```

## Scaling

```bash
# Manual scale
kubectl scale deployment receipt-app --replicas=5 -n production

# Check HPA status
kubectl get hpa receipt-app -n production -w

# Describe HPA
kubectl describe hpa receipt-app -n production
```

## Troubleshooting

### Pod not starting

```bash
# Check pod status
kubectl get pod -n production -l app=receipt-app

# View events
kubectl describe pod <pod-name> -n production

# Check logs
kubectl logs <pod-name> -n production

# Check previous logs (if crashed)
kubectl logs <pod-name> -n production --previous
```

### Database connection issues

```bash
# Check if MySQL pod is running
kubectl get pod -n production -l app=mysql

# Check MySQL logs
kubectl logs -n production -l app=mysql

# Test MySQL connection
kubectl run -it --rm mysql-client --image=mysql:8.0 --restart=Never -- \
  mysql -h mysql -u appuser -p"apppassword" appdb
```

### Resource issues

```bash
# Check resource usage
kubectl top node
kubectl top pod -n production

# Check resource requests/limits
kubectl describe pod <pod-name> -n production | grep -A 5 "Limits\|Requests"
```

## Configuration Management

### Update configuration

```bash
# Edit ConfigMap
kubectl edit configmap receipt-app-config -n production

# Or update via Helm
helm upgrade receipt-app ./helm/receipt-app \
  -n production \
  --set logging.level.root=DEBUG

# Restart pods to apply changes
kubectl rollout restart deployment/receipt-app -n production
```

### Update secrets

```bash
# Create/update secret
kubectl create secret generic receipt-app-secret \
  --from-literal=db-password=newpassword \
  --from-literal=rabbitmq-password=newpassword \
  -n production --dry-run=client -o yaml | kubectl apply -f -

# Restart pods
kubectl rollout restart deployment/receipt-app -n production
```

## Backup & Recovery

### Backup database

```bash
# Port-forward MySQL
kubectl port-forward svc/mysql 3306:3306 -n production &

# Backup
mysqldump -h localhost -u appuser -p"apppassword" appdb > backup.sql

# Or use Helm
helm get values receipt-app -n production > values-backup.yaml
```

### Restore database

```bash
# Port-forward MySQL
kubectl port-forward svc/mysql 3306:3306 -n production &

# Restore
mysql -h localhost -u appuser -p"apppassword" appdb < backup.sql
```

## Performance Tuning

### Adjust resource limits in values-prod.yaml

```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "1000m"
  limits:
    memory: "2Gi"
    cpu: "2000m"
```

### Enable autoscaling

```yaml
autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 60
```

### Configure JVM options

```bash
helm upgrade receipt-app ./helm/receipt-app \
  -n production \
  --set env.JAVA_OPTS="-Xms1g -Xmx2g"
```

## Security Best Practices

1. **Use NetworkPolicy**
   ```bash
   # Enable in values-prod.yaml
   networkPolicy:
     enabled: true
   ```

2. **Use RBAC**
   ```bash
   # Create limited RBAC resources
   kubectl apply -f rbac.yaml
   ```

3. **Use Secrets for sensitive data**
   ```bash
   # Don't commit passwords to Git
   # Use external secrets or sealed secrets
   ```

4. **Enable Pod Security Policy**
   ```bash
   # Configure in cluster settings
   ```

## Monitoring & Observability

### Prometheus metrics

```bash
# Port-forward Prometheus (if installed)
kubectl port-forward svc/prometheus 9090:9090 -n monitoring &

# Access metrics endpoint
curl http://localhost:8080/actuator/prometheus
```

### View metrics

```bash
# Get HPA metrics
kubectl get hpa receipt-app -n production
kubectl top pod -n production

# Check if metrics-server is installed
kubectl get deployment metrics-server -n kube-system
```

## References

- Helm Documentation: https://helm.sh/docs/
- Kubernetes Documentation: https://kubernetes.io/docs/
- Spring Boot on Kubernetes: https://spring.io/guides/kubernetes/
