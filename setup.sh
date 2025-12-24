#!/bin/bash

# Script para setup completo do projeto Car Pooling
# Uso: bash setup.sh

set -e

echo "=========================================="
echo "   Car Pooling Setup Script"
echo "=========================================="
echo ""

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para imprimir status
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_step() {
    echo -e "${YELLOW}→${NC} $1"
}

# Verificar pré-requisitos
print_step "Verificando pré-requisitos..."

if ! command -v java &> /dev/null; then
    echo "Java não encontrado. Por favor, instale Java 17+"
    exit 1
fi
print_status "Java instalado"

if ! command -v mvn &> /dev/null; then
    echo "Maven não encontrado. Por favor, instale Maven"
    exit 1
fi
print_status "Maven instalado"

if ! command -v docker &> /dev/null; then
    echo "Docker não encontrado. Por favor, instale Docker"
    exit 1
fi
print_status "Docker instalado"

if ! command -v docker-compose &> /dev/null; then
    echo "Docker Compose não encontrado. Por favor, instale Docker Compose"
    exit 1
fi
print_status "Docker Compose instalado"

echo ""
print_step "Compilando módulos Maven..."

# Compilar
mvn clean install -DskipTests=true

print_status "Compilação concluída"

echo ""
print_step "Construindo imagens Docker..."

# Build images
docker-compose build

print_status "Imagens Docker construídas"

echo ""
print_step "Iniciando serviços..."

# Start services
docker-compose up -d

print_status "Serviços iniciados"

echo ""
echo "=========================================="
echo "   Setup Completo!"
echo "=========================================="
echo ""
echo "Serviços disponíveis em:"
echo "  • Eureka Dashboard: http://localhost:8761/"
echo "  • API Gateway: http://localhost:8080/"
echo "  • Auth Service: http://localhost:8081/swagger-ui.html"
echo "  • Trip Service: http://localhost:8082/swagger-ui.html"
echo ""
echo "Para ver logs: docker-compose logs -f"
echo "Para parar: docker-compose down"
echo ""
