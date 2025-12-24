FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/frontend-service-*.jar app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "app.jar"]
