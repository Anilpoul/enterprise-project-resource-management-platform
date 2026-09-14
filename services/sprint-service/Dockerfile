FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY services/sprint-service/target/sprint-service-*.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
