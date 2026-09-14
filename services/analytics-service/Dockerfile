FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY services/analytics-service/target/analytics-service-*.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
