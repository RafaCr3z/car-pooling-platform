FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --chown=appuser:appgroup ${JAR_FILE} vehicle-service.jar
USER appuser
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "vehicle-service.jar"]
