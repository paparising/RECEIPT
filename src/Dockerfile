# =========================
# 1) Build stage
# =========================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only pom.xml first to leverage Docker layer caching
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Now copy the rest of the source
COPY src ./src

# Build the Spring Boot jar
RUN mvn -q -DskipTests clean package

# =========================
# 2) Runtime stage
# =========================
FROM eclipse-temurin:17-jre-alpine

# Set a non-root user (optional but recommended)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Environment variables for DB & RabbitMQ (override in docker-compose or k8s)
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/appdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC \
    SPRING_DATASOURCE_USERNAME=appuser \
    SPRING_DATASOURCE_PASSWORD=apppassword \
    SPRING_JPA_HIBERNATE_DDL_AUTO=update \
    SPRING_RABBITMQ_HOST=rabbitmq \
    SPRING_RABBITMQ_PORT=5672 \
    SPRING_RABBITMQ_USERNAME=guest \
    SPRING_RABBITMQ_PASSWORD=guest

# Optional: JVM tuning / active profile
ENV JAVA_OPTS="-Xms256m -Xmx512m" \
    SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
