---
Last Updated: 2025-01-18T00:00:00Z
Updated By: davidparker-lv-bmth
---

# DMS Microservices Architecture

## Overview

The DMS system is implemented as independent microservices, each deployable separately in Kubernetes. Each service is completely self-contained with its own database, configuration, and deployment artifacts.

## Service Architecture

```
dparker-dms/
├── dms-core-service/           # Shared library - Common components (security, utils, DTOs)
├── dms-admin-service/          # Dev: 8081 / Prod: 8080 - User, Role, Permission, Application management
├── dms-document-service/       # Dev: 8083 / Prod: 8080 - Document CRUD operations
├── dms-audit-service/          # Dev: 8082 / Prod: 8080 - Centralized audit logging
├── dms-compliance-service/     # Dev: 8084 / Prod: 8080 - PCI-DSS, GDPR, ISO 27001 compliance
├── dms-llm-service/            # Dev: 8085 / Prod: 8080 - AI/LLM queries and embeddings
└── dms-frontend-service/       # Port 8080 - Angular 21 frontend application
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

### dms-admin-service (Ports: 8081 dev/docker, 8080 prod)
**Purpose**: Administration and management
- User management (CRUD)
- Role management (CRUD)
- Permission management (CRUD)
- Application registration management
- **Database**: `dms_admin` (PostgreSQL)
- **Dependencies**: dms-core-service (library), calls audit-service for logging
- **Independent**: Yes - can start without other services
- **POM Parent**: `com.davidparker.dms:dms-parent:1.0.0-SNAPSHOT`
- **Configuration**: Multiple profiles (dev, docker, prod, test)
- **Environment Variables**: `SPRING_DATASOURCE_URL`, `DB_USER`, `DB_PASSWORD`

### dms-document-service (Ports: 8083 dev/docker, 8080 prod)
**Purpose**: Document management
- Document upload/download
- Document versioning
- Document metadata management
- Blob storage integration
- **Database**: `dms_document` (PostgreSQL)
- **Dependencies**: dms-core-service (library), calls admin-service (verify app), audit-service (logging), llm-service (embeddings)
- **Independent**: Yes - can start without other services (with degraded functionality)
- **POM Parent**: `com.davidparker.dms:dms-parent:1.0.0-SNAPSHOT`
- **Configuration**: Multiple profiles (dev, docker, prod, test)
- **Environment Variables**: `SPRING_DATASOURCE_URL`, `DB_USER`, `DB_PASSWORD`

### dms-audit-service (Ports: 8082 dev/docker, 8080 prod)
**Purpose**: Centralized audit logging
- Receives audit events from all services
- Stores audit logs in database
- Publishes to Azure Event Hubs
- Audit log query API
- **Database**: `dms_audit` (PostgreSQL)
- **Dependencies**: dms-core-service (library), other services call it
- **Independent**: Yes - completely independent
- **POM Parent**: `com.davidparker.dms:dms-parent:1.0.0-SNAPSHOT`
- **Configuration**: Multiple profiles (dev, docker, prod, test)
- **Environment Variables**: `SPRING_DATASOURCE_URL`, `DB_USER`, `DB_PASSWORD`

### dms-compliance-service (Ports: 8084 dev/docker, 8080 prod)
**Purpose**: Compliance framework
- PCI-DSS compliance controls
- GDPR data subject rights
- ISO 27001 security controls
- Compliance reporting
- **Database**: None (stateless, calls other services)
- **Dependencies**: dms-core-service (library), calls audit-service, document-service, admin-service
- **Independent**: Yes - can start without other services
- **POM Parent**: `com.davidparker.dms:dms-parent:1.0.0-SNAPSHOT`
- **Configuration**: Multiple profiles (dev, docker, prod, test)

### dms-llm-service (Ports: 8085 dev/docker, 8080 prod)
**Purpose**: AI/LLM integration
- Document embedding generation
- Natural language document queries
- Azure AI Search integration
- Azure AI Foundry integration
- **Database**: None (uses Azure AI Search)
- **Dependencies**: dms-core-service (library), calls audit-service (logging), document-service (document info)
- **Independent**: Yes - can start without other services
- **POM Parent**: `com.davidparker.dms:dms-parent:1.0.0-SNAPSHOT`
- **Configuration**: Multiple profiles (dev, docker, prod, test)

### dms-frontend-service (Port 8080)
**Purpose**: Angular frontend application
- User interface for all DMS functionality
- MSAL authentication
- Admin dashboard
- Document management UI
- Compliance reporting UI
- **Type**: Angular 21 SPA
- **Dependencies**: Calls all backend services via REST APIs
- **Independent**: Yes - frontend only, no backend dependencies
- **Entrypoint**: `entrypoint.sh` for nginx configuration substitution
- **Environment Variables**: `API_GATEWAY_URL` or `APIGATEWAY_SERVICE_URL`

## Inter-Service Communication

Services communicate via REST APIs using HTTP/HTTPS:
- **Synchronous**: WebClient (Reactive) for service-to-service calls
- **Asynchronous**: Azure Event Hubs for audit events (optional)
- **Service Discovery**: Kubernetes DNS (service-name.namespace.svc.cluster.local)

## Service URLs (Kubernetes)

All services use port 8080 in Kubernetes:
- `dms-admin-service:8080`
- `dms-audit-service:8080`
- `dms-document-service:8080`
- `dms-compliance-service:8080`
- `dms-llm-service:8080`
- `dms-frontend-service:8080`
- `dms-api-gateway-service:8080`

## Service URLs (Docker Compose)

Services use unique ports in docker-compose:
- `dms-admin-service:8081`
- `dms-audit-service:8082`
- `dms-document-service:8083`
- `dms-compliance-service:8084`
- `dms-llm-service:8085`
- `dms-frontend-service:8080`
- `dms-api-gateway-service:8080`

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

## Configuration Standards

### Environment Variables

All services use standardized LV environment variables:
- **Database**: `SPRING_DATASOURCE_URL`, `DB_USER`, `DB_PASSWORD`
- **Service URLs**: `ADMIN_SERVICE_URL`, `AUDIT_SERVICE_URL`, etc.
- **Azure AD**: `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, `AZURE_JWK_SET_URI`

### Application Profiles

Each service supports multiple Spring profiles:
- **dev** - H2 in-memory, authentication bypass, unique ports
- **docker** - PostgreSQL, authentication bypass, unique ports
- **prod** - PostgreSQL, full security, port 8080
- **test** - H2 in-memory, authentication bypass, random port

### POM Inheritance

All services inherit from:
```xml
<parent>
    <groupId>com.davidparker.dms</groupId>
    <artifactId>dms-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>
```

### Dockerfile Patterns

Each service has three Dockerfile variants:
- **Dockerfile** - Standard multi-stage build
- **Dockerfile.dev** - Development build
- **Dockerfile.prod** - Production build with OpenTelemetry

## Application Identifier

All services use `davidparker-lv-bmth` as specified in `.cursorrules`.
