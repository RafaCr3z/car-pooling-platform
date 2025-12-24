# FICHEIROS DE CONFIGURAÇÃO APLICACIONAL (application.yml)

## 1. EUREKA SERVER (eureka-server/src/main/resources/application.yml)

```yaml
spring:
  application:
    name: eureka-server
  config:
    import: optional:configserver:http://localhost:8888

server:
  port: 8761

eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  server:
    enableSelfPreservation: false
    evictionIntervalTimerInMs: 5000
```

## 2. API GATEWAY (api-gateway/src/main/resources/application.yml)

```yaml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=2

        - id: trip-service
          uri: lb://trip-service
          predicates:
            - Path=/api/trips/**
          filters:
            - StripPrefix=2

        - id: vehicle-service
          uri: lb://vehicle-service
          predicates:
            - Path=/api/vehicles/**
          filters:
            - StripPrefix=2

        - id: location-service
          uri: lb://location-service
          predicates:
            - Path=/api/locations/**
          filters:
            - StripPrefix=2

        - id: payment-service
          uri: lb://payment-service
          predicates:
            - Path=/api/payments/**
          filters:
            - StripPrefix=2

        - id: admin-service
          uri: lb://admin-service
          predicates:
            - Path=/api/admin/**
          filters:
            - StripPrefix=2

server:
  port: 8080

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

## 3. AUTH SERVICE (auth-service/src/main/resources/application.yml)

```yaml
spring:
  application:
    name: auth-service
  datasource:
    url: jdbc:postgresql://localhost:5432/carpooling_auth
    username: carpooling
    password: carpooling123
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
    show-sql: false

server:
  port: 8081

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/

jwt:
  secret: MyVeryLongSecretKeyFor256BitHS256AlgorithmThatIsAtLeast32Characters
  expiration: 86400000  # 24 horas em ms

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
```

## 4. TRIP SERVICE (trip-service/src/main/resources/application.yml)

```yaml
spring:
  application:
    name: trip-service
  datasource:
    url: jdbc:postgresql://localhost:5432/carpooling_trips
    username: carpooling
    password: carpooling123
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
    show-sql: false

server:
  port: 8082

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/

resilience4j:
  circuitbreaker:
    instances:
      tripService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 5s
        failureRateThreshold: 50

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,circuitbreaker
  endpoint:
    health:
      show-details: always
  metrics:
    enabled: true

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
```

## 5. VEHICLE SERVICE (EXEMPLO - a ser criado)

```yaml
spring:
  application:
    name: vehicle-service
  datasource:
    url: jdbc:postgresql://localhost:5432/carpooling_vehicles
    username: carpooling
    password: carpooling123
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8083

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

## 6. PAYMENT SERVICE (EXEMPLO - a ser criado)

```yaml
spring:
  application:
    name: payment-service
  datasource:
    url: jdbc:postgresql://localhost:5432/carpooling_payments
    username: carpooling
    password: carpooling123
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8084

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

## 7. ADMIN SERVICE (EXEMPLO - a ser criado)

```yaml
spring:
  application:
    name: admin-service
  datasource:
    url: jdbc:postgresql://localhost:5432/carpooling_admin
    username: carpooling
    password: carpooling123
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8085

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

## 8. LOCATION SERVICE (EXEMPLO - a ser criado)

```yaml
spring:
  application:
    name: location-service

server:
  port: 8086

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/

# Integração com APIs externas
google-maps:
  api-key: ${GOOGLE_MAPS_API_KEY:your-api-key}
  
mapbox:
  api-key: ${MAPBOX_API_KEY:your-api-key}

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

## NOTAS IMPORTANTES

### Variáveis de Ambiente em Produção

Em Docker, use variáveis de ambiente em vez de valores hardcoded:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400000}
```

### Docker Compose Environment Variables

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/carpooling_auth
  SPRING_DATASOURCE_USERNAME: ${DB_USER}
  SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
  EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
  JWT_SECRET: ${JWT_SECRET}
```

### Profiles de Configuração

Criar ficheiros para diferentes ambientes:

- `application.yml` (padrão)
- `application-dev.yml` (desenvolvimento)
- `application-prod.yml` (produção)
- `application-docker.yml` (Docker)

Ativar com: `spring.profiles.active=docker`

### Logging Configuração

Adicionar a `application.yml` para mais controlo de logs:

```yaml
logging:
  level:
    root: INFO
    com.carpooling: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: logs/application.log
```

---

**Última atualização:** 24 de Dezembro de 2024
