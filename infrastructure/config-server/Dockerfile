FROM eclipse-temurin:21-jre

WORKDIR /app

COPY infrastructure/config-server/target/config-server-*.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]