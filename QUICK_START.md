# 🚀 QUICK START - Car Pooling com Frontend

## ⚡ 5 Minutos para ter tudo a funcionar

### 1️⃣ Build (1 minuto)

```bash
cd d:\car-pooling-sharing
mvn clean install
```

**Esperado:** `BUILD SUCCESS` no final

---

### 2️⃣ Docker Compose (2 minutos)

```bash
docker-compose up -d
```

**Esperado:** Todos os 6 serviços iniciando

```
Creating postgres-db ... done
Creating eureka-server ... done
Creating api-gateway ... done
Creating auth-service ... done
Creating trip-service ... done
Creating frontend-service ... done
```

---

### 3️⃣ Verificar Status (1 minuto)

```bash
# Verificar containers
docker-compose ps

# Esperado: Todos com status "Up"
```

---

### 4️⃣ Aceder à Plataforma (1 minuto)

**Frontend**: http://localhost:3000/

Você será redirecionado para a página inicial com:
- 🎯 Hero section com call-to-action
- ⭐ 6 Features principais
- 📝 Como funciona
- 🔘 Botões "Registar" / "Fazer Login"

---

## 🎯 Teste o Fluxo Completo

### 1. Criar Conta
1. Clique em "Registar"
2. Preencha:
   - Email: `teste@exemplo.com`
   - Telemóvel: `+351912345678`
   - Senha: `senha123`
   - Nome: `João Silva`
   - Tipo: `PASSENGER`
3. Clique "Criar Conta"
4. Mensagem: "Conta criada com sucesso!"

### 2. Fazer Login
1. Clique em "Fazer Login"
2. Email: `teste@exemplo.com`
3. Senha: `senha123`
4. Clique "Entrar"
5. Redirecionado para `/dashboard`

### 3. Ver Dashboard
- Página de boas-vindas
- Estatísticas: 0 viagens, 0 participadas
- 3 Quick action cards (Pesquisar, Criar, Reservas)
- Link para perfil

### 4. Pesquisar Viagens
1. Clique em "Pesquisar Viagens"
2. Preencha:
   - Origem: `Lisboa`
   - Destino: `Porto`
   - Data: Qualquer data futura
3. Clique "Pesquisar"
4. Ver resultados (ou mensagem "Nenhuma viagem encontrada")

### 5. Criar Viagem
1. Clique em "Criar Viagem"
2. Preencha:
   - Origem: `Covilhã`
   - Destino: `Lisboa`
   - Data/Hora: Data futura
   - Lugares: `4`
   - Custo total: `40`
3. Clique "Criar"
4. Mensagem: "Viagem criada com sucesso!"

---

## 📊 Verificar Serviços

| Serviço | URL | Esperado |
|---------|-----|----------|
| **Frontend** | http://localhost:3000 | Página inicial |
| **Eureka** | http://localhost:8761 | Dashboard com 5 apps |
| **Gateway** | http://localhost:8080/actuator/health | `{"status":"UP"}` |
| **Auth API** | http://localhost:8081/swagger-ui.html | Swagger UI |
| **Trip API** | http://localhost:8082/swagger-ui.html | Swagger UI |

---

## 🐛 Troubleshooting Rápido

### ❌ "Frontend não carrega" (http://localhost:3000)
```bash
# Verificar logs
docker logs frontend-service

# Reiniciar
docker-compose restart frontend-service

# Aguardar 10 segundos
```

### ❌ "Login falha"
```bash
# Verificar se Auth Service está ok
curl http://localhost:8080/api/auth/login -X GET

# Verificar database
docker exec -it postgres-db psql -U carpooling -c "SELECT * FROM users;"
```

### ❌ "Pesquisa vazia"
```bash
# Criar uma viagem manualmente com Postman/Curl
# Ou aguardar que outra pessoa crie uma viagem

# Verificar se Trip Service está ok
curl http://localhost:8080/api/trips -X GET
```

### ❌ "Port 3000 em uso"
```bash
# Matar processo
lsof -i :3000
kill -9 <PID>

# Ou alterar porta no docker-compose.yml
# "3001:3000"
```

---

## 📁 Ficheiros Importantes

```
📦 d:\car-pooling-sharing\
├── 📖 README.md                    ← Leia primeiro
├── 📖 IMPLEMENTATION_SUMMARY.md    ← Status completo
├── 📖 BUILD_AND_TEST_GUIDE.md      ← Instruções detalhadas
├── 📖 FRONTEND_STATUS.md           ← Frontend detalhes
├── 📖 README_FRONTEND.md           ← Frontend técnico
├── 🐳 docker-compose.yml           ← Orquestração
├── 📋 pom.xml                      ← Maven parent
├── 📁 frontend-service/            ← Código Frontend
│   ├── pom.xml
│   ├── Dockerfile
│   ├── src/main/
│   │   ├── java/...                ← Controllers, Services
│   │   └── resources/
│   │       ├── application.yml
│   │       └── templates/          ← 10 HTML files
├── 📁 auth-service/                ← Auth
├── 📁 trip-service/                ← Viagens
├── 📁 api-gateway/                 ← Gateway
└── 📁 eureka-server/               ← Discovery
```

---

## 💾 Base de Dados

### Tabelas Principais

```sql
-- Auth Service
SELECT * FROM users;          -- Todas as contas criadas

-- Trip Service  
SELECT * FROM trips;          -- Todas as viagens
SELECT * FROM reservations;   -- Todas as reservas
```

### Aceder via Docker

```bash
docker exec -it postgres-db psql -U carpooling

# Dentro do psql:
\l                             -- Listar databases
\c carpooling_auth             -- Conectar ao BD de Auth
\dt                            -- Listar tabelas
SELECT * FROM users;           -- Ver utilizadores
\q                             -- Sair
```

---

## 🔄 Restartar Tudo (Fresh Start)

```bash
# 1. Parar serviços
docker-compose down

# 2. Remover volumes (limpar database)
docker-compose down -v

# 3. Rebuild
mvn clean install -f frontend-service/pom.xml

# 4. Iniciar
docker-compose up -d

# 5. Aguardar 30 segundos para inicialização
sleep 30

# 6. Aceder
http://localhost:3000
```

---

## 📊 Status Esperado

```
$ docker-compose ps

NAME                 COMMAND                  STATUS
postgres-db          docker-entrypoint...     Up 2m
eureka-server        java -jar app.jar        Up 1m 45s
api-gateway          java -jar app.jar        Up 1m 40s
auth-service         java -jar app.jar        Up 1m 35s
trip-service         java -jar app.jar        Up 1m 30s
frontend-service     java -jar app.jar        Up 1m 25s
```

---

## 🎯 Próximas Melhorias

Após validar que tudo funciona:

1. **Vehicle Service** (Car Sharing) - 40-50 horas
2. **Payment Service** (Processamento de pagamentos) - 30-40 horas
3. **Admin Service** (Administração) - 30-40 horas
4. **Location Service** (Geolocalização) - 40-50 horas

---

## ✨ Tudo Pronto!

A plataforma Car Pooling com interface web responsiva está completa e funcional.

**Progresso:** 50% do projeto (5 de 8 microserviços)

---

## 📞 Documentação Detalhada

- **[README.md](README.md)** - Visão geral do projeto
- **[README_FRONTEND.md](README_FRONTEND.md)** - Documentação técnica do Frontend
- **[BUILD_AND_TEST_GUIDE.md](BUILD_AND_TEST_GUIDE.md)** - Guia completo de build
- **[FRONTEND_STATUS.md](FRONTEND_STATUS.md)** - Status de implementação
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Sumário da implementação

---

**🚀 Divirta-se a explorar a plataforma!**

