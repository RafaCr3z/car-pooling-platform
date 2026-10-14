FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --chown=appuser:appgroup ${JAR_FILE} admin-service.jar
USER appuser
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "admin-service.jar"]
