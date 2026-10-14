FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --chown=appuser:appgroup target/frontend-service-*.jar app.jar
USER appuser
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]
