#!/bin/bash

# ============================================================================
# Script: test-email.sh
# Descripción: Prueba completa del sistema de envío de emails
# ============================================================================

echo "================================================"
echo "  📧 DIAGNÓSTICO DE ENVÍO DE EMAILS"
echo "================================================"
echo ""

# 1. Verificar que el backend está corriendo
echo "1️⃣  Verificando backend..."
HEALTH=$(curl -s http://localhost:8080/actuator/health 2>&1)
if echo "$HEALTH" | grep -q "UP"; then
    echo "✅ Backend está corriendo"
else
    echo "❌ Backend no está disponible"
    echo "Respuesta: $HEALTH"
    exit 1
fi
echo ""

# 2. Probar endpoint de información
echo "2️⃣  Consultando endpoints disponibles..."
INFO=$(curl -s http://localhost:8080/api/test-email/info 2>&1)
echo "$INFO"
echo ""

# 3. Probar envío de email simple
echo "3️⃣  Probando envío de email simple..."
echo "Enviando a: test@example.com"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
  "http://localhost:8080/api/test-email/simple?to=test@example.com&subject=Prueba&body=Prueba de email desde FallApp" \
  2>&1)

HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | head -n -1)

echo "Código HTTP: $HTTP_CODE"
echo "Respuesta: $BODY"

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Endpoint responde correctamente"
else
    echo "❌ Error en el endpoint (código: $HTTP_CODE)"
fi
echo ""

# 4. Verificar logs del backend
echo "4️⃣  Últimos logs del backend relacionados con email..."
sudo journalctl -u fallapp --since "1 minute ago" | grep -i "email\|mail\|smtp" | tail -10 || echo "(Sin logs de email encontrados)"
echo ""

# 5. Verificar configuración en application.properties
echo "5️⃣  Verificando configuración de email..."
echo "Host SMTP: smtp-relay.brevo.com"
echo "Puerto: 587"
echo "Usuario configurado: a27e0c001@smtp-brevo.com"
echo ""

echo "================================================"
echo "  ✅ DIAGNÓSTICO COMPLETADO"
echo "================================================"
echo ""
echo "Nota: Si el código HTTP es 200 pero no recibes el email:"
echo "1. Verifica que el dominio de destino sea válido"
echo "2. Revisa la carpeta de SPAM"
echo "3. Verifica que las credenciales de Brevo sean correctas"
echo "4. Revisa los límites de tu cuenta de Brevo (300 emails/día gratis)"
echo ""
