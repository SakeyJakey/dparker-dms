---
Last Updated: 2026-02-14T15:30:00Z
Updated By: davidparker-lv-bmth
---

# DMS Developer Guide

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Structure](#project-structure)
3. [Local Development Setup](#local-development-setup)
4. [Building Services](#building-services)
5. [Running Tests](#running-tests)
6. [Frontend Development](#frontend-development)
7. [E2E Testing](#e2e-testing)
8. [Configuration](#configuration)
9. [Common Issues](#common-issues)

---

## Prerequisites

- **Java 21+** (OpenJDK 21 recommended)
- **Maven 3.8+**
- **Node.js 20+** and **npm 10+**
- **Docker** (optional, for Docker Compose deployment)
- **PostgreSQL 16** (or Docker for databases)
- **Redis 7** (or Docker)

## Project Structure

```
dparker-dms/
├── pom.xml                          # Parent POM
├── docker-compose.yml               # Docker Compose for all services
├── dms-core-service/                # Shared library (JAR)
├── dms-admin-service/               # User/Role/Permission/App management
├── dms-audit-service/               # Centralized audit logging
├── dms-document-service/            # Document CRUD + Azure Blob Storage
├── dms-compliance-service/          # PCI-DSS, GDPR, ISO 27001
├── dms-llm-service/                 # AI/LLM queries
├── dms-api-gateway-service/         # Spring Cloud Gateway
├── dms-frontend-service/            # Angular 21 SPA
├── dms-e2e-tests/                   # Playwright E2E tests
└── dms-docs/                        # Documentation
```

## Local Development Setup

### 1. Clone and Build

```bash
git clone https://github.com/davidparker-lv-bmth/dparker-dms.git
cd dparker-dms

# Build all backend services
mvn clean compile -DskipTests -Ddependency-check.skip=true

# Install frontend dependencies
cd dms-frontend-service
npm install --legacy-peer-deps
cd ..
```

### 2. Database Setup (without Docker)

Create three PostgreSQL databases:
```sql
CREATE DATABASE dms_admin;
CREATE DATABASE dms_document;
CREATE DATABASE dms_audit;
```

### 3. Run Individual Services

Each service can be run independently:

```bash
# Admin Service (port 8081)
cd dms-admin-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Audit Service (port 8082)
cd dms-audit-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Document Service (port 8083)
cd dms-document-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Compliance Service (port 8084)
cd dms-compliance-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# LLM Service (port 8085)
cd dms-llm-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Run with Docker Compose

```bash
docker compose up -d
```

## Building Services

### Backend (Maven)

```bash
# Build all modules
mvn clean package -DskipTests -Ddependency-check.skip=true

# Build specific service
mvn clean package -pl dms-admin-service -DskipTests -Ddependency-check.skip=true

# Build with tests
mvn clean verify -Ddependency-check.skip=true
```

### Frontend (Angular CLI)

```bash
cd dms-frontend-service

# Development build
npx ng build --configuration=development

# Production build
npx ng build --configuration=production

# Dev server (port 4200)
npx ng serve
```

## Running Tests

### Backend Unit Tests

```bash
# Run all tests
mvn test -Ddependency-check.skip=true

# Run specific service tests
mvn test -pl dms-admin-service -Ddependency-check.skip=true

# Run specific test class
mvn test -pl dms-admin-service -Dtest=UserManagementServiceTest

# Run with coverage report (JaCoCo)
mvn verify -Ddependency-check.skip=true
# Reports at: dms-*/target/site/jacoco/index.html
```

### Frontend Unit Tests

```bash
cd dms-frontend-service
npx ng test                    # Run tests with Karma
npx ng test --code-coverage    # With coverage report
```

### E2E Tests (Playwright)

```bash
cd dms-e2e-tests
npm install
npx playwright install         # Install browsers

# Run tests (requires frontend running at localhost:4200)
npx playwright test

# Run with browser visible
npx playwright test --headed

# View report
npx playwright show-report
```

## Configuration

### Spring Profiles

| Profile | Description | Database | Auth |
|---------|------------|----------|------|
| `dev` | Local development | H2 in-memory | Disabled |
| `docker` | Docker Compose | PostgreSQL | Disabled |
| `test` | Automated tests | H2 in-memory | Disabled |
| `prod` | Production | PostgreSQL | Azure AD |

### Environment Variables

| Variable | Description | Default |
|----------|------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `SPRING_DATASOURCE_URL` | JDBC connection URL | `jdbc:postgresql://localhost:5432/dms_*` |
| `DB_USER` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `AUDIT_SERVICE_URL` | Audit service URL | `http://localhost:8082` |
| `ADMIN_SERVICE_URL` | Admin service URL | `http://localhost:8081` |
| `AZURE_TENANT_ID` | Azure AD tenant ID | *(empty)* |
| `AZURE_CLIENT_ID` | Azure AD client ID | *(empty)* |

### Service Ports

| Service | Dev/Docker | Production |
|---------|-----------|------------|
| Frontend | 4200/8080 | 8080 |
| API Gateway | 8080 | 8080 |
| Admin | 8081 | 8080 |
| Audit | 8082 | 8080 |
| Document | 8083 | 8080 |
| Compliance | 8084 | 8080 |
| LLM | 8085 | 8080 |

## Common Issues

### Azure Key Vault Authentication Error
**Symptom**: `CredentialUnavailableException` on startup
**Fix**: Use `dev` or `docker` profile which disables Azure Key Vault

### Flyway Migration Fails with H2
**Symptom**: SQL syntax errors with H2 in-memory database
**Fix**: Use test profile which disables Flyway (`spring.flyway.enabled=false`)

### Angular Build Fails with TypeScript Version
**Symptom**: `Angular Compiler requires TypeScript >=5.9.0`
**Fix**: Run `npm install typescript@~5.9.2 --save-dev`

### Test Context Loading Failure
**Symptom**: `@WebMvcTest` fails to load context
**Fix**: Use `@ContextConfiguration(classes = {ControllerClass.class})` pattern with `@ActiveProfiles("test")`
