FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} location-service.jar
ENTRYPOINT ["java","-jar","/location-service.jar"]
