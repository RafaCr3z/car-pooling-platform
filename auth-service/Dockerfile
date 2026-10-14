FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} auth-service.jar
ENTRYPOINT ["java","-jar","/auth-service.jar"]
