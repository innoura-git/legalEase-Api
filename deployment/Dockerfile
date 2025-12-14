# ============================================
# Optimized Multi-stage Dockerfile for Java/Spring Boot
# ============================================

# -------- Stage 1: Build --------
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml ./

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds, run tests in CI)
RUN mvn clean package -DskipTests -B

# -------- Stage 2: Production Runtime --------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Create file upload directory (ephemeral - lost on container restart)
RUN mkdir -p /app/files

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set correct permissions (including files directory)
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Build arguments for metadata
ARG BUILD_DATE
ARG VCS_REF
ARG BRANCH

# Labels
LABEL org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.revision="${VCS_REF}" \
      org.opencontainers.image.branch="${BRANCH}"

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

