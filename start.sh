#!/bin/bash

# 🚀 Car Pooling Platform - Script de Inicialização Automática

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                                                                ║"
echo "║         🚀 Car Pooling & Sharing Platform - v1.0.0            ║"
echo "║                                                                ║"
echo "║              Iniciando Sistema Completo...                    ║"
echo "║                                                                ║"
echo "╚════════════════════════════════════════════════════════════════╝"

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Função para imprimir com cor
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

# Verificar se Maven está instalado
print_status "Verificando dependências..."
if ! command -v mvn &> /dev/null; then
    print_error "Maven não está instalado!"
    exit 1
fi
print_success "Maven encontrado"

# Verificar se Docker está instalado
if ! command -v docker &> /dev/null; then
    print_error "Docker não está instalado!"
    exit 1
fi
print_success "Docker encontrado"

# Verificar se Docker Compose está instalado
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose não está instalado!"
    exit 1
fi
print_success "Docker Compose encontrado"

echo ""
print_status "Iniciando build Maven..."
echo ""

# Build Maven
if mvn clean package -DskipTests -q; then
    print_success "Maven build completado com sucesso!"
else
    print_error "Erro no build Maven!"
    exit 1
fi

echo ""
print_status "Iniciando serviços com Docker Compose..."
echo ""

# Docker Compose
if docker-compose up --build; then
    print_success "Todos os serviços iniciados com sucesso!"
else
    print_error "Erro ao iniciar serviços!"
    exit 1
fi

echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                                                                ║"
echo "║              🎉 Sistema Iniciado com Sucesso!                 ║"
echo "║                                                                ║"
echo "║  📊 Eureka Dashboard:    http://localhost:8761               ║"
echo "║  🌐 Frontend:            http://localhost:3000               ║"
echo "║  🔌 API Gateway:         http://localhost:8080               ║"
echo "║                                                                ║"
echo "║  Serviços Disponíveis:                                        ║"
echo "║  ✓ Auth Service          (8081)                              ║"
echo "║  ✓ Trip Service          (8082)                              ║"
echo "║  ✓ Vehicle Service       (8083)                              ║"
echo "║  ✓ Payment Service       (8084)                              ║"
echo "║  ✓ Location Service      (8085)                              ║"
echo "║                                                                ║"
echo "║  Pressione Ctrl+C para parar os serviços                     ║"
echo "║                                                                ║"
echo "╚════════════════════════════════════════════════════════════════╝"
