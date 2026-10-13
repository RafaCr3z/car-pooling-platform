@echo off
REM ==============================================================================
REM Car Pooling Platform - Script de Inicialização Completa (Windows)
REM Aplicações Distribuídas 2025/2026 - EST-IPCB
REM ==============================================================================

cls
echo.
echo ==============================================================================
echo           Car Pooling PT - Plataforma Distribuida de Boleias
echo ==============================================================================
echo.

REM Verificar Docker
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Docker nao esta instalado ou nao esta a correr. Inicie o Docker Desktop primeiro.
    pause
    exit /b 1
)
echo [OK] Docker verificado

echo.
echo [1/2] A compilar e empacotar microsservicos com Maven...
echo.
call mvn clean package -DskipTests
if errorlevel 1 (
    echo [AVISO] mvn direto nao encontrado no PATH, a tentar maven local...
    if exist "D:\maven\apache-maven-3.9.9\bin\mvn.cmd" (
        call "D:\maven\apache-maven-3.9.9\bin\mvn.cmd" clean package -DskipTests
    )
)

echo.
echo [2/2] A iniciar contentores com Docker Compose...
echo.
docker compose up -d --build

echo.
echo ==============================================================================
echo               Sistema Car Pooling Iniciado com Sucesso!
echo ==============================================================================
echo.
echo   Frontend Web:            http://localhost:3000
echo   API Gateway:             http://localhost:8080
echo   Eureka Service Registry: http://localhost:8761
echo   RabbitMQ Management:     http://localhost:15672 (guest/guest)
echo.
echo   Microsservicos Ativos:
echo   [x] auth-service         :8081  (JWT, Utilizadores, Avaliacoes 1-5 estrelas)
echo   [x] trip-service         :8082  (Viagens, Reservas, Custos Combustivel+Portagens)
echo   [x] vehicle-service      :8083  (Viaturas, Consumos L/100km, Emissoes CO2)
echo   [x] payment-service      :8084  (MBWay, Pagamentos e Transacoes)
echo   [x] location-service     :8085  (Cidades PT, Geocoding, Calculo Rotas)
echo   [x] notification-service :8086  (Eventos RabbitMQ, Alertas em Tempo Real)
echo   [x] admin-service        :8087  (KPIs Globais, Metricas CO2, Moderacao)
echo.
echo   Contas de Teste Pre-Carregadas:
echo   - Condutor:   joao.silva@carpooling.pt / password123
echo   - Passageira: maria.santos@carpooling.pt / password123
echo   - Admin:      admin@carpooling.pt / admin123
echo.
echo ==============================================================================
pause
