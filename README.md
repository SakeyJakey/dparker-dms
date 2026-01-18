# Document Management System (DMS) API

Enterprise-grade Document Management System with RBAC, multi-application isolation, LLM query support, and PCI-DSS/ISO 27001/GDPR compliance.

## Technology Stack

- **Java**: 25 LTS
- **Spring Boot**: 3.4.x
- **PostgreSQL**: 16.x
- **Redis**: 7.x
- **Azure Services**: Key Vault, Blob Storage, AI Search, Event Hubs

## Getting Started

### Prerequisites

- Java 25 LTS
- Maven 3.8+
- PostgreSQL 16
- Redis 7
- Azure subscription with required services

### Configuration

1. Set environment variables:
   - `ENVIRONMENT`: Deployment environment (dev, staging, prod)
   - `database-connection-string`: PostgreSQL connection string
   - `redis-host`, `redis-port`, `redis-password`: Redis configuration
   - Azure service credentials (stored in Key Vault)

2. Configure Azure Key Vault:
   - Create Key Vault: `dms-keyvault-{env}`
   - Store all secrets as specified in the architecture plan

### Running the Application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### API Documentation

Once running, access Swagger UI at:
- `http://localhost:8080/swagger-ui.html`

## Application Identifier

All application identifiers use `davidparker-lv-bmth` as specified in `.cursorrules`.

## License

Proprietary - Internal Use Only
