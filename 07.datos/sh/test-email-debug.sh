#!/bin/bash

# Script para probar envío de emails y capturar errores detallados

echo "============================================"
echo "🧪 TEST DE EMAIL - DIAGNÓSTICO COMPLETO"
echo "============================================"
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración
EMAIL_DESTINO="${1:-fallappproyect@proton.me}"

echo -e "${BLUE}📧 Email de destino: $EMAIL_DESTINO${NC}"
echo ""

# 1. Verificar que backend esté funcionando
echo "1️⃣  Verificando backend..."
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}   ✅ Backend funcionando${NC}"
else
    echo -e "${RED}   ❌ Backend no está disponible${NC}"
    exit 1
fi
echo ""

# 2. Limpiar logs anteriores (últimos 30 segundos)
echo "2️⃣  Preparando para capturar logs..."
sleep 2
echo ""

# 3. Enviar email de prueba
echo "3️⃣  Enviando email de prueba..."
RESPONSE=$(curl -s -w "\n%{http_code}" "http://localhost:8080/api/test-email/simple?to=$EMAIL_DESTINO&subject=Prueba%20Debug&text=Email%20de%20prueba%20con%20debug%20habilitado")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

echo "   HTTP Status: $HTTP_CODE"
echo "   Respuesta: $BODY"
echo ""

# 4. Esperar a que se procese
echo "4️⃣  Esperando logs (2 segundos)..."
sleep 2
echo ""

# 5. Mostrar logs de email
echo "============================================"
echo "📋 LOGS DE EMAIL (últimos 60 segundos)"
echo "============================================"
echo ""

sudo journalctl -u fallapp --since "60 seconds ago" --no-pager | grep -i "DEBUG\|mail\|smtp\|send\|error\|exception\|authentication\|535\|550\|554" | tail -100

echo ""
echo "============================================"
echo "🔍 ANÁLISIS DE ERRORES"
echo "============================================"
echo ""

# Buscar errores específicos
ERRORS=$(sudo journalctl -u fallapp --since "60 seconds ago" --no-pager | grep -i "ERROR\|Exception\|Failed\|rejected\|authentication failed\|535\|550")

if [ -n "$ERRORS" ]; then
    echo -e "${RED}❌ ERRORES ENCONTRADOS:${NC}"
    echo "$ERRORS"
    echo ""
    
    # Analizar errores comunes
    if echo "$ERRORS" | grep -qi "authentication failed\|535"; then
        echo -e "${YELLOW}⚠️  Error de autenticación SMTP${NC}"
        echo "   - Verifica usuario y contraseña de Brevo"
        echo "   - Usuario actual: $(grep 'spring.mail.username' /srv/FallApp/01.backend/src/main/resources/application.properties | cut -d= -f2)"
    fi
    
    if echo "$ERRORS" | grep -qi "550\|554\|sender"; then
        echo -e "${YELLOW}⚠️  Error: Remitente rechazado${NC}"
        echo "   - El correo remitente NO está verificado en Brevo"
        echo "   - Remitente actual: $(grep 'app.mail.from=' /srv/FallApp/01.backend/src/main/resources/application.properties | cut -d= -f2)"
        echo ""
        echo "   🔧 SOLUCIÓN:"
        echo "   1. Accede a: https://app.brevo.com/settings/senders"
        echo "   2. Agrega y verifica el correo: $(grep 'app.mail.from=' /srv/FallApp/01.backend/src/main/resources/application.properties | cut -d= -f2)"
        echo "   3. Abre el email de verificación en tu bandeja"
        echo "   4. Haz clic en el link de verificación"
    fi
else
    echo -e "${GREEN}✅ No se encontraron errores en los logs${NC}"
    echo ""
    echo "   Posibles causas de que no llegue el email:"
    echo "   1. Email en carpeta SPAM (revisa carpeta de correo no deseado)"
    echo "   2. Remitente no verificado en Brevo"
    echo "   3. Límite diario de Brevo alcanzado (300 emails/día en plan gratuito)"
    echo "   4. Proton Mail está bloqueando emails de Brevo"
fi

echo ""
echo "============================================"
echo "📊 CONFIGURACIÓN ACTUAL"
echo "============================================"
grep -E "spring.mail|app.mail" /srv/FallApp/01.backend/src/main/resources/application.properties | grep -v password
echo ""

echo "============================================"
echo "✅ Diagnóstico completado"
echo "============================================"
