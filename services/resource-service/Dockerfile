FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY services/resource-service/target/resource-service-*.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
