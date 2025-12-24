# 🚀 Guia de Build e Testes - Car Pooling

## 📋 Pré-Requisitos

- **Java 17+**: `java -version`
- **Maven 3.8+**: `mvn -version`
- **Docker**: `docker --version`
- **Docker Compose**: `docker-compose --version`
- **Git**: `git --version`

---

## 🔨 Build Local (sem Docker)

### 1. Build Completo de Todos os Serviços

```bash
# Ir para raiz do projeto
cd d:\car-pooling-sharing

# Build completo (compila todos os 8 módulos)
mvn clean install -DskipTests

# Saída esperada:
# [INFO] Building car-pooling-parent
# [INFO] --- maven-clean-plugin:3.2.0:clean
# [INFO] --- maven-resources-plugin:3.3.0:resources
# [INFO] --- maven-compiler-plugin:3.10.1:compile
# [INFO] --- maven-jar-plugin:3.2.2:jar
# [INFO] BUILD SUCCESS
```

### 2. Build de Módulos Individuais

```bash
# Build apenas Auth Service
mvn clean install -f auth-service/pom.xml

# Build apenas Trip Service
mvn clean install -f trip-service/pom.xml

# Build apenas Frontend Service
mvn clean install -f frontend-service/pom.xml
```

### 3. Executar Serviços Localmente

```bash
# Terminal 1: Eureka Server
cd eureka-server
mvn spring-boot:run

# Terminal 2: API Gateway
cd api-gateway
mvn spring-boot:run

# Terminal 3: Auth Service
cd auth-service
mvn spring-boot:run

# Terminal 4: Trip Service
cd trip-service
mvn spring-boot:run

# Terminal 5: Frontend Service
cd frontend-service
mvn spring-boot:run

# Aceder a http://localhost:3000
```

---

## 🐳 Docker & Docker Compose

### 1. Validar docker-compose.yml

```bash
# Validar sintaxe YAML
docker-compose config

# Saída esperada: lista completa dos serviços sem erros
```

### 2. Build das Imagens Docker

```bash
# Build todas as imagens (definidas em docker-compose.yml)
docker-compose build

# Build apenas do frontend
docker-compose build frontend-service

# Ver imagens criadas
docker images | grep frontend-service
```

### 3. Iniciar Stack Completo

```bash
# Iniciar todos os serviços em background
docker-compose up -d

# Ver status dos containers
docker-compose ps

# Saída esperada:
# NAME                 COMMAND                  SERVICE              STATUS
# eureka-server        "java -jar app.jar"      eureka-server        Up 2m
# api-gateway          "java -jar app.jar"      api-gateway          Up 2m
# auth-service         "java -jar app.jar"      auth-service         Up 2m
# trip-service         "java -jar app.jar"      trip-service         Up 2m
# frontend-service     "java -jar app.jar"      frontend-service     Up 2m
# postgres-db          "docker-entrypoint..."   postgres             Up 2m
```

### 4. Verificar Logs

```bash
# Logs de todos os serviços
docker-compose logs -f

# Logs apenas do frontend
docker-compose logs -f frontend-service

# Logs dos últimas 100 linhas
docker-compose logs --tail=100 frontend-service

# Procurar por erros
docker-compose logs frontend-service | grep ERROR
```

### 5. Parar e Remover Serviços

```bash
# Parar containers (mantém dados)
docker-compose stop

# Parar e remover containers (remove volumes por padrão)
docker-compose down

# Parar e remover tudo (incluindo volumes)
docker-compose down -v
```

---

## ✅ Testes de Integração

### 1. Verificar Eureka

```
URL: http://localhost:8761

Esperado:
- Applications (registro de serviços)
  - EUREKA-SERVER
  - API-GATEWAY
  - AUTH-SERVICE
  - TRIP-SERVICE
  - FRONTEND-SERVICE
```

### 2. Verificar API Gateway Health

```bash
curl http://localhost:8080/actuator/health

Resposta esperada:
{"status":"UP"}
```

### 3. Testar Frontend

```
1. Abrir http://localhost:3000
   Esperado: Página inicial com hero section, features, how-it-works

2. Clicar "Registar"
   Esperado: Formulário de registo com campos:
   - Email
   - Telemóvel
   - Password
   - Confirmar Password
   - Nome Completo
   - Tipo (PASSENGER/DRIVER)
   - Aceitar Termos

3. Preencher e submeter
   Esperado: "Conta criada com sucesso! Faça login."
            Redireciona para /login

4. Fazer Login
   Esperado: Dashboard com:
   - Welcome message
   - Quick action cards
   - Statistics grid

5. Clique em "Pesquisar Viagens"
   Esperado: Formulário com origem/destino/data

6. Pesquisar
   Esperado: Lista de viagens (ou "Nenhuma viagem encontrada")
             Cards com info: rota, hora, lugares, preço
             Botão "Reservar" ativo (se houver lugares)
```

### 4. Testar Autenticação

```bash
# Testar endpoint de login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Resposta esperada:
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "email": "test@example.com",
#   "fullName": "Test User"
# }
```

### 5. Testar Viagens

```bash
# Usar token obtido no login
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Pesquisar viagens
curl -X GET 'http://localhost:8080/api/trips/search?origin=Lisboa&destination=Porto' \
  -H "Authorization: Bearer $TOKEN"

# Criar viagem
curl -X POST http://localhost:8080/api/trips \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "departureLocation": "Lisboa",
    "destinationLocation": "Porto",
    "departureDateTime": "2024-03-15T10:00:00",
    "availableSeats": 3,
    "costPerPassenger": 15.50
  }'
```

---

## 🧪 Checklist de Testes

### Frontend Service

- [ ] Frontend compila sem warnings
- [ ] Imagem Docker cria com sucesso
- [ ] Container inicia sem erros
- [ ] Logs não mostram stack traces
- [ ] Health check passa (http://localhost:3000 returns 200)
- [ ] Eureka descobre o frontend-service
- [ ] Página home carrega (http://localhost:3000/)
- [ ] Registo funciona (testa 5 contas diferentes)
- [ ] Login funciona com credenciais corretas
- [ ] Login falha com credenciais erradas
- [ ] Dashboard carrega após login
- [ ] Sessão timeout após 30 minutos
- [ ] Logout limpa a sessão
- [ ] Pesquisa de viagens funciona
- [ ] Criação de viagem funciona
- [ ] Reserva de viagem funciona
- [ ] Perfil carrega e permite edição
- [ ] Responsive design em mobile (abrir DevTools, F12)
- [ ] Alertas auto-dismissem após 5 segundos
- [ ] Validação client-side funciona

### Integração Completa

- [ ] Frontend → API Gateway → Auth Service (login)
- [ ] Frontend → API Gateway → Trip Service (pesquisa)
- [ ] Tokens JWT são validados corretamente
- [ ] Cross-origin requests funcionam (CORS)
- [ ] Database persiste dados entre reinícios
- [ ] Nenhum serviço pode aceder sem registar em Eureka

---

## 📊 Monitoramento

### Verificar Uso de Recursos

```bash
# Docker stats
docker stats

# Saída exemplo:
# CONTAINER           CPU %    MEM USAGE / LIMIT
# frontend-service    0.5%     120MiB / 4GiB
# api-gateway         1.2%     200MiB / 4GiB
# trip-service        0.8%     180MiB / 4GiB
```

### Verificar Database

```bash
# Aceder à database PostgreSQL
docker exec -it postgres-db psql -U carpooling

# Listar databases
\l

# Conectar a uma database
\c carpooling_auth

# Ver tabelas
\dt

# Sair
\q
```

---

## 🐛 Troubleshooting

### Problema: "Port 3000 already in use"

```bash
# Listar processo na porta 3000
lsof -i :3000

# Matar processo
kill -9 <PID>

# Ou usar porta diferente
docker-compose up -d -e "SERVER_PORT=3001" frontend-service
```

### Problema: "Connection refused" ao chamar API

```bash
# Verificar se API Gateway está rodando
curl http://localhost:8080/actuator/health

# Verificar logs do gateway
docker logs api-gateway

# Verificar rede
docker network ls
docker network inspect carpooling-network
```

### Problema: "Eureka Server not responding"

```bash
# Verificar Eureka
curl http://localhost:8761/eureka/apps

# Verificar logs
docker logs eureka-server

# Reiniciar Eureka
docker-compose restart eureka-server
```

### Problema: Frontend não consegue fazer login

```bash
# Verificar se Auth Service está rodando
curl http://localhost:8080/api/auth/login -X GET

# Verificar logs do auth service
docker logs auth-service

# Verificar database
docker exec -it postgres-db psql -U carpooling -c "SELECT * FROM users;"
```

### Problema: Imagem Docker muito grande

```bash
# Limpar imagens não usadas
docker image prune

# Remover todas as imagens de um serviço
docker rmi frontend-service:latest

# Rebuild
docker-compose build --no-cache frontend-service
```

---

## 🔄 Desenvolvimento Contínuo

### Hot Reload (auto-reload ao editar código)

```yaml
# application.yml deve ter:
spring:
  devtools:
    restart:
      enabled: true
  thymeleaf:
    cache: false
```

```bash
# Reiniciar serviço quando detectar mudanças
mvn spring-boot:run -Dspring-boot.run.fork=false
```

### Build Incremental

```bash
# Compilar apenas ficheiros alterados
mvn install -DskipTests

# Muito mais rápido do que clean install
```

### Maven Clean vs Build

```bash
# Clean (remove tudo)
mvn clean install

# Build (reutiliza o que já existe)
mvn install

# Test (apenas executa testes)
mvn test

# Package (empacota sem instalar)
mvn package
```

---

## 📦 Artefatos Gerados

### Após Build Local

```
frontend-service/target/
├── frontend-service-1.0.0.jar          (JAR executável)
├── classes/
├── generated-sources/
├── maven-status/
└── surefire-reports/                   (Testes)
```

### Após Docker Build

```
Docker Image:
- Repository: frontend-service
- Tag: latest
- Size: ~200-300MB
- Base: openjdk:17-jdk-slim

Docker Container:
- Name: frontend-service
- Port: 3000
- Network: carpooling-network
- Environment: Spring profile docker, Eureka URL
```

---

## 🎯 Próximas Fases

### Vehicle Service
```bash
# Estrutura pronta, apenas precisa implementação
mvn clean install -f vehicle-service/pom.xml
```

### Payment Service
```bash
mvn clean install -f payment-service/pom.xml
```

### Admin Service
```bash
mvn clean install -f admin-service/pom.xml
```

### Location Service
```bash
mvn clean install -f location-service/pom.xml
```

---

## 📚 Referências

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Thymeleaf](https://www.thymeleaf.org)
- [Bootstrap 5](https://getbootstrap.com)
- [Docker Compose](https://docs.docker.com/compose)
- [Maven Documentation](https://maven.apache.org/guides)

---

**Última Atualização**: Março 2024
**Status**: Frontend Service 100% Completo ✅
**Próximo**: Vehicle Service & Payment Service

