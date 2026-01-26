// Configuración global para frontend (punto único para cambiar la URL del backend)
// Intenta cargar `../config.json` (archivo local con credenciales), si está disponible.
(function(){
  let cfg = null;
  try {
    // En renderer con nodeIntegration=true se puede usar require para leer archivos JSON
    cfg = require('../config.json');
  } catch (e) {
    cfg = null;
  }

  const defaultBase = 'http://127.0.0.1:8080';
  const apiBase = (cfg && cfg.apiBase) ? cfg.apiBase : defaultBase;

  window._API_BASE = window._API_BASE || apiBase;
  // Endpoints usados por los scripts
  window._recurso = window._recurso || (window._API_BASE + '/api/fallas');
  window._eventsResource = window._eventsResource || (window._API_BASE + '/api/events');

  // Credenciales (si están en config.json). Evita imprimirlas en consola.
  if (cfg && cfg.apiUser) {
    window._API_CREDENTIALS = {
      user: cfg.apiUser,
      pass: cfg.apiPassword || ''
    };
  } else {
    window._API_CREDENTIALS = window._API_CREDENTIALS || { user: '', pass: '' };
  }
})();
