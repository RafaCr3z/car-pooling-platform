FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --chown=appuser:appgroup ${JAR_FILE} eureka-server.jar
USER appuser
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "eureka-server.jar"]
