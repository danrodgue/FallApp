// Lógica cliente para pantalla de detalles/edición de falla (restaurado desde screen2.js)
const nf = require('node-fetch');
document.addEventListener('DOMContentLoaded', function () {
   const params = new URLSearchParams(window.location.search);
   const id = params.get('id') || '';
   // Base del backend (Spring Boot): ajusta puerto si es necesario
   window._recurso = window._recurso || 'http://127.0.0.1:8080/api/fallas';

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
   if (deleteBtn) deleteBtn.addEventListener('click', function(){ if(confirm('¿Eliminar esta falla?')){ const idToDel = document.getElementById('fallaId').value; if(idToDel){ nf(window._recurso + '/' + idToDel, { method: 'delete' }).then(()=>{ alert('Falla eliminada'); window.location.href='events.html'; }).catch(()=>{ alert('Error al eliminar'); }); } else { alert('No hay id para eliminar'); } } });
});

function loadFallaById(id) {
   // Si no hay id, rellenamos con un ejemplo (modo offline/desarrollo)
   if (!id) {
      const ejemplo = {
         id: 'sample-123',
         fecha: new Date().toISOString().slice(0,16),
         ubicacion: 'Calle Falsa 123',
         tipo: 'Caída',
         descripcion: 'El usuario se tropezó con un bordillo y sufrió una caída leve.',
         estado: 'abierta',
         reportado_por: 'Juan Pérez'
      };
      populateForm(ejemplo);
      return;
   }

   // Petición al backend (usa node-fetch en renderer, como en librosexpress)
   nf(window._recurso + '/' + id)
      .then(res => {
         if (!res.ok) throw new Error('No encontrado');
         return res.json();
      })
      .then(json => populateForm(json))
      .catch(err => {
         console.warn('No se pudo cargar desde backend, usando stub. ', err);
         const ejemplo = {
            id: id,
            fecha: new Date().toISOString().slice(0,16),
            ubicacion: 'Calle Falsa 123',
            tipo: 'Caída',
            descripcion: 'El usuario se tropezó con un bordillo y sufrió una caída leve.',
            estado: 'abierta',
            reportado_por: 'Juan Pérez'
         };
         populateForm(ejemplo);
      });
}

function populateForm(data) {
   const el = (id) => document.getElementById(id);
   if (!el('fallaId')) return; // formulario no presente
   el('fallaId').value = data.id || '';
   if (data.fecha) el('fallaFecha').value = data.fecha;
   el('fallaUbicacion').value = data.ubicacion || '';
   el('fallaTipo').value = data.tipo || '';
   el('fallaDescripcion').value = data.descripcion || '';
   el('fallaEstado').value = data.estado || 'abierta';
   el('fallaReportadoPor').value = data.reportado_por || '';
   // Actualizar side panel
   const sc = document.getElementById('statusChip'); if(sc) sc.textContent = (data.estado || 'abierta');
   const side = document.getElementById('sideSummary'); if(side) side.textContent = (data.descripcion||'').slice(0,140) + (data.descripcion && data.descripcion.length>140? '...':'' );
}

function saveFalla() {
   const payload = {
      id: document.getElementById('fallaId').value,
      fecha: document.getElementById('fallaFecha').value,
      ubicacion: document.getElementById('fallaUbicacion').value,
      tipo: document.getElementById('fallaTipo').value,
      descripcion: document.getElementById('fallaDescripcion').value,
      estado: document.getElementById('fallaEstado').value,
      reportado_por: document.getElementById('fallaReportadoPor').value
   };

   // Si tiene id => actualizar (PUT), si no => crear (POST)
   const id = payload.id;
   const url = id ? (window._recurso + '/' + id) : window._recurso;
   const method = id ? 'put' : 'post';

   nf(url, {
      method: method,
      body: JSON.stringify(payload),
      headers: { 'Content-Type': 'application/json' }
   }).then(res => {
      if (!res.ok) throw new Error('Error en guardado');
      return res.json();
   }).then(json => {
      alert('Cambios guardados');
      // actualizar side
      const sc = document.getElementById('statusChip'); if(sc) sc.textContent = json.estado || payload.estado;
      const side = document.getElementById('sideSummary'); if(side) side.textContent = (json.descripcion||payload.descripcion||'').slice(0,140) + ((json.descripcion||payload.descripcion) && (json.descripcion||payload.descripcion).length>140? '...':'');
      // salir de modo edición
      setEditMode(false);
      // si era creación, rellenar el id devuelto
      if (json.id) document.getElementById('fallaId').value = json.id;
   }).catch(err=>{
      console.error('Error guardando:',err);
      alert('Error al guardar. Ver consola para más detalles.');
   });
}

// Exportar para pruebas (opcional)
window._falla = { loadFallaById, populateForm, saveFalla };

// ------------------ Edit mode helpers ------------------
function setEditMode(enabled){
   const fields = ['fallaFecha','fallaUbicacion','fallaTipo','fallaDescripcion','fallaEstado','fallaReportadoPor'];
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

