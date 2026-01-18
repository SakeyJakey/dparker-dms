---
Last Updated: 2024-01-18T13:32:00Z
Updated By: davidparker-lv-bmth
---

# DMS Code Organization

## Service-Based Architecture

The codebase is organized into service modules following the `dms-{{function}}-service` naming convention:

### Service Modules

1. **dms-core-service** - Core shared components
   - Models (Document, User, Role, Permission, etc.)
   - Repositories
   - Security (JWT, filters)
   - Configuration (Security, KeyVault, Azure, etc.)
   - Application context and isolation

2. **dms-document-service** - Document management
   - DocumentController
   - DocumentService
   - Document versioning
   - Storage integration

3. **dms-admin-service** - Administration and management
   - AdminController
   - UserManagementController/Service
   - RoleManagementController/Service
   - PermissionManagementController/Service
   - ApplicationManagementController/Service

4. **dms-audit-service** - Audit logging
   - AuditController
   - AuditService
   - Event publishing

5. **dms-compliance-service** - Compliance framework
   - ComplianceController
   - PciDssComplianceService
   - GdprComplianceService
   - Iso27001Controls

6. **dms-llm-service** - AI/LLM integration
   - LlmQueryController
   - SecureLlmQueryService
   - DocumentEmbeddingService
   - LLM security controls

## Current Structure

```
src/main/java/com/davidparker/dms/
├── DmsApplication.java
├── dms-core-service/
│   ├── model/          # All entity models
│   ├── repository/     # All repositories
│   ├── config/         # Configuration classes
│   └── security/       # Security components
├── dms-document-service/
│   ├── controller/     # DocumentController
│   └── service/        # DocumentService
├── dms-admin-service/
│   ├── controller/     # Admin, User, Role, Permission, Application controllers
│   ├── service/        # Management services
│   └── dto/            # Request/Response DTOs
├── dms-audit-service/
│   ├── controller/     # AuditController
│   └── service/        # AuditService
├── dms-compliance-service/
│   ├── controller/     # ComplianceController
│   └── service/        # Compliance services
└── dms-llm-service/
    ├── controller/     # LlmQueryController
    └── service/        # LLM and embedding services
```

## Migration Notes

The codebase currently uses a flat structure. To migrate to the service-based structure:

1. Move models to `dms-core-service/model/`
2. Move repositories to `dms-core-service/repository/`
3. Move configs to `dms-core-service/config/`
4. Move security to `dms-core-service/security/`
5. Move document-related code to `dms-document-service/`
6. Move admin code to `dms-admin-service/`
7. Move audit code to `dms-audit-service/`
8. Move compliance code to `dms-compliance-service/`
9. Move LLM code to `dms-llm-service/`

Update all package declarations and imports accordingly.
