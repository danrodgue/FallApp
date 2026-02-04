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
         } catch (e) {
            console.error('Error removing user:', e);
         }
         window.location.href = '../js/index.html';
      });
   }

   const params = new URLSearchParams(window.location.search);
   const id = params.get('id') || '95'; // Por defecto usa ID 95
   // Base del backend (Spring Boot): ajusta puerto si es necesario
   window._recurso = window._recurso || 'http://localhost/api/fallas';
   window._apiBase = window._apiBase || 'http://35.180.21.42:8080/api';

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

   if (saveBtn) saveBtn.addEventListener('click', saveFalla);
   if (editBtn) editBtn.addEventListener('click', toggleEditMode);
   if (deleteBtn) deleteBtn.addEventListener('click', function(){ if(confirm('¿Eliminar esta falla?')){ const idToDel = document.getElementById('fallaId').value; if(idToDel){ if(window.api && window.api.deleteFalla){ window.api.deleteFalla(idToDel).then(()=>{ alert('Falla eliminada'); window.location.href='events.html'; }).catch(()=>{ alert('Error al eliminar'); }); } else { alert('API no disponible'); } } else { alert('No hay id para eliminar'); } } });
    // Habilitar edición por defecto para que el usuario pueda escribir aunque no se persista
    setEditMode(true);
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
         populateForm(json);
         // Si la falla tiene coordenadas, inicializar el mapa directamente
         if (json.latitud && json.longitud) {
            initMapa(json.latitud, json.longitud, id);
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
                  reportado_por: 'Juan Pérez',
                  latitud: 39.4699,
                  longitud: -0.3763
               };
               populateForm(ejemplo);
               initMapa(ejemplo.latitud, ejemplo.longitud, id);
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
               reportado_por: 'Juan Pérez',
               latitud: 39.4699,
               longitud: -0.3763
            };
            populateForm(ejemplo);
            initMapa(ejemplo.latitud, ejemplo.longitud, id);
         }
      });
}

function loadUbicacionMapa(fallaId) {
   // Obtener las coordenadas desde el endpoint específico de ubicación
   const ubicacionUrl = `${window._apiBase}/fallas/${fallaId}/ubicacion`;
   console.log('Cargando ubicación desde:', ubicacionUrl);
   
   fetch(ubicacionUrl)
      .then(response => {
         if (!response.ok) throw new Error(`Error ${response.status}`);
         return response.json();
      })
      .then(data => {
         console.log('Datos de ubicación recibidos:', data);
         // Se espera que el endpoint devuelva { latitude, longitude } o { lat, lng } o { latitud, longitud }
         const lat = data.latitude || data.lat || data.latitud;
         const lng = data.longitude || data.lng || data.longitud;
         
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
   
   // Mapear los campos que devuelve el API a los campos del formulario
   el('fallaId').value = data.id || data.idFalla || '';
   
   // Fecha: soportar diferentes formatos
   if (data.fecha) {
      el('fallaFecha').value = formatDateForInput(data.fecha);
   } else if (data.fechaReporte) {
      el('fallaFecha').value = formatDateForInput(data.fechaReporte);
   }
   
   // Ubicación: puede venir como ubicacion, nombre, direccion, etc.
   el('fallaUbicacion').value = data.ubicacion || data.nombre || data.direccion || data.casal || '';
   
   // Tipo: puede venir como tipo, agrupacion, categoria, etc.
   el('fallaTipo').value = data.tipo || data.agrupacion || data.categoria || '';
   
   // Descripción
   el('fallaDescripcion').value = data.descripcion || data.detalles || '';
   
   // Actualizar side panel
   const side = document.getElementById('sideSummary');
   const descText = data.descripcion || data.detalles || '';
   if (side) side.textContent = descText.slice(0, 140) + (descText && descText.length > 140 ? '...' : '');
   
   console.log('Formulario poblado con datos:', data);
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

function saveFalla() {
   const payload = {
      id: document.getElementById('fallaId').value,
      fecha: document.getElementById('fallaFecha').value,
      ubicacion: document.getElementById('fallaUbicacion').value,
      tipo: document.getElementById('fallaTipo').value,
      descripcion: document.getElementById('fallaDescripcion').value,
   };

   // Si tiene id => actualizar (PUT), si no => crear (POST)
   const id = payload.id;
   const url = id ? (window._recurso + '/' + id) : window._recurso;
   const method = id ? 'put' : 'post';

   if (window.api && window.api.saveFalla) {
      window.api.saveFalla(payload).then(json => {
         alert('Cambios guardados');
         const side = document.getElementById('sideSummary'); if(side) side.textContent = (json.descripcion||payload.descripcion||'').slice(0,140) + ((json.descripcion||payload.descripcion) && (json.descripcion||payload.descripcion).length>140? '...':'');
         setEditMode(false);
         if (json.id) document.getElementById('fallaId').value = json.id;
      }).catch(err => {
         console.error('Error guardando:', err);
         alert('Error al guardar. Ver consola para más detalles.');
      });
   } else {
      console.warn('API bridge no disponible - cambios no persistidos');
      alert('Cambios guardados localmente (simulado).');
      setEditMode(false);
   }
}

// Exportar para pruebas (opcional)
window._falla = { loadFallaById, populateForm, saveFalla };

// ------------------ Edit mode helpers ------------------
function setEditMode(enabled){
   const fields = ['fallaFecha','fallaUbicacion','fallaTipo','fallaDescripcion'];
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

