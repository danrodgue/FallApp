#!/bin/bash


echo "==================================="
echo "  PRUEBA: Sistema Verificación Email"
echo "==================================="
echo ""

BASE_URL="http://localhost:8080"

TIMESTAMP=$(date +%s)
TEST_EMAIL="prueba_verificacion_${TIMESTAMP}@example.com"
TEST_USER="user_${TIMESTAMP}"

echo "📧 Email de prueba: $TEST_EMAIL"
echo "👤 Usuario de prueba: $TEST_USER"
echo ""

echo "1️⃣  Registrando nuevo usuario..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/registro" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"${TEST_EMAIL}\",
    \"contrasena\": \"TestPassword123!\",
    \"nombreCompleto\": \"Usuario De Prueba\"
  }")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

echo "   Código HTTP: $HTTP_CODE"
echo "   Respuesta: $BODY"
echo ""

echo "2️⃣  Verificando usuario en base de datos..."
DB_CHECK=$(docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c \
  "SELECT email, verificado, token_verificacion IS NOT NULL as tiene_token
   FROM usuarios
   WHERE email = '${TEST_EMAIL}';")

echo "   Resultado BD: $DB_CHECK"
echo ""

echo "3️⃣  Obteniendo token de verificación..."
TOKEN=$(docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c \
  "SELECT token_verificacion FROM usuarios WHERE email = '${TEST_EMAIL}';" | tr -d ' ')

if [ -z "$TOKEN" ]; then
    echo "   ❌ ERROR: No se pudo obtener el token"
    exit 1
fi

echo "   Token obtenido: $TOKEN"
echo ""

echo "4️⃣  Verificando email con token..."
VERIFY_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/api/auth/verificar?token=${TOKEN}")

VERIFY_HTTP_CODE=$(echo "$VERIFY_RESPONSE" | tail -n1)
VERIFY_BODY=$(echo "$VERIFY_RESPONSE" | head -n-1)

echo "   Código HTTP: $VERIFY_HTTP_CODE"
echo "   Respuesta: $VERIFY_BODY"
echo ""

echo "5️⃣  Comprobando estado de verificación..."
FINAL_CHECK=$(docker exec fallapp-postgres psql -U fallapp_user -d fallapp -t -c \
  "SELECT email, verificado, token_verificacion IS NULL as token_eliminado
   FROM usuarios
   WHERE email = '${TEST_EMAIL}';")

echo "   Resultado final BD: $FINAL_CHECK"
echo ""

echo "6️⃣  Probando login con usuario verificado..."
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"${TEST_EMAIL}\",
    \"contrasena\": \"TestPassword123!\"
  }")

LOGIN_HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
LOGIN_BODY=$(echo "$LOGIN_RESPONSE" | head -n-1)

echo "   Código HTTP: $LOGIN_HTTP_CODE"
echo "   Respuesta: $LOGIN_BODY"
echo ""

echo "==================================="
echo "  RESUMEN DE LA PRUEBA"
echo "==================================="

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ]; then
    echo "✅ Registro: OK ($HTTP_CODE)"
else
    echo "❌ Registro: FALLO ($HTTP_CODE)"
fi

if [ "$VERIFY_HTTP_CODE" = "200" ]; then
    echo "✅ Verificación: OK ($VERIFY_HTTP_CODE)"
else
    echo "❌ Verificación: FALLO ($VERIFY_HTTP_CODE)"
fi

if [ "$LOGIN_HTTP_CODE" = "200" ]; then
    echo "✅ Login: OK ($LOGIN_HTTP_CODE)"
else
    echo "❌ Login: FALLO ($LOGIN_HTTP_CODE)"
fi

echo ""
echo "==================================="

read -p "¿Deseas eliminar el usuario de prueba? (s/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Ss]$ ]]; then
    docker exec fallapp-postgres psql -U fallapp_user -d fallapp -c \
      "DELETE FROM usuarios WHERE email = '${TEST_EMAIL}';"
    echo "✅ Usuario de prueba eliminado"
fi
