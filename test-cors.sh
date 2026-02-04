#!/bin/bash

# Script de teste para validar CORS
# Execute após iniciar a aplicação com: ./mvnw quarkus:dev

BASE_URL="${1:-http://localhost:8080}"
ORIGIN="${2:-http://localhost:3000}"

echo "=========================================="
echo "Testes de CORS - API Futebol"
echo "=========================================="
echo "Base URL: $BASE_URL"
echo "Origin: $ORIGIN"
echo ""

# Teste 1: OPTIONS (Preflight) para /api/users
echo "Teste 1: OPTIONS (Preflight) para /api/users"
echo "--------------------------------------------"
RESPONSE=$(curl -s -i -X OPTIONS "$BASE_URL/api/users" \
  -H "Origin: $ORIGIN" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Content-Type")

if echo "$RESPONSE" | grep -q "Access-Control-Allow-Origin"; then
    echo "✓ Header Access-Control-Allow-Origin encontrado"
    echo "$RESPONSE" | grep "Access-Control-Allow-Origin"
else
    echo "✗ ERRO: Header Access-Control-Allow-Origin NÃO encontrado"
fi

if echo "$RESPONSE" | grep -q "Access-Control-Allow-Methods"; then
    echo "✓ Header Access-Control-Allow-Methods encontrado"
    echo "$RESPONSE" | grep "Access-Control-Allow-Methods"
else
    echo "✗ ERRO: Header Access-Control-Allow-Methods NÃO encontrado"
fi

if echo "$RESPONSE" | grep -q "HTTP/.*200"; then
    echo "✓ Status 200 OK"
else
    echo "✗ ERRO: Status não é 200"
    echo "$RESPONSE" | grep "HTTP/"
fi

echo ""
echo "Resposta completa:"
echo "$RESPONSE"
echo ""

# Teste 2: GET com Origin
echo "Teste 2: GET /api/users com Origin"
echo "--------------------------------------------"
RESPONSE2=$(curl -s -i -X GET "$BASE_URL/api/users" \
  -H "Origin: $ORIGIN")

if echo "$RESPONSE2" | grep -q "Access-Control-Allow-Origin"; then
    echo "✓ Header Access-Control-Allow-Origin encontrado"
    echo "$RESPONSE2" | grep "Access-Control-Allow-Origin"
else
    echo "✗ ERRO: Header Access-Control-Allow-Origin NÃO encontrado"
fi

echo ""
echo "Resposta completa:"
echo "$RESPONSE2" | head -20
echo ""

# Teste 3: OPTIONS para endpoint diferente
echo "Teste 3: OPTIONS (Preflight) para /q/health/ready"
echo "--------------------------------------------"
RESPONSE3=$(curl -s -i -X OPTIONS "$BASE_URL/q/health/ready" \
  -H "Origin: $ORIGIN" \
  -H "Access-Control-Request-Method: GET")

if echo "$RESPONSE3" | grep -q "Access-Control-Allow-Origin"; then
    echo "✓ Header Access-Control-Allow-Origin encontrado"
    echo "$RESPONSE3" | grep "Access-Control-Allow-Origin"
else
    echo "✗ ERRO: Header Access-Control-Allow-Origin NÃO encontrado"
fi

echo ""
echo "=========================================="
echo "Testes concluídos"
echo "=========================================="

