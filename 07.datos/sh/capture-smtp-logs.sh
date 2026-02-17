#!/bin/bash

echo "============================================"
echo "📧 CAPTURA COMPLETA DE DEBUG SMTP"
echo "============================================"
echo ""

EMAIL="${1:-fallappproyect@proton.me}"

echo "🔄 Enviando email a: $EMAIL"
echo ""

# Capturar TODO el output del journal en tiempo real
echo "📋 Logs SMTP en tiempo real (30 segundos):"
echo "============================================"

# Iniciar captura de logs en segundo plano
sudo journalctl -u fallapp -f --no-pager -o cat &
JOURNAL_PID=$!

# Esperar un poco
sleep 2

# Enviar email
echo ""
echo "▶️  Enviando petición..."
curl -s "http://localhost:8080/api/test-email/simple?to=$EMAIL&subject=Test&text=Debug%20SMTP"

# Esperar a capturar logs
sleep 5

# Detener captura
kill $JOURNAL_PID 2>/dev/null

echo ""
echo ""
echo "============================================"
echo "✅ Captura completada"
echo "============================================"
