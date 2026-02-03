# Configuración de Servicio Systemd - FallApp Backend

**Fecha:** 2026-02-02  
**Versión:** 1.0  
**Autor:** Equipo FallApp

---

## 📋 Índice

1. [Descripción General](#descripción-general)
2. [Configuración del Servicio](#configuración-del-servicio)
3. [Gestión del Servicio](#gestión-del-servicio)
4. [Monitoreo](#monitoreo)
5. [Logs](#logs)
6. [Troubleshooting](#troubleshooting)

---

## 🎯 Descripción General

El backend de FallApp está configurado como un servicio systemd que se ejecuta automáticamente al iniciar el sistema y se reinicia automáticamente en caso de fallos.

### Características

- ✅ **Autoarranque**: Se inicia automáticamente con el sistema
- ✅ **Auto-reinicio**: Se reinicia automáticamente si falla
- ✅ **Gestión de logs**: Logs centralizados con journald
- ✅ **Sin límites de memoria**: Usa swap/zram configurado en el sistema
- ✅ **Usuario no privilegiado**: Se ejecuta como usuario `ubuntu`

---

## ⚙️ Configuración del Servicio

### Archivo de Servicio

**Ubicación:** `/etc/systemd/system/fallapp.service`

```ini
[Unit]
Description=FallApp Spring Boot Backend
After=network.target
Wants=network-online.target

[Service]
Type=simple
User=ubuntu
Group=ubuntu
WorkingDirectory=/srv/FallApp/01.backend
ExecStart=/usr/bin/java -Xmx768m -Xms256m -jar /srv/FallApp/01.backend/target/Fallapp-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=fallapp

[Install]
WantedBy=multi-user.target
```

### Parámetros Explicados

| Parámetro | Valor | Descripción |
|-----------|-------|-------------|
| **User** | ubuntu | Usuario que ejecuta el servicio |
| **WorkingDirectory** | /srv/FallApp/01.backend | Directorio de trabajo |
| **ExecStart** | java -Xmx768m... | Comando para iniciar la aplicación |
| **-Xmx768m** | 768 MB | Memoria máxima heap de JVM |
| **-Xms256m** | 256 MB | Memoria inicial heap de JVM |
| **Restart** | always | Reiniciar siempre en caso de fallo |
| **RestartSec** | 10 | Esperar 10 segundos antes de reiniciar |

---

## 🚀 Gestión del Servicio

### Comandos Principales

#### Iniciar el servicio
```bash
sudo systemctl start fallapp
```

#### Detener el servicio
```bash
sudo systemctl stop fallapp
```

#### Reiniciar el servicio
```bash
sudo systemctl restart fallapp
```

#### Ver estado del servicio
```bash
sudo systemctl status fallapp
```

#### Habilitar autoarranque
```bash
sudo systemctl enable fallapp
```

#### Deshabilitar autoarranque
```bash
sudo systemctl disable fallapp
```

#### Recargar configuración (después de editar el archivo)
```bash
sudo systemctl daemon-reload
sudo systemctl restart fallapp
```

---

## 📊 Monitoreo

### Script de Monitoreo Rápido

Se ha creado un script de utilidad para verificar el estado del servicio rápidamente.

**Ubicación:** `/usr/local/bin/fallapp-status`

**Uso:**
```bash
fallapp-status
```

**Salida esperada:**
```
==========================================
  ESTADO DE FALLAPP BACKEND
==========================================

📊 Servicio systemd:
  ✅ ACTIVO

🔌 Puerto 8080:
  ✅ ESCUCHANDO

💾 Memoria del proceso:
  Uso: 16.8% (330.914 MB)

🌐 Prueba API:
  HTTP Status: 200

📝 Últimas 5 líneas de logs:
[logs recientes...]

==========================================
Comandos útiles:
  sudo systemctl status fallapp   - Ver estado completo
  sudo systemctl restart fallapp  - Reiniciar servicio
  sudo journalctl -u fallapp -f   - Ver logs en tiempo real
==========================================
```

### Verificaciones Manuales

#### Verificar que el proceso está corriendo
```bash
ps aux | grep Fallapp
```

#### Verificar que el puerto 8080 está escuchando
```bash
ss -tulpn | grep 8080
# o
netstat -tulpn | grep 8080
```

#### Probar la API
```bash
curl http://localhost:8080/api/estadisticas/resumen
```

---

## 📝 Logs

### Ver logs en tiempo real
```bash
sudo journalctl -u fallapp -f
```

### Ver últimas N líneas de logs
```bash
sudo journalctl -u fallapp -n 50
```

### Ver logs desde una fecha específica
```bash
sudo journalctl -u fallapp --since "2026-02-02 10:00:00"
```

### Ver logs entre dos fechas
```bash
sudo journalctl -u fallapp --since "2026-02-02 00:00:00" --until "2026-02-02 23:59:59"
```

### Ver logs con prioridad (errores y superiores)
```bash
sudo journalctl -u fallapp -p err
```

### Exportar logs a archivo
```bash
sudo journalctl -u fallapp > /tmp/fallapp-logs.txt
```

### Ver logs del arranque actual
```bash
sudo journalctl -u fallapp -b
```

---

## 🔧 Troubleshooting

### El servicio no arranca

**Síntoma:** `sudo systemctl status fallapp` muestra `failed`

**Soluciones:**
1. Verificar logs de error:
   ```bash
   sudo journalctl -u fallapp -n 100 -p err
   ```

2. Verificar que el JAR existe:
   ```bash
   ls -lh /srv/FallApp/01.backend/target/Fallapp-0.0.1-SNAPSHOT.jar
   ```

3. Verificar permisos:
   ```bash
   sudo chown -R ubuntu:ubuntu /srv/FallApp/01.backend
   ```

4. Verificar Java instalado:
   ```bash
   java -version
   ```

5. Verificar PostgreSQL corriendo:
   ```bash
   ps aux | grep postgres
   ```

### El servicio se reinicia continuamente

**Síntoma:** El servicio aparece activo pero se reinicia cada pocos segundos

**Soluciones:**
1. Ver causa del reinicio en logs:
   ```bash
   sudo journalctl -u fallapp -n 200 | grep -i "error\|exception\|fail"
   ```

2. Causas comunes:
   - **No puede conectar a PostgreSQL**: Verificar que la BD está corriendo
   - **Puerto 8080 ocupado**: `ss -tulpn | grep 8080`
   - **OutOfMemoryError**: Reducir `-Xmx` en el archivo de servicio
   - **Error de configuración**: Revisar `application.properties`

### La API no responde

**Síntoma:** El servicio está activo pero no responde en puerto 8080

**Soluciones:**
1. Verificar que Spring Boot terminó de arrancar:
   ```bash
   sudo journalctl -u fallapp | grep "Started FallappApplication"
   ```
   
   El arranque completo toma ~15-20 segundos.

2. Verificar firewall:
   ```bash
   sudo ufw status
   # Si está activo, permitir puerto 8080:
   sudo ufw allow 8080/tcp
   ```

3. Verificar AWS Security Group:
   - Debe tener regla TCP 8080, source 0.0.0.0/0

### Memoria insuficiente

**Síntoma:** `java.lang.OutOfMemoryError` en logs

**Soluciones:**
1. Verificar memoria disponible:
   ```bash
   free -h
   ```

2. Verificar swap activo:
   ```bash
   swapon --show
   ```

3. Reducir memoria heap de Java:
   ```bash
   sudo nano /etc/systemd/system/fallapp.service
   # Cambiar -Xmx768m a -Xmx512m
   sudo systemctl daemon-reload
   sudo systemctl restart fallapp
   ```

### El servicio no se inicia al arrancar el sistema

**Síntoma:** Después de reiniciar el servidor EC2, el servicio no está corriendo

**Solución:**
```bash
# Verificar si está habilitado
sudo systemctl is-enabled fallapp

# Si no está habilitado:
sudo systemctl enable fallapp

# Verificar estado del servicio:
sudo systemctl status fallapp
```

---

## 📋 Checklist de Despliegue

Cuando despliegues una nueva versión del backend:

- [ ] Compilar el nuevo JAR: `cd /srv/FallApp/01.backend && mvn clean package`
- [ ] Verificar que el JAR existe: `ls -lh target/Fallapp-*.jar`
- [ ] Reiniciar el servicio: `sudo systemctl restart fallapp`
- [ ] Verificar arranque exitoso: `sudo journalctl -u fallapp -n 50`
- [ ] Esperar ~20 segundos para arranque completo
- [ ] Probar API: `curl http://localhost:8080/api/estadisticas/resumen`
- [ ] Verificar estado: `fallapp-status`

---

## 🔗 Referencias

- [Systemd Service Documentation](https://www.freedesktop.org/software/systemd/man/systemd.service.html)
- [Spring Boot Deployment](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)
- [Journalctl Documentation](https://www.freedesktop.org/software/systemd/man/journalctl.html)

---

## 📞 Contacto

Para soporte o dudas sobre el despliegue:
- **Documentación:** `/srv/FallApp/04.docs/`
- **Logs:** `sudo journalctl -u fallapp`
- **Estado:** `fallapp-status`
