// Lógica cliente para pantalla de detalles/edición de falla (restaurado desde screen2.js)
// Usar window.fetch si está disponible en el renderer; si no, intentar require('node-fetch')
let nf = null; // not used in renderer anymore; main process handles HTTP
let mapa = null; // Variable global para el mapa de Leaflet

document.addEventListener('DOMContentLoaded', function () {
   // Configurar botón de logout
   const logoutBtn = document.getElementById('logout');
   if (logoutBtn) {
      logoutBtn.addEventListener('click', function() {
         try {
            localStorage.removeItem('fallapp_user');
            localStorage.removeItem('fallapp_token');
            localStorage.removeItem('fallapp_user_email');
            localStorage.removeItem('fallapp_user_nombre');
            localStorage.removeItem('fallapp_user_rol');
            localStorage.removeItem('fallapp_user_idFalla');
            localStorage.removeItem('fallapp_user_id');
         } catch (e) {
            console.error('Error removing user:', e);
         }
         window.location.href = '../js/index.html';
      });
   }

   const params = new URLSearchParams(window.location.search);
   let id = params.get('id');
   
   // Si no hay id en URL, obtener el idFalla del usuario autenticado
   if (!id) {
      const idFallaStored = localStorage.getItem('fallapp_user_idFalla');
      if (idFallaStored) {
         id = idFallaStored;
         console.log('Usando idFalla del usuario autenticado:', id);
      } else {
         console.warn('No hay id en URL ni idFalla en localStorage, usando ID por defecto: 95');
         id = '95'; // Por defecto usa ID 95
      }
   }
   
   // Base del backend (Spring Boot): ajusta puerto si es necesario
   window._apiBase = window._apiBase || 'http://35.180.21.42:8080/api';
   window._recurso = window._recurso || (window._apiBase + '/fallas');

   // Cargar datos desde backend si hay id, si no, usar stub
   if (id) {
      loadFallaById(id);
   } else {
      loadFallaById(null); // mostrará datos por defecto (stub)
   }

   // Botones: guardar/editar/eliminar
   const saveBtn = document.getElementById('saveBtn');
   const editBtn = document.getElementById('editBtn');
   const deleteBtn = document.getElementById('deleteBtn');

   // Arrancamos en modo SOLO LECTURA
   setEditMode(false);

   if (saveBtn) saveBtn.addEventListener('click', saveFalla);
   if (editBtn) editBtn.addEventListener('click', toggleEditMode);
   if (deleteBtn) deleteBtn.addEventListener('click', function(){ if(confirm('¿Eliminar esta falla?')){ const idToDel = document.getElementById('fallaId').value; if(idToDel){ if(window.api && window.api.deleteFalla){ window.api.deleteFalla(idToDel).then(()=>{ alert('Falla eliminada'); window.location.href='events.html'; }).catch(()=>{ alert('Error al eliminar'); }); } else { alert('API no disponible'); } } else { alert('No hay id para eliminar'); } } });
});

function loadFallaById(id) {
   // Si no hay id, rellenamos con un ejemplo (modo offline/desarrollo)
   if (!id) {
      const ejemplo = {
         id: 'sample-123',
         fecha: new Date().toISOString().slice(0,16),
         nombre: 'Calle Falsa 123',
         agrupacion: 'Caída',
         descripcion: 'El usuario se tropezó con un bordillo y sufrió una caída leve.'
      };
      populateForm(ejemplo);
      return;
   }

   // Primero intentar cargar desde el API directo
   const apiUrl = `${window._apiBase}/fallas/${id}`;
   console.log('Intentando cargar falla desde:', apiUrl);
   
   fetch(apiUrl)
      .then(response => {
         if (!response.ok) throw new Error(`Error ${response.status}`);
         return response.json();
      })
      .then(json => {
         console.log('Falla cargada del backend:', json);
         // El API devuelve { exito, mensaje, datos: { ... } }
         const fallaData = json.datos || json;
         populateForm(fallaData);
         // Si la falla tiene coordenadas, inicializar el mapa directamente
         if (fallaData.latitud && fallaData.longitud) {
            initMapa(fallaData.latitud, fallaData.longitud, id);
         } else {
            // Si no hay coordenadas, intentar cargar del endpoint de ubicación
            loadUbicacionMapa(id);
         }
      })
      .catch(err => {
         console.warn('Error cargando desde API directo:', err);
         // Si falla la API, intentar con el bridge de Electron
         if (window.api && window.api.getFalla) {
            window.api.getFalla(id).then(json => {
               console.log('Falla cargada del bridge:', json);
               populateForm(json);
               if (json.latitud && json.longitud) {
                  initMapa(json.latitud, json.longitud, id);
               } else {
                  loadUbicacionMapa(id);
               }
            }).catch(err2 => {
               console.warn('Bridge también falló, usando stub:', err2);
               const ejemplo = { 
                  id: id, 
                  idFalla: id,
                  fecha: new Date().toISOString().slice(0,16), 
                  ubicacion: 'Calle Falsa 123', 
                  tipo: 'Caída', 
                  descripcion: 'El usuario se tropezó con un bordillo y sufrió una caída leve.', 
                  estado: 'abierta', 
                  reportado_por: 'Juan Pérez'
               };
               populateForm(ejemplo);
               loadUbicacionMapa(id);
            });
         } else {
            console.warn('API bridge no disponible, usando stub');
            const ejemplo = { 
               id: id, 
               idFalla: id,
               fecha: new Date().toISOString().slice(0,16), 
               ubicacion: 'Calle Falsa 123', 
               tipo: 'Caída', 
               descripcion: 'El usuario se tropezó con un bordillo y sufrió una caída leve.', 
               estado: 'abierta', 
               reportado_por: 'Juan Pérez'
            };
            populateForm(ejemplo);
            loadUbicacionMapa(id);
         }
      });
}

function loadUbicacionMapa(fallaId) {
   // Obtener las coordenadas desde el endpoint específico de ubicación
   const ubicacionUrl = `http://35.180.21.42:8080/api/fallas/${fallaId}/ubicacion`;
   console.log('Cargando ubicación desde:', ubicacionUrl);
   
   fetch(ubicacionUrl)
      .then(response => {
         if (!response.ok) throw new Error(`Error ${response.status}`);
         return response.json();
      })
      .then(data => {
         console.log('Datos de ubicación recibidos:', data);
         // El endpoint devuelve { exito, mensaje, datos: { latitud, longitud, ... } }
         const lat = data.datos?.latitud || data.latitude || data.lat || data.latitud;
         const lng = data.datos?.longitud || data.longitude || data.lng || data.longitud;
         
         console.log('Coordenadas extraídas - lat:', lat, 'lng:', lng);
         
         if (lat !== undefined && lat !== null && lng !== undefined && lng !== null) {
            initMapa(lat, lng, fallaId);
         } else {
            console.warn('No se obtuvieron coordenadas válidas del servidor', data);
            initMapaDefault();
         }
      })
      .catch(err => {
         console.warn('Error cargando ubicación desde API:', err);
         // Si falla, mostrar mapa con ubicación por defecto (Valencia, España)
         initMapaDefault();
      });
}

function initMapa(lat, lng, fallaId) {
   console.log('=== INIT MAPA ===');
   console.log('lat:', lat, '(tipo:', typeof lat, ')');
   console.log('lng:', lng, '(tipo:', typeof lng, ')');
   console.log('fallaId:', fallaId);
   
   // Asegurar que son números
   const latNum = parseFloat(lat);
   const lngNum = parseFloat(lng);
   console.log('Convertidos a números - latNum:', latNum, 'lngNum:', lngNum);
   
   // Verificar que Leaflet esté disponible
   if (typeof L === 'undefined') {
      console.error('Leaflet no está cargado');
      return;
   }
   
   console.log('Leaflet está disponible, versión:', L.version);
   
   const mapContainer = document.getElementById('mapaUbicacion');
   console.log('mapContainer encontrado:', mapContainer);
   console.log('mapContainer HTML:', mapContainer ? mapContainer.outerHTML : 'null');
   
   if (!mapContainer) {
      console.error('No se encontró el contenedor mapaUbicacion');
      return;
   }
   
   console.log('Dimensiones del contenedor:', mapContainer.offsetWidth, 'x', mapContainer.offsetHeight);
   console.log('Estilo display:', window.getComputedStyle(mapContainer).display);
   console.log('Estilo visibility:', window.getComputedStyle(mapContainer).visibility);
   
   // Inicializar el mapa si no existe
   if (!mapa) {
      console.log('Creando nuevo mapa en coordenadas:', [latNum, lngNum]);
      try {
         // Limpiar cualquier contenido previo
         mapContainer.innerHTML = '';
         
         mapa = L.map('mapaUbicacion', {
            center: [latNum, lngNum],
            zoom: 15,
            minZoom: 15
         });
         console.log('Mapa creado, configurando tile layer');
         
         // Agregar tile layer de OpenStreetMap
         L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors',
            maxZoom: 19
         }).addTo(mapa);
         console.log('Tile layer agregado');
         
         // Agregar marcador
         L.marker([latNum, lngNum])
            .addTo(mapa)
            .bindPopup(`Falla #${fallaId}<br>Latitud: ${latNum.toFixed(8)}<br>Longitud: ${lngNum.toFixed(8)}`);
         console.log('Marcador agregado');
         
         console.log('Mapa creado exitosamente en:', mapa.getCenter());
      } catch (e) {
         console.error('Error creando el mapa:', e);
         console.error('Stack trace:', e.stack);
      }
   } else {
      // Si el mapa ya existe, actualizar la vista
      console.log('Mapa existe, actualizando vista a:', [latNum, lngNum]);
      mapa.setView([latNum, lngNum], 15);
      console.log('Nueva vista:', mapa.getCenter());
      
      // Limpiar marcadores previos
      mapa.eachLayer((layer) => {
         if (layer instanceof L.Marker) {
            mapa.removeLayer(layer);
         }
      });
      
      // Agregar nuevo marcador
      L.marker([latNum, lngNum])
         .addTo(mapa)
         .bindPopup(`Falla #${fallaId}<br>Latitud: ${latNum.toFixed(8)}<br>Longitud: ${lngNum.toFixed(8)}`);
   }
}

function initMapaDefault() {
   // Valencia, España como ubicación por defecto
   const defaultLat = 39.4699;
   const defaultLng = -0.3763;
   initMapa(defaultLat, defaultLng, 'desconocida');
}

function populateForm(data) {
   const el = (id) => document.getElementById(id);
   if (!el('fallaId')) return; // formulario no presente
   
   // ID de Falla
   const idFalla = data.id || data.idFalla || '';
   if (el('fallaId')) el('fallaId').value = idFalla;
   
   // Nombre de la Falla
   const nombre = data.nombre || data.ubicacion || data.direccion || '';
   if (el('fallaNombre')) el('fallaNombre').value = nombre;
   
   // Sección
   const seccion = data.seccion || '';
   if (el('fallaSeccion')) el('fallaSeccion').value = seccion;
   
   // Fallera
   const fallera = data.fallera || '';
   if (el('fallaFallera')) el('fallaFallera').value = fallera;
   
   // Presidente
   const presidente = data.presidente || '';
   if (el('fallaPresidente')) el('fallaPresidente').value = presidente;
   
   // Artista
   const artista = data.artista || '';
   if (el('fallaArtista')) el('fallaArtista').value = artista;
   
   // Lema
   const lema = data.lema || '';
   if (el('fallaLema')) el('fallaLema').value = lema;
   
   // Web Oficial
   const webOficial = data.webOficial || data.weOficial || '';
   if (el('fallaWebOficial')) el('fallaWebOficial').value = webOficial;
   
   // Teléfono Contacto
   const telefonoContacto = data.telefonoContacto || data.telefonoContacto || '';
   if (el('fallaTelefonoContacto')) el('fallaTelefonoContacto').value = telefonoContacto;
   
   // Email Contacto
   const emailContacto = data.emailContacto || data.correoContacto || '';
   if (el('fallaEmailContacto')) el('fallaEmailContacto').value = emailContacto;
   
   // Año de Fundación
   const anyoFundacion = data.anyoFundacion || '';
   if (el('fallaAnyoFundacion')) el('fallaAnyoFundacion').value = anyoFundacion;
   
   // Distintivo
   const distintivo = data.distintivo || '';
   if (el('fallaDistintivo')) el('fallaDistintivo').value = distintivo;
   
   // URL Boceto - Mostrar imagen
   // Buscar en múltiples nombres de campo posibles
   const uriBoceto = data.uriBoceto 
      || data.imagenUrl 
      || data.imagen 
      || data.boceto 
      || data.urlBoceto 
      || data.fotoUrl 
      || data.foto 
      || '';
   
   console.log('Búsqueda de imagen Boceto:');
   console.log('  - uriBoceto:', data.uriBoceto);
   console.log('  - imagenUrl:', data.imagenUrl);
   console.log('  - imagen:', data.imagen);
   console.log('  - boceto:', data.boceto);
   console.log('  - urlBoceto:', data.urlBoceto);
   console.log('  - fotoUrl:', data.fotoUrl);
   console.log('  - foto:', data.foto);
   console.log('  - URL final encontrada:', uriBoceto);
   
   if (el('fallaUrlBoceto')) el('fallaUrlBoceto').value = uriBoceto || '';
   
   const bocetoImg = el('fallaBoceto');
   const bocetoContainer = bocetoImg ? bocetoImg.parentElement : null;
   
   if (uriBoceto && bocetoImg) {
      console.log('✓ Configurando imagen Boceto con URL:', uriBoceto);
      bocetoImg.src = uriBoceto;
      if (bocetoContainer) {
         bocetoContainer.style.display = 'block';
      }
      bocetoImg.onerror = function() {
         console.warn('✗ Error cargando imagen del Boceto:', uriBoceto);
         if (this.parentElement) {
            this.parentElement.style.display = 'block'; // Mostrar contenedor
         }
      };
      bocetoImg.onload = function() {
         console.log('✓ Imagen Boceto cargada correctamente');
      };
   } else {
      // Si no hay URL, ocultar el contenedor pero mostrar placeholder
      console.log('✗ No hay URL de boceto disponible - mostrando placeholder');
      bocetoImg.src = ''; // Asegurar que src esté vacío para mostrar placeholder
   }
   
   // Categoría
   const categoria = data.categoria || '';
   if (el('fallaCategoria')) el('fallaCategoria').value = categoria;
   
   // Estadísticas
   const totalEventos = data.totalEventos || 0;
   if (el('fallaTotalEventos')) el('fallaTotalEventos').value = totalEventos;
   
   const totalHintos = data.totalNinots || data.totalHintos || 0;
   if (el('fallaTotalHintos')) el('fallaTotalHintos').value = totalHintos;
   
   const totalMiembros = data.totalMiembros || 0;
   if (el('fallaTotalMiembros')) el('fallaTotalMiembros').value = totalMiembros;
   
   // Fechas - Formatear correctamente
   const fechaCreacion = data.fechaCreacion || '';
   if (el('fallaFechaCreacion')) el('fallaFechaCreacion').value = formatDateForInput(fechaCreacion);
   
   const fechaActualizacion = data.fechaActualizacion || '';
   if (el('fallaFechaActualizacion')) el('fallaFechaActualizacion').value = formatDateForInput(fechaActualizacion);
   
   // Actualizar side panel con nombre o lema
   const side = document.getElementById('sideSummary');
   const resumen = nombre || lema || fallera || '';
   if (side && resumen) {
      side.textContent = resumen.slice(0, 140) + (resumen.length > 140 ? '...' : '');
   }
   
   // Log de diagnóstico
   console.log('=== DIAGNÓSTICO POPULATE FORM ===');
   console.log('Datos completos:', data);
   console.log('Campos disponibles en data:', Object.keys(data));
   console.log('====================================');
}

// Helper para formatear fechas para input datetime-local
function formatDateForInput(dateStr) {
   try {
      // Si ya está en formato YYYY-MM-DDTHH:mm, devolverlo como está
      if (dateStr && dateStr.match(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/)) {
         return dateStr.slice(0, 16);
      }
      // Si es ISO string, convertir
      const date = new Date(dateStr);
      if (!isNaN(date)) {
         return date.toISOString().slice(0, 16);
      }
   } catch (e) {
      console.warn('Error formateando fecha:', e);
   }
   return dateStr || '';
}

// Validar datos de falla
function validateFallaData(data) {
   const errors = [];
   
   if (!data.nombre || data.nombre.trim().length === 0) {
      errors.push('El nombre de la falla es obligatorio');
   }
   
   if (!data.seccion || data.seccion.trim().length === 0) {
      errors.push('La sección es obligatoria');
   }
   
   if (!data.presidente || data.presidente.trim().length === 0) {
      errors.push('El nombre del presidente es obligatorio');
   }
   
   if (data.anyoFundacion) {
      const year = parseInt(data.anyoFundacion);
      if (isNaN(year) || year < 1900 || year > new Date().getFullYear()) {
         errors.push('El año de fundación debe estar entre 1900 y ' + new Date().getFullYear());
      }
   }
   
   if (data.emailContacto) {
      if (!data.emailContacto.includes('@') || !data.emailContacto.includes('.')) {
         errors.push('El email contacto no es válido');
      }
   }
   
   return errors;
}

// Mostrar notificación mejorada
function showNotificationFalla(message, type = 'info') {
   const notification = document.createElement('div');
   notification.style.cssText = `
      position: fixed;
      top: 20px;
      right: 20px;
      padding: 16px 24px;
      border-radius: 8px;
      font-weight: 600;
      font-size: 14px;
      z-index: 9999;
      animation: slideIn 0.3s ease-out;
      max-width: 400px;
      word-wrap: break-word;
      white-space: pre-wrap;
      ${type === 'success' ? 'background: #10b981; color: white;' : 
        type === 'error' ? 'background: #ef4444; color: white;' :
        type === 'warning' ? 'background: #f59e0b; color: white;' :
        'background: #3b82f6; color: white;'}
   `;
   notification.textContent = message;
   document.body.appendChild(notification);
   
   const timeout = type === 'error' ? 5000 : 3000;
   setTimeout(() => notification.remove(), timeout);
}

function saveFalla() {
   const urlBocetoVal = document.getElementById('fallaUrlBoceto')?.value?.trim() || null;
   const formData = {
      nombre: document.getElementById('fallaNombre').value.trim(),
      seccion: document.getElementById('fallaSeccion').value.trim(),
      fallera: document.getElementById('fallaFallera').value.trim(),
      presidente: document.getElementById('fallaPresidente').value.trim(),
      artista: document.getElementById('fallaArtista').value.trim(),
      lema: document.getElementById('fallaLema').value.trim(),
      webOficial: document.getElementById('fallaWebOficial').value.trim(),
      telefonoContacto: document.getElementById('fallaTelefonoContacto').value.trim(),
      emailContacto: document.getElementById('fallaEmailContacto').value.trim(),
      anyoFundacion: parseInt(document.getElementById('fallaAnyoFundacion').value) || null,
      distintivo: document.getElementById('fallaDistintivo').value.trim(),
      categoria: document.getElementById('fallaCategoria').value.trim(),
      urlBoceto: urlBocetoVal
      // Nota: totalEventos, totalNinots, totalMiembros son read-only (calculados por backend)
   };

   // Validar datos
   const validationErrors = validateFallaData(formData);
   if (validationErrors.length > 0) {
      showNotificationFalla(validationErrors.join('\n'), 'error');
      return;
   }

   const id = document.getElementById('fallaId').value;
   
   // Mostrar indicador de carga
   const saveBtn = document.getElementById('saveBtn');
   const originalText = saveBtn.textContent;
   if (saveBtn) {
      saveBtn.textContent = 'Guardando...';
      saveBtn.disabled = true;
   }

   // Si tiene id => actualizar (PUT), si no => crear (POST)
   if (id) {
      // Actualizar falla existente
      actualizar_falla_directo(id, formData, originalText, saveBtn);
   } else {
      showNotificationFalla('Error: No se puede crear una nueva falla desde aquí. Por favor, usando el formulario correcto.', 'error');
      if (saveBtn) {
         saveBtn.textContent = originalText;
         saveBtn.disabled = false;
      }
   }
}

// Actualizar falla directamente llamando a la API
function actualizar_falla_directo(id, formData, originalText, saveBtn) {
   // Usar la función actualizarFalla de api.js
   actualizarFalla(id, formData)
   .then(json => {
      const result = json.datos || json;
      showNotificationFalla('Falla actualizada correctamente', 'success');
      const side = document.getElementById('sideSummary');
      if(side) side.textContent = (result.nombre||formData.nombre||'').slice(0,140) + ((result.nombre||formData.nombre) && (result.nombre||formData.nombre).length>140? '...':'');
      setEditMode(false);
   })
   .catch(err => {
      console.error('Error actualizando falla:', err);
      showNotificationFalla(`Error al guardar: ${err.message}`, 'error');
   })
   .finally(() => {
      if (saveBtn) {
         saveBtn.textContent = originalText;
         saveBtn.disabled = false;
      }
   });
}

// Exportar para pruebas (opcional)
window._falla = { loadFallaById, populateForm, saveFalla };

// ------------------ Edit mode helpers ------------------
function setEditMode(enabled){
   const fields = [
      'fallaNombre', 'fallaSeccion', 'fallaFallera', 'fallaPresidente', 
      'fallaArtista', 'fallaLema', 'fallaWebOficial', 'fallaTelefonoContacto', 'fallaEmailContacto',
      'fallaAnyoFundacion', 'fallaDistintivo', 'fallaCategoria', 
      'fallaTotalEventos', 'fallaTotalHintos', 'fallaTotalMiembros'
   ];
   fields.forEach(id=>{ const el = document.getElementById(id); if(!el) return; el.disabled = !enabled; });
   const saveBtn = document.getElementById('saveBtn'); const editBtn = document.getElementById('editBtn');
   if(saveBtn) saveBtn.style.display = enabled? 'inline-block':'none';
   if(editBtn) editBtn.textContent = enabled? 'Cancelar':'Editar';
}

function toggleEditMode(){
   const saveBtn = document.getElementById('saveBtn');
   const enabled = !(saveBtn && saveBtn.style.display === 'inline-block');
   setEditMode(enabled);
}

// Agregar animación de deslizamiento si no existe
if (!document.querySelector('style:contains(slideIn)')) {
   const style = document.createElement('style');
   style.textContent = `
      @keyframes slideIn {
         from {
            transform: translateX(400px);
            opacity: 0;
         }
         to {
            transform: translateX(0);
            opacity: 1;
         }
      }
   `;
   document.head.appendChild(style);
}

