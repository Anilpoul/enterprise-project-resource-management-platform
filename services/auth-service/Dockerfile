FROM eclipse-temurin:21-jre

WORKDIR /app

COPY services/auth-service/target/auth-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]