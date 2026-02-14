---
Last Updated: 2026-02-14T16:00:00Z
Updated By: davidparker-lv-bmth
---

# DMS Agentic Integration Guide

## Overview

The DMS is designed to work seamlessly with AI agents and automated workflows. This guide describes how to integrate the DMS with agentic systems, including MCP (Model Context Protocol) servers, LangChain agents, and custom automation tools.

## API Design for Agents

### RESTful API Patterns

All DMS endpoints follow predictable REST patterns that agents can discover and use:

```
GET    /api/v1/{resource}          → List (paginated)
GET    /api/v1/{resource}/{id}     → Get by ID
POST   /api/v1/{resource}          → Create
PUT    /api/v1/{resource}/{id}     → Update
DELETE /api/v1/{resource}/{id}     → Delete
```

### OpenAPI Discovery

Each service exposes OpenAPI 3.0 specs at:
- `http://{service-url}/swagger-ui.html` — Swagger UI
- `http://{service-url}/v3/api-docs` — OpenAPI JSON spec

Agents can parse the OpenAPI spec to discover available operations, request/response schemas, and authentication requirements.

### Structured Error Responses

All errors return machine-readable JSON:
```json
{
  "error": "Human-readable description",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "details": {
    "field": "username",
    "constraint": "REQUIRED"
  },
  "correlationId": "uuid",
  "timestamp": "2026-02-14T16:00:00Z"
}
```

### Correlation ID Propagation

Pass `X-Correlation-ID` header on all requests. The DMS propagates this ID across all inter-service calls and includes it in audit logs, enabling full request tracing for debugging agent workflows.

```
X-Correlation-ID: agent-workflow-123-step-4
```

## Agentic Workflow Patterns

### 1. Document Processing Pipeline

```
Agent → Upload Document → DMS API → Auto-classify → Index for LLM → Audit Log
```

```typescript
// Step 1: Upload document
const doc = await fetch('/api/v1/documents', {
  method: 'POST',
  body: formData,
  headers: { 'X-Correlation-ID': workflowId }
});

// Step 2: Query document content via LLM
const result = await fetch('/api/v1/llm/query', {
  method: 'POST',
  body: JSON.stringify({ query: "Summarize this document", applicationId }),
  headers: { 'X-Correlation-ID': workflowId }
});
```

### 2. Compliance Automation

```
Agent → Check PCI Compliance → Export GDPR Data → Generate Report
```

### 3. User Provisioning

```
Agent → Create User → Assign Roles → Audit Trail
```

### 4. Batch Document Operations

```
Agent → List Documents → Filter by Classification → Bulk Reclassify → Audit
```

## LLM Service Integration

### Natural Language Queries

The LLM service accepts natural language queries and returns structured results:

```json
{
  "query": "Find all documents containing personal data created in the last 30 days",
  "filters": {
    "classifications": ["CONFIDENTIAL", "RESTRICTED"],
    "dateRange": { "start": "2026-01-15", "end": "2026-02-14" }
  },
  "maxResults": 50,
  "includeSummary": true
}
```

### Compliance Queries

For compliance-focused analysis:
```json
{
  "query": "Which documents may contain PCI card data?",
  "mode": "compliance"
}
```

## Authentication for Agents

### Development/Testing

In dev/docker mode, authentication is bypassed. Agents can call APIs directly.

### Production

Use Azure AD service principal with client credentials:

```bash
# Get token
TOKEN=$(curl -X POST https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token \
  -d "client_id={agent-client-id}&client_secret={secret}&scope=api://{api-id}/.default&grant_type=client_credentials")

# Use token
curl -H "Authorization: Bearer $TOKEN" http://dms-api/api/v1/documents
```

## Monitoring Agent Activity

All agent interactions are fully audited:
- Every API call creates an audit log entry
- Correlation IDs link related operations
- Query the audit service to track agent activity:

```
GET /api/v1/audit/logs?eventCategory=LLM_QUERIES
GET /api/v1/audit/logs/{correlationId}
```

## Rate Limiting

The LLM service implements rate limiting to prevent abuse:
- Default: 100 queries per minute per application
- Configurable per application via admin service

## Best Practices for Agent Integration

1. **Always use correlation IDs** — enables tracing through the entire system
2. **Handle pagination** — use `page` and `size` parameters for list endpoints
3. **Check health endpoints** — verify service availability before starting workflows
4. **Use appropriate classifications** — classify documents correctly for compliance
5. **Respect rate limits** — implement exponential backoff on 429 responses
6. **Log agent decisions** — use the audit service to record agent decision rationale
7. **Validate inputs** — validate data before sending to avoid 400 errors
8. **Use idempotent operations** — design workflows to be safely retryable
