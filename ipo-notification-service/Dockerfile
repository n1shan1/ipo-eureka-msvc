# Multi-stage build: Build stage
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy parent pom first
COPY pom.xml ../pom.xml
COPY common-dto ../common-dto

# Copy service files
COPY pom.xml .
COPY src ./src

# Build the application
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests -f ../pom.xml -pl ipo-notification-service -am

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
