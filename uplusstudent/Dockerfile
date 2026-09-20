# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy gradle configuration files first to cache dependencies
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

# Grant execution rights on gradlew
RUN chmod +x gradlew

# Download dependencies (this layer will be cached unless gradle files change)
RUN ./gradlew dependencies --no-daemon

# Copy the source code
COPY src src

# Build the application
RUN ./gradlew bootJar --no-daemon

# Find the correct jar (excluding the plain one) and copy it to a fixed name
RUN find build/libs -name "*.jar" ! -name "*-plain.jar" -exec cp {} app.jar \;

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/app.jar app.jar

# Expose port
EXPOSE 8080

# Install curl for health check
RUN apk add --no-cache curl

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]