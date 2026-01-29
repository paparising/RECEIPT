# Main Terraform Configuration for Receipt Application Deployment

# Create Kubernetes Namespaces
resource "kubernetes_namespace" "receipt_app" {
  metadata {
    name = var.namespace_name

    labels = merge(
      var.additional_labels,
      {
        "environment" = var.environment
        "managed-by"  = "terraform"
      }
    )
  }

  depends_on = []
}

# Create Storage Classes
resource "kubernetes_storage_class" "standard" {
  count = var.create_storage_classes ? 1 : 0

  metadata {
    name = var.storage_class_standard
    labels = merge(
      var.additional_labels,
      {
        "type" = "standard"
      }
    )
  }

  storage_provisioner = "ebs.csi.aws.com"
  reclaim_policy      = "Delete"
  volume_binding_mode = "WaitForFirstConsumer"

  parameters = {
    type      = "gp2"
    fstype    = "ext4"
    encrypted = "true"
  }
}

resource "kubernetes_storage_class" "fast_ssd" {
  count = var.create_storage_classes ? 1 : 0

  metadata {
    name = var.storage_class_fast_ssd
    labels = merge(
      var.additional_labels,
      {
        "type" = "ssd"
      }
    )
  }

  storage_provisioner = "ebs.csi.aws.com"
  reclaim_policy      = "Delete"
  volume_binding_mode = "WaitForFirstConsumer"

  parameters = {
    type      = "gp3"
    iops      = tostring(var.storage_iops)
    throughput = tostring(var.storage_throughput)
    fstype    = "ext4"
    encrypted = "true"
  }

  allows_expansion = true
}

# Create Bitnami Helm Repository
resource "helm_repository" "bitnami" {
  name = "bitnami"
  url  = "https://charts.bitnami.com/bitnami"
}

# Deploy Helm Chart
resource "helm_release" "receipt_app" {
  count = var.enable_helm_chart ? 1 : 0

  name             = var.helm_release_name
  repository       = helm_repository.bitnami.name
  chart            = try(var.helm_chart_path, "bitnami/receipt-app") # Use local chart if available
  namespace        = kubernetes_namespace.receipt_app.metadata[0].name
  create_namespace = false
  wait             = true
  timeout          = 600

  # Use local chart if path is provided and exists
  dynamic "chart" {
    for_each = can(file("${var.helm_chart_path}/Chart.yaml")) ? [1] : []
    content {
      path = var.helm_chart_path
    }
  }

  # Application Configuration
  set {
    name  = "replicaCount"
    value = var.app_replicas
  }

  set {
    name  = "image.tag"
    value = var.app_image
  }

  set {
    name  = "image.pullPolicy"
    value = var.environment == "dev" ? "Always" : "IfNotPresent"
  }

  set {
    name  = "service.port"
    value = var.app_port
  }

  # Resource Configuration
  set {
    name  = "resources.requests.cpu"
    value = var.app_cpu_request
  }

  set {
    name  = "resources.requests.memory"
    value = var.app_memory_request
  }

  set {
    name  = "resources.limits.cpu"
    value = var.app_cpu_limit
  }

  set {
    name  = "resources.limits.memory"
    value = var.app_memory_limit
  }

  # Autoscaling Configuration
  dynamic "set" {
    for_each = var.enable_autoscaling ? [1] : []
    content {
      name  = "autoscaling.enabled"
      value = "true"
    }
  }

  dynamic "set" {
    for_each = var.enable_autoscaling ? [1] : []
    content {
      name  = "autoscaling.minReplicas"
      value = var.hpa_min_replicas
    }
  }

  dynamic "set" {
    for_each = var.enable_autoscaling ? [1] : []
    content {
      name  = "autoscaling.maxReplicas"
      value = var.hpa_max_replicas
    }
  }

  dynamic "set" {
    for_each = var.enable_autoscaling ? [1] : []
    content {
      name  = "autoscaling.targetCPUUtilizationPercentage"
      value = var.hpa_target_cpu
    }
  }

  dynamic "set" {
    for_each = var.enable_autoscaling ? [1] : []
    content {
      name  = "autoscaling.targetMemoryUtilizationPercentage"
      value = var.hpa_target_memory
    }
  }

  # Ingress Configuration
  dynamic "set" {
    for_each = var.enable_ingress ? [1] : []
    content {
      name  = "ingress.enabled"
      value = "true"
    }
  }

  dynamic "set" {
    for_each = var.enable_ingress ? [1] : []
    content {
      name  = "ingress.className"
      value = var.ingress_class
    }
  }

  dynamic "set" {
    for_each = var.enable_ingress ? [1] : []
    content {
      name  = "ingress.hosts[0].host"
      value = var.ingress_hostname
    }
  }

  dynamic "set" {
    for_each = var.enable_ingress && var.ingress_tls_enabled ? [1] : []
    content {
      name  = "ingress.tls[0].secretName"
      value = "${var.helm_release_name}-tls"
    }
  }

  dynamic "set" {
    for_each = var.enable_ingress && var.ingress_tls_enabled ? [1] : []
    content {
      name  = "ingress.tls[0].hosts[0]"
      value = var.ingress_hostname
    }
  }

  # MySQL Configuration
  dynamic "set" {
    for_each = var.enable_mysql ? [1] : []
    content {
      name  = "mysql.enabled"
      value = "true"
    }
  }

  dynamic "set" {
    for_each = var.enable_mysql ? [1] : []
    content {
      name  = "mysql.auth.rootPassword"
      value = var.mysql_root_password
    }
  }

  dynamic "set" {
    for_each = var.enable_mysql ? [1] : []
    content {
      name  = "mysql.auth.username"
      value = var.mysql_user
    }
  }

  dynamic "set" {
    for_each = var.enable_mysql ? [1] : []
    content {
      name  = "mysql.auth.password"
      value = var.mysql_password
    }
  }

  dynamic "set" {
    for_each = var.enable_mysql ? [1] : []
    content {
      name  = "mysql.auth.database"
      value = var.mysql_database
    }
  }

  dynamic "set" {
    for_each = var.enable_mysql ? [1] : []
    content {
      name  = "mysql.primary.persistence.size"
      value = var.mysql_storage_size
    }
  }

  dynamic "set" {
    for_each = var.enable_mysql ? [1] : []
    content {
      name  = "mysql.primary.persistence.storageClass"
      value = var.environment == "production" ? var.storage_class_fast_ssd : var.storage_class_standard
    }
  }

  # RabbitMQ Configuration
  dynamic "set" {
    for_each = var.enable_rabbitmq ? [1] : []
    content {
      name  = "rabbitmq.enabled"
      value = "true"
    }
  }

  dynamic "set" {
    for_each = var.enable_rabbitmq ? [1] : []
    content {
      name  = "rabbitmq.auth.username"
      value = var.rabbitmq_user
    }
  }

  dynamic "set" {
    for_each = var.enable_rabbitmq ? [1] : []
    content {
      name  = "rabbitmq.auth.password"
      value = var.rabbitmq_password
    }
  }

  dynamic "set" {
    for_each = var.enable_rabbitmq ? [1] : []
    content {
      name  = "rabbitmq.persistence.size"
      value = var.rabbitmq_storage_size
    }
  }

  dynamic "set" {
    for_each = var.enable_rabbitmq ? [1] : []
    content {
      name  = "rabbitmq.persistence.storageClass"
      value = var.environment == "production" ? var.storage_class_fast_ssd : var.storage_class_standard
    }
  }

  # Monitoring Configuration
  dynamic "set" {
    for_each = var.enable_prometheus_scraping ? [1] : []
    content {
      name  = "podAnnotations.prometheus\\.io/scrape"
      value = "true"
    }
  }

  dynamic "set" {
    for_each = var.enable_prometheus_scraping ? [1] : []
    content {
      name  = "podAnnotations.prometheus\\.io/port"
      value = var.app_port
    }
  }

  depends_on = [
    kubernetes_namespace.receipt_app,
    kubernetes_storage_class.standard,
    kubernetes_storage_class.fast_ssd,
    helm_repository.bitnami
  ]
}

# Create Service Account for RBAC
resource "kubernetes_service_account" "receipt_app" {
  count = var.enable_rbac ? 1 : 0

  metadata {
    name      = "${var.helm_release_name}-sa"
    namespace = kubernetes_namespace.receipt_app.metadata[0].name

    labels = merge(
      var.additional_labels,
      {
        "managed-by" = "terraform"
      }
    )
  }

  depends_on = [kubernetes_namespace.receipt_app]
}

# Create Cluster Role for RBAC
resource "kubernetes_cluster_role" "receipt_app" {
  count = var.enable_rbac ? 1 : 0

  metadata {
    name = "${var.helm_release_name}-cluster-role"
    labels = merge(
      var.additional_labels,
      {
        "managed-by" = "terraform"
      }
    )
  }

  rule {
    api_groups = [""]
    resources  = ["configmaps", "secrets", "pods"]
    verbs      = ["get", "list", "watch"]
  }

  rule {
    api_groups = ["apps"]
    resources  = ["deployments", "statefulsets"]
    verbs      = ["get", "list", "watch"]
  }

  rule {
    api_groups = ["autoscaling"]
    resources  = ["horizontalpodautoscalers"]
    verbs      = ["get", "list", "watch"]
  }
}

# Create Cluster Role Binding
resource "kubernetes_cluster_role_binding" "receipt_app" {
  count = var.enable_rbac ? 1 : 0

  metadata {
    name = "${var.helm_release_name}-cluster-role-binding"
  }

  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind      = "ClusterRole"
    name      = kubernetes_cluster_role.receipt_app[0].metadata[0].name
  }

  subject {
    kind      = "ServiceAccount"
    name      = kubernetes_service_account.receipt_app[0].metadata[0].name
    namespace = kubernetes_namespace.receipt_app.metadata[0].name
  }
}

# Create Network Policy
resource "kubernetes_network_policy" "receipt_app" {
  count = var.enable_network_policies ? 1 : 0

  metadata {
    name      = "${var.helm_release_name}-network-policy"
    namespace = kubernetes_namespace.receipt_app.metadata[0].name

    labels = merge(
      var.additional_labels,
      {
        "managed-by" = "terraform"
      }
    )
  }

  spec {
    pod_selector {
      match_labels = {
        "app.kubernetes.io/name" = "receipt-app"
      }
    }

    policy_types = ["Ingress", "Egress"]

    # Allow ingress from anywhere on port 8080
    ingress {
      from {
        namespace_selector {}
      }

      ports {
        port     = "8080"
        protocol = "TCP"
      }
    }

    # Allow egress to MySQL
    egress {
      to {
        pod_selector {
          match_labels = {
            "app" = "mysql"
          }
        }
      }

      ports {
        port     = "3306"
        protocol = "TCP"
      }
    }

    # Allow egress to RabbitMQ
    egress {
      to {
        pod_selector {
          match_labels = {
            "app" = "rabbitmq"
          }
        }
      }

      ports {
        port     = "5672"
        protocol = "TCP"
      }
    }

    # Allow DNS
    egress {
      to {
        namespace_selector {}
      }

      ports {
        port     = "53"
        protocol = "UDP"
      }
    }
  }

  depends_on = [kubernetes_namespace.receipt_app]
}
