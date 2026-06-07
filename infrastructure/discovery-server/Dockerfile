FROM eclipse-temurin:21-jre

WORKDIR /app

COPY infrastructure/discovery-server/target/discovery-server-*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]