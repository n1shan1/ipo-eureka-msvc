FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the pre-built JAR file
COPY ipo-allotment-service/target/ipo-allotment-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "app.jar"]
