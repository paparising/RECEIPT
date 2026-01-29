# Helm Chart - Advanced Configuration Guide

## Overview

This guide provides advanced configuration options for the Receipt App Helm Chart deployment on Kubernetes.

## Table of Contents

1. [Chart Structure](#chart-structure)
2. [Installation](#installation)
3. [Configuration](#configuration)
4. [Database Management](#database-management)
5. [RabbitMQ Setup](#rabbitmq-setup)
6. [Monitoring](#monitoring)
7. [Backup & Recovery](#backup--recovery)
8. [Troubleshooting](#troubleshooting)

## Chart Structure

```
helm/receipt-app/
├── Chart.yaml                 # Chart metadata
├── values.yaml               # Default values
├── values-dev.yaml          # Development values
├── values-staging.yaml       # Staging values
├── values-prod.yaml         # Production values
├── charts/                   # Dependent charts (mysql, rabbitmq)
│   ├── mysql/
│   └── rabbitmq/
└── templates/
    ├── deployment.yaml       # Pod deployment
    ├── service.yaml         # Service definition
    ├── ingress.yaml         # Ingress rules
    ├── configmap.yaml       # Application configuration
    ├── secret.yaml          # Secrets (passwords, tokens)
    ├── serviceaccount.yaml   # Service account
    ├── hpa.yaml            # Horizontal Pod Autoscaler
    ├── pvc.yaml            # Persistent Volume Claim
    ├── pdb.yaml            # Pod Disruption Budget
    ├── networkpolicy.yaml    # Network policies
    ├── _helpers.tpl         # Template helpers
    └── NOTES.txt           # Post-installation notes
```

## Installation

### 1. Update Helm Chart Dependencies

```bash
cd helm/receipt-app
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
helm dependency update
```

### 2. Validate Chart

```bash
helm lint .
helm template receipt-app . --values values-dev.yaml
```

### 3. Install Chart

```bash
# Development
helm install receipt-app . \
  --namespace dev \
  --values values-dev.yaml \
  --create-namespace

# Production
helm install receipt-app . \
  --namespace production \
  --values values-prod.yaml \
  --create-namespace
```

## Configuration

### Global Settings

```yaml
global:
  environment: production  # dev, staging, production
  imagePullPolicy: IfNotPresent
```

### Image Configuration

```yaml
image:
  repository: myregistry/receipt-app  # Change to your registry
  tag: "1.0.0"                        # Version tag
  pullPolicy: IfNotPresent            # Pull policy
```

### Replica Count & Scaling

```yaml
replicaCount: 3  # Initial replicas

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 60
  targetMemoryUtilizationPercentage: 75
```

### Resource Management

```yaml
resources:
  requests:
    cpu: 500m
    memory: 512Mi
  limits:
    cpu: 1000m
    memory: 1Gi
```

### Service Configuration

```yaml
service:
  type: ClusterIP              # ClusterIP, NodePort, LoadBalancer
  port: 8080
  targetPort: 8080
```

### Ingress Configuration

```yaml
ingress:
  enabled: true
  className: "nginx"
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
  hosts:
    - host: receipt-app.example.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: receipt-app-tls
      hosts:
        - receipt-app.example.com
```

## Database Management

### MySQL Configuration

```yaml
mysql:
  enabled: true
  architecture: standalone
  auth:
    rootPassword: "root123"
    database: appdb
    username: appuser
    password: "apppassword"
  primary:
    persistence:
      enabled: true
      size: 50Gi
      storageClass: "fast-ssd"
```

### Database Initialization

```bash
# Connect to MySQL pod
kubectl exec -it pod/mysql-xxxxx -n production -- mysql -u root -p

# Create database if not exists
CREATE DATABASE IF NOT EXISTS appdb;

# Show databases
SHOW DATABASES;
```

### Database Backup

```bash
# Create backup
kubectl exec pod/mysql-xxxxx -n production -- \
  mysqldump -u appuser -p"apppassword" appdb > backup.sql

# Restore backup
kubectl exec -i pod/mysql-xxxxx -n production -- \
  mysql -u appuser -p"apppassword" appdb < backup.sql
```

## RabbitMQ Setup

### RabbitMQ Configuration

```yaml
rabbitmq:
  enabled: true
  auth:
    username: guest
    password: "guest"
  replicaCount: 3
  persistence:
    enabled: true
    size: 20Gi
    storageClass: "fast-ssd"
```

### Access RabbitMQ Management

```bash
# Port-forward RabbitMQ management UI
kubectl port-forward svc/rabbitmq 15672:15672 -n production &

# Access at http://localhost:15672
# Username: guest
# Password: guest
```

### Create Exchanges & Queues

```bash
# Connect to RabbitMQ pod
kubectl exec -it pod/rabbitmq-xxxxx -n production -- /bin/bash

# Use rabbitmqctl
rabbitmqctl list_exchanges
rabbitmqctl list_queues
rabbitmqctl list_bindings

# Create exchange
rabbitmqctl declare_exchange name=report-dlx type=direct
```

## Monitoring

### Enable Prometheus Metrics

```yaml
# In values.yaml
podAnnotations:
  prometheus.io/scrape: "true"
  prometheus.io/port: "8080"
  prometheus.io/path: "/actuator/prometheus"
```

### Access Application Metrics

```bash
# Port-forward application
kubectl port-forward svc/receipt-app 8080:8080 -n production &

# Access metrics
curl http://localhost:8080/actuator/metrics

# Access Prometheus format
curl http://localhost:8080/actuator/prometheus
```

### View Logs

```bash
# Real-time logs
kubectl logs -f deployment/receipt-app -n production

# Logs from specific pod
kubectl logs pod/receipt-app-xxxxx -n production

# Logs with timestamps
kubectl logs deployment/receipt-app -n production --timestamps=true

# Logs from previous container (if crashed)
kubectl logs deployment/receipt-app -n production --previous
```

## Backup & Recovery

### Volume Snapshots

```bash
# Create snapshot
kubectl exec pod/receipt-app-xxxxx -n production -- \
  tar -czf /backup/app-backup-$(date +%Y%m%d).tar.gz /app/logs

# List backups
kubectl exec pod/receipt-app-xxxxx -n production -- ls -la /backup/
```

### ConfigMap Backup

```bash
# Export all ConfigMaps
kubectl get configmap -n production -o yaml > configmap-backup.yaml

# Export specific ConfigMap
kubectl get configmap receipt-app-config -n production -o yaml > receipt-app-config.yaml
```

### Secret Backup

```bash
# Export all Secrets (encrypted)
kubectl get secret -n production -o yaml > secrets-backup.yaml

# Extract secret value (base64)
kubectl get secret receipt-app-secret -n production -o jsonpath='{.data.db-password}' | base64 -d
```

## Troubleshooting

### Common Issues

#### 1. Pod won't start

```bash
# Check pod status
kubectl describe pod receipt-app-xxxxx -n production

# Check events
kubectl get events -n production --sort-by='.lastTimestamp'

# Check logs
kubectl logs receipt-app-xxxxx -n production

# Debug container
kubectl debug pod/receipt-app-xxxxx -n production -it --image=busybox
```

#### 2. Database connection failure

```bash
# Check if MySQL is running
kubectl get pod -l app.kubernetes.io/name=mysql -n production

# Test MySQL connection
kubectl run mysql-test --image=mysql:8.0 -it --rm --restart=Never -- \
  mysql -h mysql -u appuser -p"apppassword" -e "SELECT 1"

# Check network connectivity
kubectl exec pod/receipt-app-xxxxx -n production -- \
  nc -zv mysql 3306
```

#### 3. Resource issues

```bash
# Check node resources
kubectl top nodes

# Check pod resource usage
kubectl top pod -n production

# Increase resource limits
helm upgrade receipt-app . \
  --set resources.limits.memory=2Gi \
  --set resources.limits.cpu=2000m
```

#### 4. Ingress not working

```bash
# Check ingress status
kubectl get ingress -n production

# Describe ingress
kubectl describe ingress receipt-app -n production

# Check ingress controller
kubectl get pods -n ingress-nginx

# Test DNS resolution
kubectl run dns-test --image=busybox -it --rm --restart=Never -- \
  nslookup receipt-app.example.com
```

### Debug Commands

```bash
# Shell into pod
kubectl exec -it pod/receipt-app-xxxxx -n production -- /bin/sh

# View environment variables
kubectl exec pod/receipt-app-xxxxx -n production -- env

# Check running processes
kubectl exec pod/receipt-app-xxxxx -n production -- ps aux

# Network debugging
kubectl exec pod/receipt-app-xxxxx -n production -- \
  curl -v http://mysql:3306

# Disk usage
kubectl exec pod/receipt-app-xxxxx -n production -- df -h
```

## Performance Tuning

### JVM Tuning

```bash
helm upgrade receipt-app . \
  --set env.JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"
```

### Database Connection Pool

```yaml
database:
  hikariMaximumPoolSize: 20
  poolSize: 10
```

### Request Timeouts

```bash
kubectl edit configmap receipt-app-config -n production

# Add to properties
server.tomcat.connection-timeout=20000
server.servlet.session.timeout=30m
```

### CPU Pinning

```yaml
affinity:
  nodeAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      nodeSelectorTerms:
      - matchExpressions:
        - key: kubernetes.io/os
          operator: In
          values:
          - linux
```

## Security

### Network Policies

```yaml
networkPolicy:
  enabled: true
```

### Pod Security Context

```yaml
podSecurityContext:
  runAsNonRoot: true
  runAsUser: 1000
  fsGroup: 1000
```

### RBAC

```bash
# Create role
kubectl create role receipt-app-role \
  --verb=get,list,watch \
  --resource=configmaps,secrets \
  -n production

# Create role binding
kubectl create rolebinding receipt-app-binding \
  --role=receipt-app-role \
  --serviceaccount=production:receipt-app-sa \
  -n production
```

## Useful Commands Reference

```bash
# Helm
helm list -n production
helm history receipt-app -n production
helm rollback receipt-app 1 -n production
helm get values receipt-app -n production
helm template receipt-app . | less

# Kubectl
kubectl get all -n production
kubectl describe deployment receipt-app -n production
kubectl scale deployment receipt-app --replicas=5 -n production
kubectl set image deployment/receipt-app \
  receipt-app=myregistry/receipt-app:2.0.0 -n production
kubectl rollout status deployment/receipt-app -n production
kubectl rollout undo deployment/receipt-app -n production
```
