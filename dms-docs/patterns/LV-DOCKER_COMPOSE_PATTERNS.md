# LV Docker Compose Patterns

This document describes the standard LV patterns for Docker Compose configuration.

## Overview

Docker Compose files should follow consistent patterns for:
- Environment variables
- Health checks
- Service dependencies
- Network configuration

## Environment Variable Standards

### Database Variables

**LV Standard**:
```yaml
environment:
  SPRING_PROFILES_ACTIVE: docker
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-${SERVICE}:5432/${DB_NAME}
  DB_USER: postgres
  DB_PASSWORD: postgres
```

**NOT**:
```yaml
environment:
  database-connection-string: jdbc:postgresql://...  # ❌ Old pattern
  database-username: postgres  # ❌ Old pattern
  database-password: postgres  # ❌ Old pattern
```

### Service URL Variables

```yaml
environment:
  ADMIN_SERVICE_URL: http://dms-admin-service:8081
  AUDIT_SERVICE_URL: http://dms-audit-service:8082
  DOCUMENT_SERVICE_URL: http://dms-document-service:8083
```

## Health Check Patterns

### Backend Services

**Standard Pattern** (using wget - more reliable in alpine):
```yaml
healthcheck:
  test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:${PORT}/actuator/health || exit 1"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

**Alternative** (using curl - requires curl installed):
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:${PORT}/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

### Frontend Services

```yaml
healthcheck:
  test: ["CMD-SHELL", "wget --quiet --tries=1 --spider http://localhost:8080/health || exit 1"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 30s
```

## Service Dependencies

### Database Dependencies

```yaml
depends_on:
  postgres-${SERVICE}:
    condition: service_healthy
```

### Service Dependencies

```yaml
depends_on:
  dms-admin-service:
    condition: service_healthy
  dms-audit-service:
    condition: service_healthy
```

## Port Assignments

| Service | Internal Port | External Port |
|---------|--------------|---------------|
| Admin | 8081 | 8081 |
| Audit | 8082 | 8082 |
| Document | 8083 | 8083 |
| Compliance | 8084 | 8084 |
| LLM | 8085 | 8085 |
| API Gateway | 8080 | 8080 |
| Frontend | 8080 | 8080 |

## Network Configuration

```yaml
networks:
  - ${APP}-network

# At bottom of file
networks:
  ${APP}-network:
    driver: bridge
```

## Volume Configuration

```yaml
volumes:
  postgres-${SERVICE}-data:
  redis-data:

# At bottom of file
volumes:
  postgres-${SERVICE}-data:
  redis-data:
```

## Complete Service Example

```yaml
dms-admin-service:
  build:
    context: ./dms-admin-service
    dockerfile: Dockerfile
  container_name: dms-admin-service
  environment:
    PORT: 8081
    SPRING_PROFILES_ACTIVE: docker
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-admin:5432/dms_admin
    DB_USER: postgres
    DB_PASSWORD: postgres
    AUDIT_SERVICE_URL: http://dms-audit-service:8082
  ports:
    - "8081:8081"
  depends_on:
    postgres-admin:
      condition: service_healthy
    redis:
      condition: service_healthy
  networks:
    - dms-network
  healthcheck:
    test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1"]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 60s
```

## Frontend Service Pattern

```yaml
dms-frontend-service:
  build:
    context: ./dms-frontend-service
    dockerfile: Dockerfile
  container_name: dms-frontend-service
  environment:
    API_GATEWAY_URL: http://dms-api-gateway-service:8080
  ports:
    - "8080:8080"
  depends_on:
    - dms-api-gateway-service
  networks:
    - dms-network
  healthcheck:
    test: ["CMD-SHELL", "wget --quiet --tries=1 --spider http://localhost:8080/health || exit 1"]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 30s
```

## Best Practices

1. **Use wget for health checks**: More reliable in alpine-based images
2. **Standardize env vars**: Use `SPRING_DATASOURCE_URL`, `DB_USER`, `DB_PASSWORD`
3. **Set SPRING_PROFILES_ACTIVE**: Always set to `docker` for docker-compose
4. **Health check dependencies**: Use `condition: service_healthy` for proper startup order
5. **Unique ports**: Use unique ports per service for easier debugging
6. **Network isolation**: Use dedicated network for service communication

## References

- Reference implementations:
  - `/Users/davidparker/Documents/LV-Code/dparker-rti-now/TOBE-Java-RTI-Angular/docker-compose.yml`
  - `/Users/davidparker/Documents/LV-Code/dparker-mendix-test/docker-compose.yml`
