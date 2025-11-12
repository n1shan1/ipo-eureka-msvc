FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the pre-built JAR file
COPY ipo-notification-service/target/ipo-notification-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
