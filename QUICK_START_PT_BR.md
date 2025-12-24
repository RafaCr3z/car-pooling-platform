# 🚀 GUIA DE INÍCIO RÁPIDO - Car Pooling Platform

## ⚡ Iniciar em 3 Passos

### Passo 1: Build
```bash
cd d:\car-pooling-sharing
mvn clean package -DskipTests
```
**Tempo estimado:** 3-5 minutos

### Passo 2: Iniciar Docker
```bash
docker-compose up --build
```
**Tempo estimado:** 2-3 minutos

### Passo 3: Acessar
- **Frontend:** http://localhost:3000
- **Eureka Dashboard:** http://localhost:8761

---

## 📋 Teste os Serviços

### 1. Eureka Dashboard
```
URL: http://localhost:8761
Verificar se todos os 7 serviços estão registrados:
✓ eureka-server
✓ api-gateway
✓ auth-service
✓ trip-service
✓ vehicle-service
✓ payment-service
✓ location-service
```

### 2. Teste de Registro (Auth Service)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@test.com",
    "password": "password123",
    "fullName": "Test User",
    "role": "PASSENGER"
  }'
```

### 3. Teste de Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@test.com",
    "password": "password123"
  }'
```

### 4. Teste de Vehicle Service
```bash
curl -X POST http://localhost:8080/api/vehicles \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "ABC-1234",
    "brand": "Toyota",
    "model": "Corolla",
    "year": 2023,
    "type": "CAR",
    "ownerId": 1,
    "pricePerHour": 50.00,
    "pricePerKm": 0.50,
    "location": "São Paulo, SP"
  }'
```

### 5. Teste de Location Service
```bash
curl -X POST http://localhost:8080/api/locations/distance \
  -H "Content-Type: application/json" \
  -d '{
    "lat1": -23.5505,
    "lon1": -46.6333,
    "lat2": -23.6345,
    "lon2": -46.7456
  }'
```

---

## 🔍 Solução de Problemas

### Serviço não aparece no Eureka
1. Verificar logs: `docker logs <service-name>`
2. Verificar conexão com Eureka Server
3. Certificar que `@EnableDiscoveryClient` está presente

### Erro de Banco de Dados
```bash
# Verificar se PostgreSQL está rodando
docker ps | grep postgres

# Conectar ao banco
psql -h localhost -U carpooling -d carpooling_auth
```

### Porta Já em Uso
```bash
# Parar todos os containers
docker-compose down

# Limpar volumes (se necessário)
docker-compose down -v

# Reconstruir
docker-compose up --build
```

---

## 📊 Monitoramento

### Logs em Tempo Real
```bash
docker-compose logs -f vehicle-service
docker-compose logs -f payment-service
docker-compose logs -f location-service
```

### Verificar Status dos Serviços
```bash
# Todos os serviços devem retornar {"status":"UP"}
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
```

---

## 📚 Documentação

| Documento | Descrição |
|-----------|-----------|
| `IMPLEMENTATION_COMPLETE.md` | Resumo completo da implementação |
| `README.md` | Visão geral do projeto |
| `DEPLOY_GUIDE.md` | Guia de deploy para produção |
| `BUILD_AND_TEST_GUIDE.md` | Como fazer build e testes |
| `QUICK_START.md` | Guia rápido |

---

## 🎯 Funcionalidades por Serviço

| Serviço | Endpoints | Status |
|---------|-----------|--------|
| **Vehicle Service** | 6 endpoints | ✅ Completo |
| **Payment Service** | 7 endpoints | ✅ Completo |
| **Location Service** | 4 endpoints | ✅ Completo |
| **Trip Service** | 11 endpoints | ✅ Completo |
| **Auth Service** | 5 endpoints | ✅ Completo |
| **Frontend Service** | 15 endpoints | ✅ Completo |

---

## 🐳 Comandos Docker Úteis

```bash
# Ver todos os containers
docker-compose ps

# Ver logs de um serviço
docker-compose logs vehicle-service

# Entrar em um container
docker exec -it vehicle-service bash

# Parar todos os serviços
docker-compose down

# Remover volumes (limpar dados)
docker-compose down -v

# Reconstruir todas as imagens
docker-compose build --no-cache
```

---

## ✅ Checklist

- [ ] Maven build completado
- [ ] Docker Compose iniciado
- [ ] Frontend acessível em http://localhost:3000
- [ ] Eureka Dashboard mostra 7 serviços
- [ ] Teste de registro de usuário funciona
- [ ] Teste de login funciona
- [ ] Testes de API retornam sucesso
- [ ] Logs não mostram erros críticos

---

## 🚀 Próximos Passos

1. ✅ Implementação - **COMPLETO**
2. 🔧 Testes Unitários - Adicionar JUnit5 + Mockito
3. 📊 Monitoramento - Configurar Prometheus/Grafana
4. 🔒 Segurança - Adicionar SSL/TLS
5. 🌍 Deployment - Deploy em Kubernetes ou AWS

---

**Status:** ✅ PRONTO PARA USAR
**Última Atualização:** 2024
**Versão:** 1.0.0
