// Lógica cliente para pantalla de detalles/edición de falla (restaurado desde screen2.js)
document.addEventListener('DOMContentLoaded', function () {
   const params = new URLSearchParams(window.location.search);
   const id = params.get('id') || 'sample-123';
   // Cargar datos (por ahora stub). En el futuro pedirás al backend/DB.
   loadFallaById(id);

   // Botones: guardar/editar/eliminar
   const saveBtn = document.getElementById('saveBtn');
   const editBtn = document.getElementById('editBtn');
   const deleteBtn = document.getElementById('deleteBtn');

   if (saveBtn) saveBtn.addEventListener('click', saveFalla);
   if (editBtn) editBtn.addEventListener('click', toggleEditMode);
   if (deleteBtn) deleteBtn.addEventListener('click', function(){ if(confirm('¿Eliminar esta falla?')){ /* future: call backend */ alert('Falla eliminada (simulado)'); window.location.href='events.html'; } });
});

function loadFallaById(id) {
   // Stub: datos de ejemplo. Reemplazar por petición real al backend.
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

   // Por ahora simulamos el guardado. En el futuro usarás IPC/Fetch para persistir en DB.
   console.log('Guardar falla (simulado):', payload);
   alert('Cambios guardados (simulado). Conectar con la DB para persistir.');
   // actualizar side
   const sc = document.getElementById('statusChip'); if(sc) sc.textContent = payload.estado;
   const side = document.getElementById('sideSummary'); if(side) side.textContent = (payload.descripcion||'').slice(0,140) + (payload.descripcion && payload.descripcion.length>140? '...':'');
   // salir de modo edición
   setEditMode(false);
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

