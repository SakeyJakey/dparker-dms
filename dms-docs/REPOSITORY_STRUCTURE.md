---
Last Updated: 2024-01-18T13:42:00Z
Updated By: davidparker-lv-bmth
---

# DMS Repository Structure

## Overview

The DMS project is structured to support both monorepo development and future migration to standalone repositories. This document describes the current structure and the planned repository split.

## Current Monorepo Structure

```
dparker-dms/
├── dms-admin-service/          # Administration service
├── dms-audit-service/          # Audit logging service
├── dms-compliance-service/     # Compliance service
├── dms-document-service/       # Document management service
├── dms-llm-service/           # LLM/AI service
├── dms-frontend-service/       # Angular frontend
├── dms-core-service/           # Shared library
├── dms-e2e-tests/              # End-to-end test suite
├── dms-docs/                   # Documentation and reference configs
│   ├── reference/              # Reference configurations
│   │   ├── clusters/           # Kubernetes cluster configs
│   │   ├── flux-config/        # Flux GitOps configs
│   │   └── istio-config/       # Istio service mesh configs
│   ├── scripts/                # Development scripts
│   └── [documentation files]   # All project documentation
├── docker-compose.yml          # Local development orchestration
├── pom.xml                     # Parent POM (if multi-module)
└── README.md                   # Project overview
```

## Planned Standalone Repositories

Each service and component will have its own GitHub repository:

| Repository Name | Purpose | Contains |
|----------------|---------|----------|
| `dms-admin-service` | Administration service | Service code, Dockerfile, k8s-deployment.yaml, README.md |
| `dms-audit-service` | Audit logging service | Service code, Dockerfile, k8s-deployment.yaml, README.md |
| `dms-compliance-service` | Compliance service | Service code, Dockerfile, k8s-deployment.yaml, README.md |
| `dms-document-service` | Document management | Service code, Dockerfile, k8s-deployment.yaml, README.md |
| `dms-llm-service` | LLM/AI service | Service code, Dockerfile, k8s-deployment.yaml, README.md |
| `dms-frontend-service` | Angular frontend | Frontend code, Dockerfile, nginx.conf, README.md |
| `dms-core-service` | Shared library | Library code, pom.xml, README.md |
| `dms-e2e-tests` | E2E test suite | Test code, pom.xml, README.md |
| `dms-docs` | Documentation | All docs, reference configs, dev scripts |

## Folder Naming Conventions

- **Documentation**: `dms-docs/` (NOT `docs/`)
- **E2E Tests**: `dms-e2e-tests/` (NOT `e2e-tests/`)
- **Reference Configs**: `dms-docs/reference/`
- **Dev Scripts**: `dms-docs/scripts/`

## Docker Compose for Local Development

The root `docker-compose.yml` file:
- Builds and starts all services
- Includes all dependencies (PostgreSQL, Redis)
- Configures service networking
- Sets up health checks
- Uses service names matching repository names

### Usage

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f [service-name]

# Rebuild services
docker-compose build
```

## Development Scripts

All development scripts are in `dms-docs/scripts/`:

- `setup-local-dev.sh` - Sets up local development environment
- `stop-services.sh` - Stops all running services
- `entrypoint.sh` - Frontend service entrypoint (reference)
- `verify-requirements.sh` - Verifies project requirements

## Reference Configurations

Reference configurations in `dms-docs/reference/`:

- **clusters/**: Kubernetes cluster manifests and configurations
- **flux-config/**: Flux GitOps repository and kustomization configs
- **istio-config/**: Istio service mesh policies and configurations

These are reference documents. Service-specific deployments should copy and adapt these configs.

## Migration to Standalone Repositories

When migrating to standalone repositories:

1. **Service Repositories**: Copy service folder to new repository
   - Include: source code, Dockerfile, k8s-deployment.yaml, pom.xml/package.json
   - Include: README.md with service-specific documentation
   - Exclude: parent POM references (if applicable)

2. **dms-docs Repository**: Copy entire `dms-docs/` folder
   - All documentation files
   - Reference configurations
   - Development scripts

3. **dms-e2e-tests Repository**: Copy entire `dms-e2e-tests/` folder
   - Test code
   - Test configuration
   - README.md

4. **Update References**: Update service URLs and dependencies
   - Inter-service communication URLs
   - Docker Compose service names
   - CI/CD pipeline configurations

## Best Practices

1. **Service Independence**: Each service should be independently deployable
2. **Documentation**: Keep service-specific docs in service repos, shared docs in dms-docs
3. **Scripts**: Shared utilities in dms-docs/scripts, service-specific in service repos
4. **Configs**: Reference configs in dms-docs/reference, service-specific in service repos
5. **Docker Compose**: Maintain in root for local dev, or in dms-docs for reference

---

*Last Updated: 2024-01-18T13:42:00Z*  
*Updated By: davidparker-lv-bmth*
