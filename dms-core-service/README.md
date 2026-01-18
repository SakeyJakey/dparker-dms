# DMS Core Service

Shared library containing common components for all DMS microservices.

## Purpose

This module provides shared components that can be used across all DMS microservices to:
- Reduce code duplication
- Ensure consistency across services
- Provide common security configurations
- Share utility classes and DTOs

## Components

### Security
- `AadJwtAuthenticationConverter` - Azure AD JWT authentication converter
- `SecurityConfigBase` - Base security configuration

### Configuration
- `KeyVaultConfig` - Azure Key Vault configuration

### Utilities
- `JsonConverter` - JPA JSON/JSONB converter
- `SecureCredentialService` - Key Vault key retrieval service

### Exceptions
- `DmsException` - Base exception class
- `ResourceNotFoundException` - Resource not found exception
- `UnauthorizedException` - Unauthorized access exception

### DTOs
- `AuditEventDto` - Audit event data transfer object

### Clients
- `AuditEventClient` - Client for sending audit events to audit service

## Usage

Add this module as a dependency in your service's `pom.xml`:

```xml
<dependency>
    <groupId>com.davidparker.dms</groupId>
    <artifactId>dms-core-service</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Building

```bash
mvn clean install
```

This will install the artifact to your local Maven repository, making it available to other services.

## Note

This is a library module (packaging: jar), not a standalone service. It provides shared code that other services can depend on while maintaining service independence.
