# LV Flux GitOps Patterns

This document describes the standard LV patterns for Flux GitOps configuration management in Kubernetes environments. These patterns are application-generic and can be applied to any microservices application.

## Overview

Flux is used for GitOps-based configuration management, automatically syncing Kubernetes manifests from Git repositories. This ensures all infrastructure is version-controlled and changes are traceable.

## Directory Structure

### Standard Structure

```
<project-root>/
├── clusters/
│   ├── dev/
│   │   ├── manifests/
│   │   │   └── istio-configs/
│   │   │       └── dev/
│   │   │           ├── authorization-policy.yaml
│   │   │           └── request-authentication.yaml
│   │   ├── namespaces/
│   │   │   ├── <app>-dev-frontend.yaml
│   │   │   └── <app>-dev.yaml
│   │   └── releases/
│   │       └── <app>/
│   │           └── dev/
│   │               ├── config-maps/
│   │               ├── frontend/
│   │               ├── istio-configs/
│   │               │   ├── authz-policies/
│   │               │   ├── destination-rules/
│   │               │   └── virtual-services/
│   │               └── services/
│   ├── uat/
│   │   └── [same structure as dev]
│   └── prod/
│       └── [same structure as dev]
└── flux-config/
    ├── gitrepository.yaml
    └── kustomization.yaml
```

## Flux Configuration

### GitRepository Resource

**Location**: `flux-config/gitrepository.yaml`

```yaml
apiVersion: source.toolkit.fluxcd.io/v1
kind: GitRepository
metadata:
  name: <app>-config
  namespace: flux-system
spec:
  interval: 5m
  url: https://github.com/<org>/<repo>
  ref:
    branch: main
  secretRef:
    name: <app>-git-credentials
```

**Key Points**:
- Namespace: Always `flux-system`
- Interval: Typically 5 minutes for automatic sync
- Secret: Git credentials stored as Kubernetes secret
- Branch: Usually `main` or environment-specific branches

### Kustomization Resource

**Location**: `flux-config/kustomization.yaml`

```yaml
apiVersion: kustomize.toolkit.fluxcd.io/v1
kind: Kustomization
metadata:
  name: <app>-services
  namespace: flux-system
spec:
  interval: 5m
  path: ./clusters/<env>  # Environment-specific path
  prune: true
  sourceRef:
    kind: GitRepository
    name: <app>-config
  validation: client
  healthChecks:
    - apiVersion: apps/v1
      kind: Deployment
      name: <app>-<service>-service
      namespace: <app>-<env>
```

**Key Points**:
- Path: Points to environment-specific directory (dev/uat/prod)
- Prune: Automatically removes resources deleted from Git
- Health Checks: Validates deployment health after sync
- Validation: Uses client-side validation

## Namespace Structure

### Standard Namespaces

Each environment uses two namespaces:
1. **Backend namespace**: `<app>-<env>` (e.g., `dms-dev`, `rti-dev`)
2. **Frontend namespace**: `<app>-<env>-frontend` (e.g., `dms-dev-frontend`, `rti-dev-frontend`)

### Namespace Definition

**Location**: `clusters/<env>/namespaces/<app>-<env>.yaml`

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: <app>-<env>
  labels:
    istio-injection: enabled
    environment: <env>
    app: <app>
```

**Frontend Namespace**: `clusters/<env>/namespaces/<app>-<env>-frontend.yaml`

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: <app>-<env>-frontend
  labels:
    istio-injection: enabled
    environment: <env>
    app: <app>
    tier: frontend
```

**Key Points**:
- `istio-injection: enabled` label enables automatic Istio sidecar injection
- Environment label for filtering and organization
- Frontend namespace separated for isolation

## ConfigMap Patterns

### Service URLs ConfigMap

**Location**: `clusters/<env>/releases/<app>/<env>/config-maps/<app>-service-urls-configmap.yaml`

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: <app>-service-urls-configmap
  namespace: <app>-<env>
data:
  apigateway.service: "http://<app>-api-gateway-service.<app>-<env>.svc.cluster.local:8080"
  auth.service: "http://<app>-auth-service.<app>-<env>.svc.cluster.local:8080"
  <service>.service: "http://<app>-<service>-service.<app>-<env>.svc.cluster.local:8080"
```

**Naming Convention**:
- Service name pattern: `<app>-<service>-service`
- FQDN pattern: `<app>-<service>-service.<app>-<env>.svc.cluster.local:8080`
- Port: Always `8080` for backend services

### Frontend ConfigMap

**Location**: `clusters/<env>/releases/<app>/<env>/config-maps/<app>-frontend-configmap.yaml`

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: <app>-service-urls-configmap
  namespace: <app>-<env>-frontend
data:
  apigateway.service: "http://<app>-api-gateway-service.<app>-<env>.svc.cluster.local:8080"
```

**Key Points**:
- Frontend only needs API Gateway URL
- All `/api/*` requests are proxied to API Gateway
- Frontend never talks directly to backend services

## Deployment Structure

### Service Deployment

**Location**: `clusters/<env>/releases/<app>/<env>/services/<app>-<service>-service.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: <app>-<service>-service
  namespace: <app>-<env>
  labels:
    app: <app>-<service>-service
    service: <service>
spec:
  replicas: 2
  selector:
    matchLabels:
      app: <app>-<service>-service
  template:
    metadata:
      labels:
        app: <app>-<service>-service
        service: <service>
        version: v1
    spec:
      containers:
      - name: <app>-<service>-service
        image: <app>-<service>-service:<tag>
        ports:
        - containerPort: 8080
        env:
        - name: <SERVICE>_SERVICE_URL
          valueFrom:
            configMapKeyRef:
              name: <app>-service-urls-configmap
              key: <service>.service
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
---
apiVersion: v1
kind: Service
metadata:
  name: <app>-<service>-service
  namespace: <app>-<env>
  labels:
    app: <app>-<service>-service
spec:
  type: ClusterIP
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP
    name: http
  selector:
    app: <app>-<service>-service
```

**Key Points**:
- Service name must include `-service` suffix
- Port 8080 for all backend services
- Environment variables from ConfigMap
- Resource limits required
- Health checks via `/actuator/health` (Spring Boot)

## Naming Conventions

### Service Names
- Pattern: `<app>-<service>-service`
- Examples: `dms-admin-service`, `rti-auth-service`, `sickpay-customer-service`
- **Critical**: Must include `-service` suffix for Kubernetes service discovery

### Namespace Names
- Backend: `<app>-<env>` (e.g., `dms-dev`, `rti-prod`)
- Frontend: `<app>-<env>-frontend` (e.g., `dms-dev-frontend`, `rti-uat-frontend`)

### ConfigMap Names
- Pattern: `<app>-service-urls-configmap` or `<app>-<purpose>-configmap`
- Examples: `dms-service-urls-configmap`, `rti-azure-configmap`

### Kubernetes Service FQDN
- Pattern: `<app>-<service>-service.<app>-<env>.svc.cluster.local:8080`
- Example: `dms-admin-service.dms-dev.svc.cluster.local:8080`

## Environment-Specific Configuration

### Development (dev)
- Namespace: `<app>-dev` and `<app>-dev-frontend`
- Replicas: Usually 1-2
- Resource limits: Lower for cost optimization
- Debugging: Additional logging enabled

### UAT (uat)
- Namespace: `<app>-uat` and `<app>-uat-frontend`
- Replicas: 2
- Resource limits: Production-like
- Testing: Full integration testing environment

### Production (prod)
- Namespace: `<app>-prod` and `<app>-prod-frontend`
- Replicas: 2+ (high availability)
- Resource limits: Full production sizing
- Monitoring: Full observability stack

## Best Practices

1. **Separate Frontend Namespace**: Always use separate namespace for frontend services
2. **Service Name Suffix**: Always include `-service` suffix in service names
3. **Port Standardization**: Use port 8080 for all backend services
4. **ConfigMap Organization**: Group related configs in separate ConfigMaps
5. **Health Checks**: Always include liveness and readiness probes
6. **Resource Limits**: Always set resource requests and limits
7. **Istio Labels**: Ensure `istio-injection: enabled` on namespaces
8. **GitOps Workflow**: All changes via Git, Flux auto-syncs
9. **Environment Isolation**: Separate directories for each environment
10. **Version Control**: All manifests in Git, never manual kubectl apply

## Flux Sync Process

1. **Developer commits** changes to Git repository
2. **Flux detects** changes via GitRepository (every 5 minutes)
3. **Kustomization reconciles** changes to cluster
4. **Health checks** validate deployment success
5. **Notifications** (optional) alert on sync status

## Troubleshooting

### Flux Not Syncing
- Check GitRepository status: `kubectl get gitrepository -n flux-system`
- Verify Git credentials secret exists
- Check Kustomization status: `kubectl get kustomization -n flux-system`
- Review Flux logs: `kubectl logs -n flux-system -l app=kustomize-controller`

### Resources Not Applied
- Verify path in Kustomization matches directory structure
- Check namespace exists before deploying resources
- Validate YAML syntax: `kubectl apply --dry-run=client -f <file>`

### Health Check Failures
- Verify deployment is actually healthy
- Check health endpoint: `/actuator/health`
- Review pod logs for errors
- Ensure resource limits are sufficient

## References

- [Flux Documentation](https://fluxcd.io/docs/)
- [Kustomize Documentation](https://kustomize.io/)
- Reference implementations:
  - `/Users/davidparker/Documents/LV-Code/dparker-rti-now/clusters/`
  - `/Users/davidparker/Documents/LV-Code/dparker-mendix-test/docs/`

## Version History

- **2026-01-XX**: Initial pattern documentation based on LV production implementations
