FROM maven:3.8.1-openjdk-17 as builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /build/target/payment-service-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
