FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY services/audit-service/target/audit-service-*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
