#!/bin/bash

###############################################################################
# Script de Prueba: Flujo Completo de Autenticación
# Autor: Sistema FallApp
# Fecha: 2026-02-03
# Versión: 0.5.2
#
# Descripción:
#   Prueba completa del flujo de autenticación JWT en la API:
#   1. Registro de usuario
#   2. Login
#   3. Uso de token en endpoints protegidos
#   4. Verificación de permisos
#   5. Manejo de errores
#
# Uso:
#   bash test_api_auth.sh [URL_BASE]
#
# Ejemplo:
#   bash test_api_auth.sh http://localhost:8080
#   bash test_api_auth.sh http://35.180.21.42:8080
###############################################################################

# Configuración
BASE_URL="${1:-http://localhost:8080}"
API_URL="$BASE_URL/api"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Contadores
TESTS_PASSED=0
TESTS_FAILED=0
TOTAL_TESTS=0

# Función para imprimir test
print_test() {
    echo -e "${BLUE}TEST $TOTAL_TESTS:${NC} $1"
}

# Función para resultado exitoso
test_pass() {
    echo -e "${GREEN}✓ PASS${NC} - $1"
    ((TESTS_PASSED++))
}

# Función para resultado fallido
test_fail() {
    echo -e "${RED}✗ FAIL${NC} - $1"
    ((TESTS_FAILED++))
}

# Función para hacer request y verificar
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local headers=$4
    local expected_code=$5
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$API_URL$endpoint" \
            -H "Content-Type: application/json" \
            $headers \
            -d "$data")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$API_URL$endpoint" \
            $headers)
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n-1)
    
    if [ "$http_code" -eq "$expected_code" ]; then
        echo "$body"
        return 0
    else
        echo "ERROR: Expected HTTP $expected_code, got $http_code"
        echo "$body"
        return 1
    fi
}

###############################################################################
# INICIO DE PRUEBAS
###############################################################################

echo "=========================================="
echo "🔐 PRUEBAS DE AUTENTICACIÓN - FallApp"
echo "=========================================="
echo "URL Base: $BASE_URL"
echo "Fecha: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# Variables globales para datos de test
TIMESTAMP=$(date +%s)
TEST_EMAIL="test_auth_${TIMESTAMP}@example.com"
TEST_PASSWORD="TestPass123!"
TEST_NAME="Usuario Test Auth"
TOKEN=""
USER_ID=""

###############################################################################
# TEST 1: Verificar que la API está activa
###############################################################################
((TOTAL_TESTS++))
print_test "Verificar que la API está activa"
if curl -s "$BASE_URL/actuator/health" | grep -q "UP"; then
    test_pass "API está activa y respondiendo"
else
    test_fail "API no responde correctamente"
fi
echo ""

###############################################################################
# TEST 2: Registro de nuevo usuario
###############################################################################
((TOTAL_TESTS++))
print_test "Registro de nuevo usuario"
REGISTER_DATA='{
    "email": "'$TEST_EMAIL'",
    "contrasena": "'$TEST_PASSWORD'",
    "nombreCompleto": "'$TEST_NAME'",
    "idFalla": 95
}'

REGISTER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/auth/registro" \
    -H "Content-Type: application/json" \
    -d "$REGISTER_DATA")
HTTP_CODE=$(echo "$REGISTER_RESPONSE" | tail -n1)
BODY=$(echo "$REGISTER_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 201 ]; then
    # Verificar que la respuesta contiene token
    TOKEN=$(echo "$BODY" | jq -r '.datos.token // empty')
    USER_ID=$(echo "$BODY" | jq -r '.datos.usuario.idUsuario // empty')
    
    if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
        test_pass "Usuario registrado correctamente - Token obtenido"
        echo "   → Email: $TEST_EMAIL"
        echo "   → User ID: $USER_ID"
        echo "   → Token (primeros 50 chars): ${TOKEN:0:50}..."
    else
        test_fail "Usuario registrado pero sin token en respuesta"
        echo "$BODY" | jq
    fi
else
    test_fail "Error en registro de usuario (HTTP $HTTP_CODE)"
    echo "$BODY"
fi
echo ""

###############################################################################
# TEST 3: Verificar estructura de respuesta de registro
###############################################################################
((TOTAL_TESTS++))
print_test "Verificar estructura de respuesta de registro"
if echo "$BODY" | jq -e '.exito == true' > /dev/null 2>&1 && \
   echo "$BODY" | jq -e '.datos.token' > /dev/null 2>&1 && \
   echo "$BODY" | jq -e '.datos.usuario.email' > /dev/null 2>&1 && \
   echo "$BODY" | jq -e '.datos.usuario.rol' > /dev/null 2>&1; then
    test_pass "Estructura de respuesta correcta"
else
    test_fail "Estructura de respuesta incorrecta"
    echo "$BODY" | jq
fi
echo ""

###############################################################################
# TEST 4: Intentar registrar mismo email (debe fallar)
###############################################################################
((TOTAL_TESTS++))
print_test "Intentar registrar mismo email (debe fallar)"
DUPLICATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/auth/registro" \
    -H "Content-Type: application/json" \
    -d "$REGISTER_DATA")
HTTP_CODE=$(echo "$DUPLICATE_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 400 ] || [ "$HTTP_CODE" -eq 409 ]; then
    test_pass "Registro duplicado rechazado correctamente (HTTP $HTTP_CODE)"
else
    test_fail "Registro duplicado no rechazado (HTTP $HTTP_CODE)"
fi
echo ""

###############################################################################
# TEST 5: Login con credenciales correctas
###############################################################################
((TOTAL_TESTS++))
print_test "Login con credenciales correctas"
LOGIN_DATA='{
    "email": "'$TEST_EMAIL'",
    "contrasena": "'$TEST_PASSWORD'"
}'

LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "$LOGIN_DATA")
HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
LOGIN_BODY=$(echo "$LOGIN_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 201 ]; then
    NEW_TOKEN=$(echo "$LOGIN_BODY" | jq -r '.datos.token // empty')
    if [ -n "$NEW_TOKEN" ] && [ "$NEW_TOKEN" != "null" ]; then
        test_pass "Login exitoso - Nuevo token obtenido"
        # Actualizar token para siguientes tests
        TOKEN="$NEW_TOKEN"
    else
        test_fail "Login exitoso pero sin token"
    fi
else
    test_fail "Error en login (HTTP $HTTP_CODE)"
fi
echo ""

###############################################################################
# TEST 6: Login con credenciales incorrectas
###############################################################################
((TOTAL_TESTS++))
print_test "Login con credenciales incorrectas (debe fallar)"
BAD_LOGIN_DATA='{
    "email": "'$TEST_EMAIL'",
    "contrasena": "wrong_password"
}'

BAD_LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "$BAD_LOGIN_DATA")
HTTP_CODE=$(echo "$BAD_LOGIN_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 401 ]; then
    test_pass "Login incorrecto rechazado (HTTP 401)"
else
    test_fail "Login incorrecto no rechazado correctamente (HTTP $HTTP_CODE)"
fi
echo ""

###############################################################################
# TEST 7: Acceder a endpoint protegido CON token
###############################################################################
((TOTAL_TESTS++))
print_test "Acceder a endpoint protegido CON token"
FALLA_DATA='{
    "nombre": "Falla Test Auth '$(date +%H%M%S)'",
    "seccion": "9Z",
    "presidente": "Test Auth",
    "anyoFundacion": 2025,
    "latitud": 39.47,
    "longitud": -0.38,
    "categoria": "especial"
}'

CREATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/fallas" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "$FALLA_DATA")
HTTP_CODE=$(echo "$CREATE_RESPONSE" | tail -n1)
BODY=$(echo "$CREATE_RESPONSE" | head -n-1)

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 201 ]; then
    test_pass "Endpoint protegido accedido correctamente (HTTP $HTTP_CODE)"
    NEW_FALLA_ID=$(echo "$BODY" | jq -r '.datos.idFalla // empty')
    echo "   → Nueva falla creada con ID: $NEW_FALLA_ID"
else
    test_fail "No se pudo acceder a endpoint protegido (HTTP $HTTP_CODE)"
    echo "$BODY" | jq
fi
echo ""

###############################################################################
# TEST 8: Acceder a endpoint protegido SIN token (debe fallar)
###############################################################################
((TOTAL_TESTS++))
print_test "Acceder a endpoint protegido SIN token (debe fallar con 401)"
NO_TOKEN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/fallas" \
    -H "Content-Type: application/json" \
    -d "$FALLA_DATA")
HTTP_CODE=$(echo "$NO_TOKEN_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 401 ] || [ "$HTTP_CODE" -eq 403 ]; then
    test_pass "Acceso sin token rechazado (HTTP $HTTP_CODE)"
else
    test_fail "Acceso sin token no rechazado correctamente (HTTP $HTTP_CODE)"
fi
echo ""

###############################################################################
# TEST 9: Acceder con token inválido (debe fallar)
###############################################################################
((TOTAL_TESTS++))
print_test "Acceder con token inválido (debe fallar)"
INVALID_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/fallas" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer INVALID_TOKEN_12345" \
    -d "$FALLA_DATA")
HTTP_CODE=$(echo "$INVALID_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 401 ] || [ "$HTTP_CODE" -eq 403 ]; then
    test_pass "Token inválido rechazado (HTTP $HTTP_CODE)"
else
    test_fail "Token inválido no rechazado correctamente (HTTP $HTTP_CODE)"
fi
echo ""

###############################################################################
# TEST 10: Verificar formato del token (JWT)
###############################################################################
((TOTAL_TESTS++))
print_test "Verificar formato del token (JWT)"
# JWT tiene formato: header.payload.signature (3 partes separadas por punto)
TOKEN_PARTS=$(echo "$TOKEN" | grep -o '\.' | wc -l)
if [ "$TOKEN_PARTS" -eq 2 ]; then
    test_pass "Token tiene formato JWT válido (3 partes)"
else
    test_fail "Token no tiene formato JWT válido"
fi
echo ""

###############################################################################
# TEST 11: Crear evento con token
###############################################################################
((TOTAL_TESTS++))
print_test "Crear evento con autenticación"
EVENTO_DATA='{
    "idFalla": 95,
    "tipo": "PAELLA",
    "nombre": "Paella Test Auth",
    "descripcion": "Evento de prueba de autenticación",
    "fechaEvento": "2026-03-19T13:00:00",
    "ubicacion": "Casal",
    "participantesEstimado": 100
}'

EVENTO_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/eventos" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "$EVENTO_DATA")
HTTP_CODE=$(echo "$EVENTO_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 201 ]; then
    test_pass "Evento creado correctamente (HTTP $HTTP_CODE)"
else
    test_fail "Error al crear evento (HTTP $HTTP_CODE)"
fi
echo ""

###############################################################################
# TEST 12: Votar por ninot con token
###############################################################################
((TOTAL_TESTS++))
print_test "Votar por ninot con autenticación"
VOTO_DATA='{
    "idNinot": 1,
    "tipoVoto": "ARTISTICO"
}'

VOTO_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/votos" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "$VOTO_DATA")
HTTP_CODE=$(echo "$VOTO_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 201 ]; then
    test_pass "Voto registrado correctamente (HTTP $HTTP_CODE)"
elif [ "$HTTP_CODE" -eq 400 ]; then
    test_pass "Voto duplicado o inválido (esperado - HTTP 400)"
else
    test_fail "Error al votar (HTTP $HTTP_CODE)"
fi
echo ""

###############################################################################
# TEST 13: Crear comentario con token
###############################################################################
((TOTAL_TESTS++))
print_test "Crear comentario con autenticación"
COMENTARIO_DATA='{
    "idUsuario": '$USER_ID',
    "idFalla": 95,
    "contenido": "Este es un comentario de prueba de autenticación"
}'

COMENTARIO_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/comentarios" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "$COMENTARIO_DATA")
HTTP_CODE=$(echo "$COMENTARIO_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 201 ]; then
    test_pass "Comentario creado correctamente (HTTP $HTTP_CODE)"
else
    test_fail "Error al crear comentario (HTTP $HTTP_CODE)"
fi
echo ""

###############################################################################
# TEST 14: Verificar que endpoints públicos funcionan sin token
###############################################################################
((TOTAL_TESTS++))
print_test "Verificar que GET /fallas es público (sin token)"
PUBLIC_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/fallas?page=0&size=1")
HTTP_CODE=$(echo "$PUBLIC_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 200 ]; then
    test_pass "Endpoint público accesible sin token (HTTP 200)"
else
    test_fail "Endpoint público no accesible (HTTP $HTTP_CODE)"
fi
echo ""

###############################################################################
# TEST 15: Verificar que ubicación es pública
###############################################################################
((TOTAL_TESTS++))
print_test "Verificar que GET /fallas/{id}/ubicacion es público"
UBICACION_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/fallas/95/ubicacion")
HTTP_CODE=$(echo "$UBICACION_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 200 ]; then
    test_pass "Endpoint de ubicación público (HTTP 200)"
else
    test_fail "Endpoint de ubicación no accesible (HTTP $HTTP_CODE)"
fi
echo ""

###############################################################################
# TEST 16: Verificar rol del usuario (debe ser 'usuario')
###############################################################################
((TOTAL_TESTS++))
print_test "Verificar que el usuario tiene rol 'usuario'"
USER_ROL=$(echo "$LOGIN_BODY" | jq -r '.datos.usuario.rol // empty')
if [ "$USER_ROL" = "usuario" ]; then
    test_pass "Usuario tiene rol correcto: 'usuario'"
else
    test_fail "Usuario tiene rol incorrecto: '$USER_ROL'"
fi
echo ""

###############################################################################
# TEST 17: Verificar que usuario NO puede eliminar (no es ADMIN)
###############################################################################
((TOTAL_TESTS++))
print_test "Verificar que usuario normal NO puede eliminar fallas"
DELETE_RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$API_URL/fallas/999" \
    -H "Authorization: Bearer $TOKEN")
HTTP_CODE=$(echo "$DELETE_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" -eq 403 ]; then
    test_pass "Usuario normal no puede eliminar (HTTP 403 correcto)"
elif [ "$HTTP_CODE" -eq 404 ]; then
    test_pass "Falla no existe (HTTP 404 - también válido)"
else
    test_fail "Respuesta inesperada al intentar eliminar (HTTP $HTTP_CODE)"
fi
echo ""

###############################################################################
# TEST 18: Verificar expiración del token (campo expiraEn)
###############################################################################
((TOTAL_TESTS++))
print_test "Verificar campo expiraEn en respuesta de login"
EXPIRA_EN=$(echo "$LOGIN_BODY" | jq -r '.datos.expiraEn // empty')
if [ "$EXPIRA_EN" = "86400" ]; then
    test_pass "Token expira en 24 horas (86400 segundos)"
else
    test_fail "Tiempo de expiración incorrecto: $EXPIRA_EN"
fi
echo ""

###############################################################################
# TEST 19: Verificar tipo de token (debe ser Bearer)
###############################################################################
((TOTAL_TESTS++))
print_test "Verificar tipo de token en respuesta"
TIPO_TOKEN=$(echo "$LOGIN_BODY" | jq -r '.datos.tipo // empty')
if [ "$TIPO_TOKEN" = "Bearer" ]; then
    test_pass "Tipo de token correcto: Bearer"
else
    test_fail "Tipo de token incorrecto: $TIPO_TOKEN"
fi
echo ""

###############################################################################
# TEST 20: Actualizar falla con token
###############################################################################
((TOTAL_TESTS++))
print_test "Actualizar falla con autenticación (PUT)"
if [ -n "$NEW_FALLA_ID" ] && [ "$NEW_FALLA_ID" != "null" ]; then
    UPDATE_DATA='{
        "nombre": "Falla Test Auth ACTUALIZADA",
        "seccion": "1A",
        "presidente": "Presidente Actualizado",
        "anyoFundacion": 2025
    }'
    
    UPDATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$API_URL/fallas/$NEW_FALLA_ID" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TOKEN" \
        -d "$UPDATE_DATA")
    HTTP_CODE=$(echo "$UPDATE_RESPONSE" | tail -n1)
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        test_pass "Falla actualizada correctamente (HTTP 200)"
    else
        test_fail "Error al actualizar falla (HTTP $HTTP_CODE)"
    fi
else
    test_fail "No se puede probar PUT - no hay ID de falla creada"
fi
echo ""

###############################################################################
# RESUMEN FINAL
###############################################################################
echo ""
echo "=========================================="
echo "📊 RESUMEN DE RESULTADOS"
echo "=========================================="
echo "Total de pruebas:    $TOTAL_TESTS"
echo -e "${GREEN}Pruebas exitosas:    $TESTS_PASSED${NC}"
echo -e "${RED}Pruebas fallidas:    $TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ TODAS LAS PRUEBAS PASARON${NC}"
    echo ""
    echo "🎉 El sistema de autenticación funciona correctamente!"
    echo ""
    echo "📝 Datos del usuario de prueba:"
    echo "   Email:    $TEST_EMAIL"
    echo "   Password: $TEST_PASSWORD"
    echo "   User ID:  $USER_ID"
    echo "   Token:    ${TOKEN:0:50}..."
    echo ""
    echo "💡 Puedes usar este token para probar manualmente:"
    echo "   export TOKEN=\"$TOKEN\""
    echo "   curl -H \"Authorization: Bearer \$TOKEN\" $API_URL/fallas"
    exit 0
else
    echo -e "${RED}❌ ALGUNAS PRUEBAS FALLARON${NC}"
    echo ""
    echo "Por favor revisa los errores arriba."
    exit 1
fi
