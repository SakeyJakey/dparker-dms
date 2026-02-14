---
Last Updated: 2026-02-14T15:30:00Z
Updated By: davidparker-lv-bmth
---

# DMS Deployment Guide

## Table of Contents

1. [Local Docker Compose](#local-docker-compose)
2. [Azure AKS Deployment](#azure-aks-deployment)
3. [Flux GitOps Configuration](#flux-gitops-configuration)
4. [Istio Service Mesh Setup](#istio-service-mesh-setup)
5. [Environment Configuration](#environment-configuration)

---

## Local Docker Compose

### Quick Start

```bash
# Build and start all services
docker compose up -d --build

# View service status
docker compose ps

# View logs
docker compose logs -f [service-name]

# Stop all services
docker compose down
```

### Services Started

| Service | Port | Description |
|---------|------|-------------|
| postgres-admin | 5432 | Admin DB |
| postgres-document | 5433 | Document DB |
| postgres-audit | 5434 | Audit DB |
| redis | 6379 | Cache |
| dms-admin-service | 8081 | Admin API |
| dms-audit-service | 8082 | Audit API |
| dms-document-service | 8083 | Document API |
| dms-compliance-service | 8084 | Compliance API |
| dms-llm-service | 8085 | LLM API |
| dms-api-gateway-service | 8080 | API Gateway |
| dms-frontend-service | 8080 | Frontend |

### Health Checks

```bash
# Check individual services
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
```

---

## Azure AKS Deployment

### Prerequisites

- Azure CLI (`az`) authenticated
- kubectl configured for AKS cluster
- Flux installed on cluster
- Istio installed on cluster

### Cluster Setup

```bash
# Create AKS cluster
az aks create --resource-group dms-rg \
  --name dms-aks \
  --node-count 3 \
  --enable-managed-identity \
  --network-plugin azure

# Get credentials
az aks get-credentials --resource-group dms-rg --name dms-aks

# Install Istio
istioctl install --set profile=demo
kubectl label namespace dms istio-injection=enabled
```

### Kubernetes Manifests

Each service has a `k8s-deployment.yaml` with:
- Deployment (2 replicas)
- Service (ClusterIP)
- Health checks (liveness/readiness)
- Resource limits
- Environment variables from ConfigMaps/Secrets

### Deploy Services

```bash
# Apply namespace
kubectl apply -f dms-docs/reference/clusters/dev/namespaces/dms-dev.yaml

# Apply config maps
kubectl apply -f dms-docs/reference/clusters/dev/releases/dms/dev/config-maps/

# Deploy services
kubectl apply -f dms-admin-service/k8s-deployment.yaml
kubectl apply -f dms-audit-service/k8s-deployment.yaml
kubectl apply -f dms-document-service/k8s-deployment.yaml
kubectl apply -f dms-compliance-service/k8s-deployment.yaml
kubectl apply -f dms-llm-service/k8s-deployment.yaml
kubectl apply -f dms-api-gateway-service/k8s-deployment.yaml
kubectl apply -f dms-frontend-service/k8s-deployment.yaml
```

---

## Flux GitOps Configuration

### Setup

Reference configurations are in `dms-docs/reference/flux-config/`:
- `gitrepository.yaml` — Git source configuration
- `kustomization-dev.yaml` — Development environment
- `kustomization-prod.yaml` — Production environment
- `kustomization-uat.yaml` — UAT environment

### Apply Flux

```bash
kubectl apply -f dms-docs/reference/flux-config/gitrepository.yaml
kubectl apply -f dms-docs/reference/flux-config/kustomization-dev.yaml
```

---

## Istio Service Mesh Setup

### Reference Configs

Located in `dms-docs/reference/istio-config/`:
- `peer-authentication.yaml` — mTLS (STRICT mode)
- `authorization-policy.yaml` — Service authorization
- `virtual-service.yaml` — Traffic routing
- `destination-rule.yaml` — Circuit breakers

### Apply

```bash
kubectl apply -f dms-docs/reference/istio-config/
```

---

## Environment Configuration

### Required Azure Resources

1. **Azure Key Vault** — Secrets management
2. **Azure Blob Storage** — Document storage
3. **Azure AD App Registration** — Authentication
4. **Azure AI Search** — Document search (for LLM service)
5. **Azure Event Hubs** — Audit event streaming

### Environment Variables by Profile

| Variable | dev | docker | prod |
|----------|-----|--------|------|
| DB URL | H2 memory | PostgreSQL | PostgreSQL |
| Auth | Disabled | Disabled | Azure AD |
| Key Vault | Disabled | Disabled | Enabled |
| Blob Storage | N/A | N/A | Azure |
| Event Hubs | N/A | N/A | Azure |

### Docker Compose .env

Create `.env` file in project root:
```env
POSTGRES_PASSWORD=postgres
REDIS_PASSWORD=
AZURE_TENANT_ID=
AZURE_CLIENT_ID=
```
