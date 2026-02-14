# Multi-stage build for DMS API Gateway Service
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy service POM and source (API Gateway doesn't use parent POM)
COPY dms-api-gateway-service/pom.xml ./dms-api-gateway-service/pom.xml
COPY dms-api-gateway-service/src ./dms-api-gateway-service/src

# Build the application from service directory
WORKDIR /app/dms-api-gateway-service
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy the built JAR
COPY --from=build /app/dms-api-gateway-service/target/dms-api-gateway-service-*.jar app.jar

# Expose port 8080 (standard for all environments)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
