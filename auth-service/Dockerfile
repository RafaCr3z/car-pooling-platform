FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} auth-service.jar
ENTRYPOINT ["java","-jar","/auth-service.jar"]
