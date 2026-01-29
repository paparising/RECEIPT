# Terraform Outputs for Receipt Application Deployment

# Kubernetes Namespace Output
output "kubernetes_namespace" {
  description = "Kubernetes namespace where application is deployed"
  value       = kubernetes_namespace.receipt_app.metadata[0].name
}

# Helm Release Output
output "helm_release_name" {
  description = "Helm release name"
  value       = var.enable_helm_chart ? helm_release.receipt_app[0].name : null
}

output "helm_release_version" {
  description = "Helm release version"
  value       = var.enable_helm_chart ? helm_release.receipt_app[0].version : null
}

output "helm_release_status" {
  description = "Helm release status"
  value       = var.enable_helm_chart ? helm_release.receipt_app[0].status : null
}

# Service Output
output "service_name" {
  description = "Kubernetes service name"
  value       = "${var.helm_release_name}-receipt-app"
}

output "service_port" {
  description = "Service port"
  value       = var.app_port
}

output "service_endpoint" {
  description = "Service endpoint URL"
  value       = "http://${var.helm_release_name}-receipt-app.${kubernetes_namespace.receipt_app.metadata[0].name}.svc.cluster.local:${var.app_port}"
}

# Ingress Output
output "ingress_hostname" {
  description = "Ingress hostname"
  value       = var.enable_ingress ? var.ingress_hostname : null
}

output "ingress_url" {
  description = "Ingress URL for external access"
  value       = var.enable_ingress && var.ingress_tls_enabled ? "https://${var.ingress_hostname}" : var.enable_ingress ? "http://${var.ingress_hostname}" : null
}

# Database Output
output "mysql_service_name" {
  description = "MySQL service name"
  value       = var.enable_mysql ? "${var.helm_release_name}-mysql" : null
}

output "mysql_service_endpoint" {
  description = "MySQL service endpoint"
  value       = var.enable_mysql ? "${var.helm_release_name}-mysql.${kubernetes_namespace.receipt_app.metadata[0].name}.svc.cluster.local:3306" : null
}

output "mysql_database" {
  description = "MySQL database name"
  value       = var.mysql_database
}

output "mysql_user" {
  description = "MySQL application user"
  value       = var.mysql_user
}

# RabbitMQ Output
output "rabbitmq_service_name" {
  description = "RabbitMQ service name"
  value       = var.enable_rabbitmq ? "${var.helm_release_name}-rabbitmq" : null
}

output "rabbitmq_amqp_endpoint" {
  description = "RabbitMQ AMQP endpoint"
  value       = var.enable_rabbitmq ? "${var.helm_release_name}-rabbitmq.${kubernetes_namespace.receipt_app.metadata[0].name}.svc.cluster.local:5672" : null
}

output "rabbitmq_management_endpoint" {
  description = "RabbitMQ management UI endpoint"
  value       = var.enable_rabbitmq ? "${var.helm_release_name}-rabbitmq.${kubernetes_namespace.receipt_app.metadata[0].name}.svc.cluster.local:15672" : null
}

output "rabbitmq_user" {
  description = "RabbitMQ default user"
  value       = var.rabbitmq_user
}

# Storage Class Output
output "storage_class_standard" {
  description = "Standard storage class name"
  value       = var.create_storage_classes ? var.storage_class_standard : null
}

output "storage_class_fast_ssd" {
  description = "Fast SSD storage class name"
  value       = var.create_storage_classes ? var.storage_class_fast_ssd : null
}

# Application Access Information
output "application_access_info" {
  description = "Information on how to access the application"
  value = {
    namespace             = kubernetes_namespace.receipt_app.metadata[0].name
    service_name          = "${var.helm_release_name}-receipt-app"
    service_port          = var.app_port
    ingress_hostname      = var.enable_ingress ? var.ingress_hostname : "N/A"
    ingress_url           = var.enable_ingress && var.ingress_tls_enabled ? "https://${var.ingress_hostname}" : var.enable_ingress ? "http://${var.ingress_hostname}" : "N/A"
    port_forward_command  = "kubectl port-forward svc/${var.helm_release_name}-receipt-app ${var.app_port}:${var.app_port} -n ${kubernetes_namespace.receipt_app.metadata[0].name}"
    local_access_url      = "http://localhost:${var.app_port}"
    health_check_endpoint = "/actuator/health"
    metrics_endpoint      = "/actuator/prometheus"
  }
}

# Database Access Information
output "database_access_info" {
  description = "Information on how to access the database"
  value = var.enable_mysql ? {
    service_name         = "${var.helm_release_name}-mysql"
    host                 = "${var.helm_release_name}-mysql.${kubernetes_namespace.receipt_app.metadata[0].name}.svc.cluster.local"
    port                 = 3306
    database             = var.mysql_database
    user                 = var.mysql_user
    password_secret_name = "receipt-app-secrets"
    password_secret_key  = "mysql-password"
    connection_string    = "mysql://${var.mysql_user}@${var.helm_release_name}-mysql.${kubernetes_namespace.receipt_app.metadata[0].name}.svc.cluster.local:3306/${var.mysql_database}"
    port_forward_command = "kubectl port-forward svc/${var.helm_release_name}-mysql 3306:3306 -n ${kubernetes_namespace.receipt_app.metadata[0].name}"
  } : null
}

# RabbitMQ Access Information
output "rabbitmq_access_info" {
  description = "Information on how to access RabbitMQ"
  value = var.enable_rabbitmq ? {
    service_name          = "${var.helm_release_name}-rabbitmq"
    amqp_host             = "${var.helm_release_name}-rabbitmq.${kubernetes_namespace.receipt_app.metadata[0].name}.svc.cluster.local"
    amqp_port             = 5672
    management_host       = "${var.helm_release_name}-rabbitmq.${kubernetes_namespace.receipt_app.metadata[0].name}.svc.cluster.local"
    management_port       = 15672
    management_url        = "http://${var.helm_release_name}-rabbitmq.${kubernetes_namespace.receipt_app.metadata[0].name}.svc.cluster.local:15672"
    default_user          = var.rabbitmq_user
    password_secret_name  = "receipt-app-secrets"
    password_secret_key   = "rabbitmq-password"
    amqp_url              = "amqp://${var.rabbitmq_user}:password@${var.helm_release_name}-rabbitmq.${kubernetes_namespace.receipt_app.metadata[0].name}.svc.cluster.local:5672//"
    management_ui_command = "kubectl port-forward svc/${var.helm_release_name}-rabbitmq 15672:15672 -n ${kubernetes_namespace.receipt_app.metadata[0].name}"
  } : null
}

# Monitoring Information
output "monitoring_info" {
  description = "Information for monitoring the application"
  value = {
    namespace             = kubernetes_namespace.receipt_app.metadata[0].name
    prometheus_scrape_interval = var.prometheus_scrape_interval
    metrics_endpoint      = "/actuator/prometheus"
    health_endpoint       = "/actuator/health"
    metrics_port          = var.app_port
    hpa_name              = var.enable_autoscaling ? "${var.helm_release_name}-receipt-app" : "N/A"
    hpa_min_replicas      = var.enable_autoscaling ? var.hpa_min_replicas : null
    hpa_max_replicas      = var.enable_autoscaling ? var.hpa_max_replicas : null
    hpa_target_cpu        = var.enable_autoscaling ? var.hpa_target_cpu : null
    hpa_target_memory     = var.enable_autoscaling ? var.hpa_target_memory : null
  }
}

# Useful Commands
output "useful_commands" {
  description = "Useful kubectl commands for managing the deployment"
  value = {
    check_deployment_status     = "kubectl rollout status deployment/${var.helm_release_name}-receipt-app -n ${kubernetes_namespace.receipt_app.metadata[0].name}"
    view_pods                   = "kubectl get pods -n ${kubernetes_namespace.receipt_app.metadata[0].name}"
    view_services               = "kubectl get svc -n ${kubernetes_namespace.receipt_app.metadata[0].name}"
    view_logs                   = "kubectl logs -f deployment/${var.helm_release_name}-receipt-app -n ${kubernetes_namespace.receipt_app.metadata[0].name}"
    describe_deployment         = "kubectl describe deployment/${var.helm_release_name}-receipt-app -n ${kubernetes_namespace.receipt_app.metadata[0].name}"
    describe_pod                = "kubectl describe pod <pod-name> -n ${kubernetes_namespace.receipt_app.metadata[0].name}"
    port_forward_app            = "kubectl port-forward svc/${var.helm_release_name}-receipt-app ${var.app_port}:${var.app_port} -n ${kubernetes_namespace.receipt_app.metadata[0].name}"
    helm_status                 = "helm status ${var.helm_release_name} -n ${kubernetes_namespace.receipt_app.metadata[0].name}"
    helm_values                 = "helm get values ${var.helm_release_name} -n ${kubernetes_namespace.receipt_app.metadata[0].name}"
  }
}
