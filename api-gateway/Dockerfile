FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --chown=appuser:appgroup ${JAR_FILE} api-gateway.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]
