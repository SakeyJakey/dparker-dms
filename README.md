# Document Management System (DMS)

Enterprise-grade Document Management System with RBAC, multi-application isolation, LLM query support, and PCI-DSS/ISO 27001/GDPR compliance.

## Repository Structure

This monorepo contains all DMS services and will be split into individual repositories:
- `dms-admin-service/` - Administration service
- `dms-audit-service/` - Audit logging service
- `dms-compliance-service/` - Compliance service
- `dms-document-service/` - Document management service
- `dms-llm-service/` - LLM/AI service
- `dms-frontend-service/` - Angular frontend
- `dms-core-service/` - Shared library
- `dms-e2e-tests/` - End-to-end test suite
- `dms-docs/` - Documentation and reference configurations

## Technology Stack

- **Java**: 25 LTS
- **Spring Boot**: 3.4.x
- **Angular**: 21
- **PostgreSQL**: 16.x
- **Redis**: 7.x
- **Azure Services**: Key Vault, Blob Storage, AI Search, Event Hubs
- **Docker**: Containerization
- **Kubernetes**: Container orchestration

## Quick Start with Docker Compose

### Prerequisites

- Docker and Docker Compose
- (Optional) Azure subscription for production features

### Running All Services

1. **Start all services**:
   ```bash
   docker-compose up -d
   ```

2. **Or use the setup script**:
   ```bash
   ./dms-docs/scripts/setup-local-dev.sh
   ```

3. **Access services**:
   - Frontend: http://localhost:80
   - Admin Service: http://localhost:8081
   - Audit Service: http://localhost:8082
   - Document Service: http://localhost:8083
   - Compliance Service: http://localhost:8084
   - LLM Service: http://localhost:8085

4. **Stop services**:
   ```bash
   docker-compose down
   # Or
   ./dms-docs/scripts/stop-services.sh
   ```

### Service Health Checks

Check service health:
```bash
docker-compose ps
```

View logs:
```bash
docker-compose logs -f [service-name]
```

## Individual Service Development

### Backend Services (Java/Spring Boot)

```bash
cd dms-admin-service
mvn spring-boot:run
```

### Frontend Service (Angular)

```bash
cd dms-frontend-service
npm install
npm start
```

## Documentation

All documentation is in the `dms-docs/` folder:
- Architecture and design documents
- User guides
- API documentation
- Reference configurations (Kubernetes, Flux, Istio)
- Development scripts and utilities

See `dms-docs/README.md` for more information.

## Application Identifier

All application identifiers use `davidparker-lv-bmth` as specified in `.cursorrules`.

## License

Proprietary - Internal Use Only
