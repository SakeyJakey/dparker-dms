---
name: DMS LV Patterns Alignment
overview: Align all DMS services with LV standard patterns for Dockerfiles, application configurations, database connectivity, security, and Kubernetes deployment configurations based on reference implementations from dparker-rti-now, dparker-mendix-test, and dparker-demos projects.
todos:
  - id: pom-admin-parent
    content: "Update dms-admin-service/pom.xml: Change parent from spring-boot-starter-parent to dms-parent (com.davidparker.dms:dms-parent:1.0.0-SNAPSHOT)"
    status: completed
  - id: pom-audit-parent
    content: "Update dms-audit-service/pom.xml: Change parent to dms-parent, remove duplicated properties"
    status: completed
  - id: pom-document-parent
    content: "Update dms-document-service/pom.xml: Change parent to dms-parent"
    status: completed
  - id: pom-compliance-parent
    content: "Update dms-compliance-service/pom.xml: Change parent to dms-parent"
    status: completed
  - id: pom-llm-parent
    content: "Update dms-llm-service/pom.xml: Change parent to dms-parent"
    status: completed
  - id: pom-core-parent
    content: "Update dms-core-service/pom.xml: Change parent to dms-parent"
    status: completed
  - id: dockerfile-admin-update
    content: "Update dms-admin-service/Dockerfile: Use maven:3.9-eclipse-temurin-25, add health check, add curl install"
    status: completed
  - id: dockerfile-admin-dev
    content: "Create dms-admin-service/Dockerfile.dev: Multi-stage build with Maven, port 8081, health check"
    status: completed
  - id: dockerfile-admin-prod
    content: "Create dms-admin-service/Dockerfile.prod: Distroless + OpenTelemetry, expects pre-built JAR"
    status: completed
  - id: dockerfile-audit-update
    content: "Update dms-audit-service/Dockerfile: Same pattern as admin"
    status: completed
  - id: dockerfile-audit-dev
    content: Create dms-audit-service/Dockerfile.dev
    status: completed
  - id: dockerfile-audit-prod
    content: Create dms-audit-service/Dockerfile.prod
    status: completed
  - id: dockerfile-document-update
    content: Update dms-document-service/Dockerfile
    status: completed
  - id: dockerfile-document-dev
    content: Create dms-document-service/Dockerfile.dev
    status: completed
  - id: dockerfile-document-prod
    content: Create dms-document-service/Dockerfile.prod
    status: completed
  - id: dockerfile-compliance-update
    content: Update dms-compliance-service/Dockerfile
    status: completed
  - id: dockerfile-compliance-dev
    content: Create dms-compliance-service/Dockerfile.dev
    status: completed
  - id: dockerfile-compliance-prod
    content: Create dms-compliance-service/Dockerfile.prod
    status: completed
  - id: dockerfile-llm-update
    content: Update dms-llm-service/Dockerfile
    status: completed
  - id: dockerfile-llm-dev
    content: Create dms-llm-service/Dockerfile.dev
    status: completed
  - id: dockerfile-llm-prod
    content: Create dms-llm-service/Dockerfile.prod
    status: completed
  - id: dockerfile-gateway-update
    content: "Update dms-api-gateway-service/Dockerfile: Add health check"
    status: completed
  - id: dockerfile-gateway-dev
    content: Create dms-api-gateway-service/Dockerfile.dev
    status: completed
  - id: dockerfile-gateway-prod
    content: Create dms-api-gateway-service/Dockerfile.prod
    status: completed
  - id: dockerfile-frontend-entrypoint
    content: "Create dms-frontend-service/entrypoint.sh: Copy from sickpay-frontend-service pattern"
    status: completed
  - id: dockerfile-frontend-update
    content: "Update dms-frontend-service/Dockerfile: Reference entrypoint.sh, ensure port 8080"
    status: completed
  - id: dockerfile-frontend-prod
    content: Create dms-frontend-service/Dockerfile.prod
    status: completed
  - id: appconfig-admin-base
    content: "Update dms-admin-service/application.yml: Change database-* to SPRING_DATASOURCE_URL/DB_USER/DB_PASSWORD, add Azure AD config section"
    status: completed
  - id: appconfig-admin-dev
    content: "Create dms-admin-service/application-dev.yml: H2 database, port 8081, debug logging, bypass auth"
    status: completed
  - id: appconfig-admin-docker
    content: "Create dms-admin-service/application-docker.yml: PostgreSQL with docker hostnames, port 8081"
    status: completed
  - id: appconfig-admin-prod
    content: "Create dms-admin-service/application-prod.yml: PostgreSQL, port 8080, Azure AD, OpenTelemetry"
    status: completed
  - id: appconfig-admin-test
    content: "Create dms-admin-service/application-test.yml: H2 in-memory, port 0 (random)"
    status: completed
  - id: appconfig-audit-base
    content: "Update dms-audit-service/application.yml: Standardize DB env vars, add Azure AD"
    status: completed
  - id: appconfig-audit-dev
    content: "Create dms-audit-service/application-dev.yml: port 8082, H2, bypass auth"
    status: completed
  - id: appconfig-audit-docker
    content: Create dms-audit-service/application-docker.yml
    status: completed
  - id: appconfig-audit-prod
    content: Create dms-audit-service/application-prod.yml
    status: completed
  - id: appconfig-document-base
    content: "Update dms-document-service/application.yml: Standardize DB env vars"
    status: completed
  - id: appconfig-document-dev
    content: "Create dms-document-service/application-dev.yml: port 8083"
    status: completed
  - id: appconfig-document-docker
    content: Create dms-document-service/application-docker.yml
    status: completed
  - id: appconfig-document-prod
    content: Create dms-document-service/application-prod.yml
    status: completed
  - id: appconfig-compliance-base
    content: Update dms-compliance-service/application.yml
    status: completed
  - id: appconfig-compliance-dev
    content: "Create dms-compliance-service/application-dev.yml: port 8084"
    status: completed
  - id: appconfig-compliance-docker
    content: Create dms-compliance-service/application-docker.yml
    status: completed
  - id: appconfig-compliance-prod
    content: Create dms-compliance-service/application-prod.yml
    status: completed
  - id: appconfig-llm-base
    content: Update dms-llm-service/application.yml
    status: completed
  - id: appconfig-llm-dev
    content: "Create dms-llm-service/application-dev.yml: port 8085"
    status: completed
  - id: appconfig-llm-docker
    content: Create dms-llm-service/application-docker.yml
    status: completed
  - id: appconfig-llm-prod
    content: Create dms-llm-service/application-prod.yml
    status: completed
  - id: appconfig-gateway-base
    content: "Update dms-api-gateway-service/application.yml: Add local CORS origins"
    status: completed
  - id: appconfig-gateway-dev
    content: Create dms-api-gateway-service/application-dev.yml
    status: completed
  - id: appconfig-gateway-docker
    content: Create dms-api-gateway-service/application-docker.yml
    status: completed
  - id: appconfig-gateway-prod
    content: Create dms-api-gateway-service/application-prod.yml
    status: completed
  - id: security-admin-devmode
    content: "Update dms-admin-service/SecurityConfig.java: Add profile-based dev mode (bypass auth in dev/docker)"
    status: completed
  - id: security-audit-devmode
    content: "Update dms-audit-service/SecurityConfig.java: Add profile-based dev mode"
    status: completed
  - id: security-document-devmode
    content: "Update dms-document-service/SecurityConfig.java: Add profile-based dev mode"
    status: completed
  - id: security-compliance-devmode
    content: "Update dms-compliance-service/SecurityConfig.java: Add profile-based dev mode"
    status: completed
  - id: security-llm-devmode
    content: "Update dms-llm-service/SecurityConfig.java: Add profile-based dev mode"
    status: completed
  - id: docker-compose-env-vars
    content: "Update docker-compose.yml: Replace database-connection-string with SPRING_DATASOURCE_URL, database-username with DB_USER, database-password with DB_PASSWORD"
    status: completed
  - id: docker-compose-profiles
    content: "Update docker-compose.yml: Add SPRING_PROFILES_ACTIVE=docker to all services"
    status: completed
  - id: docker-compose-healthchecks
    content: "Update docker-compose.yml: Standardize health checks using wget (works in alpine)"
    status: completed
  - id: docker-compose-frontend
    content: "Update docker-compose.yml: Add API_GATEWAY_URL env var to frontend, fix health check"
    status: completed
  - id: configmap-azure-create
    content: Create dms-docs/reference/clusters/dev/releases/dms/dev/config-maps/dms-azure-configmap.yaml
    status: completed
  - id: configmap-database-create
    content: Create dms-docs/reference/clusters/dev/releases/dms/dev/config-maps/dms-database-configmap.yaml
    status: completed
  - id: configmap-urls-update
    content: "Update dms-service-urls-configmap.yaml: Add environment variable format keys"
    status: completed
  - id: k8s-admin-envvars
    content: "Update dms-admin-service/k8s-deployment.yaml: Change DATABASE_URL to SPRING_DATASOURCE_URL, add DB_USER, DB_PASSWORD, Azure AD env vars"
    status: completed
  - id: k8s-audit-envvars
    content: "Update dms-audit-service/k8s-deployment.yaml: Same env var updates"
    status: completed
  - id: k8s-document-envvars
    content: "Update dms-document-service/k8s-deployment.yaml: Same env var updates"
    status: completed
  - id: doc-dockerfile-patterns
    content: Create dms-docs/patterns/LV-DOCKERFILE_PATTERNS.md
    status: completed
  - id: doc-appconfig-patterns
    content: Create dms-docs/patterns/LV-APPLICATION_CONFIG_PATTERNS.md
    status: completed
  - id: doc-database-patterns
    content: Create dms-docs/patterns/LV-DATABASE_CONNECTIVITY_PATTERNS.md
    status: completed
  - id: doc-docker-compose-patterns
    content: Create dms-docs/patterns/LV-DOCKER_COMPOSE_PATTERNS.md
    status: completed
  - id: verify-maven-build
    content: Run mvn clean package -DskipTests from root to verify all services build
    status: completed
  - id: verify-docker-build
    content: Run docker compose build --no-cache to verify all Dockerfiles work
    status: completed
  - id: verify-docker-up
    content: Run docker compose up and verify all services start and communicate
    status: completed
---

# DMS LV Patterns Alignment Plan - Comprehensive Update

This plan provides file-by-file, line-by-line changes required to align DMS with LV patterns.

## Detailed Code Review Findings

### Current vs Required State

| File/Component | Current State | Required Changes |

|---------------|---------------|------------------|

| Service POMs | Inherit from spring-boot-starter-parent | Inherit from dms-parent |

| Database env vars | `database-connection-string`, `database-username`, `database-password` | `SPRING_DATASOURCE_URL`, `DB_USER`, `DB_PASSWORD` |

| Dockerfiles | Basic single-stage, no health checks | Multi-stage with health checks, Dockerfile.dev + Dockerfile.prod |

| Application YAMLs | Only application.yml | application.yml + -dev + -docker + -prod + -test |

| Security Config | Always requires auth | Profile-based: bypass in dev/docker |

| Frontend | Missing entrypoint.sh | Add entrypoint.sh for nginx config substitution |

---

## Phase 1: POM Inheritance Updates

### Files to Modify:

**[dms-admin-service/pom.xml](dms-admin-service/pom.xml)** - Lines 8-13:

```xml
<!-- CURRENT -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.0</version>
    <relativePath/>
</parent>

<!-- CHANGE TO -->
<parent>
    <groupId>com.davidparker.dms</groupId>
    <artifactId>dms-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

Also remove duplicated `<properties>` section (lines 21-25) as these are inherited from parent.

**Same change for:**

- `dms-audit-service/pom.xml`
- `dms-document-service/pom.xml`  
- `dms-compliance-service/pom.xml`
- `dms-llm-service/pom.xml`
- `dms-core-service/pom.xml`

---

## Phase 2: Dockerfile Updates

### 2.1 Backend Service Dockerfile Pattern

**[dms-admin-service/Dockerfile](dms-admin-service/Dockerfile)** - Replace entire file:

```dockerfile
# Multi-stage build for DMS Admin Service
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
COPY --from=build /app/target/dms-admin-service-*.jar app.jar

# Expose port (8081 for docker-compose, 8080 in K8s via profile)
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application with spring.classformat.ignore for Java 25
ENTRYPOINT ["java", "-Dspring.classformat.ignore=true", "-jar", "app.jar"]
```

### 2.2 Create Dockerfile.dev (for all backend services)

**[dms-admin-service/Dockerfile.dev](dms-admin-service/Dockerfile.dev)** - Create new file:

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

COPY --from=build /app/target/dms-admin-service-*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-Dspring.classformat.ignore=true", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
```

### 2.3 Create Dockerfile.prod (for all backend services)

**[dms-admin-service/Dockerfile.prod](dms-admin-service/Dockerfile.prod)** - Create new file:

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
COPY dms-admin-service-${version}.jar dms-admin-service.jar

CMD ["-javaagent:opentelemetry-javaagent.jar", "-Dotel.service.name=dms-admin-service", "-Dspring.profiles.active=prod", "-jar", "dms-admin-service.jar"]
```

### 2.4 Frontend entrypoint.sh

**[dms-frontend-service/entrypoint.sh](dms-frontend-service/entrypoint.sh)** - Create new file (copy from sickpay pattern):

```bash
#!/bin/sh
# Entrypoint script for nginx in Kubernetes

GATEWAY_RAW="${APIGATEWAY_SERVICE_URL:-${API_GATEWAY_URL:-dms-api-gateway-service:8080}}"
echo "INFO: GATEWAY_RAW from env: ${GATEWAY_RAW}"

API_GATEWAY_FULL=$(echo "$GATEWAY_RAW" | sed -E 's|^https?://||' | sed 's|/.*$||')
API_GATEWAY_HOST=$(echo "$API_GATEWAY_FULL" | cut -d: -f1)
API_GATEWAY_PORT=$(echo "$API_GATEWAY_FULL" | cut -d: -f2)
if [ -z "$API_GATEWAY_PORT" ]; then
    API_GATEWAY_PORT="8080"
fi
export API_GATEWAY_HOST
export API_GATEWAY_PORT
echo "INFO: Parsed API_GATEWAY_HOST=${API_GATEWAY_HOST} API_GATEWAY_PORT=${API_GATEWAY_PORT}"

NGINX_CONF="/tmp/nginx.conf"
envsubst '${API_GATEWAY_HOST} ${API_GATEWAY_PORT}' < /etc/nginx/nginx.conf > "$NGINX_CONF"

CACHE_DIR="/tmp/cache/nginx"
mkdir -p "$CACHE_DIR/client_temp" "$CACHE_DIR/proxy_temp" "$CACHE_DIR/fastcgi_temp" "$CACHE_DIR/uwsgi_temp" "$CACHE_DIR/scgi_temp"
chmod -R 777 "$CACHE_DIR" 2>/dev/null || true

PID_FILE="/tmp/nginx.pid"

echo "INFO: Starting nginx with config $NGINX_CONF"
exec nginx -c "$NGINX_CONF" -g "daemon off; pid $PID_FILE;"
```

---

## Phase 3: Application Configuration Updates

### 3.1 Update application.yml Base Configuration

**[dms-admin-service/src/main/resources/application.yml](dms-admin-service/src/main/resources/application.yml)**:

Key changes (lines 13-16):

```yaml
# CURRENT
datasource:
  url: ${database-connection-string:jdbc:postgresql://localhost:5432/dms_admin}
  username: ${database-username:postgres}
  password: ${database-password:postgres}

# CHANGE TO
datasource:
  url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/dms_admin}
  username: ${DB_USER:postgres}
  password: ${DB_PASSWORD:postgres}
  driver-class-name: org.postgresql.Driver
```

Add Azure AD configuration (after line 41):

```yaml
# Azure AD Configuration
azure:
  tenant-id: ${AZURE_TENANT_ID:}
  client-id: ${AZURE_CLIENT_ID:}
  jwk-set-uri: ${AZURE_JWK_SET_URI:}
```

### 3.2 Create application-dev.yml

**[dms-admin-service/src/main/resources/application-dev.yml](dms-admin-service/src/main/resources/application-dev.yml)** - Create new:

```yaml
# Development profile - local development with H2
spring:
  datasource:
    url: jdbc:h2:mem:dms_admin;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
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
  port: 8081

# Bypass authentication in dev mode
security:
  bypass-auth: true

logging:
  level:
    com.davidparker.dms.admin: DEBUG
    org.springframework.security: DEBUG
```

### 3.3 Create application-docker.yml

**[dms-admin-service/src/main/resources/application-docker.yml](dms-admin-service/src/main/resources/application-docker.yml)** - Create new:

```yaml
# Docker Compose profile
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://postgres-admin:5432/dms_admin}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver

server:
  port: 8081

# Bypass authentication in docker mode for local testing
security:
  bypass-auth: true

logging:
  level:
    com.davidparker.dms.admin: DEBUG
```

### 3.4 Create application-prod.yml

**[dms-admin-service/src/main/resources/application-prod.yml](dms-admin-service/src/main/resources/application-prod.yml)** - Create new:

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

---

## Phase 4: Security Configuration Updates

### 4.1 Update SecurityConfig with Dev Mode Bypass

**[dms-admin-service/src/main/java/com/davidparker/dms/admin/config/SecurityConfig.java](dms-admin-service/src/main/java/com/davidparker/dms/admin/config/SecurityConfig.java)**:

Add profile-based security bypass (replace lines 28-47):

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final ApplicationIsolationFilter applicationIsolationFilter;
    private final CorsFilter corsFilter;
    private final Environment environment;

    public SecurityConfig(ApplicationIsolationFilter applicationIsolationFilter, 
                         CorsFilter corsFilter,
                         Environment environment) {
        this.applicationIsolationFilter = applicationIsolationFilter;
        this.corsFilter = corsFilter;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Check if we're in dev/docker mode
        boolean isDevMode = isDevMode();
        
        if (isDevMode) {
            // In dev mode, allow all requests without authentication
            return http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
        }
        
        // Production mode - require authentication
        return http
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(aadJwtConverter())
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("DMS.Admin")
                .anyRequest().authenticated()
            )
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(applicationIsolationFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .build();
    }

    private boolean isDevMode() {
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");
        return "dev".equals(activeProfile) || "docker".equals(activeProfile);
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> aadJwtConverter() {
        return new AadJwtAuthenticationConverter();
    }
}
```

---

## Phase 5: Docker Compose Updates

**[docker-compose.yml](docker-compose.yml)** - Key changes:

### 5.1 Update Environment Variables (for each service)

Example for dms-admin-service (lines 84-94):

```yaml
# CURRENT
environment:
  PORT: 8081
  ENVIRONMENT: dev
  SPRING_PROFILES_ACTIVE: dev
  database-connection-string: jdbc:postgresql://postgres-admin:5432/dms_admin
  database-username: postgres
  database-password: postgres

# CHANGE TO
environment:
  PORT: 8081
  SPRING_PROFILES_ACTIVE: docker
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-admin:5432/dms_admin
  DB_USER: postgres
  DB_PASSWORD: postgres
  AUDIT_SERVICE_URL: http://dms-audit-service:8082
```

### 5.2 Update Health Checks

Change from curl to wget (more reliable in alpine):

```yaml
healthcheck:
  test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

### 5.3 Update Frontend Service

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
```

---

## Phase 6: Service Port Assignments

| Service | Dev Port | Prod Port |

|---------|----------|-----------|

| dms-admin-service | 8081 | 8080 |

| dms-audit-service | 8082 | 8080 |

| dms-document-service | 8083 | 8080 |

| dms-compliance-service | 8084 | 8080 |

| dms-llm-service | 8085 | 8080 |

| dms-api-gateway-service | 8080 | 8080 |

| dms-frontend-service | 8080 | 8080 |

---

## Verification Steps

1. **Maven Build**: `mvn clean package -DskipTests` from root
2. **Docker Build**: `docker compose build --no-cache`
3. **Docker Up**: `docker compose up -d`
4. **Health Checks**: Verify all services report healthy
5. **API Test**: Call `/actuator/health` on each service
6. **Frontend Test**: Access http://localhost:8080 and verify API calls work