# Helm Deployment Verification Checklist

## ✅ Pre-Deployment Checklist

### Prerequisites
- [ ] Kubernetes cluster 1.20+ installed and running
- [ ] kubectl configured with cluster access (`kubectl cluster-info`)
- [ ] Helm 3.x installed (`helm version`)
- [ ] Docker installed for building images
- [ ] Container registry access (Docker Hub, ECR, ACR, GCR, etc.)
- [ ] Sufficient cluster resources (3+ nodes for production)
- [ ] Persistent Volume provisioner available (AWS EBS, Azure Disks, etc.)

### Helm Repository Setup
- [ ] Added Bitnami Helm repo: `helm repo add bitnami https://charts.bitnami.com/bitnami`
- [ ] Repository updated: `helm repo update`
- [ ] MySQL chart available: `helm search repo bitnami/mysql`
- [ ] RabbitMQ chart available: `helm search repo bitnami/rabbitmq`

### Application Preparation
- [ ] Docker image built: `docker build -t myregistry/receipt-app:1.0.0 .`
- [ ] Image pushed to registry: `docker push myregistry/receipt-app:1.0.0`
- [ ] Image pull credentials configured (if private registry)
- [ ] values.yaml updated with correct image.repository
- [ ] Ingress hostname configured (receipt-app.example.com)
- [ ] Database credentials set in secret.yaml
- [ ] RabbitMQ password updated in secret.yaml
- [ ] StorageClass verified in cluster: `kubectl get storageclass`

### Kubernetes Cluster Preparation
- [ ] Namespaces created (dev, staging, prod)
- [ ] StorageClasses defined (standard, fast-ssd)
- [ ] Nginx Ingress Controller installed: `kubectl get svc -n ingress-nginx`
- [ ] cert-manager installed: `kubectl get deployment -n cert-manager`
- [ ] RBAC enabled on cluster
- [ ] NetworkPolicy support enabled (or disabled if not needed)
- [ ] Resource quotas configured (optional)
- [ ] Network policies for the namespace exist (if required)

---

## 📋 Deployment Checklist

### Chart Validation
- [ ] Helm chart syntax valid: `helm lint helm/receipt-app/`
- [ ] Chart dependencies present: `helm dependency list helm/receipt-app/`
- [ ] Dependencies updated: `helm dependency update helm/receipt-app/`
- [ ] Chart values valid: `helm template receipt-app helm/receipt-app/`
- [ ] No template errors in output
- [ ] All required values provided (no empty substitutions)

### Pre-Installation
- [ ] Target namespace exists: `kubectl get namespace production`
- [ ] Or namespace will be created: `--create-namespace` flag used
- [ ] ServiceAccount will be created: serviceAccount.create=true
- [ ] Sufficient cluster resources available
- [ ] Node anti-affinity compatible with node count
- [ ] Storage provisioner ready for PVC

### Installation
- [ ] Install command prepared:
  ```bash
  helm install receipt-app helm/receipt-app/ \
    --namespace production \
    --create-namespace \
    -f helm/receipt-app/values-prod.yaml
  ```
- [ ] Dry-run successful: `helm install ... --dry-run --debug`
- [ ] Installation executed
- [ ] Release created: `helm list -n production`
- [ ] No installation errors reported

### Post-Installation Verification
- [ ] All resources created: `kubectl get all -n production`
- [ ] ConfigMap created: `kubectl get configmap -n production`
- [ ] Secret created: `kubectl get secret -n production`
- [ ] ServiceAccount created: `kubectl get sa -n production`
- [ ] PVC created: `kubectl get pvc -n production`

---

## 🚀 Pod Startup Verification

### Pod Health Checks
- [ ] MySQL pod running: `kubectl get pod -n production -l app=mysql`
- [ ] RabbitMQ pod running: `kubectl get pod -n production -l app=rabbitmq`
- [ ] Receipt-app pods running: `kubectl get pod -n production -l app=receipt-app`
- [ ] All pods show READY status (2/2 or higher)
- [ ] All pods show RUNNING status
- [ ] No pods in CrashLoopBackOff state
- [ ] No pods pending: `kubectl get pod -n production`

### Deployment Status
- [ ] Deployment ready: `kubectl rollout status deployment/receipt-app -n production`
- [ ] All desired replicas running
- [ ] No failed pods
- [ ] Pod restart count low (< 2)
- [ ] Age of pods recent (not restarting repeatedly)

### Logs Verification
- [ ] Application logs accessible: `kubectl logs deployment/receipt-app -n production`
- [ ] No Java exceptions in logs
- [ ] Spring Boot initialization complete
- [ ] Database connection successful
- [ ] RabbitMQ connection successful
- [ ] Application started successfully
- [ ] Port 8080 listening (if visible in logs)

### Init Container Verification
- [ ] wait-for-mysql completed: Check pod logs for completion
- [ ] wait-for-rabbitmq completed: Check pod logs for completion
- [ ] No connection timeout errors
- [ ] Database accessible from pod
- [ ] RabbitMQ accessible from pod

---

## 🔌 Connectivity Verification

### Service Connectivity
- [ ] Service created: `kubectl get svc receipt-app -n production`
- [ ] Service IP assigned (ClusterIP)
- [ ] Service port 8080 configured
- [ ] Service endpoints populated: `kubectl get endpoints receipt-app -n production`
- [ ] All pod IPs listed as endpoints

### Database Connectivity
- [ ] MySQL service accessible: `kubectl get svc mysql -n production`
- [ ] MySQL pod logs show startup: `kubectl logs pod/mysql-xxxxx -n production`
- [ ] Port 3306 listening
- [ ] Database credentials correct
- [ ] Application can connect (check logs)
- [ ] Tables created (if DDL auto-update enabled)
- [ ] Port-forward test successful:
  ```bash
  kubectl port-forward svc/mysql 3306:3306 -n production &
  mysql -h localhost -u appuser -p"apppassword" appdb
  ```

### RabbitMQ Connectivity
- [ ] RabbitMQ service accessible: `kubectl get svc rabbitmq -n production`
- [ ] RabbitMQ pod logs show startup: `kubectl logs pod/rabbitmq-xxxxx -n production`
- [ ] AMQP port 5672 listening
- [ ] Management port 15672 listening
- [ ] Application can connect (check logs)
- [ ] Queues created (if auto-creation configured)
- [ ] Port-forward test successful:
  ```bash
  kubectl port-forward svc/rabbitmq 15672:15672 -n production &
  # Access http://localhost:15672 (guest:guest)
  ```

---

## 🌐 Application Accessibility

### HTTP/HTTPS Access
- [ ] Application accessible via port-forward:
  ```bash
  kubectl port-forward svc/receipt-app 8080:8080 -n production &
  curl http://localhost:8080
  ```
- [ ] HTTP response received (not timeout)
- [ ] Response status code 200-299 (success) or 401 (auth required)
- [ ] Application responds to requests
- [ ] No 502/503 bad gateway errors

### Ingress Configuration
- [ ] Ingress created: `kubectl get ingress -n production`
- [ ] Ingress shows IP or hostname
- [ ] Ingress backend service set to receipt-app
- [ ] Ingress path configured correctly (/)
- [ ] TLS certificate section configured
- [ ] cert-manager annotation present (if using)

### TLS/Certificate Verification
- [ ] Certificate requested: `kubectl get certificate -n production`
- [ ] Certificate status Ready: `kubectl describe certificate -n production`
- [ ] Certificate secret created: `kubectl get secret -n production | grep tls`
- [ ] Certificate valid (not expired)
- [ ] Certificate auto-renewal configured
- [ ] curl HTTPS test (if DNS accessible):
  ```bash
  curl -v https://receipt-app.example.com
  ```
- [ ] Certificate matches hostname
- [ ] Certificate trusted (no SSL warnings)

### DNS Resolution
- [ ] Domain DNS record created pointing to Ingress IP
- [ ] DNS resolves to correct IP: `nslookup receipt-app.example.com`
- [ ] DNS propagated (may take 24-48 hours for global)
- [ ] Ingress shows IP address: `kubectl get ingress -n production`

### External Access Test
- [ ] Browser can access https://receipt-app.example.com
- [ ] Application page loads
- [ ] SSL certificate valid (no warnings)
- [ ] Static resources load (CSS, JS, images)
- [ ] Forms functional (if present)
- [ ] API endpoints responding

---

## 💾 Storage & Persistence Verification

### PVC Verification
- [ ] PVC created: `kubectl get pvc -n production`
- [ ] PVC status Bound: `kubectl describe pvc -n production`
- [ ] PV created and attached: `kubectl get pv`
- [ ] Storage size correct (10Gi, 50Gi, etc.)
- [ ] Storage class correct (standard, fast-ssd)
- [ ] Volume accessible from pod

### Data Persistence Test
- [ ] Delete pod: `kubectl delete pod receipt-app-xxxxx -n production`
- [ ] New pod created automatically
- [ ] Data persists after pod restart
- [ ] Database data intact
- [ ] Application logs retained (if PVC mounted)

### Backup Readiness
- [ ] Volume snapshots enabled (if supported)
- [ ] Snapshot schedule configured (if available)
- [ ] First snapshot created
- [ ] Database backup procedure documented
- [ ] Restore procedure tested

---

## 📊 Performance & Scaling Verification

### Resource Usage
- [ ] CPU usage reasonable: `kubectl top pod -n production`
- [ ] Memory usage under limits
- [ ] No OOMKilled events
- [ ] Disk usage healthy (< 80%)
- [ ] Network I/O normal
- [ ] Database queries performant

### HPA Verification
- [ ] HPA configured: `kubectl get hpa -n production`
- [ ] HPA targets correct deployment
- [ ] Min/max replicas set correctly
- [ ] CPU target set (e.g., 70%)
- [ ] Memory target set (e.g., 80%)
- [ ] Metrics server available: `kubectl get deployment metrics-server -n kube-system`

### Scaling Test (Optional)
- [ ] Generate load: `kubectl run -i --tty load-generator --rm --image=busybox --restart=Never -- /bin/sh`
  ```bash
  while sleep 0.01; do wget -q -O- http://receipt-app.default; done
  ```
- [ ] Monitor pod count increasing: `watch kubectl get pod -n production`
- [ ] HPA scales up to expected count
- [ ] New pods reach READY state
- [ ] Stop load generator
- [ ] HPA scales down (after 5-10 minutes)
- [ ] Scaling behavior smooth (no thrashing)

---

## 🔒 Security Verification

### RBAC Verification
- [ ] ServiceAccount exists: `kubectl get sa -n production`
- [ ] Role/RoleBinding created: `kubectl get role,rolebinding -n production`
- [ ] Service account mounted to pods
- [ ] Pod can access ConfigMaps/Secrets via RBAC
- [ ] No excessive permissions granted

### Pod Security
- [ ] Pod runs as non-root: `kubectl exec pod/receipt-app-xxxxx -n production -- id`
- [ ] UID 1000 or higher (not root)
- [ ] Root filesystem read-only: `kubectl exec pod/receipt-app-xxxxx -- touch /test`
- [ ] Touch command fails (read-only)
- [ ] /tmp writable for temporary files

### Network Policy Verification
- [ ] NetworkPolicy created: `kubectl get networkpolicy -n production`
- [ ] Policy rules configured
- [ ] Ingress rules allow service traffic
- [ ] Egress rules allow database/RabbitMQ
- [ ] Default deny-all ingress enforced (if configured)
- [ ] DNS queries allowed (port 53)

### Secret Management
- [ ] Secrets created: `kubectl get secret -n production`
- [ ] Passwords not in ConfigMaps
- [ ] Secrets mounted as environment variables
- [ ] Secret values not exposed in logs
- [ ] RBAC controls secret access
- [ ] Secrets encrypted at rest (etcd encryption enabled)

### Image Security
- [ ] Image from trusted registry
- [ ] Image scanned for vulnerabilities (if available)
- [ ] Image pull policy appropriate (IfNotPresent for prod)
- [ ] No latest tag (specific version tag used)
- [ ] Image signed (if registry supports)

---

## 📈 Monitoring & Observability

### Health Check Verification
- [ ] Liveness probe configured: `/actuator/health`
- [ ] Readiness probe configured: `/actuator/health/readiness`
- [ ] Probes responding with 200 status
- [ ] No liveness failures: `kubectl get events -n production`
- [ ] Pod restart count low

### Metrics & Monitoring
- [ ] Prometheus metrics endpoint available: `/actuator/prometheus`
- [ ] Prometheus scraping configured (if available)
- [ ] Custom application metrics exposed
- [ ] JVM metrics available (memory, GC, threads)
- [ ] HTTP request metrics available

### Logging
- [ ] Application logs accessible via `kubectl logs`
- [ ] Log format correct (JSON structured)
- [ ] Log levels appropriate (INFO for prod, DEBUG for dev)
- [ ] No excessive logging overhead
- [ ] Logs can be aggregated (if using ELK/Splunk/Datadog)

---

## 🔄 High Availability Verification

### Multi-Pod Setup
- [ ] Multiple replicas running (2 minimum for staging, 3 for prod)
- [ ] Pods distributed across nodes: `kubectl get pod -o wide -n production`
- [ ] Pod anti-affinity respected (on different nodes)
- [ ] No pods on same node (if nodes available)

### Pod Disruption Budget
- [ ] PDB created: `kubectl get pdb -n production`
- [ ] Min available set correctly (1-2)
- [ ] kubectl drain test safe: `kubectl drain <node> --ignore-daemonsets --dry-run`
- [ ] At least min available pods remain

### Failover Test
- [ ] Simulate node failure: `kubectl drain <node> --ignore-daemonsets --dry-run`
- [ ] Or terminate node in cloud provider
- [ ] Pods automatically rescheduled to healthy nodes
- [ ] No request loss (if load balanced)
- [ ] Service endpoints updated automatically
- [ ] Application accessible after failover

### Update Rolling Test
- [ ] Perform rolling update: `kubectl set image deployment/receipt-app app=receipt-app:2.0.0 -n production`
- [ ] Or: `helm upgrade receipt-app helm/receipt-app/ ... --set image.tag=2.0.0`
- [ ] Old pods gradually terminated
- [ ] New pods gradually started
- [ ] Service always has minimum available pods
- [ ] No request loss during update
- [ ] Readiness probe ensures traffic only to ready pods

---

## 🆘 Troubleshooting Verification

### Log Access
- [ ] Can view pod logs: `kubectl logs pod/receipt-app-xxxxx -n production`
- [ ] Can view previous logs: `kubectl logs pod/receipt-app-xxxxx -n production --previous`
- [ ] Can follow logs in real-time: `kubectl logs -f deployment/receipt-app -n production`
- [ ] Multi-pod logs aggregated: `kubectl logs -l app=receipt-app -n production`

### Pod Inspection
- [ ] Can describe pod: `kubectl describe pod receipt-app-xxxxx -n production`
- [ ] Pod events visible (shows startup issues)
- [ ] Can exec into pod: `kubectl exec pod/receipt-app-xxxxx -n production -- /bin/bash`
- [ ] Can view environment: `kubectl exec pod/receipt-app-xxxxx -- env | grep SPRING`

### Metric Inspection
- [ ] Can view resource usage: `kubectl top pod -n production`
- [ ] Can view node resources: `kubectl top node`
- [ ] Can view HPA status: `kubectl get hpa -n production`
- [ ] Can view HPA metrics: `kubectl describe hpa receipt-app -n production`

### Event Monitoring
- [ ] Can list cluster events: `kubectl get events -n production`
- [ ] Can sort by time: `kubectl get events -n production --sort-by='.lastTimestamp'`
- [ ] Warnings/errors visible
- [ ] Can identify pod restart causes
- [ ] Can identify scaling events

---

## ✅ Final Acceptance Criteria

### Functionality
- [ ] Application responds to HTTP requests
- [ ] Database queries execute successfully
- [ ] RabbitMQ messages send/receive
- [ ] Authentication/authorization working
- [ ] Core business logic functioning
- [ ] Error handling graceful (500 errors for failures)
- [ ] No data corruption on restart
- [ ] Backup/restore procedures validated

### Reliability
- [ ] Uptime metrics captured (>99.9% for 24 hours)
- [ ] No unexpected pod restarts
- [ ] No OOMKilled or evicted pods
- [ ] Zero data loss on pod restart
- [ ] Graceful handling of RabbitMQ/MySQL downtime
- [ ] Automatic pod recovery working

### Performance
- [ ] Response time acceptable (< 200ms for typical requests)
- [ ] Database query time reasonable (< 100ms typical)
- [ ] CPU usage stable (not spiking)
- [ ] Memory usage within limits
- [ ] Disk I/O efficient
- [ ] Network bandwidth efficient
- [ ] Scaling response time acceptable (< 2 minutes)

### Security
- [ ] No sensitive data in logs
- [ ] No default credentials exposed
- [ ] RBAC policies enforced
- [ ] Network policies blocking unauthorized traffic
- [ ] TLS encryption working
- [ ] Pod security context enforced
- [ ] No privilege escalation possible
- [ ] Image scanning passed (if available)

### Maintainability
- [ ] Deployment repeatable via Helm
- [ ] Chart values well-documented
- [ ] Runbooks available for operators
- [ ] Troubleshooting guide comprehensive
- [ ] Backup procedures clear
- [ ] Recovery procedures tested
- [ ] Monitoring alerts configured
- [ ] Logging aggregation ready

---

## 📝 Sign-Off Checklist

- [ ] All verification checks completed
- [ ] All failures resolved or documented
- [ ] Performance acceptable
- [ ] Security requirements met
- [ ] Monitoring configured
- [ ] Runbooks reviewed by ops team
- [ ] Team trained on deployment
- [ ] Rollback procedure tested
- [ ] Change log updated
- [ ] Stakeholders notified
- [ ] **DEPLOYMENT APPROVED FOR PRODUCTION** ✅

---

## 📞 Contact & Support

If issues encountered:

1. **Check logs**: `kubectl logs -f deployment/receipt-app -n production`
2. **Describe pod**: `kubectl describe pod <pod-name> -n production`
3. **Review events**: `kubectl get events -n production`
4. **Check documentation**: See KUBERNETES_DEPLOYMENT_GUIDE.md
5. **Contact team**: Escalate to platform engineering team

---

**Generated**: $(date)
**Deployment Target**: Kubernetes 1.20+ with Helm 3.x
**Application**: Receipt Management System
**Chart Version**: 1.0.0

