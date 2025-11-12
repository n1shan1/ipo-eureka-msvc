FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the pre-built JAR file
COPY ipo-payment-service/target/ipo-payment-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
