---
name: DMS Implementation Plan
overview: "Convert the DMS architecture document into a detailed, actionable implementation plan with 50+ granular tasks organized across 5 phases: Foundation & Security, Compliance Framework, AI Integration, Frontend & APIs, and Production Readiness."
todos: []
---

# DMS Implementation Plan

## Overview

This plan converts the architecture document ([PLAN.md](PLAN.md)) into actionable implementation tasks. The system will be built using Java 25 LTS, Spring Boot 3.4.x, Angular 25, PostgreSQL 16, and Azure services, with full PCI-DSS, ISO 27001, and GDPR compliance.

## Phase 1: Foundation & Security

### 1.1 Azure Infrastructure Setup

- Create Azure Resource Group for DMS environment
- Provision Azure Key Vault (`dms-keyvault-{env}`) with access policies
- Create Azure Blob Storage account (`dmsstorage{env}`) with hierarchical namespace
- Set up Azure PostgreSQL 16 database with private endpoint
- Provision Azure Redis 7 cache instance
- Create Azure Event Hubs namespace for audit log streaming
- Configure Azure AI Search service for vector indexing
- Set up Azure API Management instance for API gateway

### 1.2 Azure AD / Entra ID App Registrations

- Register DMS API application (`dms-api-{env}`) with API scopes:
- `dms.documents.read`
- `dms.documents.write`
- `dms.documents.delete`
- `dms.admin`
- `dms.llm.query`
- Register DMS Web App (Angular SPA) with Auth Code + PKCE flow
- Register davidparker-lv-bmth service client application (Confidential Client)
- Register Azure AI Foundry service application (`dms-ai-foundry-{env}`)
- Configure app roles: `DMS.davidparker-lv-bmth`, `DMS.LLM.Service`, `DMS.Admin`
- Set up service principal for davidparker-lv-bmth with client credentials flow
- Configure custom claims for application isolation (dms_application_id, dms_storage_container)

### 1.3 Key Vault Secrets Configuration

- Store PostgreSQL connection string in Key Vault
- Store Redis connection string in Key Vault
- Create and store blob storage account key for davidparker-lv-bmth
- Store Azure AI Search API key
- Store Azure OpenAI API key for embeddings
- Store Event Hubs connection string
- Store davidparker-lv-bmth client secret
- Store LLM service client secret
- Create document encryption key (CMK) in Key Vault
- Create audit log encryption key (CMK) in Key Vault
- Create backup encryption key in Key Vault
- Configure Key Vault access policies for DMS API Managed Identity

### 1.4 Spring Boot Project Setup

- Initialize Spring Boot 3.4.x project with Java 25 LTS
- Configure Spring Security with OAuth2 Resource Server
- Set up Azure Key Vault integration using Spring Cloud Azure
- Configure PostgreSQL datasource with connection pooling
- Set up Redis for session management and caching
- Implement JWT authentication converter for Azure AD tokens
- Create ApplicationIsolationFilter for request scoping
- Configure application properties YAML with Key Vault references
- Set up logging framework (Logback/SLF4J) with structured logging
- Configure health check endpoints (`/api/v1/health`)

### 1.5 Storage Container Provisioning

- Create `davidparker-lv-bmth-documents` container in Blob Storage
- Create `shared-templates` container (read-only for all apps)
- Create `audit-logs-archive` container with immutable policy
- Configure container-level encryption with CMK for davidparker-lv-bmth
- Set up RBAC for davidparker-lv-bmth service principal on storage containers
- Configure blob lifecycle policies for archival
- Set up blob versioning and soft delete

### 1.6 Database Schema Implementation

- Create `registered_applications` table with UUID primary key
- Create `documents` table with application_id foreign key
- Create `audit_logs` table with partitioning by timestamp
- Create `users` table for user management
- Create `roles` and `permissions` tables for RBAC
- Create `document_permissions` junction table
- Create indexes: `idx_documents_application`, `idx_documents_classification`
- Create indexes on audit_logs for query performance
- Set up database migrations using Flyway or Liquibase
- Configure row-level security policies for audit_logs (immutable)

### 1.7 Application Registration Service

- Create `RegisteredApplication` entity class
- Create `RegisteredApplicationRepository` interface
- Implement `ApplicationProvisioningService` with container creation
- Implement encryption key creation in Key Vault
- Implement storage RBAC assignment
- Create `ApplicationContext` thread-local storage
- Implement `ApplicationContextFilter` for request scoping
- Create `ApplicationScopedStorageService` for container access

### 1.8 Basic RBAC Implementation

- Create `Permission` entity and repository
- Create `Role` entity with role hierarchy
- Implement `PermissionService` for permission checks
- Create Spring Security method security annotations
- Implement `@PreAuthorize` checks for document operations
- Create role-based access control for API endpoints
- Implement application-scoped permission filtering
- Create permission caching in Redis

## Phase 2: Compliance Framework

### 2.1 Audit Logging System

- Implement `AuditEvent` entity with all required fields
- Create `AuditService` for event logging
- Implement audit event publishing to Event Hubs
- Create audit log repository with immutable constraints
- Implement checksum calculation (SHA-256) for audit integrity
- Set up audit log partitioning strategy (monthly partitions)
- Create audit log archival job to Blob Storage
- Implement audit log retention policy (7 years)
- Create audit log query API with filtering
- Implement audit log export functionality

### 2.2 PCI-DSS Controls

- Create `PciDssComplianceService` component
- Implement `CardholderDataProtection` service
- Create document content scanner for PCI data detection
- Implement PCI classification logic
- Create `PciAuditAspect` for automatic PCI access logging
- Implement PCI encryption level enforcement
- Create PCI access restriction service
- Implement PCI data masking for logs
- Create PCI compliance reporting API

### 2.3 GDPR Implementation

- Create `GdprComplianceService` with erasure request processing
- Implement `processErasureRequest` method with legal hold checks
- Implement `exportDataSubjectData` for data portability
- Create document anonymization service
- Implement PII detection and classification
- Create GDPR data subject access request (DSAR) API
- Implement consent management tracking
- Create processing activity records generation (Article 30)
- Schedule monthly processing records report
- Implement data retention policy enforcement

### 2.4 ISO 27001 Security Controls

- Create `Iso27001Controls` configuration class
- Implement `AccessControlPolicy` with MFA requirements
- Configure session timeout (30 minutes)
- Implement failed login attempt tracking and lockout
- Create `CryptographyPolicy` with AES-256-GCM
- Configure key rotation period (annual)
- Implement `SecurityMonitoringConfig` with real-time alerts
- Set up vulnerability scanning schedule
- Create ISO 27001 control status dashboard API
- Implement security incident response procedures

### 2.5 Compliance Reporting APIs

- Create `/api/v1/compliance/pci/report` endpoint
- Create `/api/v1/compliance/gdpr/data-subject/{id}` endpoints
- Create `/api/v1/compliance/iso27001/controls` endpoint
- Implement compliance report generation logic
- Create report export functionality (PDF/JSON)
- Implement report scheduling and delivery

## Phase 3: AI Integration

### 3.1 Azure AI Search Setup

- Create Azure AI Search service instance
- Define search index schema with vector fields
- Configure semantic ranking configuration
- Set up vector search configuration for embeddings
- Create indexer for document metadata
- Configure search index with application_id field for filtering
- Set up search index with classification field
- Test search index with sample documents

### 3.2 Document Embedding Pipeline

- Create `DocumentEmbeddingService` class
- Implement text extraction from documents (PDF, Word, etc.)
- Create document chunking service with configurable chunk size
- Integrate Azure OpenAI for embedding generation (text-embedding-ada-002)
- Implement embedding storage in search index
- Create event listener for `DocumentUploadedEvent`
- Implement async processing for embeddings
- Add error handling and retry logic for embedding failures
- Create embedding status tracking in database

### 3.3 Azure AI Foundry Integration

- Register Azure AI Foundry application in Entra ID
- Configure Azure AI Foundry client credentials
- Create `SecureLlmQueryService` class
- Implement query intent analysis using LLM
- Create query parameter extraction from natural language
- Implement secure search options builder with application isolation
- Create vector search query builder
- Implement hybrid search (vector + keyword)
- Add RBAC filtering to search results
- Implement result summarization with citations

### 3.4 LLM Query API

- Create `/api/v1/llm/query` POST endpoint
- Create `/api/v1/llm/compliance-check` POST endpoint
- Implement `LlmQueryRequest` and `LlmQueryResponse` DTOs
- Create OpenAPI 3.0 specification for LLM endpoints
- Implement query validation and sanitization
- Create content filter to prevent prompt injection
- Implement rate limiting per application (100 queries/min)
- Add query correlation ID tracking
- Implement comprehensive LLM interaction auditing

### 3.5 LLM Security Controls

- Create `LlmSecurityConfig` configuration class
- Implement `ContentFilter` with blocked patterns
- Create rate limiter with per-application, per-user, and global limits
- Implement query length validation (max 2000 chars)
- Create `LlmAuditService` for interaction logging
- Implement token usage tracking
- Add content filtering audit logging
- Create LLM query monitoring dashboard

## Phase 4: Frontend & APIs

### 4.1 Angular Application Setup

- Initialize Angular 25 project with TypeScript
- Install and configure Angular Material 25
- Set up NgRx 18 for state management
- Install MSAL Angular 3.x for authentication
- Configure MSAL PublicClientApplication with Azure AD settings
- Set up routing with route guards
- Configure HTTP interceptors for authentication
- Create environment configuration files

### 4.2 MSAL Authentication Integration

- Configure MSAL_INSTANCE provider
- Set up MSAL_GUARD_CONFIG with redirect flow
- Implement login/logout functionality
- Create token refresh interceptor
- Implement role-based route guards
- Create authentication state management in NgRx
- Handle authentication errors and redirects
- Implement session management

### 4.3 Document Management UI

- Create document list component with pagination
- Implement document upload component with drag-and-drop
- Create document detail view component
- Implement document search interface
- Create document metadata editing form
- Implement document classification selector
- Create document version history viewer
- Implement document download functionality
- Add document preview component

### 4.4 Admin Dashboard

- Create admin dashboard layout
- Implement user management interface
- Create role and permission management UI
- Implement application registration interface
- Create audit log viewer with filtering
- Implement compliance report viewer
- Create system configuration interface
- Add monitoring and metrics dashboard

### 4.5 Compliance Reporting UI

- Create PCI-DSS compliance report viewer
- Implement GDPR data subject request interface
- Create ISO 27001 controls status dashboard
- Implement compliance report export functionality
- Create audit log search and filter interface
- Add compliance metrics visualization

### 4.6 OpenAPI Documentation

- Generate OpenAPI 3.0 specification from Spring Boot annotations
- Document all API endpoints with request/response schemas
- Add authentication requirements to OpenAPI spec
- Create API documentation site using Swagger UI
- Document error responses and status codes
- Add example requests and responses
- Create API client SDK generation configuration

### 4.7 API Implementation

- Implement `/api/v1/documents` GET endpoint (list with pagination)
- Implement `/api/v1/documents` POST endpoint (upload)
- Implement `/api/v1/documents/{id}` GET endpoint (detail)
- Implement `/api/v1/documents/{id}` PUT endpoint (update)
- Implement `/api/v1/documents/{id}` DELETE endpoint
- Implement `/api/v1/documents/{id}/download` endpoint
- Implement `/api/v1/documents/{id}/versions` endpoint
- Create document metadata update endpoints
- Implement document classification update endpoint

## Phase 5: Production Readiness

### 5.1 Performance Optimization

- Implement database query optimization
- Add Redis caching for frequently accessed data
- Optimize document upload/download with streaming
- Implement connection pooling tuning
- Add response compression (GZIP)
- Optimize search queries with proper indexing
- Implement pagination for all list endpoints
- Add database query performance monitoring
- Create performance testing suite

### 5.2 Security Hardening

- Conduct security code review
- Implement input validation and sanitization
- Add SQL injection prevention measures
- Implement XSS protection
- Configure CORS policies
- Set up rate limiting on all endpoints
- Implement request size limits
- Add security headers (HSTS, CSP, etc.)
- Configure TLS 1.3 only

### 5.3 Penetration Testing

- Engage security testing team
- Perform vulnerability scanning
- Conduct OWASP Top 10 testing
- Test authentication and authorization bypass attempts
- Test for injection vulnerabilities
- Perform API security testing
- Test for sensitive data exposure
- Conduct security audit review

### 5.4 Compliance Audit Preparation

- Prepare PCI-DSS compliance documentation
- Prepare ISO 27001 control evidence
- Prepare GDPR compliance documentation
- Create compliance audit trail reports
- Document security controls implementation
- Prepare data flow diagrams
- Create incident response procedures documentation
- Prepare access control documentation

### 5.5 Disaster Recovery

- Create database backup strategy (RPO: 1 hour)
- Implement automated backup scheduling
- Set up cross-region backup replication
- Create disaster recovery runbook
- Test backup restoration procedures (RTO: 4 hours)
- Implement failover procedures
- Create disaster recovery testing schedule
- Document recovery time objectives

### 5.6 Monitoring & Alerting

- Set up Azure Monitor for application metrics
- Configure Log Analytics workspace
- Create performance monitoring dashboards
- Set up alert rules for critical errors
- Implement health check monitoring
- Create audit log monitoring alerts
- Set up security incident alerts
- Configure availability monitoring (99.9% target)

### 5.7 Documentation

- Create system architecture documentation
- Write API user guide
- Create deployment guide
- Write operations runbook
- Create troubleshooting guide
- Document configuration parameters
- Create user training materials
- Write developer onboarding guide

## Key Implementation Notes

- All application identifiers must use `davidparker-lv-bmth` as specified in [.cursorrules](.cursorrules)
- GitHub user for all operations: `davidparker-lv-bmth`
- Storage container: `davidparker-lv-bmth-documents`
- Application role: `DMS.davidparker-lv-bmth`
- All secrets in Key Vault should be prefixed with `davidparker-lv-bmth-` where applicable

## Success Criteria

- All API endpoints respond within 200ms (p95)
- LLM queries complete within 3 seconds
- System availability: 99.9%
- All audit logs retained for 7 years
- Full PCI-DSS, ISO 27001, and GDPR compliance
- All security controls implemented and tested