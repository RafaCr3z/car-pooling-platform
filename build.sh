#!/bin/bash

# Script para compilar todos os microserviços

echo "================================"
echo "Compilando Car Pooling Platform"
echo "================================"

# Compilar common-lib primeiro
echo -e "\n[1/7] Compilando common-lib..."
mvn -f common-lib/pom.xml clean package -DskipTests || exit 1

# Compilar eureka-server
echo -e "\n[2/7] Compilando eureka-server..."
mvn -f eureka-server/pom.xml clean package -DskipTests || exit 1

# Compilar api-gateway
echo -e "\n[3/7] Compilando api-gateway..."
mvn -f api-gateway/pom.xml clean package -DskipTests || exit 1

# Compilar auth-service
echo -e "\n[4/7] Compilando auth-service..."
mvn -f auth-service/pom.xml clean package -DskipTests || exit 1

# Compilar trip-service
echo -e "\n[5/7] Compilando trip-service..."
mvn -f trip-service/pom.xml clean package -DskipTests || exit 1

echo -e "\n================================"
echo "Compilação Completa!"
echo "================================"
echo -e "\nPróximos passos:"
echo "1. Execute: docker-compose build"
echo "2. Execute: docker-compose up -d"
echo "3. Aceda a: http://localhost:8761/ (Eureka)"
