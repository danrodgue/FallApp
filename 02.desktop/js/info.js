// Lógica cliente para pantalla de detalles/edición de falla (restaurado desde screen2.js)
// Usar window.fetch si está disponible en el renderer; si no, intentar require('node-fetch')
let nf = null; // not used in renderer anymore; main process handles HTTP
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
   const id = params.get('id') || '';
   // Base del backend (Spring Boot): ajusta puerto si es necesario
   window._recurso = window._recurso || 'http://localhost/api/fallas';

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

   // Pedir al main process que obtenga la falla
   if (window.api && window.api.getFalla) {
      window.api.getFalla(id).then(json => populateForm(json)).catch(err => {
         console.warn('No se pudo cargar desde backend, usando stub. ', err);
         const ejemplo = { id: id, fecha: new Date().toISOString().slice(0,16), ubicacion: 'Calle Falsa 123', tipo: 'Caída', descripcion: 'El usuario se tropezó con un bordillo y sufrió una caída leve.', estado: 'abierta', reportado_por: 'Juan Pérez' };
         populateForm(ejemplo);
      });
   } else {
      console.warn('API bridge no disponible, usando stub');
      const ejemplo = { id: id, fecha: new Date().toISOString().slice(0,16), ubicacion: 'Calle Falsa 123', tipo: 'Caída', descripcion: 'El usuario se tropezó con un bordillo y sufrió una caída leve.', estado: 'abierta', reportado_por: 'Juan Pérez' };
      populateForm(ejemplo);
   }
}

function populateForm(data) {
   const el = (id) => document.getElementById(id);
   if (!el('fallaId')) return; // formulario no presente
   el('fallaId').value = data.id || '';
   if (data.fecha) el('fallaFecha').value = data.fecha;
   el('fallaUbicacion').value = data.ubicacion || '';
   el('fallaTipo').value = data.tipo || '';
   el('fallaDescripcion').value = data.descripcion || '';
   // Actualizar side panel
   const side = document.getElementById('sideSummary');
   if (side) side.textContent = (data.descripcion || '').slice(0, 140) + (data.descripcion && data.descripcion.length > 140 ? '...' : '');

   // Imagen (si viene desde backend)
   const img = document.getElementById('fallaImagen');
   const placeholder = document.getElementById('fallaImagePlaceholder');
   const imageUrl = normalizeImageUrl(data);
   if (img) {
      if (imageUrl) {
         img.src = imageUrl;
         img.style.display = 'block';
         if (placeholder) placeholder.style.display = 'none';
      } else {
         img.style.display = 'none';
         if (placeholder) placeholder.style.display = 'flex';
      }
   }
}

function normalizeImageUrl(data) {
   if (!data) return '';

   // Acepta varios nombres posibles
   let candidate = data.imagen || data.imagenUrl || data.boceto || data.mapImage || '';

   // Si viene como objeto (ej. { url, base64 })
   if (candidate && typeof candidate === 'object') {
      candidate = candidate.url || candidate.base64 || '';
   }

   if (!candidate || typeof candidate !== 'string') return '';

   // Si ya es un data URL o URL válida
   if (/^data:image\//i.test(candidate)) return candidate;
   if (/^https?:\/\//i.test(candidate)) return candidate;
   if (candidate.startsWith('/') || candidate.startsWith('../')) return candidate;

   // Asumir base64 puro (png por defecto)
   const base64Like = /^[A-Za-z0-9+/]+={0,2}$/;
   if (base64Like.test(candidate)) {
      return `data:image/png;base64,${candidate}`;
   }

   return candidate;
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

