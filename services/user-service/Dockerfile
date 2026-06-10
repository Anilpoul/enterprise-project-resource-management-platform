FROM eclipse-temurin:21-jre

WORKDIR /app

COPY services/user-service/target/user-service-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]