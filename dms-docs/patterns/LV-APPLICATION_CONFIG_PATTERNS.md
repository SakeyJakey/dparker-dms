# LV Application Configuration Patterns

This document describes the standard LV patterns for Spring Boot application configuration files.

## Overview

Each service should have multiple application YAML files for different environments:

| File | Purpose | Database | Port | Auth |
|------|---------|----------|------|------|
| `application.yml` | Base config | PostgreSQL (default) | 8080 | Required |
| `application-dev.yml` | Local dev | H2 in-memory | Unique | Bypassed |
| `application-docker.yml` | Docker Compose | PostgreSQL | Unique | Bypassed |
| `application-prod.yml` | Production/K8s | PostgreSQL | 8080 | Required |
| `application-test.yml` | Tests | H2 in-memory | Random | Bypassed |

## application.yml (Base Configuration)

Base configuration with profile-agnostic defaults.

**Pattern**:
```yaml
spring:
  application:
    name: ${SERVICE_NAME}
  
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/${DB_NAME}}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

server:
  port: ${PORT:8080}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized

# Azure AD Configuration
azure:
  tenant-id: ${AZURE_TENANT_ID:}
  client-id: ${AZURE_CLIENT_ID:}
  jwk-set-uri: ${AZURE_JWK_SET_URI:}

logging:
  level:
    root: INFO
    com.davidparker.dms: DEBUG
```

## application-dev.yml (Development Profile)

Local development with H2 database and authentication bypass.

**Pattern**:
```yaml
# Development profile - local development with H2
spring:
  datasource:
    url: jdbc:h2:mem:${DB_NAME};DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect

server:
  port: ${DEV_PORT}  # Unique per service

# Bypass authentication in dev mode
security:
  bypass-auth: true

logging:
  level:
    com.davidparker.dms.${SERVICE}: DEBUG
    org.springframework.security: DEBUG
```

## application-docker.yml (Docker Compose Profile)

Docker Compose environment with PostgreSQL and authentication bypass.

**Pattern**:
```yaml
# Docker Compose profile
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://postgres-${SERVICE}:5432/${DB_NAME}}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver

server:
  port: ${DOCKER_PORT}  # Unique per service

# Bypass authentication in docker mode for local testing
security:
  bypass-auth: true

logging:
  level:
    com.davidparker.dms.${SERVICE}: DEBUG
```

## application-prod.yml (Production Profile)

Production/Kubernetes deployment with full security and observability.

**Pattern**:
```yaml
# Production profile - Kubernetes deployment
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    show-sql: false
  
  h2:
    console:
      enabled: false

# Azure AD Configuration
azure:
  tenant-id: ${AZURE_TENANT_ID}
  client-id: ${AZURE_CLIENT_ID}
  jwk-set-uri: ${AZURE_JWK_SET_URI:https://login.microsoftonline.com/${AZURE_TENANT_ID}/discovery/v2.0/keys}

server:
  port: 8080
  error:
    include-stacktrace: never
    include-message: never
    include-exception: false

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  health:
    group:
      liveness:
        include: ping
      readiness:
        include: db,diskSpace

logging:
  level:
    root: INFO
    com.davidparker.dms: INFO
    org.springframework.security: WARN
```

## application-test.yml (Test Profile)

Test configuration with H2 in-memory database.

**Pattern**:
```yaml
# Test profile - H2 in-memory database
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect

server:
  port: 0  # Random port for tests

# Bypass authentication in test mode
security:
  bypass-auth: true
```

## Environment Variable Standards

### Database Variables

**LV Standard**:
- `SPRING_DATASOURCE_URL` - Full JDBC connection string
- `DB_USER` - Database username
- `DB_PASSWORD` - Database password

**NOT**:
- `database-connection-string` ❌
- `database-username` ❌
- `database-password` ❌

### Service URLs

**LV Standard**:
- `ADMIN_SERVICE_URL`
- `AUDIT_SERVICE_URL`
- `DOCUMENT_SERVICE_URL`
- etc.

## Profile Activation

Profiles are activated via:
- Environment variable: `SPRING_PROFILES_ACTIVE`
- Docker Compose: `SPRING_PROFILES_ACTIVE=docker`
- Kubernetes: `SPRING_PROFILES_ACTIVE=prod`
- Local dev: `SPRING_PROFILES_ACTIVE=dev`

## References

- Reference implementations:
  - `/Users/davidparker/Documents/LV-Code/dparker-mendix-test/repository-templates/sickpay-auth-service/src/main/resources/application-prod.yml`
  - `/Users/davidparker/Documents/LV-Code/dparker-rti-now/TOBE-Java-RTI-Angular/rti-submission-service/src/main/resources/application-prod.yml`
