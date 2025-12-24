@echo off
REM Script para compilação do Car Pooling em Windows

setlocal enabledelayexpansion

echo ========================================
echo      Car Pooling Build Script (Windows)
echo ========================================
echo.

REM Verificar Maven
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Maven nao encontrado. Instale Maven primeiro.
    pause
    exit /b 1
)

echo [OK] Maven encontrado
echo.

REM Compilar common-lib
echo [1/5] Compilando common-lib...
cd common-lib
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 goto error
cd ..

REM Compilar eureka-server
echo [2/5] Compilando eureka-server...
cd eureka-server
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 goto error
cd ..

REM Compilar api-gateway
echo [3/5] Compilando api-gateway...
cd api-gateway
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 goto error
cd ..

REM Compilar auth-service
echo [4/5] Compilando auth-service...
cd auth-service
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 goto error
cd ..

REM Compilar trip-service
echo [5/5] Compilando trip-service...
cd trip-service
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 goto error
cd ..

echo.
echo ========================================
echo       Compilacao Completa!
echo ========================================
echo.
echo Proximo passo:
echo   docker-compose up -d
echo.
pause
exit /b 0

:error
echo.
echo ERRO na compilacao!
pause
exit /b 1
