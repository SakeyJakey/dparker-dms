# DMS Microservices Architecture

## Overview

The DMS system is implemented as independent microservices, each deployable separately in Kubernetes. Each service is completely self-contained with its own database, configuration, and deployment artifacts.

## Service Architecture

```
dparker-dms/
├── dms-core-service/           # Shared library - Common components (security, utils, DTOs)
├── dms-admin-service/          # Port 8081 - User, Role, Permission, Application management
├── dms-document-service/       # Port 8083 - Document CRUD operations
├── dms-audit-service/          # Port 8082 - Centralized audit logging
├── dms-compliance-service/     # Port 8084 - PCI-DSS, GDPR, ISO 27001 compliance
├── dms-llm-service/            # Port 8085 - AI/LLM queries and embeddings
└── dms-frontend-service/        # Port 80 - Angular 25 frontend application
```

## Service Details

### dms-core-service (Library)
**Purpose**: Shared components library
- Common security configurations (JWT converter, base security config)
- Shared utilities (JSON converter, credential service)
- Common exceptions
- Shared DTOs (AuditEventDto)
- Shared clients (AuditEventClient)
- **Type**: Maven library (jar packaging)
- **Dependencies**: None (base library)
- **Usage**: Included as dependency in other services

### dms-admin-service (Port 8081)
**Purpose**: Administration and management
- User management (CRUD)
- Role management (CRUD)
- Permission management (CRUD)
- Application registration management
- **Database**: `dms_admin` (PostgreSQL)
- **Dependencies**: dms-core-service (library), calls audit-service for logging
- **Independent**: Yes - can start without other services

### dms-document-service (Port 8083)
**Purpose**: Document management
- Document upload/download
- Document versioning
- Document metadata management
- Blob storage integration
- **Database**: `dms_document` (PostgreSQL)
- **Dependencies**: dms-core-service (library), calls admin-service (verify app), audit-service (logging), llm-service (embeddings)
- **Independent**: Yes - can start without other services (with degraded functionality)

### dms-audit-service (Port 8082)
**Purpose**: Centralized audit logging
- Receives audit events from all services
- Stores audit logs in database
- Publishes to Azure Event Hubs
- Audit log query API
- **Database**: `dms_audit` (PostgreSQL)
- **Dependencies**: dms-core-service (library), other services call it
- **Independent**: Yes - completely independent

### dms-compliance-service (Port 8084)
**Purpose**: Compliance framework
- PCI-DSS compliance controls
- GDPR data subject rights
- ISO 27001 security controls
- Compliance reporting
- **Database**: None (stateless, calls other services)
- **Dependencies**: dms-core-service (library), calls audit-service, document-service, admin-service
- **Independent**: Yes - can start without other services

### dms-llm-service (Port 8085)
**Purpose**: AI/LLM integration
- Document embedding generation
- Natural language document queries
- Azure AI Search integration
- Azure AI Foundry integration
- **Database**: None (uses Azure AI Search)
- **Dependencies**: dms-core-service (library), calls audit-service (logging), document-service (document info)
- **Independent**: Yes - can start without other services

### dms-frontend-service (Port 80)
**Purpose**: Angular frontend application
- User interface for all DMS functionality
- MSAL authentication
- Admin dashboard
- Document management UI
- Compliance reporting UI
- **Type**: Angular 25 SPA
- **Dependencies**: Calls all backend services via REST APIs
- **Independent**: Yes - frontend only, no backend dependencies

## Inter-Service Communication

Services communicate via REST APIs using HTTP/HTTPS:
- **Synchronous**: WebClient (Reactive) for service-to-service calls
- **Asynchronous**: Azure Event Hubs for audit events (optional)
- **Service Discovery**: Kubernetes DNS (service-name.namespace.svc.cluster.local)

## Service URLs (Kubernetes)

- `dms-admin-service:8081`
- `dms-audit-service:8082`
- `dms-document-service:8083`
- `dms-compliance-service:8084`
- `dms-llm-service:8085`
- `dms-frontend-service:80`

## Database Per Service

Each service has its own database schema:
- **dms-admin-service**: `dms_admin` - Users, roles, permissions, applications
- **dms-document-service**: `dms_document` - Documents metadata
- **dms-audit-service**: `dms_audit` - Audit logs (partitioned)

## Shared Components (dms-core-service)

The `dms-core-service` is a Maven library that provides:
- **Security**: JWT authentication converter, base security config
- **Configuration**: Key Vault configuration
- **Utilities**: JSON converter, credential service
- **Exceptions**: Common exception classes
- **DTOs**: Shared data transfer objects
- **Clients**: Audit event client

Services include this library as a dependency to share common code while maintaining independence.

## Deployment

Each service includes:
- `Dockerfile` for containerization
- `k8s-deployment.yaml` for Kubernetes deployment
- Independent configuration via `application.yml`
- Health check endpoints (`/actuator/health`)

## Independence Guarantees

1. **Shared Library**: Common code in `dms-core-service` library (not a runtime dependency)
2. **No Direct Dependencies**: Services communicate via HTTP only
3. **Independent Databases**: Each service has its own database
4. **Independent Deployment**: Each service can be deployed/updated independently
5. **Graceful Degradation**: Services handle failures of other services gracefully

## Kubernetes Deployment

All services are configured for:
- Horizontal scaling (replicas)
- Health checks (liveness/readiness probes)
- Resource limits
- Service discovery via DNS
- ConfigMaps and Secrets for configuration

## Application Identifier

All services use `davidparker-lv-bmth` as specified in `.cursorrules`.
