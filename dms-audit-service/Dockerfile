# Multi-stage build for DMS Audit Service
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy parent POM first
COPY pom.xml ./pom.xml

# Copy dms-core-service (needed by audit-service)
COPY dms-core-service/pom.xml ./dms-core-service/pom.xml
COPY dms-core-service/src ./dms-core-service/src

# Copy service POM and source
COPY dms-audit-service/pom.xml ./dms-audit-service/pom.xml
COPY dms-audit-service/src ./dms-audit-service/src

# Build dms-core-service first
WORKDIR /app/dms-core-service
RUN mvn clean install -DskipTests -B

# Build the service
WORKDIR /app/dms-audit-service
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built JAR
COPY --from=build /app/dms-audit-service/target/dms-audit-service-*.jar app.jar

# Expose port (8082 for docker-compose, 8080 in K8s via profile)
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
