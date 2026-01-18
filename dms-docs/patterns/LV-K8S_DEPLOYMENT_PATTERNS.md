# LV Kubernetes Deployment Patterns

This document describes the standard LV patterns for Kubernetes deployments in production environments. These patterns are application-generic and can be applied to any microservices application.

## Overview

This document covers:
- Service naming conventions
- Deployment structure
- Service definitions
- Resource limits
- Health checks
- ConfigMap and Secret usage
- Port standardization

## Naming Conventions

### Service Names

**Pattern**: `<app>-<service>-service`

**Examples**:
- `dms-admin-service`
- `rti-auth-service`
- `sickpay-customer-service`

**Critical**: Always include `-service` suffix for Kubernetes service discovery.

### Namespace Names

- **Backend**: `<app>-<env>` (e.g., `dms-dev`, `rti-prod`)
- **Frontend**: `<app>-<env>-frontend` (e.g., `dms-dev-frontend`, `rti-uat-frontend`)

### Kubernetes Service FQDN

**Pattern**: `<app>-<service>-service.<app>-<env>.svc.cluster.local:8080`

**Example**: `dms-admin-service.dms-dev.svc.cluster.local:8080`

## Deployment Structure

### Standard Deployment Template

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: <app>-<service>-service
  namespace: <app>-<env>
  labels:
    app: <app>-<service>-service
    service: <service>
    version: v1
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
      serviceAccountName: <app>-<service>-service
      containers:
      - name: <app>-<service>-service
        image: <app>-<service>-service:<tag>
        imagePullPolicy: Always
        ports:
        - name: http
          containerPort: 8080
          protocol: TCP
        env:
        - name: PORT
          value: "8080"
        - name: ENVIRONMENT
          value: <env>
        - name: <SERVICE>_SERVICE_URL
          valueFrom:
            configMapKeyRef:
              name: <app>-service-urls-configmap
              key: <service>.service
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: <app>-secrets
              key: database-url
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
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
    service: <service>
spec:
  type: ClusterIP
  ports:
  - name: http
    port: 8080
    targetPort: 8080
    protocol: TCP
  selector:
    app: <app>-<service>-service
```

## Port Standardization

### Backend Services

- **Container Port**: `8080` (all backend services)
- **Service Port**: `8080` (matches container port)
- **Protocol**: `TCP`

### Frontend Services

- **Container Port**: `8080` (not 80 to avoid permission issues)
- **Service Port**: `8080`
- **Protocol**: `TCP`

**Why Port 8080?**
- Avoids permission denied errors (port 80 requires root)
- Kubernetes standard for application services
- Consistent across all services

## Resource Limits

### Standard Resource Sizing

**Small Service** (e.g., audit, compliance):
```yaml
resources:
  requests:
    memory: "256Mi"
    cpu: "250m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

**Medium Service** (e.g., admin, document):
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

**Large Service** (e.g., API Gateway, LLM):
```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "1000m"
  limits:
    memory: "2Gi"
    cpu: "2000m"
```

**Key Points**:
- Always set both requests and limits
- Requests: Guaranteed resources
- Limits: Maximum resources (prevents resource exhaustion)
- CPU: Measured in millicores (1000m = 1 core)
- Memory: Measured in Mi/Gi (binary units)

## Health Checks

### Liveness Probe

**Purpose**: Detects if container is running but unresponsive

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

**Key Points**:
- Initial delay: Allow time for startup
- Period: Check every 10 seconds
- Failure threshold: Restart after 3 failures

### Readiness Probe

**Purpose**: Detects if container is ready to serve traffic

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3
```

**Key Points**:
- Shorter initial delay than liveness
- More frequent checks (every 5 seconds)
- Removes pod from service endpoints if unhealthy

### Health Endpoint

**Spring Boot Actuator**:
- Path: `/actuator/health`
- Returns: `{"status":"UP"}` or `{"status":"DOWN"}`
- Components: Can include component health checks

## Environment Variables

### Standard Environment Variables

```yaml
env:
# Port configuration
- name: PORT
  value: "8080"
- name: SERVER_PORT
  value: "8080"

# Environment
- name: ENVIRONMENT
  value: <env>  # dev, uat, prod
- name: SPRING_PROFILES_ACTIVE
  value: <env>

# Service URLs from ConfigMap
- name: APIGATEWAY_SERVICE_URL
  valueFrom:
    configMapKeyRef:
      name: <app>-service-urls-configmap
      key: apigateway.service
- name: AUTH_SERVICE_URL
  valueFrom:
    configMapKeyRef:
      name: <app>-service-urls-configmap
      key: auth.service

# Secrets
- name: DATABASE_URL
  valueFrom:
    secretKeyRef:
      name: <app>-secrets
      key: database-url
- name: JWT_SECRET
  valueFrom:
    secretKeyRef:
      name: <app>-secrets
      key: jwt-secret
```

## ConfigMap Usage

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

**Usage in Deployment**:
```yaml
env:
- name: <SERVICE>_SERVICE_URL
  valueFrom:
    configMapKeyRef:
      name: <app>-service-urls-configmap
      key: <service>.service
```

## Secret Usage

### Secrets from Key Vault

**Pattern**: Use Azure Key Vault Provider or External Secrets Operator

```yaml
env:
- name: DATABASE_URL
  valueFrom:
    secretKeyRef:
      name: <app>-secrets
      key: database-url
- name: JWT_SECRET
  valueFrom:
    secretKeyRef:
      name: <app>-secrets
      key: jwt-secret
- name: AZURE_CLIENT_ID
  valueFrom:
    secretKeyRef:
      name: <app>-secrets
      key: azure-client-id
- name: AZURE_CLIENT_SECRET
  valueFrom:
    secretKeyRef:
      name: <app>-secrets
      key: azure-client-secret
```

**Best Practices**:
- Never hardcode secrets in YAML
- Use Key Vault or secret management system
- Rotate secrets regularly
- Use separate secrets per environment

## Service Accounts

### Service Account Definition

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: <app>-<service>-service
  namespace: <app>-<env>
  labels:
    app: <app>-<service>-service
```

### Usage in Deployment

```yaml
spec:
  template:
    spec:
      serviceAccountName: <app>-<service>-service
```

**Benefits**:
- Service identity for mTLS
- RBAC permissions
- Pod security policies

## Replica Configuration

### Development
- **Replicas**: 1-2
- **Reason**: Cost optimization, sufficient for testing

### UAT
- **Replicas**: 2
- **Reason**: Production-like, high availability testing

### Production
- **Replicas**: 2+ (minimum 2 for HA)
- **Reason**: High availability, load distribution

## Image Configuration

### Image Tags

**Pattern**: `<app>-<service>-service:<tag>`

**Tag Strategies**:
- **Version tags**: `v1.0.0`, `v1.0.1`
- **Commit SHA**: `abc123def456`
- **Environment**: `dev`, `uat`, `prod`

### Image Pull Policy

```yaml
imagePullPolicy: Always
```

**Key Points**:
- `Always`: Always pull latest image (recommended for CI/CD)
- `IfNotPresent`: Pull if not cached (faster, but may use stale images)
- `Never`: Never pull (only use cached images)

## Labels and Selectors

### Standard Labels

```yaml
metadata:
  labels:
    app: <app>-<service>-service
    service: <service>
    version: v1
    environment: <env>
    managed-by: flux
```

### Selector Matching

**Deployment Selector**:
```yaml
spec:
  selector:
    matchLabels:
      app: <app>-<service>-service
```

**Service Selector**:
```yaml
spec:
  selector:
    app: <app>-<service>-service
```

**Key Point**: Selectors must match pod labels exactly.

## Best Practices

1. **Service Name Suffix**: Always include `-service` suffix
2. **Port Standardization**: Use port 8080 for all services
3. **Resource Limits**: Always set requests and limits
4. **Health Checks**: Include both liveness and readiness probes
5. **ConfigMaps**: Use for non-sensitive configuration
6. **Secrets**: Use for sensitive data (never hardcode)
7. **Service Accounts**: Create service accounts for each service
8. **Labels**: Use consistent labeling strategy
9. **Replicas**: Minimum 2 for production
10. **Image Pull Policy**: Use `Always` for CI/CD pipelines

## Troubleshooting

### Pod Not Starting
- Check resource limits: `kubectl describe pod <pod-name>`
- Review events: `kubectl get events --sort-by='.lastTimestamp'`
- Check logs: `kubectl logs <pod-name>`

### Service Not Accessible
- Verify service selector matches pod labels
- Check service port matches container port
- Verify network policies allow traffic

### Health Check Failures
- Review health endpoint: `kubectl exec <pod> -- curl localhost:8080/actuator/health`
- Check application logs for errors
- Verify resource limits are sufficient

## References

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- Reference implementations:
  - `/Users/davidparker/Documents/LV-Code/dparker-rti-now/clusters/dev/releases/`
  - `/Users/davidparker/Documents/LV-Code/dparker-dms/dms-*/k8s-deployment.yaml`

## Version History

- **2026-01-XX**: Initial pattern documentation based on LV production implementations
