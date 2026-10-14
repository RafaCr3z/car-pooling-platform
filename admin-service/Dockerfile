FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} admin-service.jar
ENTRYPOINT ["java","-jar","/admin-service.jar"]
