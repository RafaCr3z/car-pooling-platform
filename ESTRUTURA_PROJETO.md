# 🎯 ESTRUTURA DO PROJETO - CAR POOLING SHARING

## ✅ Ficheiros e Pastas Criadas

### 📁 Estrutura Principal
```
d:\car-pooling-sharing\
├── .gitignore
├── pom.xml (Parent POM)
├── docker-compose.yml
├── README.md
└── build.sh
```

### 🔧 Microserviços Implementados

#### 1. **common-lib/** (Biblioteca Comum)
- `pom.xml`
- `src/main/java/com/carpooling/common/ApiResponse.java`

#### 2. **eureka-server/** (Descoberta de Serviços)
- `pom.xml`
- `Dockerfile`
- `src/main/java/com/carpooling/eureka/EurekaServerApplication.java`
- `src/main/resources/application.yml`

#### 3. **api-gateway/** (Gateway de Requisições)
- `pom.xml`
- `Dockerfile`
- `src/main/java/com/carpooling/gateway/ApiGatewayApplication.java`
- `src/main/resources/application.yml`

#### 4. **auth-service/** (Autenticação e Utilizadores)
- `pom.xml`
- `Dockerfile`
- Entities:
  - `User.java`
  - `UserRole.java`
- DTOs:
  - `LoginRequest.java`
  - `LoginResponse.java`
  - `RegisterRequest.java`
  - `UserDTO.java`
- Services:
  - `AuthService.java` (Lógica de negócio)
  - `JwtTokenProvider.java` (Geração de tokens JWT)
  - `SecurityConfig.java` (Configuração BCrypt)
- Controllers:
  - `AuthController.java` (Endpoints REST)
- Repositories:
  - `UserRepository.java`
- Main:
  - `AuthServiceApplication.java`
  - `application.yml`

#### 5. **trip-service/** (Gestão de Viagens - Car Pooling)
- `pom.xml`
- `Dockerfile`
- Entities:
  - `Trip.java`
  - `Reservation.java`
  - `TripStatus.java`
  - `ReservationStatus.java`
- DTOs:
  - `TripDTO.java`
  - `CreateTripRequest.java`
  - `ReservationDTO.java`
  - `CreateReservationRequest.java`
- Services:
  - `TripService.java` (Lógica com Circuit Breaker)
- Controllers:
  - `TripController.java` (Endpoints REST)
- Repositories:
  - `TripRepository.java`
  - `ReservationRepository.java`
- Main:
  - `TripServiceApplication.java`
  - `application.yml`

### 🗄️ Configuração de Base de Dados
- `db-init/01-init.sql` - Script de inicialização (criar 5 bases de dados)

---

## 🚀 PRÓXIMOS PASSOS - IMPLEMENTAR

### Phase 2 - Microserviços Restantes

#### **Vehicle Service** (porta 8083)
- Registar e gerir veículos
- Consultar disponibilidade
- Cálculo de custos por hora
- Histórico de utilizações

#### **Payment Service** (porta 8084)
- Processamento de pagamentos
- Cálculo de divisão de despesas
- Histórico de transações
- Integração com sistema de pagamentos

#### **Admin Service** (porta 8085)
- Dashboard administrativo
- Gestão de utilizadores e contas
- Bloqueio/ativação de contas
- Relatórios de utilização
- Monitorização de métricas ambientais (CO₂)

#### **Location Service** (porta 8086)
- Geolocalização e rotas
- Integração com APIs de mapas (Google Maps/Mapbox)
- Cálculo de proximidade
- Sugestão de viagens próximas

#### **Frontend Service** (porta 3000)
- Interface web responsiva (Bootstrap)
- Autenticação e perfil de utilizador
- Pesquisa e reserva de viagens
- Gestão de reservas
- Painel de utilizador
- Dashboard administrativo

#### **Notification Service** (porta 8087 - opcional)
- Notificações por email
- Push notifications
- Confirmação de reservas
- Alertas de viagem

---

## 📝 CONFIGURAÇÃO NECESSÁRIA

### 1. Configurar o próprio Security Filter para Auth
No **Trip Service** (e outros), adicionar JWT Filter:

```java
// auth-service/src/main/java/com/carpooling/auth/security/JwtAuthenticationFilter.java
```

### 2. Configurar OpenFeign para comunicação entre serviços

```java
// Em Trip Service chamar Auth Service via Feign
@FeignClient(name = "auth-service")
public interface AuthServiceClient {
    @GetMapping("/api/auth/user/{id}")
    UserDTO getUser(@PathVariable Long id);
}
```

### 3. Implementar mais Circuit Breakers

Adicionar a `resilience4j` em cada serviço que chame outros

### 4. Adicionar Actuator e Health Checks

Para melhor monitorização via Docker Compose health checks

---

## 🐳 COMPILAÇÃO E EXECUÇÃO

### Passo 1: Compilar Tudo
```bash
cd d:\car-pooling-sharing

# Opção 1: Com Maven (todos os módulos)
mvn clean install

# Opção 2: Com script bash (apenas JAR)
bash build.sh
```

### Passo 2: Build das Imagens Docker
```bash
docker-compose build
```

### Passo 3: Iniciar Serviços
```bash
docker-compose up -d
```

### Passo 4: Verificar Status
```bash
# Ver status dos contentores
docker-compose ps

# Ver logs
docker-compose logs -f

# Aceder ao Eureka
http://localhost:8761/
```

---

## 🧪 TESTAR OS ENDPOINTS

### 1. Registar Utilizador (Auth Service)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@example.com",
    "password": "senha123",
    "fullName": "João Silva",
    "phoneNumber": "+351912345678",
    "role": "DRIVER"
  }'
```

### 2. Fazer Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@example.com",
    "password": "senha123"
  }'
```

### 3. Criar Viagem (Trip Service)
```bash
curl -X POST http://localhost:8080/api/trips \
  -H "Content-Type: application/json" \
  -d '{
    "driverId": 1,
    "departureLocation": "Castelo Branco",
    "destinationLocation": "Lisboa",
    "departureDateTime": "2024-01-20T08:00:00",
    "availableSeats": 3,
    "totalCost": 30.00,
    "distance": 285.5
  }'
```

### 4. Fazer Reserva
```bash
curl -X POST http://localhost:8080/api/trips/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "tripId": 1,
    "passengerId": 2,
    "numberOfSeats": 2
  }'
```

---

## 📊 ESTRUTURA DE BD

PostgreSQL criarão as tabelas automaticamente via JPA Hibernate:
- `carpooling_auth` - Utilizadores e autenticação
- `carpooling_trips` - Viagens e reservas
- `carpooling_vehicles` - Veículos (future)
- `carpooling_payments` - Pagamentos (future)
- `carpooling_admin` - Dados administrativos (future)

---

## ⚠️ NOTAS IMPORTANTES

1. **JWT Secret** - Mudança a chave em `application.yml`
2. **CORS** - Configurado no API Gateway para frontend
3. **Circuit Breaker** - Implementado com Resilience4j (Trip Service)
4. **Logs** - Todos os serviços têm logging com SLF4J
5. **Swagger** - Disponível em cada serviço (ex: http://localhost:8081/swagger-ui.html)

---

## 🎓 AVALIAÇÃO - CHECKLIST

- [x] Estrutura de microserviços (5 serviços implementados)
- [x] Service Discovery (Eureka)
- [x] API Gateway
- [x] Autenticação e JWT
- [x] Base de dados (PostgreSQL)
- [x] DTOs e validação
- [x] Repositories e JPA
- [x] Controllers REST
- [x] Lógica de negócio (TripService com cálculos)
- [x] Swagger/OpenAPI
- [x] Docker & Docker Compose
- [x] Circuit Breakers (Resilience4j)
- [ ] Frontend (em desenvolvimento)
- [ ] Testes (adicionar posteriormente)

---

## 📚 RECURSOS ÚTEIS

- Swagger Endpoints: http://localhost:8081/swagger-ui.html (Auth)
- Eureka Dashboard: http://localhost:8761/
- API Gateway: http://localhost:8080/
- PostgreSQL: localhost:5432

---

**Projeto criado com sucesso! 🎉**
