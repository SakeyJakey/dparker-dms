---
Last Updated: 2026-02-14T17:00:00Z
Updated By: davidparker-lv-bmth
---

# DMS API Reference

## Base URLs

| Service | URL |
|---------|-----|
| Admin Service | `http://localhost:8081/api/v1` |
| Audit Service | `http://localhost:8082/api/v1` |
| Document Service | `http://localhost:8083/api/v1` |
| Compliance Service | `http://localhost:8084/api/v1` |
| LLM Service | `http://localhost:8085/api/v1` |

## Authentication

All endpoints (except health checks) require a valid Azure AD JWT token in production mode.
In dev/docker mode, authentication is bypassed.

**Header**: `Authorization: Bearer <jwt-token>`

---

## Admin Service API

### Users

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/admin/users?page=0&size=20` | List users (paginated) | DMS.Admin |
| GET | `/admin/users/{id}` | Get user by ID | DMS.Admin |
| POST | `/admin/users` | Create user | DMS.Admin |
| PUT | `/admin/users/{id}` | Update user | DMS.Admin |
| DELETE | `/admin/users/{id}` | Delete user | DMS.Admin |
| PUT | `/admin/users/{id}/enable` | Enable user | DMS.Admin |
| PUT | `/admin/users/{id}/disable` | Disable user | DMS.Admin |
| POST | `/admin/users/{id}/roles?roleId={uuid}` | Assign role | DMS.Admin |
| DELETE | `/admin/users/{id}/roles/{roleId}` | Remove role | DMS.Admin |

**Create User Request Body:**
```json
{
  "username": "string (required)",
  "email": "string (required, valid email)",
  "displayName": "string (optional)"
}
```

### Roles

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/admin/roles?page=0&size=20` | List roles | DMS.Admin |
| GET | `/admin/roles/{id}` | Get role by ID | DMS.Admin |
| POST | `/admin/roles` | Create role | DMS.Admin |
| PUT | `/admin/roles/{id}` | Update role | DMS.Admin |
| DELETE | `/admin/roles/{id}` | Delete role | DMS.Admin |
| POST | `/admin/roles/{id}/permissions?permissionId={uuid}` | Assign permission | DMS.Admin |
| DELETE | `/admin/roles/{id}/permissions/{permId}` | Remove permission | DMS.Admin |

### Permissions

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/admin/permissions?page=0&size=100` | List permissions | DMS.Admin |
| GET | `/admin/permissions/{id}` | Get permission | DMS.Admin |
| POST | `/admin/permissions` | Create permission | DMS.Admin |
| DELETE | `/admin/permissions/{id}` | Delete permission | DMS.Admin |

### Applications

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/admin/applications?page=0&size=20` | List applications | DMS.Admin |
| GET | `/admin/applications/{id}` | Get application | DMS.Admin |
| POST | `/admin/applications` | Provision application | DMS.Admin |
| PUT | `/admin/applications/{id}/status?status=ACTIVE` | Update status | DMS.Admin |
| DELETE | `/admin/applications/{id}` | Deprovision application | DMS.Admin |

### Dashboard

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/admin/dashboard` | Get dashboard stats | DMS.Admin |

---

## Document Service API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/documents?applicationId={uuid}&page=0&size=20` | List documents | DMS.User |
| GET | `/documents?applicationId={uuid}&classification=INTERNAL` | List with filter | DMS.User |
| GET | `/documents/{id}` | Get document details | DMS.User |
| POST | `/documents` (multipart) | Upload document | DMS.User |
| PUT | `/documents/{id}` | Update document metadata | DMS.User |
| DELETE | `/documents/{id}` | Delete document | DMS.User |
| GET | `/documents/{id}/download` | Download document file | DMS.User |

**Upload (multipart/form-data):**
- `file`: File binary (required)
- `name`: Document name (required)
- `classification`: PUBLIC|INTERNAL|CONFIDENTIAL|RESTRICTED|PCI (required)

**Document Classifications:**
- `PUBLIC` — Available to all users
- `INTERNAL` — Internal use only
- `CONFIDENTIAL` — Restricted access
- `RESTRICTED` — Highly restricted access
- `PCI` — PCI-DSS relevant data

---

## Audit Service API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/audit/events` | Log audit event | Internal |
| GET | `/audit/logs?page=0&size=20` | Get audit logs | DMS.Admin |
| GET | `/audit/logs?eventType=CREATE` | Filter by type | DMS.Admin |
| GET | `/audit/logs?applicationId={uuid}` | Filter by app | DMS.Admin |
| GET | `/audit/logs?startTime=...&endTime=...` | Filter by time | DMS.Admin |
| GET | `/audit/logs/{correlationId}` | Get by correlation | DMS.Admin |

---

## Compliance Service API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/compliance/pci/report?period=MONTHLY` | PCI compliance report | DMS.Admin |
| GET | `/compliance/gdpr/data-subject/{id}` | Export data subject data | DMS.Admin |
| DELETE | `/compliance/gdpr/data-subject/{id}` | Request data erasure | DMS.Admin |
| GET | `/compliance/iso27001/controls` | ISO 27001 controls status | DMS.Admin |

---

## LLM Service API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/llm/query` | Execute natural language query | DMS.LLM.Service |
| POST | `/llm/compliance-check` | Compliance-focused query | DMS.LLM.Service |

**Query Request Body:**
```json
{
  "query": "Find documents about financial compliance",
  "filters": {
    "classifications": ["INTERNAL", "CONFIDENTIAL"],
    "dateRange": { "start": "2026-01-01", "end": "2026-12-31" }
  },
  "maxResults": 10,
  "includeSummary": true,
  "applicationId": "uuid (optional)"
}
```

**Query Response:**
```json
{
  "correlationId": "uuid",
  "summary": "Found 3 documents matching your query",
  "results": [
    { "documentId": "uuid", "name": "...", "relevanceScore": 0.95 }
  ],
  "totalCount": 3
}
```

---

## Document Workflow API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/documents/{id}/workflow` | Get current workflow status |
| GET | `/documents/{id}/workflow/history` | Get workflow history |
| POST | `/documents/{id}/workflow/transition` | Transition workflow state |
| POST | `/documents/{id}/workflow/submit-for-review` | Submit for review |
| POST | `/documents/{id}/workflow/approve` | Approve document |
| POST | `/documents/{id}/workflow/reject` | Reject document |
| POST | `/documents/{id}/workflow/publish` | Publish document |
| POST | `/documents/{id}/workflow/archive` | Archive document |

**Workflow States**: DRAFT → REVIEW → APPROVED → PUBLISHED → ARCHIVED (REJECTED → DRAFT)

---

## Bulk Operations API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/documents/bulk/operation` | Execute bulk action (delete, classify, tag, archive) |
| POST | `/documents/bulk/upload` | Upload multiple files at once |

---

## Search API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/documents/search` | Advanced search with filters |
| GET | `/documents/search/fulltext?query=...` | Full-text keyword search |

---

## Collaboration API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/documents/{id}/comments` | Get document comments |
| POST | `/documents/{id}/comments` | Add comment (supports threading) |
| DELETE | `/documents/{id}/comments/{commentId}` | Delete comment |
| GET | `/documents/{id}/shares` | Get document shares |
| POST | `/documents/{id}/shares` | Share document with user |
| DELETE | `/documents/{id}/shares/{shareId}` | Revoke share |
| GET | `/documents/favorites?userId=...` | Get user's favorites |
| POST | `/documents/{id}/favorites?userId=...` | Add to favorites |
| DELETE | `/documents/{id}/favorites?userId=...` | Remove from favorites |
| GET | `/documents/recent?userId=...` | Get recent documents |

---

## Templates API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/documents/templates` | List templates |
| GET | `/documents/templates/{id}` | Get template |
| POST | `/documents/templates` | Create template |
| PUT | `/documents/templates/{id}` | Update template |
| DELETE | `/documents/templates/{id}` | Delete template |

---

## Preview API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/documents/{id}/preview` | Get document preview |
| GET | `/documents/{id}/preview/info` | Get preview metadata |

---

## Analytics API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/documents/analytics/dashboard` | Dashboard analytics (stats, trends) |

---

## Webhook Management API (Admin Service)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/webhooks` | List webhooks |
| POST | `/admin/webhooks` | Create webhook |
| PUT | `/admin/webhooks/{id}` | Update webhook |
| DELETE | `/admin/webhooks/{id}` | Delete webhook |
| POST | `/admin/webhooks/{id}/test` | Test webhook delivery |

---

## API Key Management (Admin Service)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/api-keys` | List API keys |
| POST | `/admin/api-keys` | Create API key (returns key once) |
| DELETE | `/admin/api-keys/{id}` | Revoke API key |
| POST | `/admin/api-keys/{id}/regenerate` | Regenerate API key |

---

## Export API (Admin Service)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/export/users/csv` | Export users as CSV |
| GET | `/admin/export/audit/csv` | Export audit logs as CSV |
| GET | `/admin/export/compliance/report?format=csv` | Export compliance report |

---

## Health Endpoints

All services expose:
- `GET /actuator/health` — Service health status
- `GET /actuator/info` — Service information

---

## Error Responses

```json
{
  "error": "Error description",
  "status": 400,
  "message": "Detailed message"
}
```

| Status | Meaning |
|--------|---------|
| 400 | Bad Request / Validation Error |
| 401 | Unauthorized (missing/invalid token) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Resource Not Found |
| 500 | Internal Server Error |
