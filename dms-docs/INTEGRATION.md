---
Last Updated: 2024-01-18T13:32:00Z
Updated By: davidparker-lv-bmth
---

# DMS Integration Documentation

## Table of Contents

1. [Integration Overview](#integration-overview)
2. [API Integration](#api-integration)
3. [Service Integration](#service-integration)
4. [Azure Services Integration](#azure-services-integration)
5. [Authentication Integration](#authentication-integration)
6. [Data Integration](#data-integration)
7. [Event Integration](#event-integration)

---

## Integration Overview

The DMS system integrates with multiple services and platforms. This document describes integration patterns, APIs, and protocols.

### Integration Architecture

```mermaid
graph TB
    subgraph "External Systems"
        EXT_API[External APIs]
        AZURE_SERVICES[Azure Services]
        IDENTITY[Identity Providers]
    end
    
    subgraph "DMS Services"
        FRONTEND[Frontend Service]
        ADMIN[Admin Service]
        DOC[Document Service]
        AUDIT[Audit Service]
        COMPLIANCE[Compliance Service]
        LLM[LLM Service]
    end
    
    subgraph "Integration Layer"
        API_GW[API Gateway]
        AUTH_GW[Auth Gateway]
        EVENT_BUS[Event Bus]
    end
    
    EXT_API --> API_GW
    IDENTITY --> AUTH_GW
    
    API_GW --> FRONTEND
    API_GW --> ADMIN
    API_GW --> DOC
    API_GW --> COMPLIANCE
    API_GW --> LLM
    
    AUTH_GW --> ADMIN
    AUTH_GW --> DOC
    
    ADMIN --> EVENT_BUS
    DOC --> EVENT_BUS
    COMPLIANCE --> EVENT_BUS
    
    EVENT_BUS --> AUDIT
    EVENT_BUS --> AZURE_SERVICES
```

---

## API Integration

### REST API Standards

All DMS services expose RESTful APIs following OpenAPI 3.0 specification.

#### Base URL Structure

```
https://{service-name}.{domain}/api/v1/{resource}
```

#### Example Endpoints

**Admin Service**:
- `https://admin.dms.example.com/api/v1/admin/users`
- `https://admin.dms.example.com/api/v1/admin/roles`
- `https://admin.dms.example.com/api/v1/admin/applications`

**Document Service**:
- `https://documents.dms.example.com/api/v1/documents`
- `https://documents.dms.example.com/api/v1/documents/{id}/versions`

**LLM Service**:
- `https://llm.dms.example.com/api/v1/llm/query`
- `https://llm.dms.example.com/api/v1/llm/compliance-check`

### API Request/Response Flow

```mermaid
sequenceDiagram
    participant Client
    participant API_Gateway
    participant Service
    participant Database
    participant Audit
    
    Client->>API_Gateway: HTTP Request
    API_Gateway->>API_Gateway: Authenticate JWT
    API_Gateway->>API_Gateway: Rate Limit Check
    API_Gateway->>Service: Forward Request
    Service->>Service: Validate Request
    Service->>Database: Query/Update
    Database-->>Service: Result
    Service->>Audit: Log Event
    Audit-->>Service: Confirmed
    Service-->>API_Gateway: Response
    API_Gateway-->>Client: HTTP Response
```

### Authentication Header

All API requests require JWT authentication:

```http
Authorization: Bearer {jwt_token}
```

### Request Example

```http
POST /api/v1/documents HTTP/1.1
Host: documents.dms.example.com
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data

file: [binary data]
name: "Document Name"
classification: "CONFIDENTIAL"
```

### Response Example

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Document Name",
  "classification": "CONFIDENTIAL",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z",
  "version": 1,
  "size": 1024000,
  "mimeType": "application/pdf"
}
```

### Error Response Format

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid classification value",
  "path": "/api/v1/documents"
}
```

---

## Service Integration

### Service-to-Service Communication

Services communicate via REST APIs using WebClient (reactive) for HTTP calls.

```mermaid
graph LR
    S1[Service 1] -->|HTTP REST| ISTIO[Istio Service Mesh]
    ISTIO -->|mTLS| S2[Service 2]
    
    ISTIO --> DISCOVERY[Service Discovery]
    ISTIO --> LOAD_BALANCE[Load Balancing]
    ISTIO --> CIRCUIT[Circuit Breaker]
```

### Service Discovery

Services discover each other using Kubernetes DNS:

```
{service-name}.{namespace}.svc.cluster.local:{port}
```

**Example**:
- `dms-admin-service.dms.svc.cluster.local:8081`
- `dms-document-service.dms.svc.cluster.local:8083`

### Inter-Service Call Pattern

```java
@Service
public class DocumentService {
    
    private final WebClient adminServiceClient;
    
    public Document uploadDocument(UUID applicationId, MultipartFile file) {
        // Verify application exists
        adminServiceClient
            .get()
            .uri("http://dms-admin-service:8081/api/v1/admin/applications/{id}", applicationId)
            .retrieve()
            .bodyToMono(Application.class)
            .block();
        
        // Continue with document upload...
    }
}
```

### Circuit Breaker Pattern

```mermaid
graph TD
    REQ[Request] --> CB[Circuit Breaker]
    CB -->|Closed| NORMAL[Normal Operation]
    CB -->|Open| FALLBACK[Fallback Response]
    CB -->|Half-Open| TEST[Test Request]
    
    NORMAL -->|Success| CLOSE[Keep Closed]
    NORMAL -->|Failure| OPEN[Open Circuit]
    TEST -->|Success| CLOSE
    TEST -->|Failure| OPEN
```

---

## Azure Services Integration

### Azure Key Vault Integration

```mermaid
graph LR
    APP[Application] --> SDK[Azure SDK]
    SDK --> AUTH[Managed Identity]
    AUTH --> KV[Azure Key Vault]
    KV --> SECRETS[Secrets]
    
    SECRETS --> DB_PWD[Database Password]
    SECRETS --> API_KEY[API Keys]
    SECRETS --> CERT[Certificates]
```

**Configuration**:
```yaml
azure:
  keyvault:
    uri: https://dms-keyvault-dev.vault.azure.net/
    enabled: true
    secrets:
      - database-connection-string
      - redis-password
      - blob-storage-key
```

### Azure Blob Storage Integration

```mermaid
graph TD
    DOC_SVC[Document Service] --> SDK[Azure Storage SDK]
    SDK --> AUTH[Storage Account Key]
    AUTH --> BLOB[Blob Storage]
    
    BLOB --> CONTAINER[Application Containers]
    CONTAINER --> APP1[app-{id1}-documents]
    CONTAINER --> APP2[app-{id2}-documents]
    
    APP1 --> FILES[Document Files]
    APP2 --> FILES
```

**Upload Flow**:
```java
@Service
public class DocumentService {
    
    private final BlobContainerClient containerClient;
    
    public Document uploadDocument(UUID applicationId, MultipartFile file) {
        String blobName = UUID.randomUUID().toString();
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(file.getInputStream(), file.getSize());
        
        // Save metadata to database...
    }
}
```

### Azure AI Search Integration

```mermaid
graph LR
    LLM_SVC[LLM Service] --> SDK[Azure AI Search SDK]
    SDK --> INDEX[Search Index]
    
    INDEX --> VECTORS[Vector Embeddings]
    INDEX --> METADATA[Document Metadata]
    
    VECTORS --> SEARCH[Vector Search]
    METADATA --> FILTER[Filter Results]
```

**Query Flow**:
```java
@Service
public class SecureLlmQueryService {
    
    private final SearchClient searchClient;
    
    public LlmQueryResponse executeQuery(LlmQueryRequest request) {
        // Generate query embedding
        float[] queryVector = generateEmbedding(request.getQuery());
        
        // Vector search
        SearchOptions options = new SearchOptions()
            .setVectorSearch(new VectorSearchOptions()
                .setQueries(new VectorizedQuery(queryVector, "contentVector")));
        
        SearchPagedIterable<Document> results = searchClient.search(request.getQuery(), options);
        
        // Process results...
    }
}
```

### Azure AI Foundry Integration

```mermaid
graph TD
    LLM_SVC[LLM Service] --> SDK[Azure AI SDK]
    SDK --> ENDPOINT[AI Foundry Endpoint]
    
    ENDPOINT --> MODEL[LLM Model]
    MODEL --> PROMPT[Prompt Engineering]
    PROMPT --> RESPONSE[Generated Response]
    
    RESPONSE --> CITATIONS[Source Citations]
    RESPONSE --> ANSWER[Natural Language Answer]
```

### Azure Event Hubs Integration

```mermaid
graph LR
    SERVICES[Services] --> PRODUCER[Event Producer]
    PRODUCER --> EVENT_HUB[Azure Event Hubs]
    
    EVENT_HUB --> CONSUMER1[Audit Consumer]
    EVENT_HUB --> CONSUMER2[Analytics Consumer]
    EVENT_HUB --> CONSUMER3[Monitoring Consumer]
```

**Event Publishing**:
```java
@Service
public class AuditService {
    
    private final EventHubProducerClient producerClient;
    
    public void publishEvent(AuditEvent event) {
        EventData eventData = new EventData(JsonConverter.toJson(event));
        producerClient.send(Collections.singletonList(eventData));
    }
}
```

---

## Authentication Integration

### Azure AD / Entra ID Integration

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
    User->>AzureAD: Credentials + MFA
    AzureAD->>AzureAD: Authenticate
    AzureAD->>Frontend: Authorization Code
    Frontend->>AzureAD: Exchange Code for Token
    AzureAD->>Frontend: JWT Access Token
    Frontend->>Backend: API Request + JWT
    Backend->>KeyVault: Get Signing Keys
    KeyVault-->>Backend: Public Keys
    Backend->>Backend: Validate JWT
    Backend-->>Frontend: Authorized Response
```

### JWT Token Structure

```json
{
  "aud": "api://dms-api",
  "iss": "https://login.microsoftonline.com/{tenant-id}/v2.0",
  "iat": 1705315200,
  "exp": 1705318800,
  "sub": "user-id",
  "roles": [
    "DMS.davidparker-lv-bmth",
    "DMS.User"
  ],
  "appid": "application-id",
  "appidacr": "1"
}
```

### MSAL Angular Integration

```typescript
// Frontend authentication
import { MsalService } from '@azure/msal-angular';

constructor(private msalService: MsalService) {}

login() {
  this.msalService.loginPopup({
    scopes: ['User.Read', 'api://dms-api/documents.read']
  }).subscribe(response => {
    this.accessToken = response.accessToken;
  });
}
```

---

## Data Integration

### Database Integration

Each service has its own database schema:

```mermaid
graph TD
    ADMIN_SVC[Admin Service] --> ADMIN_DB[(dms_admin)]
    DOC_SVC[Document Service] --> DOC_DB[(dms_document)]
    AUDIT_SVC[Audit Service] --> AUDIT_DB[(dms_audit)]
    
    ADMIN_DB --> USERS[users table]
    ADMIN_DB --> ROLES[roles table]
    ADMIN_DB --> PERMS[permissions table]
    ADMIN_DB --> APPS[applications table]
    
    DOC_DB --> DOCS[documents table]
    DOC_DB --> VERSIONS[document_versions table]
    
    AUDIT_DB --> LOGS[audit_logs table<br/>Partitioned]
```

### Redis Cache Integration

```mermaid
graph LR
    SERVICES[Services] --> REDIS[Redis Cache]
    
    REDIS --> PERMS[Permission Cache]
    REDIS --> SESSIONS[Session Cache]
    REDIS --> RATE_LIMIT[Rate Limit Cache]
    
    PERMS --> TTL[TTL: 1 hour]
    SESSIONS --> TTL2[TTL: 30 min]
    RATE_LIMIT --> TTL3[TTL: 1 min]
```

**Caching Pattern**:
```java
@Service
public class PermissionService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public List<Permission> getUserPermissions(UUID userId) {
        String cacheKey = "permissions:user:" + userId;
        
        // Check cache
        List<Permission> cached = (List<Permission>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Load from database
        List<Permission> permissions = permissionRepository.findByUserId(userId);
        
        // Cache for 1 hour
        redisTemplate.opsForValue().set(cacheKey, permissions, Duration.ofHours(1));
        
        return permissions;
    }
}
```

### Data Synchronization

```mermaid
graph TD
    SOURCE[Source Service] --> EVENT[Publish Event]
    EVENT --> BUS[Event Bus]
    BUS --> TARGET[Target Service]
    TARGET --> SYNC[Synchronize Data]
    
    SYNC --> DB[(Target Database)]
    SYNC --> CACHE[Update Cache]
```

---

## Event Integration

### Event-Driven Architecture

```mermaid
graph TB
    PRODUCERS[Event Producers]
    
    PRODUCERS --> ADMIN[Admin Service]
    PRODUCERS --> DOC[Document Service]
    PRODUCERS --> COMPLIANCE[Compliance Service]
    
    ADMIN --> EVENT_HUB[Azure Event Hubs]
    DOC --> EVENT_HUB
    COMPLIANCE --> EVENT_HUB
    
    EVENT_HUB --> CONSUMERS[Event Consumers]
    
    CONSUMERS --> AUDIT[Audit Service]
    CONSUMERS --> ANALYTICS[Analytics Service]
    CONSUMERS --> MONITORING[Monitoring Service]
```

### Event Schema

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "DOCUMENT_UPLOADED",
  "timestamp": "2024-01-15T10:30:00Z",
  "source": "dms-document-service",
  "applicationId": "app-123",
  "userId": "user-456",
  "data": {
    "documentId": "doc-789",
    "documentName": "example.pdf",
    "classification": "CONFIDENTIAL",
    "size": 1024000
  },
  "correlationId": "corr-abc123"
}
```

### Event Types

| Event Type | Source | Description |
|------------|--------|-------------|
| `USER_CREATED` | Admin Service | New user created |
| `USER_UPDATED` | Admin Service | User information updated |
| `ROLE_ASSIGNED` | Admin Service | Role assigned to user |
| `DOCUMENT_UPLOADED` | Document Service | New document uploaded |
| `DOCUMENT_DOWNLOADED` | Document Service | Document downloaded |
| `DOCUMENT_DELETED` | Document Service | Document deleted |
| `QUERY_EXECUTED` | LLM Service | LLM query executed |
| `GDPR_EXPORT` | Compliance Service | GDPR data export requested |
| `GDPR_ERASURE` | Compliance Service | GDPR erasure requested |

### Event Consumer Example

```java
@Service
public class AuditEventConsumer {
    
    @EventListener
    public void handleDocumentUploaded(DocumentUploadedEvent event) {
        AuditEvent auditEvent = AuditEvent.builder()
            .eventType("DOCUMENT_UPLOADED")
            .timestamp(Instant.now())
            .source("dms-document-service")
            .applicationId(event.getApplicationId())
            .userId(event.getUserId())
            .data(JsonConverter.toJson(event))
            .build();
        
        auditService.logEvent(auditEvent);
    }
}
```

---

## Integration Testing

### Test Integration Points

```mermaid
graph TD
    TEST[Integration Test] --> MOCK[Mock Services]
    TEST --> STUB[Stub External APIs]
    TEST --> CONTAINER[Test Containers]
    
    MOCK --> ADMIN_MOCK[Admin Service Mock]
    MOCK --> DOC_MOCK[Document Service Mock]
    
    STUB --> AZURE_STUB[Azure Services Stub]
    
    CONTAINER --> POSTGRES[PostgreSQL Container]
    CONTAINER --> REDIS[Redis Container]
```

### Integration Test Example

```java
@SpringBootTest
@AutoConfigureMockMvc
class DocumentServiceIntegrationTest {
    
    @MockBean
    private AdminServiceClient adminServiceClient;
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
    
    @Test
    void testDocumentUploadWithApplicationVerification() {
        // Mock admin service response
        when(adminServiceClient.getApplication(any()))
            .thenReturn(Application.builder().id(UUID.randomUUID()).build());
        
        // Upload document
        Document document = documentService.uploadDocument(applicationId, file);
        
        // Verify
        assertThat(document).isNotNull();
        verify(adminServiceClient).getApplication(applicationId);
    }
}
```

---

## Integration Best Practices

### 1. Service Discovery
- Use Kubernetes DNS for service discovery
- Implement health checks for service availability
- Use circuit breakers for resilience

### 2. Error Handling
- Implement retry logic with exponential backoff
- Use fallback mechanisms for critical operations
- Log all integration errors for troubleshooting

### 3. Security
- Always use mTLS for service-to-service communication
- Validate all inputs from external services
- Implement rate limiting to prevent abuse

### 4. Monitoring
- Log all integration calls with correlation IDs
- Monitor integration latency and error rates
- Set up alerts for integration failures

### 5. Versioning
- Version all APIs to support backward compatibility
- Deprecate old versions with sufficient notice
- Document breaking changes clearly

---

*Last Updated: 2024*
