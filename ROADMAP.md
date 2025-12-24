# 📋 ROADMAP DO PROJETO - CAR POOLING SHARING

## ✅ FASE 1 - ESTRUTURA BASE (CONCLUÍDA)

### Infra & Arquitetura
- [x] Estrutura Maven multi-módulo
- [x] Eureka Server (Service Discovery)
- [x] API Gateway (Spring Cloud Gateway)
- [x] Docker & Docker Compose
- [x] PostgreSQL containerizado

### Auth Service
- [x] Entidade User com roles
- [x] Endpoints de registo e login
- [x] JWT Token Provider
- [x] Criptografia BCrypt
- [x] Swagger/OpenAPI

### Trip Service
- [x] Entidades Trip e Reservation
- [x] CRUD de viagens
- [x] Pesquisa de viagens
- [x] Sistema de reservas
- [x] Cálculo automático de custos
- [x] Circuit Breaker (Resilience4j)
- [x] Swagger/OpenAPI

### Documentação
- [x] README.md completo
- [x] ESTRUTURA_PROJETO.md
- [x] Postman Collection
- [x] Scripts de build (bash + batch)
- [x] Setup script

---

## 🔄 FASE 2 - MICROSERVIÇOS ESSENCIAIS (PRÓXIMO)

### Vehicle Service (Car Sharing)
- [ ] Criar estrutura Maven (pom.xml)
- [ ] Entidades:
  - [ ] Vehicle (placa, modelo, tipo, localização)
  - [ ] Reservation (aluguel de veículo)
  - [ ] VehicleStatus enum
- [ ] DTOs para criação e consulta
- [ ] Repositórios JPA
- [ ] VehicleService com lógica de negócio:
  - [ ] Registar novo veículo
  - [ ] Pesquisar por localização
  - [ ] Reservar veículo
  - [ ] Calcular custo por hora
  - [ ] Finalizar aluguel
- [ ] VehicleController com endpoints REST
- [ ] Integração com Location Service (Feign)
- [ ] Adicionar ao docker-compose.yml
- [ ] Dockerfile
- [ ] application.yml
- [ ] Documentação Swagger

### Payment Service
- [ ] Criar estrutura Maven (pom.xml)
- [ ] Entidades:
  - [ ] Payment
  - [ ] Transaction
  - [ ] PaymentStatus enum
- [ ] DTOs
- [ ] PaymentService:
  - [ ] Processar pagamentos
  - [ ] Dividir custos entre passageiros
  - [ ] Gerar recibos
  - [ ] Histórico de transações
- [ ] PaymentController
- [ ] Integração com Trip e Vehicle Services (Feign)
- [ ] Adicionar ao docker-compose.yml
- [ ] Dockerfile
- [ ] application.yml
- [ ] Swagger

### Location Service
- [ ] Criar estrutura Maven (pom.xml)
- [ ] Entidades:
  - [ ] Location
  - [ ] Route
- [ ] DTOs
- [ ] LocationService:
  - [ ] Geolocalização
  - [ ] Cálculo de distância
  - [ ] Determinação de proximidade
  - [ ] Sugestão de viagens próximas
- [ ] Integração com APIs externas:
  - [ ] Google Maps API
  - [ ] OpenStreetMap API
  - [ ] Mapbox (opcional)
- [ ] LocationController
- [ ] Adicionar ao docker-compose.yml
- [ ] Dockerfile
- [ ] application.yml
- [ ] Swagger

### Admin Service
- [ ] Criar estrutura Maven (pom.xml)
- [ ] Entidades:
  - [ ] AdminLog
  - [ ] SystemMetrics
- [ ] DTOs
- [ ] AdminService:
  - [ ] Gestão de utilizadores
  - [ ] Bloqueio de contas
  - [ ] Geração de relatórios
  - [ ] Monitorização de métricas (CO₂)
  - [ ] Estatísticas de utilização
- [ ] AdminController
- [ ] Integração com Auth Service (Feign)
- [ ] Adicionar ao docker-compose.yml
- [ ] Dockerfile
- [ ] application.yml
- [ ] Swagger

---

## 🎨 FASE 3 - FRONTEND (PARALELAMENTE) ✅ CONCLUÍDA

### Frontend Service (Spring MVC + Thymeleaf) ✅
- [x] Criar projeto Spring MVC
- [x] Bootstrap 5.3.0 CSS framework
- [x] Templates Thymeleaf (10 templates):
  - [x] Login/Register
  - [x] Dashboard de utilizador
  - [x] Pesquisa de viagens
  - [x] Criação de viagem
  - [x] Reserva de viagem
  - [x] Histórico (Minhas Viagens, Minhas Reservas)
  - [x] Perfil de utilizador
  - [x] Home page pública
  - [x] Layout base (navbar, footer, alerts)
  - [x] Error pages
- [x] Controllers MVC (3 controllers, 11 endpoints)
- [x] Integração com API Gateway via WebClient
- [x] JavaScript para interatividade (form validation, auto-dismiss alerts)
- [x] Responsive design (mobile-first com Bootstrap 5)
- [x] Validação client-side e server-side
- [x] Session management com JWT tokens
- [x] Dockerfile (openjdk:17-jdk-slim)
- [x] docker-compose.yml (porta 3000)
- [x] README_FRONTEND.md com documentação completa

### Alternativa: Frontend React/Vue
- [ ] Setup projeto Node.js
- [ ] Componentes React/Vue
- [ ] Integração com APIs
- [ ] Autenticação JWT
- [ ] Responsivo
- [ ] Docker
- [ ] docker-compose.yml

---

## 🔧 FASE 4 - FEATURES AVANÇADAS

### Notification Service
- [ ] Email service (Java Mail)
- [ ] Push notifications
- [ ] Message Queue (RabbitMQ/Kafka)
- [ ] Templates de notificação
- [ ] Agendamento de avisos

### Melhorias de Segurança
- [ ] CORS configuration refinada
- [ ] Rate limiting
- [ ] OAuth2 integration
- [ ] SSL/TLS
- [ ] Validação de email
- [ ] 2FA (Two-Factor Authentication)

### Observability
- [ ] ELK Stack (Elasticsearch, Logstash, Kibana)
- [ ] Prometheus para métricas
- [ ] Grafana para dashboards
- [ ] Distributed tracing (Sleuth + Zipkin)
- [ ] Application Insights

### Testing
- [ ] Unit tests (JUnit 5)
- [ ] Integration tests
- [ ] API tests (RestAssured)
- [ ] Load tests (JMeter)
- [ ] Security tests

### CI/CD
- [ ] GitHub Actions workflow
- [ ] Automated tests
- [ ] SonarQube analysis
- [ ] Auto deployment
- [ ] Docker registry push

---

## 📊 FASE 5 - OTIMIZAÇÃO E DEPLOYMENT

### Performance
- [ ] Caching (Redis)
- [ ] Database indexing
- [ ] Query optimization
- [ ] Load testing
- [ ] Performance profiling

### Escalabilidade
- [ ] Kubernetes deployment
- [ ] Helm charts
- [ ] Horizontal scaling
- [ ] Load balancing
- [ ] Database replication

### Cloud Deployment
- [ ] Azure App Service
- [ ] Azure Kubernetes Service (AKS)
- [ ] Azure Database for PostgreSQL
- [ ] Azure Container Registry
- [ ] Infrastructure as Code (Terraform/Bicep)

---

## 📈 PRIORIDADES

### Alta Prioridade
1. Vehicle Service ⚡
2. Payment Service ⚡
3. Frontend básico ⚡
4. Testes e documentação ⚡

### Média Prioridade
5. Admin Service
6. Location Service com APIs
7. Notification Service
8. Melhorias de segurança

### Baixa Prioridade
9. CI/CD avançado
10. Observability completa
11. Kubernetes
12. Cloud deployment

---

## 📝 CRITÉRIOS DE ACEITAÇÃO

Para cada novo microserviço:
- [ ] Pom.xml configurado
- [ ] Entidades JPA
- [ ] DTOs
- [ ] Repository
- [ ] Service com lógica
- [ ] Controller REST
- [ ] Swagger documentação
- [ ] Dockerfile
- [ ] application.yml
- [ ] Integrado no docker-compose.yml
- [ ] Testes básicos
- [ ] README atualizado

---

## 🎯 ESTIMATIVAS DE TEMPO

| Fase | Componente | Horas | Status |
|------|-----------|-------|--------|
| 1 | Estrutura Base | 12 | ✅ |
| 2 | Vehicle Service | 8 | ⏳ |
| 2 | Payment Service | 6 | ⏳ |
| 2 | Location Service | 8 | ⏳ |
| 2 | Admin Service | 6 | ⏳ |
| 3 | Frontend (Thymeleaf) | 16 | ⏳ |
| 4 | Notification Service | 6 | ⏳ |
| 4 | Security + Testing | 10 | ⏳ |
| 5 | Observability | 8 | ⏳ |
| 5 | CI/CD + Deployment | 12 | ⏳ |
| **TOTAL** | | **92 horas** | |

---

## 🚀 COMO CONTRIBUIR

1. Escolher uma tarefa da lista
2. Criar branch: `git checkout -b feature/nova-feature`
3. Implementar de acordo com a estrutura
4. Adicionar documentação
5. Fazer push e criar PR

---

## 📞 CONTACTOS

- Documentação: Ver README.md
- Dúvidas: Criar issue no repositório
- Bug reports: GitHub Issues

---

**Última atualização:** 24 de Dezembro de 2024
**Próxima milestone:** Vehicle Service completado
