FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --chown=appuser:appgroup ${JAR_FILE} location-service.jar
USER appuser
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "location-service.jar"]
