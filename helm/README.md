# Helm Chart README

## Overview

This Helm chart deploys the Receipt Management Application to Kubernetes with all required dependencies (MySQL, RabbitMQ).

**Chart Name:** receipt-app  
**Chart Version:** 1.0.0  
**App Version:** 0.0.1

## Features

✅ Multi-environment support (dev, staging, production)  
✅ Integrated MySQL and RabbitMQ deployments  
✅ Automatic scaling with HPA  
✅ Health checks (liveness & readiness probes)  
✅ Persistent storage for data  
✅ Ingress support with TLS  
✅ Network policies for security  
✅ Resource limits and requests  
✅ Pod disruption budgets  
✅ RBAC configuration  

## Prerequisites

- Kubernetes 1.20+
- Helm 3.x
- 3+ nodes for production
- Storage provisioner (default: standard)

## Quick Install

```bash
# Add Bitnami Helm repo
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Install chart
cd helm/receipt-app
helm dependency update
helm install receipt-app . \
  --namespace production \
  --create-namespace \
  --values values-prod.yaml
```

## File Structure

```
helm/receipt-app/
├── Chart.yaml                    # Chart metadata
├── values.yaml                   # Default values
├── values-dev.yaml              # Development overrides
├── values-staging.yaml          # Staging overrides
├── values-prod.yaml             # Production overrides
├── charts/                       # Dependencies
│   ├── mysql/
│   └── rabbitmq/
└── templates/
    ├── deployment.yaml          # Main deployment
    ├── service.yaml            # Kubernetes service
    ├── ingress.yaml            # Ingress rules
    ├── configmap.yaml          # Application config
    ├── secret.yaml             # Secrets
    ├── serviceaccount.yaml     # Service account
    ├── hpa.yaml               # Auto scaling
    ├── pvc.yaml               # Persistent volumes
    ├── pdb.yaml               # Pod disruption budgets
    ├── networkpolicy.yaml     # Network policies
    ├── _helpers.tpl           # Template helpers
    └── NOTES.txt             # Post-install notes
```

## Configuration

### Main Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `replicaCount` | 2 | Number of pod replicas |
| `image.repository` | `receipt-app` | Image repository |
| `image.tag` | `1.0.0` | Image tag |
| `image.pullPolicy` | `IfNotPresent` | Image pull policy |
| `service.type` | `ClusterIP` | Service type |
| `service.port` | `8080` | Service port |
| `ingress.enabled` | `true` | Enable ingress |
| `autoscaling.enabled` | `true` | Enable HPA |
| `persistence.enabled` | `true` | Enable persistent storage |

### Database Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `mysql.enabled` | `true` | Deploy MySQL |
| `database.host` | `mysql` | MySQL host |
| `database.port` | `3306` | MySQL port |
| `database.name` | `appdb` | Database name |
| `database.username` | `appuser` | Database user |

### RabbitMQ Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `rabbitmq.enabled` | `true` | Deploy RabbitMQ |
| `rabbitmq.host` | `rabbitmq` | RabbitMQ host |
| `rabbitmq.port` | `5672` | RabbitMQ port |

## Deployment Environments

### Development

```bash
helm install receipt-app . \
  --namespace dev \
  --create-namespace \
  --values values-dev.yaml
```

**Characteristics:**
- 1 replica
- Image pull: Always
- 512Mi memory limit
- HPA disabled
- Smaller storage

### Staging

```bash
helm install receipt-app . \
  --namespace staging \
  --create-namespace \
  --values values-staging.yaml
```

**Characteristics:**
- 2 replicas
- 768Mi memory limit
- HPA: min 2, max 3
- Network policies enabled

### Production

```bash
helm install receipt-app . \
  --namespace production \
  --create-namespace \
  --values values-prod.yaml
```

**Characteristics:**
- 3 replicas
- 2Gi memory limit
- HPA: min 3, max 10
- All security features enabled
- 50Gi storage
- Pod disruption budget

## Common Operations

### View Deployment Status

```bash
kubectl rollout status deployment/receipt-app -n production
kubectl get pods -n production -l app=receipt-app
kubectl describe pod <pod-name> -n production
```

### Upgrade Release

```bash
helm upgrade receipt-app . \
  --namespace production \
  --values values-prod.yaml \
  --set image.tag=2.0.0
```

### Rollback Release

```bash
helm rollback receipt-app -n production
helm history receipt-app -n production
```

### Access Application

```bash
# Via port-forward
kubectl port-forward svc/receipt-app 8080:8080 -n production &
curl http://localhost:8080

# Via ingress (if configured)
curl https://receipt-app.example.com
```

### View Logs

```bash
kubectl logs -f deployment/receipt-app -n production
kubectl logs -f pod/receipt-app-xxxxx -n production
```

## Database Management

### Connect to MySQL

```bash
kubectl port-forward svc/mysql 3306:3306 -n production &
mysql -h localhost -u appuser -p"apppassword" appdb
```

### Backup Database

```bash
kubectl exec pod/mysql-xxxxx -n production -- \
  mysqldump -u appuser -p"apppassword" appdb > backup.sql
```

### Restore Database

```bash
kubectl exec -i pod/mysql-xxxxx -n production -- \
  mysql -u appuser -p"apppassword" appdb < backup.sql
```

## RabbitMQ Management

### Access Management UI

```bash
kubectl port-forward svc/rabbitmq 15672:15672 -n production &
# Access: http://localhost:15672
# User: guest, Password: guest
```

### Check Queue Status

```bash
kubectl exec pod/rabbitmq-xxxxx -n production -- \
  rabbitmqctl list_queues
```

## Scaling

### Manual Scale

```bash
kubectl scale deployment receipt-app --replicas=5 -n production
```

### Check Autoscaling Status

```bash
kubectl get hpa -n production
kubectl describe hpa receipt-app -n production
```

## Monitoring

### Check Metrics

```bash
kubectl top nodes
kubectl top pod -n production
```

### View Prometheus Metrics

```bash
kubectl port-forward svc/receipt-app 8080:8080 -n production &
curl http://localhost:8080/actuator/prometheus
```

## Uninstall

```bash
helm uninstall receipt-app -n production
kubectl delete namespace production
```

## Troubleshooting

### Pod won't start
```bash
kubectl describe pod <pod-name> -n production
kubectl logs <pod-name> -n production
```

### Database connection issue
```bash
kubectl exec pod/receipt-app-xxxxx -n production -- \
  nc -zv mysql 3306
```

### Check resource usage
```bash
kubectl top pod -n production
kubectl describe pod <pod-name> -n production | grep -A 5 "Limits\|Requests"
```

## Documentation

- **Quick Start**: [QUICK_START.md](./QUICK_START.md)
- **Kubernetes Deployment Guide**: [KUBERNETES_DEPLOYMENT_GUIDE.md](./KUBERNETES_DEPLOYMENT_GUIDE.md)
- **Helm Configuration Guide**: [HELM_CHART_CONFIGURATION_GUIDE.md](./HELM_CHART_CONFIGURATION_GUIDE.md)
- **RBAC Configuration**: [rbac.yaml](./rbac.yaml)

## Support

For issues, refer to:
1. Helm documentation: https://helm.sh/docs/
2. Kubernetes documentation: https://kubernetes.io/docs/
3. Spring Boot on Kubernetes: https://spring.io/guides/kubernetes/

## License

Same as Receipt Application
