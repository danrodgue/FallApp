#!/bin/bash
# ============================================
# Script de Diagnóstico - Testing Dashboard
# ============================================

echo "🔍 DIAGNÓSTICO DEL PANEL DE TESTING"
echo "===================================="
echo ""

# 1. Verificar servidor HTTP
echo "1️⃣ Estado del servidor HTTP (puerto 8001):"
if ps aux | grep -q '[p]ython3.*8001'; then
    echo "   ✅ Servidor corriendo"
    ps aux | grep '[p]ython3.*8001' | awk '{print "   PID:", $2}'
else
    echo "   ❌ Servidor NO está corriendo"
    echo "   💡 Ejecuta: cd /srv/FallApp/05.testing-dashboard && nohup python3 -m http.server 8001 --bind 0.0.0.0 > /tmp/dashboard.log 2>&1 &"
fi
echo ""

# 2. Verificar puerto escuchando
echo "2️⃣ Puerto escuchando:"
if ss -tlnp 2>/dev/null | grep -q 8001; then
    ss -tlnp 2>/dev/null | grep 8001 | awk '{print "   ✅ Escuchando en:", $4}'
else
    echo "   ❌ Puerto 8001 NO está escuchando"
fi
echo ""

# 3. Verificar acceso local
echo "3️⃣ Prueba de acceso local (desde el servidor):"
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8001/ 2>/dev/null)
if [ "$HTTP_STATUS" = "200" ]; then
    echo "   ✅ Acceso local: OK (HTTP $HTTP_STATUS)"
else
    echo "   ❌ Acceso local: FALLO (HTTP $HTTP_STATUS)"
fi
echo ""

# 4. Información de red
echo "4️⃣ Información de red:"
PUBLIC_IP=$(curl -s ifconfig.me 2>/dev/null || echo "No disponible")
echo "   IP Pública del servidor: $PUBLIC_IP"
echo "   URL del dashboard: http://$PUBLIC_IP:8001"
echo ""

# 5. Firewall local (UFW)
echo "5️⃣ Firewall local (UFW):"
UFW_STATUS=$(sudo ufw status 2>/dev/null | head -1)
echo "   Estado UFW: $UFW_STATUS"
if sudo ufw status 2>/dev/null | grep -q "8001.*ALLOW"; then
    echo "   ✅ Puerto 8001: PERMITIDO"
elif [ "$UFW_STATUS" = "Status: inactive" ]; then
    echo "   ℹ️  UFW inactivo (no bloquea)"
else
    echo "   ⚠️  Regla para puerto 8001 no encontrada"
    echo "   💡 Ejecuta: sudo ufw allow 8001/tcp"
fi
echo ""

# 6. Backend API
echo "6️⃣ Backend API (puerto 8080):"
API_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/fallas?pagina=0&tamano=1 2>/dev/null)
if [ "$API_STATUS" = "200" ]; then
    echo "   ✅ Backend funcionando (HTTP $API_STATUS)"
else
    echo "   ❌ Backend no responde (HTTP $API_STATUS)"
fi
echo ""

# 7. Resumen y siguientes pasos
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 RESUMEN"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

if [ "$HTTP_STATUS" = "200" ]; then
    echo "✅ El servidor local funciona correctamente"
    echo ""
    echo "⚠️  SI NO PUEDES ACCEDER DESDE TU MÁQUINA:"
    echo ""
    echo "   El problema es AWS Security Groups bloqueando el puerto 8001"
    echo ""
    echo "   🔧 SOLUCIÓN:"
    echo "   1. Ve a AWS EC2 Console: https://console.aws.amazon.com/ec2/"
    echo "   2. Selecciona tu instancia (IP: $PUBLIC_IP)"
    echo "   3. Ve a Security → Security Groups"
    echo "   4. Click 'Edit inbound rules'"
    echo "   5. Agregar regla:"
    echo "      - Type: Custom TCP"
    echo "      - Port: 8001"
    echo "      - Source: 0.0.0.0/0 (o tu IP pública)"
    echo "   6. Guardar"
    echo ""
    echo "   📖 Documentación completa:"
    echo "      /srv/FallApp/05.testing-dashboard/ACCESO_REMOTO.md"
else
    echo "❌ El servidor local no responde"
    echo ""
    echo "   🔧 SOLUCIÓN:"
    echo "   cd /srv/FallApp/05.testing-dashboard"
    echo "   nohup python3 -m http.server 8001 --bind 0.0.0.0 > /tmp/dashboard.log 2>&1 &"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
