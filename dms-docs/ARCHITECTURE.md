---
Last Updated: 2026-02-14T17:00:00Z
Updated By: davidparker-lv-bmth
---

# DMS Architecture Documentation

## Table of Contents

1. [System Overview](#system-overview)
2. [Microservices Architecture](#microservices-architecture)
3. [Service Details](#service-details)
4. [Data Flow](#data-flow)
5. [Security Architecture](#security-architecture)
6. [Deployment Architecture](#deployment-architecture)
7. [Integration Patterns](#integration-patterns)

---

## System Overview

The Document Management System (DMS) is a cloud-native, microservices-based platform built on Azure Kubernetes Service (AKS) with Istio service mesh for secure inter-service communication.

### High-Level Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        WEB[Web Browser]
        API_CLIENT[API Clients]
    end
    
    subgraph "Azure AKS Cluster"
        subgraph "Istio Service Mesh"
            subgraph "Frontend"
                FE[dms-frontend-service<br/>Port 80]
            end
            
            subgraph "Backend Services"
                ADMIN[dms-admin-service<br/>Dev: 8081 / Prod: 8080]
                DOC[dms-document-service<br/>Dev: 8083 / Prod: 8080]
                AUDIT[dms-audit-service<br/>Dev: 8082 / Prod: 8080]
                COMPLIANCE[dms-compliance-service<br/>Dev: 8084 / Prod: 8080]
                LLM[dms-llm-service<br/>Dev: 8085 / Prod: 8080]
            end
            
            subgraph "Shared Library"
                CORE[dms-core-service<br/>JAR Library]
            end
        end
    end
    
    subgraph "Azure Services"
        KV[Azure Key Vault]
        BLOB[Azure Blob Storage]
        AI_SEARCH[Azure AI Search]
        AI_FOUNDRY[Azure AI Foundry]
        EVENT_HUB[Azure Event Hubs]
    end
    
    subgraph "Databases"
        PG_ADMIN[(PostgreSQL<br/>dms_admin)]
        PG_DOC[(PostgreSQL<br/>dms_document)]
        PG_AUDIT[(PostgreSQL<br/>dms_audit)]
        REDIS[(Redis Cache)]
    end
    
    WEB --> FE
    API_CLIENT --> ADMIN
    API_CLIENT --> DOC
    API_CLIENT --> AUDIT
    API_CLIENT --> COMPLIANCE
    API_CLIENT --> LLM
    
    FE --> ADMIN
    FE --> DOC
    FE --> COMPLIANCE
    
    ADMIN --> PG_ADMIN
    ADMIN --> REDIS
    ADMIN --> AUDIT
    ADMIN --> KV
    
    DOC --> PG_DOC
    DOC --> BLOB
    DOC --> ADMIN
    DOC --> AUDIT
    DOC --> LLM
    DOC --> KV
    
    AUDIT --> PG_AUDIT
    AUDIT --> EVENT_HUB
    
    COMPLIANCE --> ADMIN
    COMPLIANCE --> DOC
    COMPLIANCE --> AUDIT
    
    LLM --> AI_SEARCH
    LLM --> AI_FOUNDRY
    LLM --> DOC
    LLM --> AUDIT
    
    ADMIN -.->|Uses| CORE
    DOC -.->|Uses| CORE
    AUDIT -.->|Uses| CORE
    COMPLIANCE -.->|Uses| CORE
    LLM -.->|Uses| CORE
```

---

## Microservices Architecture

### Service Independence

Each microservice is independently deployable with its own:
- Database schema
- Configuration
- Deployment artifacts
- Health checks

```mermaid
graph LR
    subgraph "Service Independence"
        S1[Service 1<br/>DB 1]
        S2[Service 2<br/>DB 2]
        S3[Service 3<br/>DB 3]
    end
    
    S1 -.->|HTTP REST| S2
    S2 -.->|HTTP REST| S3
    S1 -.->|HTTP REST| S3
```

### Service Communication

Services communicate via:
- **Synchronous**: REST APIs over HTTP/HTTPS
- **Asynchronous**: Azure Event Hubs (for audit events)
- **Service Discovery**: Kubernetes DNS

```mermaid
sequenceDiagram
    participant Client
    participant Frontend
    participant DocumentService
    participant AdminService
    participant AuditService
    participant BlobStorage
    
    Client->>Frontend: HTTP Request
    Frontend->>DocumentService: REST API Call
    DocumentService->>AdminService: Verify Application
    AdminService-->>DocumentService: Application Valid
    DocumentService->>BlobStorage: Upload Document
    BlobStorage-->>DocumentService: Upload Success
    DocumentService->>AuditService: Log Event
    AuditService-->>DocumentService: Event Logged
    DocumentService-->>Frontend: Document Created
    Frontend-->>Client: Response
```

---

## Service Details

### dms-core-service (Shared Library)

**Type**: Maven Library (JAR)  
**Purpose**: Common components shared across services

**Components**:
- Security configurations (JWT converter, base security)
- Key Vault integration
- Common utilities (JSON converter, credential service)
- Shared exceptions
- Common DTOs
- Audit event client

```mermaid
graph TD
    CORE[dms-core-service]
    
    CORE --> SEC[Security Config]
    CORE --> KV_INT[Key Vault Integration]
    CORE --> UTILS[Utilities]
    CORE --> EXCEPTIONS[Exceptions]
    CORE --> DTOS[DTOs]
    CORE --> CLIENT[Audit Client]
    
    SEC --> JWT[JWT Converter]
    SEC --> BASE[Base Security]
    
    UTILS --> JSON[JSON Converter]
    UTILS --> CRED[Credential Service]
```

### dms-admin-service

**Ports**: 8081 (dev/docker-compose), 8080 (production/Kubernetes)  
**Database**: `dms_admin` (PostgreSQL)  
**Purpose**: User, role, permission, and application management  
**POM Parent**: `com.davidparker.dms:dms-parent:1.0.0-SNAPSHOT`  
**Configuration**: Multiple profiles (dev, docker, prod, test)

**Endpoints**:
- `/api/v1/admin/users` - User management
- `/api/v1/admin/roles` - Role management
- `/api/v1/admin/permissions` - Permission management
- `/api/v1/admin/applications` - Application management
- `/api/v1/admin/dashboard` - Admin dashboard

```mermaid
graph LR
    ADMIN[dms-admin-service]
    
    ADMIN --> USER_MGMT[User Management]
    ADMIN --> ROLE_MGMT[Role Management]
    ADMIN --> PERM_MGMT[Permission Management]
    ADMIN --> APP_MGMT[Application Management]
    
    USER_MGMT --> DB[(dms_admin DB)]
    ROLE_MGMT --> DB
    PERM_MGMT --> DB
    APP_MGMT --> DB
```

### dms-document-service

**Ports**: 8083 (dev/docker-compose), 8080 (production/Kubernetes)  
**Database**: `dms_document` (PostgreSQL)  
**Purpose**: Document CRUD operations and versioning  
**POM Parent**: `com.davidparker.dms:dms-parent:1.0.0-SNAPSHOT`  
**Configuration**: Multiple profiles (dev, docker, prod, test)

**Endpoints**:
- `/api/v1/documents` - Document operations
- `/api/v1/documents/{id}/versions` - Version management
- `/api/v1/documents/{id}/download` - Document download

**Dependencies**:
- Azure Blob Storage (document storage)
- Admin Service (application verification)
- Audit Service (logging)
- LLM Service (embeddings)

```mermaid
graph TD
    DOC[dms-document-service]
    
    DOC --> UPLOAD[Upload Document]
    DOC --> DOWNLOAD[Download Document]
    DOC --> LIST[List Documents]
    DOC --> UPDATE[Update Metadata]
    DOC --> DELETE[Delete Document]
    DOC --> VERSION[Version Management]
    
    UPLOAD --> BLOB[Azure Blob Storage]
    DOWNLOAD --> BLOB
    LIST --> DB[(dms_document DB)]
    UPDATE --> DB
    DELETE --> BLOB
    DELETE --> DB
    VERSION --> DB
```

### dms-audit-service

**Ports**: 8082 (dev/docker-compose), 8080 (production/Kubernetes)  
**Database**: `dms_audit` (PostgreSQL, partitioned)  
**Purpose**: Centralized audit logging  
**POM Parent**: `com.davidparker.dms:dms-parent:1.0.0-SNAPSHOT`  
**Configuration**: Multiple profiles (dev, docker, prod, test)

**Features**:
- Receives audit events from all services
- Stores in partitioned PostgreSQL database
- Publishes to Azure Event Hubs
- Query API for audit logs

```mermaid
graph LR
    AUDIT[dms-audit-service]
    
    AUDIT --> RECEIVE[Receive Events]
    RECEIVE --> STORE[Store in DB]
    RECEIVE --> PUBLISH[Publish to Event Hub]
    
    STORE --> DB[(dms_audit DB<br/>Partitioned)]
    PUBLISH --> EVENT_HUB[Azure Event Hubs]
```

### dms-compliance-service

**Ports**: 8084 (dev/docker-compose), 8080 (production/Kubernetes)  
**Database**: None (stateless)  
**Purpose**: Compliance framework (PCI-DSS, GDPR, ISO 27001)  
**POM Parent**: `com.davidparker.dms:dms-parent:1.0.0-SNAPSHOT`  
**Configuration**: Multiple profiles (dev, docker, prod, test)

**Endpoints**:
- `/api/v1/compliance/pci/report` - PCI compliance reports
- `/api/v1/compliance/gdpr/data-subject/{id}` - GDPR data export
- `/api/v1/compliance/gdpr/data-subject/{id}` (DELETE) - GDPR erasure
- `/api/v1/compliance/iso27001/controls` - ISO 27001 controls

```mermaid
graph TD
    COMPLIANCE[dms-compliance-service]
    
    COMPLIANCE --> PCI[PCI-DSS]
    COMPLIANCE --> GDPR[GDPR]
    COMPLIANCE --> ISO[ISO 27001]
    
    PCI --> REPORT[Compliance Reports]
    GDPR --> EXPORT[Data Export]
    GDPR --> ERASURE[Data Erasure]
    ISO --> CONTROLS[Security Controls]
    
    REPORT --> AUDIT_SVC[Audit Service]
    EXPORT --> DOC_SVC[Document Service]
    EXPORT --> ADMIN_SVC[Admin Service]
    ERASURE --> DOC_SVC
    ERASURE --> ADMIN_SVC
    CONTROLS --> AUDIT_SVC
```

### dms-llm-service

**Ports**: 8085 (dev/docker-compose), 8080 (production/Kubernetes)  
**Database**: None (uses Azure AI Search)  
**Purpose**: AI/LLM integration for document queries  
**POM Parent**: `com.davidparker.dms:dms-parent:1.0.0-SNAPSHOT`  
**Configuration**: Multiple profiles (dev, docker, prod, test)

**Endpoints**:
- `/api/v1/llm/query` - Natural language document queries
- `/api/v1/llm/compliance-check` - Compliance-focused queries

**Dependencies**:
- Azure AI Search (vector search)
- Azure AI Foundry (LLM models)
- Document Service (document metadata)
- Audit Service (query logging)

```mermaid
graph LR
    LLM[dms-llm-service]
    
    LLM --> QUERY[Query Processing]
    QUERY --> EMBED[Generate Embeddings]
    EMBED --> SEARCH[Azure AI Search]
    SEARCH --> FOUNDRY[Azure AI Foundry]
    FOUNDRY --> RESPONSE[Generate Response]
    
    QUERY --> AUDIT[Audit Service]
    SEARCH --> DOC[Document Service]
```

### dms-frontend-service

**Port**: 8080 (all environments)  
**Type**: Angular 21 SPA  
**Purpose**: User interface for all DMS functionality  
**Entrypoint**: `entrypoint.sh` for nginx configuration substitution

**Features**:
- MSAL authentication (Azure AD)
- Document management UI
- Admin dashboard
- Compliance reporting UI
- LLM query interface

```mermaid
graph TD
    FE[dms-frontend-service<br/>Angular 21]
    
    FE --> AUTH[MSAL Auth]
    FE --> DOC_UI[Document UI]
    FE --> ADMIN_UI[Admin UI]
    FE --> COMPLIANCE_UI[Compliance UI]
    FE --> LLM_UI[LLM Query UI]
    
    AUTH --> AZURE_AD[Azure AD]
    DOC_UI --> DOC_API[Document API]
    ADMIN_UI --> ADMIN_API[Admin API]
    COMPLIANCE_UI --> COMPLIANCE_API[Compliance API]
    LLM_UI --> LLM_API[LLM API]
```

---

## Data Flow

### Document Upload Flow

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant DocumentService
    participant AdminService
    participant BlobStorage
    participant LLMService
    participant AuditService
    
    User->>Frontend: Upload Document
    Frontend->>DocumentService: POST /api/v1/documents
    DocumentService->>AdminService: Verify Application
    AdminService-->>DocumentService: Application Valid
    DocumentService->>BlobStorage: Upload File
    BlobStorage-->>DocumentService: File Uploaded
    DocumentService->>DocumentService: Save Metadata to DB
    DocumentService->>LLMService: Generate Embeddings
    LLMService-->>DocumentService: Embeddings Created
    DocumentService->>AuditService: Log Upload Event
    AuditService-->>DocumentService: Event Logged
    DocumentService-->>Frontend: Document Created
    Frontend-->>User: Success Message
```

### Document Query Flow (LLM)

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant LLMService
    participant AISearch
    participant AIFoundry
    participant DocumentService
    participant AuditService
    
    User->>Frontend: Enter Query
    Frontend->>LLMService: POST /api/v1/llm/query
    LLMService->>LLMService: Validate & Sanitize Query
    LLMService->>AISearch: Vector Search
    AISearch-->>LLMService: Relevant Documents
    LLMService->>DocumentService: Get Document Metadata
    DocumentService-->>LLMService: Metadata
    LLMService->>AIFoundry: Generate Answer
    AIFoundry-->>LLMService: Answer with Citations
    LLMService->>AuditService: Log Query
    AuditService-->>LLMService: Event Logged
    LLMService-->>Frontend: Query Response
    Frontend-->>User: Display Results
```

### User Management Flow

```mermaid
sequenceDiagram
    participant Admin
    participant Frontend
    participant AdminService
    participant Database
    participant AuditService
    
    Admin->>Frontend: Create User
    Frontend->>AdminService: POST /api/v1/admin/users
    AdminService->>AdminService: Validate Request
    AdminService->>Database: Check Duplicate Email
    Database-->>AdminService: No Duplicate
    AdminService->>Database: Create User
    Database-->>AdminService: User Created
    AdminService->>AuditService: Log User Creation
    AuditService-->>AdminService: Event Logged
    AdminService-->>Frontend: User Created
    Frontend-->>Admin: Success Message
```

---

## Security Architecture

### Authentication Flow

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AzureAD
    participant Backend
    participant KeyVault
    
    User->>Frontend: Access Application
    Frontend->>AzureAD: Redirect to Login
    AzureAD->>User: Login Form
    User->>AzureAD: Credentials
    AzureAD->>AzureAD: Authenticate
    AzureAD->>Frontend: JWT Token
    Frontend->>Backend: API Request + JWT
    Backend->>Backend: Validate JWT
    Backend->>KeyVault: Get Signing Keys
    KeyVault-->>Backend: Public Keys
    Backend->>Backend: Verify Token
    Backend-->>Frontend: Authorized Response
```

### Application Isolation

```mermaid
graph TD
    REQ[Incoming Request]
    REQ --> JWT[Extract JWT]
    JWT --> APP_ID[Extract Application ID]
    APP_ID --> FILTER[ApplicationIsolationFilter]
    FILTER --> CONTEXT[Set Application Context]
    CONTEXT --> SERVICE[Service Processing]
    SERVICE --> STORAGE[Application-Scoped Storage]
    SERVICE --> DB[Application-Scoped DB Queries]
    
    STORAGE --> BLOB[Blob Container<br/>app-{id}-documents]
    DB --> WHERE[WHERE application_id = {id}]
```

### RBAC Permission Flow

```mermaid
graph LR
    REQ[Request] --> AUTH[Authentication]
    AUTH --> JWT[JWT Token]
    JWT --> ROLES[Extract Roles]
    ROLES --> PERMS[Get Permissions]
    PERMS --> CHECK[Permission Check]
    CHECK -->|Allowed| ALLOW[Allow Request]
    CHECK -->|Denied| DENY[Deny Request]
    
    PERMS --> CACHE[Redis Cache]
    CACHE --> DB[(Permission DB)]
```

---

## Deployment Architecture

### Kubernetes Deployment

```mermaid
graph TB
    subgraph "Azure AKS Cluster"
        subgraph "Namespace: dms"
            subgraph "Frontend Deployment"
                FE_POD1[Frontend Pod 1]
                FE_POD2[Frontend Pod 2]
                FE_SVC[Frontend Service]
            end
            
            subgraph "Admin Service Deployment"
                ADMIN_POD1[Admin Pod 1]
                ADMIN_POD2[Admin Pod 2]
                ADMIN_SVC[Admin Service]
            end
            
            subgraph "Document Service Deployment"
                DOC_POD1[Document Pod 1]
                DOC_POD2[Document Pod 2]
                DOC_SVC[Document Service]
            end
            
            subgraph "Other Services"
                AUDIT_SVC[Audit Service]
                COMPLIANCE_SVC[Compliance Service]
                LLM_SVC[LLM Service]
            end
        end
        
        subgraph "Istio Components"
            ISTIO_GW[Istio Gateway]
            ISTIO_VS[Virtual Service]
            ISTIO_DR[Destination Rule]
            ISTIO_AUTH[Authorization Policy]
        end
    end
    
    INGRESS[Ingress Load Balancer] --> ISTIO_GW
    ISTIO_GW --> ISTIO_VS
    ISTIO_VS --> FE_SVC
    FE_SVC --> FE_POD1
    FE_SVC --> FE_POD2
    
    FE_POD1 --> ISTIO_DR
    FE_POD2 --> ISTIO_DR
    ISTIO_DR --> ADMIN_SVC
    ISTIO_DR --> DOC_SVC
    ISTIO_DR --> AUDIT_SVC
    ISTIO_DR --> COMPLIANCE_SVC
    ISTIO_DR --> LLM_SVC
    
    ISTIO_AUTH --> ADMIN_SVC
    ISTIO_AUTH --> DOC_SVC
```

### Flux GitOps Flow

```mermaid
graph LR
    GIT[Git Repository]
    GIT --> FLUX[Flux Controller]
    FLUX --> K8S[Kubernetes API]
    K8S --> DEPLOY[Deploy Services]
    
    GIT -->|Manifests| FLUX
    FLUX -->|Sync| K8S
    K8S -->|Apply| DEPLOY
```

---

## Integration Patterns

### Service-to-Service Communication

```mermaid
graph TD
    S1[Service 1] -->|HTTP REST| ISTIO[Istio Service Mesh]
    ISTIO -->|mTLS| S2[Service 2]
    
    ISTIO --> AUTH[Authentication]
    ISTIO --> ROUTING[Traffic Routing]
    ISTIO --> CIRCUIT[Circuit Breaker]
    ISTIO --> RETRY[Retry Logic]
```

### Event-Driven Architecture

```mermaid
graph LR
    S1[Service 1] -->|Publish| EVENT_HUB[Azure Event Hubs]
    S2[Service 2] -->|Publish| EVENT_HUB
    S3[Service 3] -->|Publish| EVENT_HUB
    
    EVENT_HUB -->|Subscribe| CONSUMER1[Consumer 1]
    EVENT_HUB -->|Subscribe| CONSUMER2[Consumer 2]
    EVENT_HUB -->|Subscribe| CONSUMER3[Consumer 3]
```

### Database Per Service Pattern

```mermaid
graph TD
    ADMIN_SVC[Admin Service] --> ADMIN_DB[(Admin DB)]
    DOC_SVC[Document Service] --> DOC_DB[(Document DB)]
    AUDIT_SVC[Audit Service] --> AUDIT_DB[(Audit DB)]
    
    ADMIN_DB -.->|Independent| DOC_DB
    DOC_DB -.->|Independent| AUDIT_DB
```

---

## Configuration Patterns

### Environment Variables

All services use standardized LV environment variables:

**Database Configuration**:
- `SPRING_DATASOURCE_URL` - Full JDBC connection string
- `DB_USER` - Database username
- `DB_PASSWORD` - Database password

**Service URLs**:
- `ADMIN_SERVICE_URL` - Admin service endpoint
- `AUDIT_SERVICE_URL` - Audit service endpoint
- `DOCUMENT_SERVICE_URL` - Document service endpoint
- `COMPLIANCE_SERVICE_URL` - Compliance service endpoint
- `LLM_SERVICE_URL` - LLM service endpoint

**Azure AD Configuration**:
- `AZURE_TENANT_ID` - Azure AD tenant ID
- `AZURE_CLIENT_ID` - Azure AD client ID
- `AZURE_JWK_SET_URI` - JWK Set URI for token validation

### Application Profiles

Each service supports multiple Spring profiles:

- **dev** - Local development with H2 in-memory database, authentication bypass
- **docker** - Docker Compose environment with PostgreSQL, authentication bypass
- **prod** - Production/Kubernetes with full security, PostgreSQL, Azure AD
- **test** - Test environment with H2 in-memory database

### POM Inheritance

All services inherit from the parent POM:
```xml
<parent>
    <groupId>com.davidparker.dms</groupId>
    <artifactId>dms-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

### Dockerfile Patterns

Each service has three Dockerfile variants:
- **Dockerfile** - Standard multi-stage build for docker-compose
- **Dockerfile.dev** - Development build with docker profile
- **Dockerfile.prod** - Production build with OpenTelemetry, expects pre-built JAR

## Technology Stack Summary

| Component | Technology |
|-----------|-----------|
| **Language** | Java 21 LTS |
| **Framework** | Spring Boot 3.4.x |
| **Frontend** | Angular 21 |
| **Database** | PostgreSQL 16.x |
| **Cache** | Redis 7.x |
| **Container Orchestration** | Kubernetes 1.28+ |
| **Service Mesh** | Istio |
| **GitOps** | Flux |
| **Cloud Platform** | Azure AKS |
| **Storage** | Azure Blob Storage |
| **AI Services** | Azure AI Search, Azure AI Foundry |
| **Messaging** | Azure Event Hubs |
| **Secrets** | Azure Key Vault |
| **Build Tool** | Maven (parent POM pattern) |
| **Observability** | OpenTelemetry (production) |

---

*Last Updated: 2025-01-18T00:00:00Z*  
*Updated By: davidparker-lv-bmth*
