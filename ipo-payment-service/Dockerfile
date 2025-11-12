# Multi-stage build: Build stage
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy parent POM
COPY pom.xml .

# Copy all module POMs (Maven needs to know about all modules)
COPY common-dto/pom.xml ./common-dto/
COPY service-registry/pom.xml ./service-registry/
COPY api-gateway/pom.xml ./api-gateway/
COPY ipo-application-service/pom.xml ./ipo-application-service/
COPY ipo-payment-service/pom.xml ./ipo-payment-service/
COPY ipo-allotment-service/pom.xml ./ipo-allotment-service/
COPY ipo-notification-service/pom.xml ./ipo-notification-service/

# Copy source for modules we need to build
COPY common-dto/src ./common-dto/src
COPY ipo-payment-service/src ./ipo-payment-service/src

# Build the application
RUN mvn clean package -DskipTests -pl ipo-payment-service -am

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /build/ipo-payment-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
