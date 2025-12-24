# Frontend Service - Car Pooling

## 📱 Visão Geral

O Frontend Service é a interface web responsiva da plataforma Car Pooling, desenvolvida com **Spring Boot MVC** e **Thymeleaf**, servindo em **porta 3000**.

## 🏗️ Arquitetura

```
Frontend Service (Porto 3000)
    ↓
Spring MVC Controllers
    ↓
Thymeleaf Templates + Bootstrap 5
    ↓
JavaScript/AJAX
    ↓
API Gateway (Porto 8080)
    ↓
Microserviços (Auth, Trip, etc.)
```

## 📁 Estrutura de Ficheiros

```
frontend-service/
├── src/main/
│   ├── java/com/carpooling/frontend/
│   │   ├── FrontendServiceApplication.java
│   │   ├── controller/
│   │   │   ├── AuthController.java         (Login/Register)
│   │   │   ├── HomeController.java         (Home/Dashboard)
│   │   │   └── TripController.java         (Viagens)
│   │   ├── service/
│   │   │   ├── AuthService.java            (Integração com Auth API)
│   │   │   └── TripService.java            (Integração com Trip API)
│   │   ├── dto/
│   │   │   ├── TripDTO.java
│   │   │   └── ReservationDTO.java
│   │   └── config/
│   │       └── WebClientConfig.java        (WebClient para chamadas HTTP)
│   └── resources/
│       ├── application.yml
│       └── templates/
│           ├── layout.html                 (Template base)
│           ├── index.html                  (Home)
│           ├── auth/
│           │   ├── login.html
│           │   └── register.html
│           ├── dashboard/
│           │   ├── dashboard.html
│           │   └── profile.html
│           └── trips/
│               ├── search-trips.html
│               ├── create-trip.html
│               ├── my-trips.html
│               └── my-reservations.html
├── pom.xml
├── Dockerfile
└── .gitignore
```

## 🎯 Controllers

### AuthController
- `GET /login` - Página de login
- `POST /login` - Processa login (valida credenciais, guarda token em sessão)
- `GET /register` - Página de registo
- `POST /register` - Processa registo
- `GET /logout` - Encerra sessão

### HomeController
- `GET /` - Página inicial (home)
- `GET /dashboard` - Dashboard principal (requer autenticação)
- `GET /profile` - Página de perfil

### TripController
- `GET /trips/create` - Página para criar viagem
- `POST /trips/create` - Processa criação de viagem
- `GET /trips/search` - Página de pesquisa com resultados
- `POST /trips/reserve` - Processa reserva de viagem
- `GET /trips/my-trips` - Lista minhas viagens criadas
- `GET /trips/my-reservations` - Lista minhas reservas

## 🔐 Segurança

### Session Management
- Tokens JWT guardados em `HttpSession`
- Email do utilizador guardado na sessão
- Validação de sessão em endpoints protegidos

### Fluxo de Autenticação
```
1. Utilizador faz login no formulário
2. AuthController chama AuthService.login()
3. AuthService faz POST para http://localhost:8080/api/auth/login
4. API Gateway roteia para Auth Service
5. Token JWT é retornado
6. Token é guardado em session.setAttribute("token", token)
7. Utilizador é redirecionado para /dashboard
8. Nos headers, token é passado como: Authorization: Bearer {token}
```

### Proteção de Endpoints
- Endpoints sem autenticação: `/`, `/login`, `/register`
- Endpoints protegidos: `/dashboard`, `/trips/**`, `/profile`
- Se aceder sem token → Redireciona para `/login`

## 🎨 Templates (Thymeleaf + Bootstrap 5)

### Características
- ✅ Responsivo (Mobile-first com Bootstrap 5)
- ✅ Gradientes modernos nas cores primárias
- ✅ Cards com hover effects
- ✅ Ícones Font Awesome
- ✅ Formulários estilizados
- ✅ Alertas animados
- ✅ Tabelas responsivas
- ✅ Validação JavaScript client-side

### Temas Visuais

**Cores Primárias:**
- Azul primário: `#007bff`
- Azul escuro: `#0056b3`
- Verde (sucesso): `#28a745`
- Vermelho (perigo): `#dc3545`
- Amarelo (aviso): `#ffc107`

**Componentes Reutilizáveis:**
- Cards com borders e shadows
- Botões com gradientes
- Formulários com validação
- Navbars collapse-responsive
- Footers sticky
- Modais para confirmações

## 🔗 Integração com APIs

### AuthService - Comunicação com API

```java
// Login
POST http://localhost:8080/api/auth/login
Body: {"email": "...", "password": "..."}
Response: {"token": "...", "email": "...", "fullName": "..."}

// Register
POST http://localhost:8080/api/auth/register
Body: {"email": "...", "password": "...", "fullName": "...", ...}
Response: 201 Created
```

### TripService - Comunicação com API

```java
// Pesquisar viagens
GET http://localhost:8080/api/trips/search?origin=...&destination=...
Header: Authorization: Bearer {token}

// Criar viagem
POST http://localhost:8080/api/trips
Body: {"departureLocation": "...", "destinationLocation": "...", ...}
Header: Authorization: Bearer {token}

// Fazer reserva
POST http://localhost:8080/api/trips/reservations
Body: {"tripId": ..., "numberOfSeats": ...}
Header: Authorization: Bearer {token}
```

## 📦 Dependências Principais

```xml
<!-- Spring Boot Web & Thymeleaf -->
spring-boot-starter-web
spring-boot-starter-thymeleaf
thymeleaf-extras-springsecurity6

<!-- Spring Security -->
spring-boot-starter-security

<!-- Spring Cloud (Discovery) -->
spring-cloud-starter-netflix-eureka-client

<!-- HTTP Client -->
spring-boot-starter-webflux

<!-- JSON -->
gson

<!-- Dev Tools -->
spring-boot-devtools
```

## 🚀 Como Executar

### Local (sem Docker)

```bash
# 1. Build
mvn clean package

# 2. Run
java -jar target/frontend-service-1.0.0.jar

# 3. Aceder
http://localhost:3000
```

### Docker

```bash
# 1. Build image
docker build -t frontend-service:latest .

# 2. Run container
docker run -p 3000:3000 --network carpooling-network frontend-service:latest
```

### Docker Compose

```bash
docker-compose up -d frontend-service
```

## 📋 Páginas Implementadas

### 1. **Home (index.html)**
- Hero section com call-to-action
- 6 feature cards (economize, sustentável, pessoas, tempo, seguro, fácil)
- Seção "Como Funciona"
- Seção "Call to Action"
- Responsive layout

### 2. **Login (auth/login.html)**
- Formulário com email/password
- Link para registar
- Validação client-side
- Alertas de erro/sucesso

### 3. **Register (auth/register.html)**
- Formulário com todos os campos
- Validação de passwords
- Seleção de tipo (PASSENGER/DRIVER)
- Aceitar termos e condições

### 4. **Dashboard (dashboard/dashboard.html)**
- Secção de boas-vindas
- 3 cards de ações rápidas (Pesquisar, Criar, Minhas Reservas)
- 4 cards de estatísticas (viagens, participações, economizado, km)
- Cards com recursos disponíveis

### 5. **Perfil (dashboard/profile.html)**
- Avatar do utilizador
- Rating (estrelas)
- Estatísticas (viagens, avaliações, km)
- Formulário de edição de perfil
- Secção de segurança (alterar password)
- Preferências (idioma, notificações)
- Zona de perigo (eliminar conta)

### 6. **Pesquisar Viagens (trips/search-trips.html)**
- Formulário com origem/destino/data
- Lista de viagens como cards
- Info: rota, hora, lugares, preço
- Botão de reserva
- Mensagem quando nenhuma viagem

### 7. **Criar Viagem (trips/create-trip.html)**
- Formulário com todos os campos
- Validação de inputs
- Exemplo de cálculo automático
- Help text para cada campo

### 8. **Minhas Viagens (trips/my-trips.html)**
- Lista das viagens criadas
- Status badge (OPEN, IN_PROGRESS, etc)
- Info: origem, destino, hora, lugares, preço
- Botões: Detalhes, Editar, Cancelar

### 9. **Minhas Reservas (trips/my-reservations.html)**
- Lista de reservas
- Status badge (PENDING, CONFIRMED, COMPLETED)
- Info: viagem ID, lugares, preço total
- Botão cancelar (apenas se PENDING)

## 🎓 Padrões de Código

### Controller Pattern

```java
@Controller
@RequestMapping("/endpoint")
@AllArgsConstructor
public class XyzController {
    private final XyzService xyzService;
    
    @GetMapping("/page")
    public String page(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/login";
        
        // lógica
        model.addAttribute("key", value);
        return "template/page";
    }
}
```

### Service Pattern

```java
@Service
@AllArgsConstructor
public class XyzService {
    private final WebClient webClient;
    
    public void doAction(String token) {
        webClient.post()
            .uri("http://localhost:8080/api/...")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }
}
```

### Template Pattern

```html
<form method="POST" action="/endpoint">
    <div class="mb-3">
        <label for="field" class="form-label">Label</label>
        <input type="type" class="form-control" id="field" name="field" required>
    </div>
    <button type="submit" class="btn btn-primary">Submit</button>
</form>
```

## 🧪 Testes Manuais

### Fluxo Completo

```
1. Abrir http://localhost:3000
2. Clicar "Registar"
3. Preencher formulário (email, password, nome, telemóvel, tipo)
4. Clicar "Criar Conta"
5. Clicar "Fazer Login"
6. Fazer login com as credenciais
7. Ser redirecionado para /dashboard
8. Clicar em "Pesquisar Viagens"
9. Preencher critérios (origem, destino, data)
10. Ver viagens nos resultados
11. Clicar "Reservar" em uma viagem
12. Ver confirmação de reserva
13. Ir para "Minhas Reservas" e ver a reserva
```

## 📊 Métricas de Performance

- Tempo de carregamento: < 1s
- Tamanho das páginas: 50-200KB
- Imagens otimizadas
- CSS/JS minificados (via Bootstrap CDN)
- Session timeout: 30 minutos

## 🔧 Troubleshooting

### "Connection refused" ao chamar API
- Verificar se API Gateway está rodando na porta 8080
- Verificar se Auth/Trip Services estão registados em Eureka

### "Token inválido"
- Verificar se token está sendo guardado corretamente na sessão
- Verificar se header Authorization está sendo passado

### Formulário não submete
- Verificar validação JavaScript
- Verificar console do browser para erros

## 📝 Notas de Desenvolvimento

- Usar `@AllArgsConstructor` para injetar dependências
- Sempre guardar token em HttpSession
- Sempre passar token como "Bearer {token}" nos headers
- Usar model.addAttribute() para passar dados aos templates
- Usar redirectAttributes para mensagens flash
- Validar autenticação no início de cada controller method

## 🎯 Próximas Melhorias

- [ ] Adicionar integração com Google Maps
- [ ] Implementar chat em tempo real
- [ ] Adicionar sistema de notificações
- [ ] Implementar dark mode
- [ ] Adicionar PWA (Progressive Web App)
- [ ] Otimizar imagens e assets
- [ ] Implementar analytics

---

**Desenvolvido com ❤️ para Car Pooling**
