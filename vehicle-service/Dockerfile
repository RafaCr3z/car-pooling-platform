FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} vehicle-service.jar
ENTRYPOINT ["java","-jar","/vehicle-service.jar"]
