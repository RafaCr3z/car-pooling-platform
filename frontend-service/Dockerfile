FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/frontend-service-*.jar app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "app.jar"]
