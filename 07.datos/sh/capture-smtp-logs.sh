#!/bin/bash

echo "============================================"
echo "📧 CAPTURA COMPLETA DE DEBUG SMTP"
echo "============================================"
echo ""

EMAIL="${1:-fallappproyect@proton.me}"

echo "🔄 Enviando email a: $EMAIL"
echo ""

echo "📋 Logs SMTP en tiempo real (30 segundos):"
echo "============================================"

sudo journalctl -u fallapp -f --no-pager -o cat &
JOURNAL_PID=$!

sleep 2

echo ""
echo "▶️  Enviando petición..."
curl -s "http://localhost:8080/api/test-email/simple?to=$EMAIL&subject=Test&text=Debug%20SMTP"

sleep 5

kill $JOURNAL_PID 2>/dev/null

echo ""
echo ""
echo "============================================"
echo "✅ Captura completada"
echo "============================================"
