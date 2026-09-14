FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY services/task-service/target/task-service-*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
