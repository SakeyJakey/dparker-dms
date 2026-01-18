---
Last Updated: 2024-01-18T13:32:00Z
Updated By: davidparker-lv-bmth
---

# Document Management System (DMS) - Architecture Plan

## Executive Summary

This document outlines the architecture and implementation plan for an extensible, enterprise-grade Document Management System (DMS). The system is designed with security-first principles, featuring Role-Based Access Control (RBAC), comprehensive audit logging, multi-application segregated storage, and AI-powered document querying via Azure AI Foundry. The solution is fully compliant with **PCI-DSS**, **ISO 27001**, and **GDPR** requirements.

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Technology Stack](#2-technology-stack)
3. [Architecture Design](#3-architecture-design)
4. [Security & RBAC](#4-security--rbac)
5. [Azure AD & Entra ID Authentication](#5-azure-ad--entra-id-authentication)
6. [Azure Key Vault Integration](#6-azure-key-vault-integration)
7. [Multi-Application Segregated Storage](#7-multi-application-segregated-storage)
8. [Azure AI Foundry LLM Integration](#8-azure-ai-foundry-llm-integration)
9. [Audit Trail System](#9-audit-trail-system)
10. [Compliance Framework](#10-compliance-framework-pci-dss-iso-27001-gdpr)
11. [API Design (OpenAPI 3.0)](#11-api-design-openapi-30)
12. [Storage Architecture](#12-storage-architecture)
13. [Frontend Design](#13-frontend-design)
14. [Service Integration](#14-service-integration)
15. [Data Models](#15-data-models)
16. [Implementation Phases](#16-implementation-phases)
17. [Non-Functional Requirements](#17-non-functional-requirements)

---

## 1. System Overview

### 1.1 Purpose

The Document Management System (DMS) provides a centralized, secure platform for storing, organizing, retrieving, and managing documents across the enterprise. It supports multi-tenant, multi-application access with fine-grained permissions, complete audit trails, and AI-powered document querying for compliance and discovery.

### 1.2 Key Features

- **Document Lifecycle Management**: Upload, version, archive, and delete documents
- **Role-Based Access Control (RBAC)**: Granular permissions at document, folder, and system levels
- **Multi-Application Segregated Storage**: Complete isolation between applications accessing the DMS
- **Full Audit Trail**: Comprehensive logging of all document access and modifications
- **AI-Powered Document Query**: Azure AI Foundry integration for natural language document search
- **Compliance-Ready**: PCI-DSS, ISO 27001, and GDPR compliant architecture
- **Azure Key Vault Security**: Centralized secrets management for all service credentials
- **Application Integration**: RESTful API for seamless service-to-service communication
- **Search & Discovery**: Full-text search, semantic search, and metadata-based discovery

### 1.3 Stakeholders

| Stakeholder | Role | Needs |
|-------------|------|-------|
| End Users | Document consumers/creators | Easy upload, search, download |
| Administrators | System managers | User management, permission control |
| Application Services | Automated systems | API access, segregated storage |
| AI/LLM Services | Compliance queries | Secure document retrieval |
| Compliance Officers | Auditors | Audit reports, access history |
| Security Team | Security oversight | Key management, encryption |

---

## 2. Technology Stack

### 2.1 Backend

| Component | Technology | Version | Justification |
|-----------|------------|---------|---------------|
| Runtime | Java | 25 LTS | Latest LTS with virtual threads, pattern matching |
| Framework | Spring Boot | 3.4.x | Enterprise-grade, extensive ecosystem |
| API Documentation | OpenAPI | 3.0 | Industry standard, tooling support |
| Security | Spring Security | 6.x | Comprehensive security framework |
| Database | PostgreSQL | 16.x | ACID compliance, JSON support, audit capabilities |
| Caching | Redis | 7.x | Session management, permission caching |
| Search | Azure AI Search | Latest | Vector search, semantic ranking for LLM |
| Message Queue | Azure Event Hubs | Latest | Event streaming, audit log ingestion |

### 2.2 Frontend

| Component | Technology | Version | Justification |
|-----------|------------|---------|---------------|
| Framework | Angular | 25.x | Enterprise-ready, TypeScript-native |
| UI Components | Angular Material | 25.x | Consistent design system |
| State Management | NgRx | 18.x | Predictable state management |
| Auth Library | MSAL Angular | 3.x | Azure AD integration |

### 2.3 Azure Infrastructure

| Component | Technology | Purpose |
|-----------|------------|---------|
| Object Storage | Azure Blob Storage | Segregated document storage per application |
| Identity | Azure AD / Entra ID | Enterprise SSO, App Registrations |
| Secrets | Azure Key Vault | All credentials, keys, certificates |
| AI Services | Azure AI Foundry | LLM-powered document queries |
| Vector Search | Azure AI Search | Embeddings and semantic search |
| API Gateway | Azure API Management | Rate limiting, authentication |
| Container Runtime | Azure Kubernetes Service | Scalable deployment |

---

## 3. Architecture Design

### 3.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                    CLIENTS                                       │
├──────────────┬──────────────┬──────────────┬──────────────┬─────────────────────┤
│  Angular     │  Application │  Application │  Azure AI    │  Third-Party        │
│  Web App     │  Service A   │  Service B   │  Foundry LLM │  Integrations       │
└──────┬───────┴──────┬───────┴──────┬───────┴──────┬───────┴──────────┬──────────┘
       │              │              │              │                   │
       ▼              ▼              ▼              ▼                   ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            AZURE API MANAGEMENT                                  │
│  • Rate Limiting  • OAuth2 Validation  • App Isolation  • Audit Logging         │
└─────────────────────────────────────┬───────────────────────────────────────────┘
                                      │
┌─────────────────────────────────────▼───────────────────────────────────────────┐
│                              SECURITY LAYER                                      │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐  ┌───────────────┐  │
│  │ Azure AD/Entra │  │ JWT Validation │  │ RBAC Engine    │  │ Key Vault     │  │
│  │ ID Apps        │  │ + App Identity │  │ + App Scoping  │  │ Integration   │  │
│  └────────────────┘  └────────────────┘  └────────────────┘  └───────────────┘  │
└─────────────────────────────────────┬───────────────────────────────────────────┘
                                      │
┌─────────────────────────────────────▼───────────────────────────────────────────┐
│                         DMS API LAYER (Spring Boot)                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐ │
│  │ Document API │  │ Admin API    │  │ Audit API    │  │ LLM Query API        │ │
│  │ (App-Scoped) │  │              │  │              │  │ (Compliance Search)  │ │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────────────┘ │
└─────────────────────────────────────┬───────────────────────────────────────────┘
                                      │
┌─────────────────────────────────────▼───────────────────────────────────────────┐
│                              SERVICE LAYER                                       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌────────────┐ │
│  │ Document    │ │ Permission  │ │ Audit       │ │ AI Query    │ │ Embedding  │ │
│  │ Service     │ │ Service     │ │ Service     │ │ Service     │ │ Service    │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ └────────────┘ │
└─────────────────────────────────────┬───────────────────────────────────────────┘
                                      │
┌─────────────────────────────────────▼───────────────────────────────────────────┐
│                               DATA LAYER                                         │
│  ┌────────────────┐  ┌─────────────────────────────────────────────────────────┐│
│  │  PostgreSQL    │  │            AZURE BLOB STORAGE (Segregated)              ││
│  │  - Metadata    │  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        ││
│  │  - Permissions │  │  │ App-A       │ │ App-B       │ │ App-N       │        ││
│  │  - Audit Logs  │  │  │ Container   │ │ Container   │ │ Container   │        ││
│  │  - App Config  │  │  │ (Isolated)  │ │ (Isolated)  │ │ (Isolated)  │        ││
│  └────────────────┘  │  └─────────────┘ └─────────────┘ └─────────────┘        ││
│                      └─────────────────────────────────────────────────────────┘│
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐                     │
│  │ Azure AI Search│  │ Azure Key      │  │ Azure Event    │                     │
│  │ (Vector Index) │  │ Vault          │  │ Hubs           │                     │
│  └────────────────┘  └────────────────┘  └────────────────┘                     │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Security & RBAC

### 4.1 Multi-Layer Security Model

```
┌─────────────────────────────────────────────────────────────────┐
│                    SECURITY LAYERS                               │
├─────────────────────────────────────────────────────────────────┤
│  Layer 1: Network Security                                       │
│  • Azure Private Endpoints                                       │
│  • Network Security Groups                                       │
│  • Azure Firewall / WAF                                          │
├─────────────────────────────────────────────────────────────────┤
│  Layer 2: Identity & Authentication                              │
│  • Azure AD / Entra ID                                           │
│  • OAuth 2.0 / OIDC                                              │
│  • Managed Identities for Services                               │
├─────────────────────────────────────────────────────────────────┤
│  Layer 3: Authorization (RBAC)                                   │
│  • Role-based permissions                                        │
│  • Application-scoped access                                     │
│  • Document-level permissions                                    │
├─────────────────────────────────────────────────────────────────┤
│  Layer 4: Data Protection                                        │
│  • Azure Key Vault managed keys                                  │
│  • Encryption at rest (AES-256)                                  │
│  • Encryption in transit (TLS 1.3)                               │
├─────────────────────────────────────────────────────────────────┤
│  Layer 5: Audit & Monitoring                                     │
│  • Complete audit trail                                          │
│  • Azure Monitor / Log Analytics                                 │
│  • Real-time alerting                                            │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 RBAC Model with Application Scoping

```
┌─────────────────────────────────────────────────────────────────┐
│                    PERMISSION HIERARCHY                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  GLOBAL SCOPE (System Administrators)                            │
│      │                                                           │
│      ├── APPLICATION SCOPE (Per Entra ID App Registration)       │
│      │       │                                                   │
│      │       ├── App-A Documents Only                            │
│      │       ├── App-B Documents Only                            │
│      │       └── App-N Documents Only                            │
│      │                                                           │
│      ├── DEPARTMENT SCOPE                                        │
│      │       │                                                   │
│      │       ├── FOLDER SCOPE                                    │
│      │       │       └── DOCUMENT SCOPE                          │
│      │       └── PROJECT SCOPE                                   │
│      │                                                           │
│      └── CLASSIFICATION SCOPE (PCI, Confidential, etc.)          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 4.3 Role Definitions

| Role | Scope | Permissions |
|------|-------|-------------|
| System Administrator | Global | Full access to all resources |
| Application Admin | Application | Manage app-specific documents and users |
| Compliance Officer | Global (Read) | View audit logs, run compliance queries |
| LLM Query Service | Application | Read documents for AI queries (no download) |
| Department Admin | Department | Manage department documents |
| Contributor | Folder/Project | Create, update own documents |
| Viewer | Document/Folder | Read-only access |

---

## 5. Azure AD & Entra ID Authentication

### 5.1 App Registration Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         AZURE ENTRA ID TENANT                                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                    DMS API App Registration                              │    │
│  │  App ID: dms-api-{env}                                                   │    │
│  │  Purpose: Backend API authentication                                     │    │
│  │  Expose API Scopes:                                                      │    │
│  │    • dms.documents.read                                                  │    │
│  │    • dms.documents.write                                                 │    │
│  │    • dms.documents.delete                                                │    │
│  │    • dms.admin                                                           │    │
│  │    • dms.llm.query (for AI services)                                     │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                  │
│  ┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐           │
│  │ DMS Web App       │  │ Application A     │  │ Application B     │           │
│  │ (Angular SPA)     │  │ (Service Client)  │  │ (Service Client)  │           │
│  │                   │  │                   │  │                   │           │
│  │ Type: SPA         │  │ Type: Confidential│  │ Type: Confidential│           │
│  │ Flow: Auth Code   │  │ Flow: Client Cred │  │ Flow: Client Cred │           │
│  │       + PKCE      │  │                   │  │                   │           │
│  │                   │  │ App Roles:        │  │ App Roles:        │           │
│  │ Permissions:      │  │ • DMS.App.A       │  │ • DMS.App.B       │           │
│  │ • User.Read       │  │                   │  │                   │           │
│  │ • dms.documents.* │  │ Permissions:      │  │ Permissions:      │           │
│  └───────────────────┘  │ • dms.documents.* │  │ • dms.documents.* │           │
│                         │ • dms.llm.query   │  │ • dms.llm.query   │           │
│                         └───────────────────┘  └───────────────────┘           │
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                    Azure AI Foundry App Registration                     │    │
│  │  App ID: dms-ai-foundry-{env}                                            │    │
│  │  Purpose: LLM service authentication for document queries                │    │
│  │  Type: Confidential Client                                               │    │
│  │  Flow: Client Credentials                                                │    │
│  │  Permissions: dms.llm.query, dms.documents.read                          │    │
│  │  App Role: DMS.LLM.Service                                               │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 5.2 Token Claims for Application Isolation

```json
{
  "aud": "api://dms-api-prod",
  "iss": "https://login.microsoftonline.com/{tenant-id}/v2.0",
  "azp": "application-a-client-id",
  "roles": ["DMS.App.A", "DMS.Documents.ReadWrite"],
  "app_id": "application-a-client-id",
  "app_displayname": "Application A",
  "custom_claims": {
    "dms_application_id": "app-a-uuid",
    "dms_storage_container": "app-a-documents",
    "dms_allowed_classifications": ["INTERNAL", "CONFIDENTIAL"]
  }
}
```

### 5.3 Spring Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(aadJwtConverter())
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/health").permitAll()
                .requestMatchers("/api/v1/llm/**").hasRole("DMS.LLM.Service")
                .requestMatchers("/api/v1/admin/**").hasRole("DMS.Admin")
                .requestMatchers("/api/v1/documents/**").hasAnyRole("DMS.App.A", "DMS.App.B", "DMS.User")
                .anyRequest().authenticated()
            )
            .addFilterBefore(applicationIsolationFilter(), UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .build();
    }
    
    @Bean
    public ApplicationIsolationFilter applicationIsolationFilter() {
        // Ensures requests only access documents within their application scope
        return new ApplicationIsolationFilter(applicationContextService);
    }
}
```

---

## 6. Azure Key Vault Integration

### 6.1 Key Vault Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          AZURE KEY VAULT                                         │
│                    (dms-keyvault-{environment})                                  │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  SECRETS                                                                         │
│  ├── database-connection-string          # PostgreSQL connection                 │
│  ├── redis-connection-string             # Redis cache connection                │
│  ├── storage-account-key-app-a           # App A blob storage key               │
│  ├── storage-account-key-app-b           # App B blob storage key               │
│  ├── azure-ai-search-api-key             # AI Search admin key                  │
│  ├── azure-openai-api-key                # Azure OpenAI for embeddings          │
│  ├── event-hubs-connection-string        # Audit event streaming                │
│  ├── application-a-client-secret         # App A service principal              │
│  ├── application-b-client-secret         # App B service principal              │
│  └── llm-service-client-secret           # AI Foundry service principal         │
│                                                                                  │
│  KEYS                                                                            │
│  ├── document-encryption-key             # CMK for document encryption          │
│  ├── audit-log-encryption-key            # CMK for audit log encryption         │
│  └── backup-encryption-key               # Backup encryption key                │
│                                                                                  │
│  CERTIFICATES                                                                    │
│  ├── api-tls-certificate                 # API TLS certificate                  │
│  └── client-auth-certificate             # mTLS client cert (optional)          │
│                                                                                  │
│  ACCESS POLICIES                                                                 │
│  ├── DMS API Managed Identity            # Get secrets, unwrap keys             │
│  ├── DMS Admin Group                     # Full access for operations           │
│  └── Azure DevOps Service Connection     # Deploy-time secret access            │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 6.2 Key Vault Configuration

```java
@Configuration
public class KeyVaultConfig {
    
    @Bean
    public SecretClient secretClient() {
        return new SecretClientBuilder()
            .vaultUrl("https://dms-keyvault-${env}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    }
    
    @Bean
    public KeyClient keyClient() {
        return new KeyClientBuilder()
            .vaultUrl("https://dms-keyvault-${env}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    }
}

@Service
public class SecureCredentialService {
    
    private final SecretClient secretClient;
    private final LoadingCache<String, String> secretCache;
    
    public SecureCredentialService(SecretClient secretClient) {
        this.secretClient = secretClient;
        this.secretCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .build(this::fetchSecret);
    }
    
    public String getSecret(String secretName) {
        return secretCache.get(secretName);
    }
    
    private String fetchSecret(String secretName) {
        KeyVaultSecret secret = secretClient.getSecret(secretName);
        auditService.logSecretAccess(secretName);
        return secret.getValue();
    }
}
```

### 6.3 Application Properties with Key Vault

```yaml
spring:
  cloud:
    azure:
      keyvault:
        secret:
          property-sources:
            - name: dms-keyvault
              endpoint: https://dms-keyvault-${ENVIRONMENT}.vault.azure.net
          
  datasource:
    url: ${database-connection-string}
    
  data:
    redis:
      url: ${redis-connection-string}

azure:
  storage:
    app-containers:
      app-a:
        account-key: ${storage-account-key-app-a}
      app-b:
        account-key: ${storage-account-key-app-b}
        
  ai:
    search:
      api-key: ${azure-ai-search-api-key}
    openai:
      api-key: ${azure-openai-api-key}
```

---

## 7. Multi-Application Segregated Storage

### 7.1 Storage Isolation Model

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                    AZURE BLOB STORAGE ACCOUNT                                    │
│                    (dmsstorage{environment})                                     │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │  CONTAINER: app-a-documents                                              │    │
│  │  Access: Application A Service Principal Only                            │    │
│  │  Encryption: Customer-Managed Key (app-a-encryption-key)                 │    │
│  │                                                                          │    │
│  │  ├── {year}/{month}/{document-id}/                                       │    │
│  │  │   ├── v1/content.pdf                                                  │    │
│  │  │   ├── v1/metadata.json                                                │    │
│  │  │   ├── v1/embeddings.json  (for LLM queries)                           │    │
│  │  │   └── thumbnail.png                                                   │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │  CONTAINER: app-b-documents                                              │    │
│  │  Access: Application B Service Principal Only                            │    │
│  │  Encryption: Customer-Managed Key (app-b-encryption-key)                 │    │
│  │                                                                          │    │
│  │  ├── {year}/{month}/{document-id}/                                       │    │
│  │  │   └── ... (same structure)                                            │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │  CONTAINER: shared-templates                                             │    │
│  │  Access: All registered applications (read-only)                         │    │
│  │  Purpose: Shared document templates, compliance documents                │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │  CONTAINER: audit-logs-archive                                           │    │
│  │  Access: Compliance Officers, System Admins only                         │    │
│  │  Retention: 7 years (immutable)                                          │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 7.2 Application Registration & Container Provisioning

```java
@Entity
@Table(name = "registered_applications")
public class RegisteredApplication {
    @Id
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String entraAppId;  // Azure AD App Registration ID
    
    @Column(unique = true, nullable = false)
    private String applicationName;
    
    @Column(nullable = false)
    private String storageContainerName;
    
    @Column(nullable = false)
    private String encryptionKeyName;  // Key Vault key reference
    
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> configuration;
    
    private Instant createdAt;
    private Instant updatedAt;
}

@Service
public class ApplicationProvisioningService {
    
    private final BlobServiceClient blobServiceClient;
    private final KeyClient keyClient;
    private final RegisteredApplicationRepository repository;
    
    @Transactional
    public RegisteredApplication provisionApplication(ApplicationProvisionRequest request) {
        // 1. Create dedicated storage container
        String containerName = "app-" + request.getApplicationName().toLowerCase() + "-documents";
        BlobContainerClient container = blobServiceClient.createBlobContainer(containerName);
        
        // 2. Create application-specific encryption key in Key Vault
        String keyName = "app-" + request.getApplicationName().toLowerCase() + "-encryption-key";
        keyClient.createRsaKey(new CreateRsaKeyOptions(keyName).setKeySize(2048));
        
        // 3. Configure container encryption with CMK
        configureContainerEncryption(containerName, keyName);
        
        // 4. Set up RBAC for the application's service principal
        assignStorageRbac(request.getEntraAppId(), containerName);
        
        // 5. Register application in database
        RegisteredApplication app = new RegisteredApplication();
        app.setId(UUID.randomUUID());
        app.setEntraAppId(request.getEntraAppId());
        app.setApplicationName(request.getApplicationName());
        app.setStorageContainerName(containerName);
        app.setEncryptionKeyName(keyName);
        app.setStatus(ApplicationStatus.ACTIVE);
        
        return repository.save(app);
    }
}
```

### 7.3 Application Context Filter

```java
@Component
public class ApplicationContextFilter extends OncePerRequestFilter {
    
    private final RegisteredApplicationRepository applicationRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String appId = jwtAuth.getToken().getClaimAsString("azp");
            
            RegisteredApplication app = applicationRepository.findByEntraAppId(appId)
                .orElseThrow(() -> new UnregisteredApplicationException(appId));
            
            // Set application context for the request
            ApplicationContext.setCurrent(app);
            
            try {
                filterChain.doFilter(request, response);
            } finally {
                ApplicationContext.clear();
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}

@Component
public class ApplicationScopedStorageService {
    
    public BlobContainerClient getContainerForCurrentApplication() {
        RegisteredApplication app = ApplicationContext.getCurrent();
        return blobServiceClient.getBlobContainerClient(app.getStorageContainerName());
    }
    
    public String uploadDocument(UUID documentId, InputStream content) {
        BlobContainerClient container = getContainerForCurrentApplication();
        String blobPath = buildBlobPath(documentId);
        
        BlobClient blobClient = container.getBlobClient(blobPath);
        blobClient.upload(content, true);
        
        return blobClient.getBlobUrl();
    }
}
```

---

## 8. Azure AI Foundry LLM Integration

### 8.1 LLM Query Architecture for Compliance

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                     AZURE AI FOUNDRY LLM INTEGRATION                             │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌───────────────────────────────────────────────────────────────────────────┐  │
│  │                         QUERY FLOW                                         │  │
│  │                                                                            │  │
│  │  1. User/Service submits natural language query                           │  │
│  │     "Find all PCI-related documents modified in the last 30 days"         │  │
│  │                              │                                             │  │
│  │                              ▼                                             │  │
│  │  2. DMS LLM Query API validates permissions & application scope           │  │
│  │                              │                                             │  │
│  │                              ▼                                             │  │
│  │  3. Query sent to Azure AI Foundry (GPT-4 / Claude via Azure)             │  │
│  │     - Query understanding & intent extraction                             │  │
│  │     - Filter generation for compliance criteria                           │  │
│  │                              │                                             │  │
│  │                              ▼                                             │  │
│  │  4. Azure AI Search performs hybrid search                                │  │
│  │     - Vector similarity (semantic)                                        │  │
│  │     - Keyword matching                                                    │  │
│  │     - Metadata filtering (classification, dates, app scope)              │  │
│  │                              │                                             │  │
│  │                              ▼                                             │  │
│  │  5. RBAC filter applied to results                                        │  │
│  │     - Only return documents user/app can access                          │  │
│  │     - Apply classification restrictions                                   │  │
│  │                              │                                             │  │
│  │                              ▼                                             │  │
│  │  6. LLM summarizes results with citations                                 │  │
│  │     - Document summaries                                                  │  │
│  │     - Compliance status indicators                                        │  │
│  │     - Retrieval provenance (audit trail)                                  │  │
│  │                              │                                             │  │
│  │                              ▼                                             │  │
│  │  7. Response returned with full audit logging                             │  │
│  │                                                                            │  │
│  └───────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 8.2 Document Embedding Pipeline

```java
@Service
public class DocumentEmbeddingService {
    
    private final OpenAIClient openAIClient;
    private final SearchClient searchClient;
    private final AuditService auditService;
    
    @Async
    @EventListener
    public void onDocumentUploaded(DocumentUploadedEvent event) {
        Document document = event.getDocument();
        
        // 1. Extract text content
        String textContent = extractText(document);
        
        // 2. Chunk document for embedding
        List<DocumentChunk> chunks = chunkDocument(textContent, document.getId());
        
        // 3. Generate embeddings via Azure OpenAI
        for (DocumentChunk chunk : chunks) {
            EmbeddingsOptions options = new EmbeddingsOptions(List.of(chunk.getContent()));
            Embeddings embeddings = openAIClient.getEmbeddings("text-embedding-ada-002", options);
            chunk.setEmbedding(embeddings.getData().get(0).getEmbedding());
        }
        
        // 4. Index in Azure AI Search with application scope
        indexDocumentChunks(chunks, document);
        
        // 5. Audit the indexing operation
        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEventType.DOCUMENT_INDEXED_FOR_LLM)
            .resourceId(document.getId())
            .details(Map.of("chunkCount", chunks.size()))
            .build());
    }
    
    private void indexDocumentChunks(List<DocumentChunk> chunks, Document document) {
        List<SearchDocument> searchDocs = chunks.stream()
            .map(chunk -> new SearchDocument()
                .put("id", chunk.getId().toString())
                .put("documentId", document.getId().toString())
                .put("applicationId", document.getApplicationId().toString())
                .put("content", chunk.getContent())
                .put("contentVector", chunk.getEmbedding())
                .put("classification", document.getClassification().name())
                .put("metadata", document.getMetadata())
                .put("createdAt", document.getCreatedAt().toString()))
            .toList();
            
        searchClient.uploadDocuments(searchDocs);
    }
}
```

### 8.3 Secure LLM Query Service

```java
@Service
public class SecureLlmQueryService {
    
    private final OpenAIClient aiFoundryClient;
    private final SearchClient searchClient;
    private final PermissionService permissionService;
    private final AuditService auditService;
    
    @PreAuthorize("hasRole('DMS.LLM.Service') or hasPermission(#request.applicationId, 'LLM_QUERY')")
    public LlmQueryResponse executeQuery(LlmQueryRequest request) {
        UUID correlationId = UUID.randomUUID();
        
        // 1. Log query initiation
        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEventType.LLM_QUERY_INITIATED)
            .correlationId(correlationId)
            .details(Map.of("query", sanitizeQuery(request.getQuery())))
            .build());
        
        // 2. Use LLM to understand query and extract search parameters
        QueryIntent intent = analyzeQueryIntent(request.getQuery());
        
        // 3. Build secure search with application isolation
        SearchOptions searchOptions = buildSecureSearchOptions(intent, request);
        
        // 4. Execute vector + keyword hybrid search
        SearchPagedIterable results = searchClient.search(
            intent.getKeywords(),
            searchOptions
        );
        
        // 5. Filter results by RBAC permissions
        List<DocumentResult> permittedResults = filterByPermissions(results, request);
        
        // 6. Generate LLM summary with citations
        String summary = generateSummaryWithCitations(request.getQuery(), permittedResults);
        
        // 7. Log query completion with result metadata
        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEventType.LLM_QUERY_COMPLETED)
            .correlationId(correlationId)
            .details(Map.of(
                "resultCount", permittedResults.size(),
                "documentsAccessed", permittedResults.stream()
                    .map(DocumentResult::getDocumentId)
                    .toList()
            ))
            .build());
        
        return LlmQueryResponse.builder()
            .correlationId(correlationId)
            .summary(summary)
            .results(permittedResults)
            .queryMetadata(intent)
            .build();
    }
    
    private SearchOptions buildSecureSearchOptions(QueryIntent intent, LlmQueryRequest request) {
        // Get current application context
        RegisteredApplication app = ApplicationContext.getCurrent();
        
        // Build filter to enforce application isolation and classification
        String securityFilter = String.format(
            "applicationId eq '%s' and classification in ('%s')",
            app.getId(),
            String.join("','", getAllowedClassifications(request))
        );
        
        // Add intent-based filters
        if (intent.getDateRange() != null) {
            securityFilter += String.format(" and createdAt ge %s and createdAt le %s",
                intent.getDateRange().getStart(),
                intent.getDateRange().getEnd());
        }
        
        return new SearchOptions()
            .setFilter(securityFilter)
            .setVectorSearchOptions(new VectorSearchOptions()
                .setQueries(new VectorizedQuery(intent.getQueryEmbedding())
                    .setKNearestNeighborsCount(50)
                    .setFields("contentVector")))
            .setTop(100)
            .setIncludeTotalCount(true);
    }
}
```

### 8.4 LLM Query API Endpoints

```yaml
# OpenAPI specification for LLM Query endpoints
paths:
  /api/v1/llm/query:
    post:
      tags: [LLM Query]
      summary: Execute natural language document query
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LlmQueryRequest'
      responses:
        '200':
          description: Query results with LLM summary
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LlmQueryResponse'
                
  /api/v1/llm/compliance-check:
    post:
      tags: [LLM Query]
      summary: Run compliance check query across documents
      description: |
        Executes a compliance-focused query to find documents matching
        specific regulatory requirements (PCI-DSS, ISO 27001, GDPR).
      security:
        - bearerAuth: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ComplianceCheckRequest'
      responses:
        '200':
          description: Compliance check results
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ComplianceCheckResponse'

components:
  schemas:
    LlmQueryRequest:
      type: object
      required:
        - query
      properties:
        query:
          type: string
          description: Natural language query
          example: "Find all documents related to PCI compliance updated this month"
        filters:
          type: object
          properties:
            classifications:
              type: array
              items:
                type: string
                enum: [PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED, PCI]
            dateRange:
              type: object
              properties:
                from:
                  type: string
                  format: date-time
                to:
                  type: string
                  format: date-time
        maxResults:
          type: integer
          default: 20
          maximum: 100
        includeSummary:
          type: boolean
          default: true
          
    LlmQueryResponse:
      type: object
      properties:
        correlationId:
          type: string
          format: uuid
        summary:
          type: string
          description: LLM-generated summary of results
        results:
          type: array
          items:
            $ref: '#/components/schemas/DocumentResult'
        totalCount:
          type: integer
        queryMetadata:
          type: object
          properties:
            interpretedQuery:
              type: string
            appliedFilters:
              type: object
            processingTimeMs:
              type: integer
```

### 8.5 LLM Security Controls

```java
@Configuration
public class LlmSecurityConfig {
    
    // Content filtering to prevent prompt injection
    @Bean
    public ContentFilter contentFilter() {
        return ContentFilter.builder()
            .maxQueryLength(2000)
            .blockedPatterns(List.of(
                "ignore previous instructions",
                "system prompt",
                "reveal.*secret"
            ))
            .sanitizeOutput(true)
            .build();
    }
    
    // Rate limiting for LLM queries
    @Bean
    public RateLimiter llmQueryRateLimiter() {
        return RateLimiter.builder()
            .perApplication(100)  // queries per minute
            .perUser(20)
            .globalLimit(1000)
            .build();
    }
}

@Service
public class LlmAuditService {
    
    // All LLM interactions must be audited for compliance
    public void auditLlmInteraction(LlmAuditRecord record) {
        AuditEvent event = AuditEvent.builder()
            .eventType(AuditEventType.LLM_INTERACTION)
            .correlationId(record.getCorrelationId())
            .details(Map.of(
                "query", record.getSanitizedQuery(),
                "model", record.getModelUsed(),
                "tokensUsed", record.getTokenCount(),
                "documentsAccessed", record.getDocumentIds(),
                "responseGenerated", record.wasResponseGenerated(),
                "contentFiltered", record.wasContentFiltered()
            ))
            .build();
        
        auditService.logEvent(event);
    }
}
```

---

## 9. Audit Trail System

### 9.1 Comprehensive Audit Events

| Event Category | Event Types | PCI-DSS | ISO 27001 | GDPR |
|----------------|-------------|:-------:|:---------:|:----:|
| **Authentication** | LOGIN, LOGOUT, FAILED_LOGIN, TOKEN_REFRESH | ✓ | ✓ | ✓ |
| **Document Access** | VIEW, DOWNLOAD, PRINT, SHARE | ✓ | ✓ | ✓ |
| **Document Lifecycle** | CREATE, UPDATE, DELETE, ARCHIVE | ✓ | ✓ | ✓ |
| **Permission Changes** | GRANT, REVOKE, ROLE_ASSIGN | ✓ | ✓ | ✓ |
| **LLM Queries** | QUERY_INITIATED, QUERY_COMPLETED, CONTENT_FILTERED | ✓ | ✓ | ✓ |
| **Admin Actions** | USER_CREATE, CONFIG_CHANGE, APP_PROVISION | ✓ | ✓ | ✓ |
| **Data Subject** | EXPORT_REQUEST, DELETION_REQUEST, CONSENT_CHANGE | | | ✓ |

### 9.2 Audit Log Schema

```sql
CREATE TABLE audit_logs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id            VARCHAR(50) NOT NULL,
    event_type          VARCHAR(50) NOT NULL,
    event_category      VARCHAR(30) NOT NULL,
    timestamp           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Actor Information
    user_id             UUID,
    username            VARCHAR(255),
    user_roles          JSONB,
    application_id      UUID,  -- For service-to-service calls
    application_name    VARCHAR(255),
    
    -- Request Context  
    ip_address          INET,
    user_agent          TEXT,
    request_id          UUID,
    correlation_id      UUID,  -- Links related events (esp. for LLM queries)
    
    -- Resource Information
    resource_type       VARCHAR(50),
    resource_id         UUID,
    resource_name       VARCHAR(500),
    
    -- Event Details
    action              VARCHAR(50) NOT NULL,
    result              VARCHAR(20) NOT NULL,
    details             JSONB,
    previous_state      JSONB,
    new_state           JSONB,
    
    -- Compliance Markers
    pci_relevant        BOOLEAN DEFAULT FALSE,
    gdpr_relevant       BOOLEAN DEFAULT FALSE,
    contains_pii        BOOLEAN DEFAULT FALSE,
    
    -- Integrity
    checksum            VARCHAR(64) NOT NULL,  -- SHA-256 of event data
    
    CONSTRAINT audit_immutable CHECK (TRUE)  -- Prevent updates
) PARTITION BY RANGE (timestamp);

-- Create immutable audit log policy
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;
CREATE POLICY audit_no_delete ON audit_logs FOR DELETE USING (FALSE);
CREATE POLICY audit_no_update ON audit_logs FOR UPDATE USING (FALSE);
```

---

## 10. Compliance Framework (PCI-DSS, ISO 27001, GDPR)

### 10.1 Compliance Matrix

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         COMPLIANCE REQUIREMENTS MATRIX                           │
├──────────────────┬──────────────────┬──────────────────┬────────────────────────┤
│   Requirement    │    PCI-DSS 4.0   │   ISO 27001:2022 │        GDPR            │
├──────────────────┼──────────────────┼──────────────────┼────────────────────────┤
│ Access Control   │ Req 7, 8         │ A.9              │ Art. 32                │
│ Implementation   │ RBAC + MFA       │ RBAC + App       │ RBAC + Consent         │
│                  │ App Isolation    │ Isolation        │ Purpose Limitation     │
├──────────────────┼──────────────────┼──────────────────┼────────────────────────┤
│ Encryption       │ Req 3, 4         │ A.10             │ Art. 32                │
│ Implementation   │ AES-256 at rest  │ TLS 1.3 transit  │ Pseudonymization       │
│                  │ Key Vault CMK    │ Key Management   │ Encryption             │
├──────────────────┼──────────────────┼──────────────────┼────────────────────────┤
│ Audit Logging    │ Req 10           │ A.12             │ Art. 30                │
│ Implementation   │ All access logged│ Security events  │ Processing records     │
│                  │ 1 year retention │ Centralized logs │ Immutable logs         │
├──────────────────┼──────────────────┼──────────────────┼────────────────────────┤
│ Data Protection  │ Req 3            │ A.8              │ Art. 5, 17, 20         │
│ Implementation   │ Tokenization     │ Classification   │ Right to erasure       │
│                  │ Masking          │ Labeling         │ Data portability       │
├──────────────────┼──────────────────┼──────────────────┼────────────────────────┤
│ Incident Response│ Req 12           │ A.16             │ Art. 33, 34            │
│ Implementation   │ IR procedures    │ Incident mgmt    │ 72hr notification      │
│                  │ Annual testing   │ Evidence collect │ DPA notification       │
└──────────────────┴──────────────────┴──────────────────┴────────────────────────┘
```

### 10.2 PCI-DSS Implementation

```java
@Component
public class PciDssComplianceService {
    
    // PCI-DSS Requirement 3: Protect stored cardholder data
    @Service
    public class CardholderDataProtection {
        
        public void classifyDocument(Document document) {
            if (containsCardholderData(document)) {
                document.setClassification(Classification.PCI);
                document.setPciRelevant(true);
                
                // Apply additional encryption
                document.setEncryptionLevel(EncryptionLevel.PCI_HSM);
                
                // Restrict access to PCI-authorized personnel
                permissionService.applyPciRestrictions(document);
            }
        }
        
        private boolean containsCardholderData(Document document) {
            // Scan for PAN patterns, CVV, etc.
            return contentScanner.scanForPciData(document);
        }
    }
    
    // PCI-DSS Requirement 10: Track and monitor access
    @Aspect
    @Component
    public class PciAuditAspect {
        
        @Around("@annotation(pciAudited)")
        public Object auditPciAccess(ProceedingJoinPoint joinPoint) throws Throwable {
            AuditEvent.Builder builder = AuditEvent.builder()
                .pciRelevant(true)
                .eventCategory(AuditEventCategory.PCI_ACCESS);
            
            try {
                Object result = joinPoint.proceed();
                builder.result(AuditResult.SUCCESS);
                return result;
            } catch (Exception e) {
                builder.result(AuditResult.FAILURE);
                throw e;
            } finally {
                auditService.logEvent(builder.build());
            }
        }
    }
}
```

### 10.3 ISO 27001 Implementation

```java
@Configuration
public class Iso27001Controls {
    
    // A.9 Access Control
    @Bean
    public AccessControlPolicy accessControlPolicy() {
        return AccessControlPolicy.builder()
            .requireMfa(true)
            .sessionTimeout(Duration.ofMinutes(30))
            .maxFailedAttempts(5)
            .lockoutDuration(Duration.ofMinutes(30))
            .passwordPolicy(PasswordPolicy.ISO27001_COMPLIANT)
            .build();
    }
    
    // A.10 Cryptography
    @Bean
    public CryptographyPolicy cryptographyPolicy() {
        return CryptographyPolicy.builder()
            .encryptionAlgorithm("AES-256-GCM")
            .keyRotationPeriod(Duration.ofDays(365))
            .keyDerivationFunction("PBKDF2")
            .minimumKeyLength(256)
            .build();
    }
    
    // A.12 Operations Security
    @Bean
    public SecurityMonitoringConfig securityMonitoring() {
        return SecurityMonitoringConfig.builder()
            .enableRealTimeAlerts(true)
            .logRetentionDays(365)
            .anomalyDetectionEnabled(true)
            .vulnerabilityScanSchedule("0 0 2 * * SUN")
            .build();
    }
}
```

### 10.4 GDPR Implementation

```java
@Service
public class GdprComplianceService {
    
    // Article 17: Right to Erasure
    @Transactional
    public ErasureResponse processErasureRequest(ErasureRequest request) {
        UUID dataSubjectId = request.getDataSubjectId();
        
        // 1. Find all documents owned by or containing data subject's PII
        List<Document> documents = documentRepository.findByDataSubject(dataSubjectId);
        
        // 2. Check for legal holds or retention requirements
        List<Document> deletable = documents.stream()
            .filter(doc -> !hasLegalHold(doc))
            .filter(doc -> !hasRetentionRequirement(doc))
            .toList();
        
        // 3. Anonymize or delete documents
        for (Document doc : deletable) {
            if (doc.getOwner().equals(dataSubjectId)) {
                documentService.permanentDelete(doc.getId());
            } else {
                documentService.anonymizePii(doc.getId(), dataSubjectId);
            }
        }
        
        // 4. Audit the erasure
        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEventType.GDPR_ERASURE_COMPLETED)
            .gdprRelevant(true)
            .details(Map.of(
                "dataSubjectId", dataSubjectId,
                "documentsDeleted", deletable.size(),
                "documentsRetained", documents.size() - deletable.size()
            ))
            .build());
        
        return new ErasureResponse(deletable.size(), documents.size() - deletable.size());
    }
    
    // Article 20: Right to Data Portability
    public DataExportResponse exportDataSubjectData(UUID dataSubjectId) {
        List<Document> documents = documentRepository.findByOwner(dataSubjectId);
        
        // Create portable format (JSON-LD or similar)
        DataExport export = DataExport.builder()
            .dataSubjectId(dataSubjectId)
            .exportDate(Instant.now())
            .documents(documents.stream()
                .map(this::toPortableFormat)
                .toList())
            .build();
        
        // Generate secure download link
        String exportPath = storageService.storeExport(export);
        
        auditService.logEvent(AuditEvent.builder()
            .eventType(AuditEventType.GDPR_DATA_EXPORT)
            .gdprRelevant(true)
            .build());
        
        return new DataExportResponse(exportPath, Duration.ofHours(24));
    }
    
    // Article 30: Records of Processing Activities
    @Scheduled(cron = "0 0 0 1 * *")  // Monthly
    public void generateProcessingRecords() {
        ProcessingActivityReport report = ProcessingActivityReport.builder()
            .reportingPeriod(YearMonth.now().minusMonths(1))
            .dataCategories(getProcessedDataCategories())
            .processingPurposes(getProcessingPurposes())
            .dataRecipients(getDataRecipients())
            .retentionPeriods(getRetentionPeriods())
            .securityMeasures(getSecurityMeasures())
            .build();
        
        complianceReportRepository.save(report);
    }
}
```

### 10.5 Compliance Reporting API

```yaml
paths:
  /api/v1/compliance/pci/report:
    get:
      tags: [Compliance]
      summary: Generate PCI-DSS compliance report
      security:
        - bearerAuth: []
      parameters:
        - name: period
          in: query
          schema:
            type: string
            enum: [DAILY, WEEKLY, MONTHLY, QUARTERLY]
      responses:
        '200':
          description: PCI compliance report
          
  /api/v1/compliance/gdpr/data-subject/{id}:
    get:
      tags: [Compliance]
      summary: Get data subject access request (DSAR) data
    delete:
      tags: [Compliance]
      summary: Process right to erasure request
      
  /api/v1/compliance/iso27001/controls:
    get:
      tags: [Compliance]
      summary: Get ISO 27001 control status dashboard
```

---

## 11. API Design (OpenAPI 3.0)

### 11.1 Core Document APIs

```yaml
openapi: 3.0.3
info:
  title: Document Management System API
  description: |
    Enterprise DMS with RBAC, multi-application isolation, LLM query support,
    and PCI-DSS/ISO 27001/GDPR compliance.
  version: 1.0.0

servers:
  - url: https://api.example.com/dms/v1

security:
  - bearerAuth: []

paths:
  /documents:
    get:
      summary: List documents (scoped to application)
      parameters:
        - $ref: '#/components/parameters/PageNumber'
        - $ref: '#/components/parameters/PageSize'
        - name: classification
          in: query
          schema:
            type: array
            items:
              $ref: '#/components/schemas/Classification'
      responses:
        '200':
          description: Documents within application scope
          
    post:
      summary: Upload document to application's storage
      requestBody:
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                file:
                  type: string
                  format: binary
                metadata:
                  $ref: '#/components/schemas/DocumentMetadata'
      responses:
        '201':
          description: Document uploaded

components:
  schemas:
    Classification:
      type: string
      enum: [PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED, PCI]
      
    DocumentMetadata:
      type: object
      properties:
        name:
          type: string
        classification:
          $ref: '#/components/schemas/Classification'
        pciRelevant:
          type: boolean
        gdprDataCategories:
          type: array
          items:
            type: string
        retentionPeriod:
          type: string
          description: ISO 8601 duration
```

---

## 12. Storage Architecture

See Section 7 for Multi-Application Segregated Storage details.

---

## 13. Frontend Design

### 13.1 Angular Application with MSAL

```typescript
// app.config.ts
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    {
      provide: MSAL_INSTANCE,
      useFactory: () => new PublicClientApplication({
        auth: {
          clientId: environment.azure.clientId,
          authority: `https://login.microsoftonline.com/${environment.azure.tenantId}`,
          redirectUri: environment.azure.redirectUri
        },
        cache: {
          cacheLocation: 'sessionStorage',
          storeAuthStateInCookie: true
        }
      })
    },
    {
      provide: MSAL_GUARD_CONFIG,
      useValue: {
        interactionType: InteractionType.Redirect,
        authRequest: {
          scopes: ['api://dms-api/dms.documents.read', 'api://dms-api/dms.documents.write']
        }
      }
    }
  ]
};
```

---

## 14. Service Integration

See Sections 5, 7, and 8 for detailed service integration patterns.

---

## 15. Data Models

### 15.1 Enhanced Database Schema

```sql
-- Application registration for multi-tenant isolation
CREATE TABLE registered_applications (
    id                      UUID PRIMARY KEY,
    entra_app_id            VARCHAR(255) UNIQUE NOT NULL,
    application_name        VARCHAR(255) UNIQUE NOT NULL,
    storage_container_name  VARCHAR(255) NOT NULL,
    encryption_key_name     VARCHAR(255) NOT NULL,
    status                  VARCHAR(30) NOT NULL,
    configuration           JSONB,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Documents with application scoping
CREATE TABLE documents (
    id                  UUID PRIMARY KEY,
    application_id      UUID NOT NULL REFERENCES registered_applications(id),
    name                VARCHAR(500) NOT NULL,
    classification      VARCHAR(30) NOT NULL,
    pci_relevant        BOOLEAN DEFAULT FALSE,
    gdpr_data_categories TEXT[],
    retention_until     TIMESTAMPTZ,
    -- ... other fields
    
    CONSTRAINT fk_application FOREIGN KEY (application_id) 
        REFERENCES registered_applications(id)
);

-- Index for application-scoped queries
CREATE INDEX idx_documents_application ON documents(application_id);
CREATE INDEX idx_documents_classification ON documents(classification);
```

---

## 16. Implementation Phases

### Phase 1: Foundation & Security
- [ ] Azure infrastructure setup (Key Vault, Storage, AD Apps)
- [ ] Spring Boot project with Azure AD integration
- [ ] Multi-application storage isolation
- [ ] Basic RBAC implementation

### Phase 2: Compliance Framework
- [ ] Audit logging system
- [ ] PCI-DSS controls implementation
- [ ] GDPR data subject rights APIs
- [ ] ISO 27001 security controls

### Phase 3: AI Integration
- [ ] Azure AI Search setup with vector indexing
- [ ] Document embedding pipeline
- [ ] Azure AI Foundry LLM integration
- [ ] Secure LLM query API

### Phase 4: Frontend & APIs
- [ ] Angular 25 application with MSAL
- [ ] OpenAPI 3.0 documentation
- [ ] Admin dashboard
- [ ] Compliance reporting UI

### Phase 5: Production Readiness
- [ ] Performance optimization
- [ ] Penetration testing
- [ ] Compliance audit preparation
- [ ] Disaster recovery procedures

---

## 17. Non-Functional Requirements

| Requirement | Target | Compliance |
|-------------|--------|------------|
| API Response (p95) | < 200ms | - |
| LLM Query Response | < 3s | - |
| Availability | 99.9% | ISO 27001 |
| Audit Log Retention | 7 years | PCI-DSS, GDPR |
| Encryption | AES-256 + TLS 1.3 | All |
| Key Rotation | Annual | PCI-DSS |
| Backup RPO | 1 hour | ISO 27001 |
| Backup RTO | 4 hours | ISO 27001 |

---

*Document Version: 2.0*  
*Last Updated: January 2026*  
*Compliance Standards: PCI-DSS 4.0, ISO 27001:2022, GDPR*
