FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY services/notification-service/target/notification-service-*.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
