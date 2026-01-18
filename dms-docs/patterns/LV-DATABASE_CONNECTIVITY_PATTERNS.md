# LV Database Connectivity Patterns

This document describes the standard LV patterns for database connectivity in microservices applications.

## Overview

LV projects use standardized environment variables for database connectivity across all services and environments.

## Environment Variable Standards

### Standard Variable Names

**LV Standard** (used in all LV projects):
- `SPRING_DATASOURCE_URL` - Full JDBC connection string
- `DB_USER` - Database username
- `DB_PASSWORD` - Database password

**NOT Used**:
- `database-connection-string` ❌
- `database-username` ❌
- `database-password` ❌
- `DATABASE_URL` ❌ (too generic)

## Application Configuration

### application.yml Pattern

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/${DB_NAME}}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
```

### Docker Compose Pattern

```yaml
environment:
  SPRING_PROFILES_ACTIVE: docker
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-${SERVICE}:5432/${DB_NAME}
  DB_USER: postgres
  DB_PASSWORD: postgres
```

### Kubernetes ConfigMap Pattern

**Database ConfigMap** (`dms-database-configmap.yaml`):
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: dms-database-config
  namespace: dms-dev
data:
  SPRING_DATASOURCE_URL_ADMIN: "jdbc:postgresql://${DB_HOST}:5432/dms_admin"
  SPRING_DATASOURCE_URL_AUDIT: "jdbc:postgresql://${DB_HOST}:5432/dms_audit"
  SPRING_DATASOURCE_URL_DOCUMENT: "jdbc:postgresql://${DB_HOST}:5432/dms_document"
  DB_USER: "${DB_USER}"
```

**Kubernetes Deployment**:
```yaml
env:
- name: SPRING_DATASOURCE_URL
  valueFrom:
    secretKeyRef:
      name: dms-secrets
      key: database-url
- name: DB_USER
  valueFrom:
    secretKeyRef:
      name: dms-secrets
      key: db-user
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: dms-secrets
      key: db-password
```

## Database Connection String Formats

### PostgreSQL

**Local Development**:
```
jdbc:postgresql://localhost:5432/dms_admin
```

**Docker Compose**:
```
jdbc:postgresql://postgres-admin:5432/dms_admin
```

**Kubernetes**:
```
jdbc:postgresql://postgres-service.dms-dev.svc.cluster.local:5432/dms_admin
```

### H2 (Development Only)

**In-Memory**:
```
jdbc:h2:mem:dms_admin;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
```

## Connection Pool Configuration

### Production Settings

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

### Development Settings

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
```

## Migration Management

### Flyway Configuration

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

### Production Profile

```yaml
spring:
  flyway:
    enabled: true
    contexts: production  # Exclude dev migrations
    drop-first: false  # NEVER true in production
```

## Security Considerations

1. **Never commit passwords**: Use environment variables or secrets
2. **Use Key Vault**: Store credentials in Azure Key Vault for production
3. **Separate databases**: Each service should have its own database
4. **Connection encryption**: Use SSL/TLS in production

## References

- Reference implementations:
  - `/Users/davidparker/Documents/LV-Code/dparker-rti-now/TOBE-Java-RTI-Angular/docker-compose.yml`
  - `/Users/davidparker/Documents/LV-Code/dparker-mendix-test/docker-compose.yml`
