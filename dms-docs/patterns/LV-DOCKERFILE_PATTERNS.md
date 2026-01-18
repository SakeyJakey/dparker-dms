# LV Dockerfile Patterns

This document describes the standard LV patterns for Dockerfiles in microservices applications.

## Overview

LV projects use three types of Dockerfiles:
1. **Dockerfile** - Standard build (for docker-compose)
2. **Dockerfile.dev** - Development build (builds within Docker)
3. **Dockerfile.prod** - Production build (expects pre-built JAR from CI)

## Dockerfile (Standard)

Used for docker-compose and local development.

**Pattern**:
```dockerfile
# Multi-stage build for Spring Boot service
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Copy POM file
COPY pom.xml .

# Copy source code  
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:25-jre

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built JAR
COPY --from=build /app/target/${SERVICE_NAME}-*.jar app.jar

# Expose port (unique for docker-compose)
EXPOSE ${PORT}

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:${PORT}/actuator/health || exit 1

# Run the application with spring.classformat.ignore for Java 25
ENTRYPOINT ["java", "-Dspring.classformat.ignore=true", "-jar", "app.jar"]
```

## Dockerfile.dev

Development Dockerfile - builds within Docker, uses docker profile.

**Pattern**:
```dockerfile
# Development Dockerfile - builds within Docker
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/${SERVICE_NAME}-*.jar app.jar

EXPOSE ${PORT}

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:${PORT}/actuator/health || exit 1

ENTRYPOINT ["java", "-Dspring.classformat.ignore=true", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
```

## Dockerfile.prod

Production Dockerfile - expects pre-built JAR from CI pipeline, includes OpenTelemetry.

**Pattern**:
```dockerfile
# Production Dockerfile - expects pre-built JAR from CI
FROM busybox AS downloader
RUN wget -O /opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

FROM mcr.microsoft.com/openjdk/jdk:25-distroless
EXPOSE 8080
ARG version

ENV TZ=Europe/London
ENV SPRING_PROFILES_ACTIVE=prod
ENV OTEL_TRACES_EXPORTER=otlp
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://alloy.observability.svc.cluster.local:4317
ENV OTEL_EXPORTER_OTLP_PROTOCOL=grpc
ENV OTEL_JAVAAGENT_ENABLED=true
ENV OTEL_INSTRUMENTATION_JDBC_ENABLED=true

COPY --from=downloader /opentelemetry-javaagent.jar opentelemetry-javaagent.jar
COPY *.yml .
COPY *.xml .
COPY ${SERVICE_NAME}-${version}.jar ${SERVICE_NAME}.jar

CMD ["-javaagent:opentelemetry-javaagent.jar", "-Dotel.service.name=${SERVICE_NAME}", "-Dspring.profiles.active=prod", "-jar", "${SERVICE_NAME}.jar"]
```

## Frontend Dockerfile Pattern

**Dockerfile**:
```dockerfile
# Stage 1: Build the Angular application
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY ./ /app

# Build the application
RUN npm run build

# Stage 2: Serve with nginx
FROM nginx:alpine

# Install gettext for envsubst
RUN apk add --no-cache gettext

# Copy built application
COPY --from=build /app/dist/${FRONTEND_NAME}/browser /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Expose port 8080 (standard for Kubernetes)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/health || exit 1

CMD ["/entrypoint.sh"]
```

**Dockerfile.prod**:
```dockerfile
# Multi-stage build for Angular frontend (Production)
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files first for better caching
COPY package*.json ./

# Install dependencies (use npm ci for production builds)
RUN npm ci

# Copy source files
COPY . .

# Build the application for production
RUN npm run build

# Production stage
FROM nginx:alpine

# Install gettext for envsubst
RUN apk add --no-cache gettext

# Copy built files
COPY --from=build /app/dist/${FRONTEND_NAME}/browser /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Remove default nginx config files
RUN rm -f /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/*.conf 2>/dev/null || true

# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Expose port 8080
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/health || exit 1

CMD ["/entrypoint.sh"]
```

## Key Principles

1. **Multi-stage builds**: Separate build and runtime stages
2. **Health checks**: Always include HEALTHCHECK directive
3. **Java 25 compatibility**: Use `-Dspring.classformat.ignore=true`
4. **OpenTelemetry**: Required in production Dockerfiles
5. **Distroless images**: Use for production (smaller, more secure)
6. **Port standardization**: 8080 in K8s, unique ports in docker-compose

## Port Assignments

| Service Type | Docker Compose Port | Kubernetes Port |
|-------------|-------------------|-----------------|
| Admin Service | 8081 | 8080 |
| Audit Service | 8082 | 8080 |
| Document Service | 8083 | 8080 |
| Compliance Service | 8084 | 8080 |
| LLM Service | 8085 | 8080 |
| API Gateway | 8080 | 8080 |
| Frontend | 8080 | 8080 |

## Health Check Patterns

**Backend Services** (with curl):
```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:${PORT}/actuator/health || exit 1
```

**Frontend Services** (with wget):
```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/health || exit 1
```

## References

- Reference implementations:
  - `/Users/davidparker/Documents/LV-Code/dparker-rti-now/TOBE-Java-RTI-Angular/rti-submission-service/Dockerfile.prod`
  - `/Users/davidparker/Documents/LV-Code/dparker-mendix-test/repository-templates/sickpay-auth-service/Dockerfile.dev`
