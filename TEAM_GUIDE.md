# 👥 GUIA PARA A EQUIPA - CAR POOLING SHARING

## 📋 Informações da Estrutura do Projeto

Projeto académico para UC de Spring/Spring Cloud desenvolvido em **arquitetura de microserviços**.

### Localização do Projeto
```
D:\car-pooling-sharing\
```

### Responsabilidades por Rol

---

## 🏗️ ARQUITETURA - DIVISÃO DE TAREFAS

### 1️⃣ RESPONSÁVEL: Infrastructure & DevOps

**Tarefas:**
- [x] Configurar Eureka Server
- [x] Configurar API Gateway
- [x] Criar docker-compose.yml
- [x] Criar Dockerfiles
- [x] Base de dados PostgreSQL
- [ ] Configurar CI/CD (GitHub Actions) - PRÓXIMO
- [ ] Configurar Kubernetes - FUTURO
- [ ] Monitoring e Logging - FUTURO

**Ficheiros principais:**
- `eureka-server/`
- `api-gateway/`
- `docker-compose.yml`
- `db-init/01-init.sql`
- Todos os `Dockerfile`

**Comandos úteis:**
```bash
# Verificar serviços
docker-compose ps

# Ver logs
docker-compose logs -f <service-name>

# Rebuild
docker-compose build <service-name>

# Down
docker-compose down

# Reset completo
docker-compose down -v && docker-compose up -d
```

---

### 2️⃣ RESPONSÁVEL: Auth Service

**Tarefas:**
- [x] Implementar entidade User
- [x] Implementar endpoints REST (login/register)
- [x] JWT Token Provider
- [x] Spring Security config
- [x] Swagger documentation
- [ ] Adicionar validações extras - PRÓXIMO
- [ ] Integrar com OAuth2 - FUTURO
- [ ] 2FA authentication - FUTURO

**Ficheiros a editar:**
```
auth-service/
├── src/main/java/com/carpooling/auth/
│   ├── entity/User.java
│   ├── controller/AuthController.java
│   ├── service/AuthService.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   └── SecurityConfig.java
│   ├── repository/UserRepository.java
│   └── dto/*.java
└── src/main/resources/application.yml
```

**Novas features:**
```java
// Email validation
// Forgot password endpoint
// Update profile endpoint
// User ratings system
```

---

### 3️⃣ RESPONSÁVEL: Trip Service

**Tarefas:**
- [x] Implementar Trip e Reservation entities
- [x] CRUD operations
- [x] Pesquisa avançada de viagens
- [x] Sistema de reservas
- [x] Cálculo automático de custos
- [x] Circuit Breaker
- [ ] Adicionar avaliações de viagens - PRÓXIMO
- [ ] Implementar notificações - PRÓXIMO
- [ ] Integrar com Payment Service - PRÓXIMO

**Ficheiros a editar:**
```
trip-service/
├── src/main/java/com/carpooling/trip/
│   ├── entity/Trip.java
│   ├── entity/Reservation.java
│   ├── controller/TripController.java
│   ├── service/TripService.java
│   ├── repository/*.java
│   └── dto/*.java
└── src/main/resources/application.yml
```

**Novas features:**
```java
// Rating system para viagens
// Cancelamento de viagens
// Histórico de viagens do utilizador
// Filtros avançados de pesquisa
```

---

### 4️⃣ RESPONSÁVEL: Vehicle Service

**STATUS:** 📁 Estrutura criada, implementação pendente

**Tarefas:**
- [ ] Implementar Vehicle entity
- [ ] Implementar VehicleReservation entity
- [ ] CRUD de veículos
- [ ] Pesquisa por localização
- [ ] Sistema de aluguel por hora
- [ ] Cálculo de custos
- [ ] Documentação Swagger

**Estrutura esperada:**
```
vehicle-service/
├── pom.xml
├── Dockerfile
└── src/main/java/com/carpooling/vehicle/
    ├── VehicleServiceApplication.java
    ├── entity/
    │   ├── Vehicle.java
    │   ├── VehicleReservation.java
    │   └── VehicleStatus.java
    ├── controller/VehicleController.java
    ├── service/VehicleService.java
    ├── repository/
    │   ├── VehicleRepository.java
    │   └── VehicleReservationRepository.java
    └── dto/
        ├── VehicleDTO.java
        └── VehicleReservationDTO.java
```

**Configuração:**
```yaml
spring:
  application:
    name: vehicle-service
  datasource:
    url: jdbc:postgresql://localhost:5432/carpooling_vehicles
server:
  port: 8083
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

---

### 5️⃣ RESPONSÁVEL: Payment Service

**STATUS:** 📁 Estrutura criada, implementação pendente

**Tarefas:**
- [ ] Implementar Payment entity
- [ ] Implementar Transaction entity
- [ ] Processar pagamentos
- [ ] Dividir custos automaticamente
- [ ] Gerar recibos
- [ ] Histórico de transações
- [ ] Documentação Swagger

**Lógica de negócio:**
```java
// Quando uma reserva é feita, gerar transação
// Calcular valor por passageiro
// Registar histórico
// Gerar recibo
// Notificar utilizadores
```

---

### 6️⃣ RESPONSÁVEL: Admin Service

**STATUS:** 📁 Estrutura criada, implementação pendente

**Tarefas:**
- [ ] Dashboard administrativo (metrics)
- [ ] Gestão de utilizadores
- [ ] Bloqueio/ativação de contas
- [ ] Relatórios de utilização
- [ ] Monitorização de CO₂ poupado
- [ ] Logs de atividade do sistema
- [ ] Documentação Swagger

**Endpoints esperados:**
```
GET    /api/admin/statistics
GET    /api/admin/users
PUT    /api/admin/users/{id}/block
DELETE /api/admin/users/{id}
GET    /api/admin/reports/trips
GET    /api/admin/reports/revenue
GET    /api/admin/metrics/co2-saved
```

---

### 7️⃣ RESPONSÁVEL: Frontend Service

**STATUS:** 📁 Estrutura criada, implementação pendente

**Opção A: Thymeleaf + Spring MVC**
```
frontend-service/
├── src/main/java/com/carpooling/frontend/
│   └── controller/*.java (MVC Controllers)
├── src/main/resources/templates/
│   ├── login.html
│   ├── register.html
│   ├── dashboard.html
│   ├── trips/search.html
│   ├── trips/detail.html
│   ├── trips/create.html
│   ├── profile.html
│   └── admin/dashboard.html
├── src/main/resources/static/
│   ├── css/
│   │   └── style.css
│   └── js/
│       └── main.js
```

**Opção B: React/Vue.js**
```
frontend-service/
├── package.json
├── src/
│   ├── components/
│   ├── pages/
│   ├── services/
│   └── App.js
```

**Páginas principais:**
- Login / Register
- Dashboard de utilizador
- Pesquisa de viagens
- Detalhes da viagem
- Fazer reserva
- Histórico de viagens
- Perfil de utilizador
- Dashboard administrativo

---

### 8️⃣ RESPONSÁVEL: Location Service

**STATUS:** 📁 Estrutura criada, implementação pendente

**Tarefas:**
- [ ] Implementar Location entity
- [ ] Geolocalização
- [ ] Cálculo de distância
- [ ] Integração com Google Maps API
- [ ] Sugestão de viagens próximas
- [ ] Documentação Swagger

**Integração externa:**
```java
// Google Maps API para rotas
// Cálculo de distância em tempo real
// Geocoding (endereço -> coordenadas)
```

---

## 📝 PADRÕES DE CÓDIGO

### Para cada novo Microserviço:

**1. POM.xml**
```xml
<parent>
    <groupId>com.carpooling</groupId>
    <artifactId>car-pooling-parent</artifactId>
    <version>1.0.0</version>
</parent>
<artifactId>novo-servico</artifactId>
<name>Novo Servico</name>
```

**2. Main Application Class**
```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class NovoServicoApplication {
    public static void main(String[] args) {
        SpringApplication.run(NovoServicoApplication.class, args);
    }
}
```

**3. Entity Pattern**
```java
@Entity
@Table(name = "tabela")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovaEntidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

**4. Service Pattern**
```java
@Service
@AllArgsConstructor
@Slf4j
public class NovoServico {
    @Transactional
    public NovoDTO criar(CriacaoRequest request) {
        // Lógica
        log.info("Entidade criada: {}", id);
        return mapToDTO(entidade);
    }
}
```

**5. Controller Pattern**
```java
@RestController
@RequestMapping("/api/novo")
@AllArgsConstructor
@Tag(name = "Novo", description = "Descrição do serviço")
public class NovoController {
    private final NovoService novoService;
    
    @PostMapping
    @Operation(summary = "Criar", description = "Cria novo recurso")
    public ResponseEntity<NovoDTO> criar(@RequestBody CriacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(novoService.criar(request));
    }
}
```

---

## 🧪 TESTES - COMO TESTAR

### Via Postman
1. Importar `Car_Pooling_API.postman_collection.json`
2. Setups > Executar sequencialmente

### Via cURL
```bash
# Ver todos os serviços em Eureka
curl http://localhost:8761/eureka/apps

# Testar Auth Service
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"123456",...}'

# Testar Trip Service
curl http://localhost:8080/api/trips
```

### Via Docker Logs
```bash
docker-compose logs -f auth-service
docker-compose logs -f trip-service
```

---

## 🔄 WORKFLOW GIT

### Criar nova branch para feature
```bash
git checkout -b feature/nova-feature

# Fazer commits
git add .
git commit -m "Implementar nova feature"

# Push e fazer PR
git push origin feature/nova-feature
```

### Branches recomendadas
```
main (produção)
├── develop (desenvolvimento)
│   ├── feature/vehicle-service
│   ├── feature/payment-service
│   ├── feature/admin-service
│   ├── feature/frontend
│   └── feature/location-service
```

---

## 📊 MÉTRICAS DE SUCESSO

- [ ] Todos os 8 microserviços implementados
- [ ] Testes unitários > 70% cobertura
- [ ] Documentação completa (README + Swagger)
- [ ] Docker Compose funcionando
- [ ] Frontend responsivo
- [ ] CI/CD configurado
- [ ] Apresentação de 12 minutos

---

## 🗓️ TIMELINE SUGERIDA

| Semana | Tarefas |
|--------|---------|
| 1-2    | Auth Service + Trip Service (DONE) |
| 3-4    | Vehicle Service + Payment Service |
| 5-6    | Admin Service + Location Service |
| 7      | Frontend + Testes |
| 8      | CI/CD + Apresentação |

---

## 🆘 DÚVIDAS?

1. Consultar README.md
2. Ver ESTRUTURA_PROJETO.md
3. Verificar Postman Collection
4. Ver ROADMAP.md para próximos passos

---

**Boa sorte no desenvolvimento! 🚀**
