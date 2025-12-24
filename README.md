# Car Pooling - Plataforma de Partilha de Viagens

Sistema baseado em **microserviços** para uma plataforma de partilha de viagens (Car Pooling) e partilha de veículos (Car Sharing), desenvolvido com **Spring Boot**, **Spring Cloud** e **Docker**.

> **⚠️ STATUS DE VERIFICAÇÃO (24 de Dezembro de 2024):** 
> - Projeto: **60% Completo** (5 de 8 microserviços implementados)
> - Estimativa de Pontuação: **14.5/20 valores** (72%)
> - Ver relatórios: [VERIFICATION_REPORT.txt](VERIFICATION_REPORT.txt), [TECHNICAL_ANALYSIS.txt](TECHNICAL_ANALYSIS.txt), [ACTION_PLAN.txt](ACTION_PLAN.txt)

## 📋 Arquitetura

### Microserviços Implementados

1. **Eureka Server** (porta 8761) - Descoberta e registo de serviços ✅
2. **API Gateway** (porta 8080) - Roteamento e orquestração de requisições ✅
3. **Auth Service** (porta 8081) - Autenticação e gestão de utilizadores ✅
4. **Trip Service** (porta 8082) - Gestão de viagens (Car Pooling) ✅
5. **Frontend Service** (porta 3000) - Interface web responsiva ✅ **NOVO**
6. **Vehicle Service** (porta 8083) - Gestão de veículos (Car Sharing) - *em desenvolvimento*
7. **Payment Service** (porta 8084) - Gestão de pagamentos - *em desenvolvimento*
8. **Admin Service** (porta 8085) - Administração e moderação - *em desenvolvimento*

### Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Cloud 2023.0.0**
- **Spring Data JPA**
- **Spring Security**
- **PostgreSQL**
- **Docker & Docker Compose**
- **JWT (JSON Web Tokens)**
- **OpenFeign** (comunicação entre serviços)
- **Resilience4j** (Circuit Breakers)
- **Swagger/OpenAPI**

## 🚀 Como Começar

### Pré-requisitos

- Java 17 ou superior
- Maven 3.8.0 ou superior
- Docker e Docker Compose
- Git

### Instalação Local

1. **Clone o repositório:**
```bash
git clone <repo-url>
cd car-pooling-sharing
```

2. **Compile todos os módulos:**
```bash
mvn clean install
```

3. **Inicie os serviços com Docker Compose:**
```bash
docker-compose up -d
```

### Verificar Serviços

- **Frontend Service**: http://localhost:3000/ ✨ **NOVO**
- **Eureka Dashboard**: http://localhost:8761/
- **API Gateway**: http://localhost:8080/
- **Auth Service Swagger**: http://localhost:8081/swagger-ui.html
- **Trip Service Swagger**: http://localhost:8082/swagger-ui.html

### Frontend Service (Novo) ✨

O Frontend Service é uma aplicação **Spring Boot MVC** com **Thymeleaf** que fornece uma interface web responsiva para a plataforma Car Pooling.

#### Funcionalidades
- ✅ Página inicial pública com features e how-it-works
- ✅ Sistema de registo e login
- ✅ Dashboard principal com estatísticas
- ✅ Perfil de utilizador com edição de dados
- ✅ Pesquisa e criação de viagens
- ✅ Sistema de reservas de viagens
- ✅ Responsive design com Bootstrap 5
- ✅ Integração com API Gateway via WebClient
- ✅ Session-based authentication

#### Tecnologias
- Spring Boot 3.2.0
- Spring MVC + Thymeleaf
- Bootstrap 5.3.0 (CSS/Responsive)
- Font Awesome 6.4 (Ícones)
- Spring WebFlux WebClient (REST)
- Spring Cloud Eureka Discovery

#### Documentação Adicional
- [README_FRONTEND.md](README_FRONTEND.md) - Documentação técnica completa
- [FRONTEND_STATUS.md](FRONTEND_STATUS.md) - Status detalhado da implementação
- [BUILD_AND_TEST_GUIDE.md](BUILD_AND_TEST_GUIDE.md) - Guia de build e testes

## 📚 Endpoints Principais

### Autenticação (Auth Service)

**POST** `/api/auth/register` - Registar novo utilizador
```json
{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "João Silva",
  "phoneNumber": "+351912345678",
  "role": "PASSENGER"
}
```

**POST** `/api/auth/login` - Fazer login
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

### Viagens (Trip Service)

**POST** `/api/trips` - Criar nova viagem
```json
{
  "driverId": 1,
  "departureLocation": "Lisboa",
  "destinationLocation": "Porto",
  "departureDateTime": "2024-01-15T09:00:00",
  "availableSeats": 4,
  "totalCost": 40.00,
  "distance": 320.5
}
```

**GET** `/api/trips` - Listar todas as viagens

**GET** `/api/trips/{id}` - Obter viagem por ID

**GET** `/api/trips/search?departure=Lisboa&destination=Porto&departureTime=2024-01-15T09:00:00` - Pesquisar viagens

**POST** `/api/trips/reservations` - Criar reserva
```json
{
  "tripId": 1,
  "passengerId": 2,
  "numberOfSeats": 2
}
```

## 🏗️ Estrutura do Projeto

```
car-pooling-sharing/
├── eureka-server/          # ✅ Serviço de descoberta
├── api-gateway/            # ✅ Gateway de API
├── auth-service/           # ✅ Serviço de autenticação
├── trip-service/           # ✅ Serviço de viagens
├── frontend-service/       # ✅ Interface web Spring MVC + Thymeleaf
├── vehicle-service/        # 🔄 Serviço de veículos (em desenvolvimento)
├── payment-service/        # 🔄 Serviço de pagamentos (em desenvolvimento)
├── admin-service/          # 🔄 Serviço de administração (em desenvolvimento)
├── location-service/       # 🔄 Serviço de geolocalização (em desenvolvimento)
├── common-lib/             # ✅ Biblioteca comum
├── docker-compose.yml      # ✅ Configuração Docker Compose
├── db-init/                # ✅ Scripts de inicialização de BD
├── BUILD_AND_TEST_GUIDE.md # 📖 Guia de build e testes (NOVO)
├── FRONTEND_STATUS.md      # 📊 Status do Frontend (NOVO)
├── README_FRONTEND.md      # 📖 Documentação do Frontend (NOVO)
└── README.md               # Este ficheiro
```

## 🔐 Segurança

- **JWT (JSON Web Tokens)** para autenticação e autorização
- **Spring Security** para proteção de endpoints
- **Password Encoding** com BCrypt
- **CORS** configurado para o API Gateway

## 📦 Desenvolvimento de Novos Microserviços

### Estrutura de um novo Microserviço

1. Criar diretório: `mkdir <novo-servico>`
2. Criar `pom.xml` com dependências do Spring Cloud
3. Criar estrutura de pastas:
```
novo-servico/
├── src/main/java/com/carpooling/<servico>/
│   ├── entity/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── dto/
│   └── Application.java
├── src/main/resources/
│   └── application.yml
├── Dockerfile
└── pom.xml
```

4. Adicionar módulo ao `pom.xml` parent

## 🔄 Comunicação entre Microserviços

Utilizamos **Spring Cloud OpenFeign** para comunicação REST entre serviços:

```java
@FeignClient(name = "auth-service")
public interface AuthServiceClient {
    @GetMapping("/api/auth/user/{id}")
    UserDTO getUser(@PathVariable Long id);
}
```

## ⚡ Circuit Breakers

Implementados com **Resilience4j**:

```java
@CircuitBreaker(name = "tripService", fallbackMethod = "fallback")
public List<TripDTO> getTrips() {
    // Código do serviço
}
```

## 📊 Base de Dados

- **PostgreSQL** como base de dados relacional
- Cada microserviço possui a sua própria base de dados
- Migrations automáticas com JPA Hibernate (`ddl-auto: update`)

## 🛠️ Troubleshooting

### Os contentores não iniciam
```bash
# Verifique os logs
docker-compose logs -f

# Limpe tudo e comece novamente
docker-compose down -v
docker-compose up -d
```

### Erro de conexão PostgreSQL
```bash
# Verifique se o container postgres está em execução
docker ps | grep postgres

# Aceda à BD direitamente
docker exec -it postgres-db psql -U carpooling
```

### Serviços não registados no Eureka
- Verifique se o Eureka Server está online: `http://localhost:8761/`
- Verifique a configuração `eureka.client.serviceUrl.defaultZone`

## 📈 Próximos Passos

- [ ] Implementar Vehicle Service (Car Sharing)
- [ ] Implementar Payment Service
- [ ] Implementar Admin Service
- [ ] Criar Frontend (React/Vue)
- [ ] Adicionar Location Service com integração GPS
- [ ] Implementar Notification Service
- [ ] Adicionar testes unitários e integração
- [ ] Configurar CI/CD (GitHub Actions)
- [ ] Implementar logging centralizado (ELK Stack)
- [ ] Implementar monitoring (Prometheus/Grafana)

## 📝 Licença

MIT License - Veja LICENSE para detalhes

## 👥 Contribuidores

- Seu Nome
- Nomes dos colegas do grupo

---

**Desenvolvido para o trabalho prático da UC de Spring/Spring Cloud**
