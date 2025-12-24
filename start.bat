@echo off

REM 🚀 Car Pooling Platform - Script de Inicialização Automática (Windows)

cls
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║         🚀 Car Pooling ^& Sharing Platform - v1.0.0            ║
echo ║                                                                ║
echo ║              Iniciando Sistema Completo...                    ║
echo ║                                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Verificar se Maven está instalado
echo [INFO] Verificando dependências...
mvn --version >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Maven não está instalado!
    pause
    exit /b 1
)
echo [OK] Maven encontrado
echo.

REM Verificar se Docker está instalado
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Docker não está instalado!
    pause
    exit /b 1
)
echo [OK] Docker encontrado
echo.

REM Verificar se Docker Compose está instalado
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Docker Compose não está instalado!
    pause
    exit /b 1
)
echo [OK] Docker Compose encontrado
echo.

echo [INFO] Iniciando build Maven...
echo.

REM Build Maven
mvn clean package -DskipTests -q
if errorlevel 1 (
    echo [ERRO] Erro no build Maven!
    pause
    exit /b 1
)
echo [OK] Maven build completado com sucesso!
echo.

echo [INFO] Iniciando serviços com Docker Compose...
echo.

REM Docker Compose
docker-compose up --build
if errorlevel 1 (
    echo [ERRO] Erro ao iniciar serviços!
    pause
    exit /b 1
)

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║              🎉 Sistema Iniciado com Sucesso!                 ║
echo ║                                                                ║
echo ║  📊 Eureka Dashboard:    http://localhost:8761               ║
echo ║  🌐 Frontend:            http://localhost:3000               ║
echo ║  🔌 API Gateway:         http://localhost:8080               ║
echo ║                                                                ║
echo ║  Serviços Disponíveis:                                        ║
echo ║  ✓ Auth Service          (8081)                              ║
echo ║  ✓ Trip Service          (8082)                              ║
echo ║  ✓ Vehicle Service       (8083)                              ║
echo ║  ✓ Payment Service       (8084)                              ║
echo ║  ✓ Location Service      (8085)                              ║
echo ║                                                                ║
echo ║  Pressione Ctrl+C para parar os serviços                     ║
echo ║                                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
pause
