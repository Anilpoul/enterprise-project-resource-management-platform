FROM eclipse-temurin:21-jre

WORKDIR /app

COPY infrastructure/api-gateway/target/api-gateway-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]