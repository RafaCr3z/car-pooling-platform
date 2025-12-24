# 🚀 DEPLOY ROADMAP - 3 Passos para Production

## PASSO 1: BUILD (5 minutos)

### Opção A: Build Completo
```bash
cd d:\car-pooling-sharing
mvn clean install
```

### Opção B: Build Apenas Frontend
```bash
mvn clean install -f frontend-service/pom.xml
```

### Verificação
```bash
# Deve terminar com:
# [INFO] BUILD SUCCESS
# [INFO] Total time: X.XXX s

# Ficheiro criado:
# frontend-service/target/frontend-service-1.0.0.jar
```

---

## PASSO 2: DOCKER SETUP (3 minutos)

### Verificar docker-compose.yml
```bash
docker-compose config
# Deve listar todos os serviços sem erros
```

### Build Docker Images
```bash
docker-compose build
# Ou apenas frontend:
docker-compose build frontend-service
```

### Verificar Imagem
```bash
docker images | grep frontend-service
# Esperado: frontend-service   latest   abc123...   200MB
```

---

## PASSO 3: DEPLOY (2 minutos)

### Iniciar Stack
```bash
docker-compose up -d
```

### Verificar Status
```bash
docker-compose ps

# Esperado após 30 segundos:
# NAME                STATUS      PORTS
# frontend-service    Up 25s      0.0.0.0:3000->3000/tcp
# api-gateway         Up 25s      0.0.0.0:8080->8080/tcp
# auth-service        Up 25s      0.0.0.0:8081->8081/tcp
# trip-service        Up 25s      0.0.0.0:8082->8082/tcp
# eureka-server       Up 25s      0.0.0.0:8761->8761/tcp
# postgres-db         Up 25s      5432/tcp
```

### Verificar Frontend
```bash
curl http://localhost:3000

# Esperado: HTML da página inicial (200 status code)
```

---

## VERIFICAÇÃO FINAL ✅

### 1. Abrir Navegador
```
http://localhost:3000
```

### Esperado na Página
- Logo/Título "Car Pooling"
- Hero section com "Partilhe a sua Viagem"
- Botões "Registar" e "Fazer Login"
- 6 Feature cards
- How-it-works section
- Footer

### 2. Criar Conta
```
1. Clique "Registar"
2. Preencha:
   - Email: teste@exemplo.com
   - Telemóvel: +351912345678
   - Senha: senha123
   - Nome: Test User
   - Tipo: PASSENGER
3. Clique "Criar Conta"
4. Mensagem: "Conta criada com sucesso!"
```

### 3. Fazer Login
```
1. Clique "Fazer Login"
2. Email: teste@exemplo.com
3. Senha: senha123
4. Clique "Entrar"
5. Redireciona para /dashboard
```

### 4. Testar Dashboard
```
1. Ver página com boas-vindas
2. Ver estatísticas (0 viagens, 0 participadas)
3. Ver 3 quick action cards
4. Clicar em "Perfil" - Funciona?
5. Logout - Limpa sessão?
```

---

## TROUBLESHOOTING RÁPIDO

### ❌ "Port 3000 em uso"
```bash
lsof -i :3000
kill -9 <PID>
# Ou alterar em docker-compose.yml: "3001:3000"
```

### ❌ "Build fails"
```bash
# Limpar cache Maven
mvn clean

# Fazer build fresh
mvn clean install -U

# Verificar Java version
java -version  # Deve ser 17+
```

### ❌ "Docker fails"
```bash
# Verificar docker
docker --version  # v20.10+

# Restart Docker
# Windows: Abrir Docker Desktop
# Linux: sudo systemctl restart docker

# Retry
docker-compose up -d
```

### ❌ "Frontend não carrega"
```bash
# Verificar logs
docker logs frontend-service

# Aguardar 30 segundos (startup lento)
sleep 30

# Tentar novamente
curl http://localhost:3000
```

### ❌ "Login falha"
```bash
# Verificar Auth Service
docker logs auth-service

# Verificar banco de dados
docker exec -it postgres-db psql -U carpooling -c "SELECT * FROM users;"
```

---

## PERFORMANCE ESPERADA

### Tempos de Startup
```
Postgres:       ~5-10 segundos
Eureka:         ~10-15 segundos
Gateway:        ~5-10 segundos (after Eureka)
Auth Service:   ~5-10 segundos (after DB)
Trip Service:   ~5-10 segundos (after DB)
Frontend:       ~2-5 segundos (after Eureka)

Total:          ~30-50 segundos até pronto
```

### Tempos de Resposta
```
Home page:      < 500ms
Login:          < 2s
Pesquisa:       < 3s
Criar viagem:   < 2s
Reservar:       < 2s
```

### Uso de Recursos
```
Frontend Container:   ~120MB RAM
API Gateway:          ~200MB RAM
Auth Service:         ~180MB RAM
Trip Service:         ~180MB RAM
Postgres:             ~100-150MB RAM
Eureka:               ~200MB RAM

Total:                ~900MB - 1GB RAM (normal)
```

---

## CHECKLIST PRÉ-DEPLOYMENT

- [ ] Java 17+ instalado: `java -version`
- [ ] Maven 3.8+ instalado: `mvn -version`
- [ ] Docker instalado: `docker --version`
- [ ] Docker Compose instalado: `docker-compose --version`
- [ ] Git instalado: `git --version`
- [ ] Repositório clonado: `d:\car-pooling-sharing`

- [ ] Maven build sucesso: `mvn clean install`
- [ ] Docker images criadas: `docker images | grep frontend`
- [ ] docker-compose válido: `docker-compose config`

- [ ] Stack iniciado: `docker-compose up -d`
- [ ] Todos containers Up: `docker-compose ps`
- [ ] Frontend carrega: `curl http://localhost:3000`
- [ ] Eureka vê serviços: `http://localhost:8761`

- [ ] Registo funciona
- [ ] Login funciona
- [ ] Dashboard carrega
- [ ] Pesquisa de viagens funciona
- [ ] Criar viagem funciona
- [ ] Perfil carrega

---

## ROLLBACK (Se necessário)

### Parar Serviços
```bash
docker-compose stop
```

### Remover Containers
```bash
docker-compose down
```

### Remover Dados (Database)
```bash
docker-compose down -v
```

### Reconstruir Tudo
```bash
docker-compose down -v
mvn clean install
docker-compose build
docker-compose up -d
```

---

## MONITORAMENTO EM PRODUÇÃO

### Verificar Saúde
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8761/eureka/apps
```

### Ver Logs
```bash
# Todos os serviços
docker-compose logs -f

# Apenas frontend
docker-compose logs -f frontend-service

# Últimas 100 linhas
docker-compose logs --tail=100 frontend-service
```

### Usar Portainer (opcional)
```bash
docker run -d -p 9000:9000 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  portainer/portainer-ce

# Aceder: http://localhost:9000
```

---

## PRÓXIMA FASE

Após validar que tudo funciona:

1. **Testar integração completa** (1-2 horas)
   - Testar todos os fluxos
   - Testar com múltiplos utilizadores
   - Testar edge cases

2. **Implementar Vehicle Service** (40-50 horas)
   - Seguir o mesmo padrão
   - Adaptar para domínio de veículos

3. **Implementar Payment Service** (30-40 horas)
   - Processamento de pagamentos
   - Cálculo de custos

4. **Deploy em Produção** (quando todos os 8 serviços estiverem prontos)
   - Kubernetes ou Azure Container Instances
   - Load balancing
   - SSL/HTTPS
   - Monitoring & alerts

---

## SCRIPTS ÚTEIS

### Build & Deploy One-Liner
```bash
mvn clean install && docker-compose build && docker-compose up -d
```

### Fresh Start
```bash
docker-compose down -v && mvn clean install && docker-compose build && docker-compose up -d
```

### Apenas Restart
```bash
docker-compose restart
```

### Logs em Tempo Real
```bash
docker-compose logs -f
```

### Stop All
```bash
docker-compose stop
```

### Remove All
```bash
docker-compose down
```

---

## VERSÕES UTILIZADAS

```
Java:             17
Spring Boot:      3.2.0
Spring Cloud:     2023.0.0
Maven:            3.8.0+
Docker:           20.10+
Docker Compose:   2.0+
PostgreSQL:       15
OpenJDK:          17-jdk-slim
```

---

## DOCUMENTAÇÃO DE REFERÊNCIA

- [QUICK_START.md](QUICK_START.md) - 5 minutos setup
- [BUILD_AND_TEST_GUIDE.md](BUILD_AND_TEST_GUIDE.md) - Build detalhado
- [README_FRONTEND.md](README_FRONTEND.md) - Frontend técnico
- [docker-compose.yml](docker-compose.yml) - Configuração serviços

---

## SUPORTE

### Documentação
- [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) - Índice completo
- [README.md](README.md) - Visão geral

### Contacto
- Verificar logs: `docker-compose logs [service-name]`
- Verificar status: `docker-compose ps`
- Restart: `docker-compose restart [service-name]`

---

## STATUS FINAL

```
✅ Frontend Service:     100% pronto
✅ Docker Setup:          Configurado
✅ docker-compose:        Pronto
✅ Documentação:          Completa
✅ Deployment:            Pronto

🚀 PODE FAZER DEPLOY AGORA! 🚀
```

---

**Tempo até Production:** ~10-15 minutos  
**Tempo até tudo validado:** ~1 hora  
**Status:** ✅ PRONTO PARA DEPLOYMENT  

