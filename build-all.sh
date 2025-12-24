#!/bin/bash

# Script completo de build tudo com um comando via Maven parent

set -e

PROJECT_ROOT=$(pwd)

echo "════════════════════════════════════════════"
echo "  Car Pooling - Build Completo com Maven"
echo "════════════════════════════════════════════"
echo ""
echo "Diretório: $PROJECT_ROOT"
echo ""

# 1. Limpar tudo
echo "[1/3] Limpando builds anteriores..."
mvn clean -q

# 2. Construir e instalar todos os módulos
echo "[2/3] Compilando e embalando todos os módulos..."
mvn install -DskipTests=true

# 3. Verificar arquivos JAR
echo "[3/3] Verificando artefatos criados..."
echo ""
echo "✓ Arquivos JAR gerados:"
find . -name "*.jar" -type f | grep target | head -10

echo ""
echo "════════════════════════════════════════════"
echo "  Build Completo com Sucesso! ✓"
echo "════════════════════════════════════════════"
echo ""
echo "Próximo passo:"
echo "  docker-compose build"
echo "  docker-compose up -d"
echo ""
echo "Acessar:"
echo "  http://localhost:8761/ (Eureka)"
echo ""
