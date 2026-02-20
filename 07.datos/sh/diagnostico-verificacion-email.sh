#!/bin/bash


set -u

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

EMAIL_DESTINO="${1:-pedropika720@gmail.com}"
BASE_URL="${2:-http://localhost:8080}"
APP_PROPS="/srv/FallApp/01.backend/src/main/resources/application.properties"
DB_CONTAINER="fallapp-postgres"
DB_USER="fallapp_user"
DB_NAME="fallapp"

TEST_TS=$(date +%s)
TEST_EMAIL="diag_verif_${TEST_TS}@example.com"
TEST_PASS="Test123456!"

STATUS_BACKEND=0
STATUS_SMTP=0
STATUS_REGISTRO=0
STATUS_TOKEN=0
STATUS_VERIFICACION=0
STATUS_LOGIN=0

START_TIME=$(date '+%Y-%m-%d %H:%M:%S')

echo -e "${BLUE}====================================================${NC}"
echo -e "${BLUE}  🩺 DIAGNÓSTICO EMAIL + VERIFICACIÓN (END-TO-END)${NC}"
echo -e "${BLUE}====================================================${NC}"
echo ""
echo -e "${BLUE}Base URL:${NC} ${BASE_URL}"
echo -e "${BLUE}Email destino test SMTP:${NC} ${EMAIL_DESTINO}"
echo -e "${BLUE}Usuario de prueba registro:${NC} ${TEST_EMAIL}"
echo ""


get_prop() {
  local key="$1"
  grep -E "^${key}=" "$APP_PROPS" 2>/dev/null | tail -n1 | cut -d'=' -f2-
}

print_step_ok() {
  echo -e "${GREEN}✅ $1${NC}"
}

print_step_fail() {
  echo -e "${RED}❌ $1${NC}"
}

print_step_warn() {
  echo -e "${YELLOW}⚠️  $1${NC}"
}


echo -e "${BLUE}1️⃣  Configuración de correo actual${NC}"
MAIL_HOST=$(get_prop "spring.mail.host")
MAIL_PORT=$(get_prop "spring.mail.port")
MAIL_USER=$(get_prop "spring.mail.username")
MAIL_PASS=$(get_prop "spring.mail.password")
MAIL_FROM=$(get_prop "app.mail.from")
BACKEND_PUBLIC_URL=$(get_prop "app.backend.public-url")

echo "- host: ${MAIL_HOST:-NO_CONFIGURADO}"
echo "- port: ${MAIL_PORT:-NO_CONFIGURADO}"
echo "- username: ${MAIL_USER:-NO_CONFIGURADO}"
echo "- from: ${MAIL_FROM:-NO_CONFIGURADO}"
echo "- app.backend.public-url: ${BACKEND_PUBLIC_URL:-NO_CONFIGURADO}"

if [ -n "${MAIL_PASS:-}" ]; then
  PASS_NO_SPACES=$(echo "$MAIL_PASS" | tr -d ' ')
  echo "- password.length: ${#PASS_NO_SPACES} (sin espacios)"
  if [ "$MAIL_PASS" != "$PASS_NO_SPACES" ]; then
    print_step_warn "La password SMTP contiene espacios. En Gmail suele funcionar mejor sin espacios (16 chars)."
  fi
else
  print_step_warn "spring.mail.password no está configurado"
fi

echo ""


echo -e "${BLUE}2️⃣  Comprobando backend${NC}"
BACKEND_INFO=$(curl -s -o /tmp/fallapp_email_info.json -w "%{http_code}" "${BASE_URL}/api/test-email/info")
if [ "$BACKEND_INFO" = "200" ]; then
  STATUS_BACKEND=1
  print_step_ok "Backend responde en ${BASE_URL}"
else
  print_step_fail "Backend no responde correctamente (HTTP ${BACKEND_INFO})"
  echo "- Prueba: sudo systemctl status fallapp"
  echo "- Logs:   sudo journalctl -u fallapp -f"
fi
echo ""


echo -e "${BLUE}3️⃣  Prueba de envío SMTP${NC}"
SMTP_HTTP=$(curl -s -o /tmp/fallapp_smtp_test.json -w "%{http_code}" \
  "${BASE_URL}/api/test-email/simple?to=${EMAIL_DESTINO}&subject=DiagSMTP&text=PruebaSMTP")

if [ "$SMTP_HTTP" = "200" ]; then
  STATUS_SMTP=1
  print_step_ok "Endpoint SMTP respondió 200"
  echo "- Respuesta: $(cat /tmp/fallapp_smtp_test.json)"
else
  print_step_fail "Prueba SMTP falló (HTTP ${SMTP_HTTP})"
  echo "- Respuesta: $(cat /tmp/fallapp_smtp_test.json 2>/dev/null)"
fi
echo ""


echo -e "${BLUE}4️⃣  Registro de usuario de prueba${NC}"
REG_HTTP=$(curl -s -o /tmp/fallapp_registro_test.json -w "%{http_code}" -X POST "${BASE_URL}/api/auth/registro" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${TEST_EMAIL}\",\"contrasena\":\"${TEST_PASS}\",\"nombreCompleto\":\"Diag Verificacion\"}")

if [ "$REG_HTTP" = "201" ] || [ "$REG_HTTP" = "200" ]; then
  STATUS_REGISTRO=1
  print_step_ok "Registro completado (HTTP ${REG_HTTP})"
else
  print_step_fail "Registro falló (HTTP ${REG_HTTP})"
fi
echo "- Respuesta: $(cat /tmp/fallapp_registro_test.json 2>/dev/null)"
echo ""


echo -e "${BLUE}5️⃣  Extrayendo token de verificación desde BD${NC}"
TOKEN=""

if docker ps --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
  TOKEN=$(docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -t -c \
    "SELECT token_verificacion FROM usuarios WHERE email='${TEST_EMAIL}';" 2>/dev/null | tr -d '[:space:]')
else
  TOKEN=$(psql -U "$DB_USER" -d "$DB_NAME" -h localhost -t -c \
    "SELECT token_verificacion FROM usuarios WHERE email='${TEST_EMAIL}';" 2>/dev/null | tr -d '[:space:]')
fi

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
  STATUS_TOKEN=1
  print_step_ok "Token obtenido en BD"
  echo "- token.length: ${#TOKEN}"
else
  print_step_fail "No se encontró token_verificacion en BD"
fi
echo ""


echo -e "${BLUE}6️⃣  Verificando cuenta con token${NC}"
if [ $STATUS_TOKEN -eq 1 ]; then
  VERIF_HTTP=$(curl -s -o /tmp/fallapp_verif_test.json -w "%{http_code}" \
    "${BASE_URL}/api/auth/verificar?token=${TOKEN}")

  if [ "$VERIF_HTTP" = "200" ]; then
    STATUS_VERIFICACION=1
    print_step_ok "Verificación correcta (HTTP 200)"
  else
    print_step_fail "Verificación falló (HTTP ${VERIF_HTTP})"
  fi
  echo "- Respuesta: $(cat /tmp/fallapp_verif_test.json 2>/dev/null)"
else
  print_step_warn "Saltado: no hay token para verificar"
fi
echo ""


echo -e "${BLUE}7️⃣  Probando login del usuario de prueba${NC}"
LOGIN_HTTP=$(curl -s -o /tmp/fallapp_login_test.json -w "%{http_code}" -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${TEST_EMAIL}\",\"contrasena\":\"${TEST_PASS}\"}")

if [ "$LOGIN_HTTP" = "200" ]; then
  STATUS_LOGIN=1
  print_step_ok "Login correcto"
else
  print_step_warn "Login no fue 200 (HTTP ${LOGIN_HTTP})"
fi
echo "- Respuesta: $(cat /tmp/fallapp_login_test.json 2>/dev/null)"
echo ""


echo -e "${BLUE}8️⃣  Logs recientes del servicio (email/smtp/error)${NC}"
if command -v journalctl >/dev/null 2>&1; then
  sudo journalctl -u fallapp --since "$START_TIME" --no-pager \
    | grep -Ei "mail|smtp|auth|exception|error|failed|535|534|5\.7" \
    | tail -n 120 || echo "(Sin coincidencias relevantes en este intervalo)"
else
  echo "journalctl no disponible"
fi
echo ""


echo -e "${BLUE}====================================================${NC}"
echo -e "${BLUE}  📊 RESUMEN DIAGNÓSTICO${NC}"
echo -e "${BLUE}====================================================${NC}"

echo "- Backend activo:            $STATUS_BACKEND"
echo "- SMTP endpoint OK:          $STATUS_SMTP"
echo "- Registro OK:               $STATUS_REGISTRO"
echo "- Token en BD:               $STATUS_TOKEN"
echo "- Verificación endpoint OK:  $STATUS_VERIFICACION"
echo "- Login OK:                  $STATUS_LOGIN"
echo ""

if [ $STATUS_BACKEND -eq 1 ] && [ $STATUS_SMTP -eq 1 ] && [ $STATUS_REGISTRO -eq 1 ] && [ $STATUS_TOKEN -eq 1 ] && [ $STATUS_VERIFICACION -eq 1 ]; then
  print_step_ok "Flujo completo correcto. Si no llega correo, revisa SPAM/rechazo del proveedor destino."
else
  print_step_warn "Hay fallos en el flujo. Revisa el bloque de logs y las respuestas HTTP de cada paso."
fi

echo ""
echo -e "${BLUE}Siguientes comandos útiles:${NC}"
echo "- Logs en vivo: sudo journalctl -u fallapp -f"
echo "- Reintento verificación: curl -X POST '${BASE_URL}/api/auth/reenviar-verificacion?email=${TEST_EMAIL}'"
echo ""

read -p "¿Quieres eliminar el usuario de prueba (${TEST_EMAIL})? (s/n): " RESP
if [[ "$RESP" =~ ^[Ss]$ ]]; then
  if docker ps --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
    docker exec "$DB_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c \
      "DELETE FROM usuarios WHERE email='${TEST_EMAIL}';" >/dev/null 2>&1
  else
    PGPASSWORD=fallapp_secure_password_2026 psql -U "$DB_USER" -d "$DB_NAME" -h localhost -c \
      "DELETE FROM usuarios WHERE email='${TEST_EMAIL}';" >/dev/null 2>&1
  fi
  print_step_ok "Usuario de prueba eliminado"
fi

echo ""
echo -e "${GREEN}✅ Diagnóstico finalizado${NC}"
