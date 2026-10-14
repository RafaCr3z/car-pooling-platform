FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} payment-service.jar
ENTRYPOINT ["java","-jar","/payment-service.jar"]
