FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the pre-built JAR file
COPY ipo-application-service/target/ipo-application-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
