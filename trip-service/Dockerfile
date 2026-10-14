FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} trip-service.jar
ENTRYPOINT ["java","-jar","/trip-service.jar"]
