# Multi-stage build for DMS LLM Service
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Copy parent POM first
COPY pom.xml ./pom.xml

# Copy dms-core-service (needed by llm-service)
COPY dms-core-service/pom.xml ./dms-core-service/pom.xml
COPY dms-core-service/src ./dms-core-service/src

# Copy service POM and source
COPY dms-llm-service/pom.xml ./dms-llm-service/pom.xml
COPY dms-llm-service/src ./dms-llm-service/src

# Build dms-core-service first
WORKDIR /app/dms-core-service
RUN mvn clean install -DskipTests -B

# Build the service
WORKDIR /app/dms-llm-service
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:25-jre

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built JAR
COPY --from=build /app/dms-llm-service/target/dms-llm-service-*.jar app.jar

# Expose port (8085 for docker-compose, 8080 in K8s via profile)
EXPOSE 8085

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8085/actuator/health || exit 1

# Run the application with spring.classformat.ignore for Java 25
ENTRYPOINT ["java", "-Dspring.classformat.ignore=true", "-jar", "app.jar"]
