// Externalized events UI logic. Uses preload API (main process) for HTTP.
const STORAGE_KEY = 'fallapp_events_v1';

let events = [];
let eventsEl, emptyEl, modal, form, inputs;

function escapeHtml(s){ return String(s||'').replace(/[&<>\"]/g, c=>({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c])); }

function saveLocal(){ try{ localStorage.setItem(STORAGE_KEY, JSON.stringify(events)); }catch(e){} }
function loadLocal(){ try{ const s = localStorage.getItem(STORAGE_KEY); return s? JSON.parse(s): [] }catch(e){ return [] } }

// ============================================
// UTILIDADES DE MENSAJES
// ============================================

function showNotification(message, type = 'info') {
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

// ============================================
// VALIDACIONES
// ============================================

function validateEventData(data) {
  const errors = [];
  
  if (!data.nombre || data.nombre.trim().length === 0) {
    errors.push('El nombre del evento es obligatorio');
  }
  
  if (!data.tipo || data.tipo.trim().length === 0) {
    errors.push('El tipo de evento es obligatorio');
  }
  
  if (!data.date || data.date.trim().length === 0) {
    errors.push('La fecha del evento es obligatoria');
  }
  
  if (!data.ubicacion || data.ubicacion.trim().length === 0) {
    errors.push('El lugar del evento es obligatorio');
  }
  
  return errors;
}

// Render list
function renderList(filter=''){
  if(!eventsEl) return;
  eventsEl.innerHTML = '';
  const q = (filter||'').trim().toLowerCase();
  const filtered = events.filter(ev => {
    if(!q) return true;
    const nombre = (ev.nombre || ev.name || '').toLowerCase();
    const ubicacion = (ev.ubicacion || ev.place || '').toLowerCase();
    return nombre.includes(q) || ubicacion.includes(q);
  });

  if(filtered.length===0){ if(emptyEl) emptyEl.style.display='block'; return } else { if(emptyEl) emptyEl.style.display='none' }

  // Ordenar por fecha del evento
  filtered.sort((a, b) => {
    const fechaA = new Date(a.fecha_evento || a.fechaEvento || (a.date + ' ' + (a.time||'00:00')));
    const fechaB = new Date(b.fecha_evento || b.fechaEvento || (b.date + ' ' + (b.time||'00:00')));
    return fechaA - fechaB;
  });

  for(const ev of filtered){
    const card = document.createElement('div'); 
    card.className='event-card';
    
    // Imagen del evento
    const eventImage = document.createElement('div'); 
    eventImage.className='event-image';
    const img = document.createElement('img');
    
    // Si el evento tiene imagen, usar la URL del servidor, si no usar logo por defecto
    if (ev.imagen_nombre || ev.imagenNombre) {
      const eventoId = ev.id_evento || ev.idEvento || ev.id;
      img.src = `http://35.180.21.42:8080/api/eventos/${eventoId}/imagen`;
    } else {
      img.src = '../renderer/src/img/fallap_logo.png';
    }
    
    img.alt = ev.nombre || ev.name || 'Evento';
    img.onerror = function() { this.style.display='none'; this.parentElement.classList.add('no-image'); };
    eventImage.appendChild(img);
    
    const cardContent = document.createElement('div'); 
    cardContent.className='event-card-content';
    const left = document.createElement('div');
    
    const title = document.createElement('strong'); 
    title.textContent = ev.nombre || ev.name || '(sin nombre)';
    
    const meta = document.createElement('div'); 
    meta.className='event-meta';
    
    // Formatear fecha desde fecha_evento
    let fechaFormato = '';
    if (ev.fecha_evento) {
      const fecha = new Date(ev.fecha_evento);
      fechaFormato = fecha.toLocaleDateString('es-ES') + ' ' + fecha.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
    } else if (ev.fechaEvento) {
      const fecha = new Date(ev.fechaEvento);
      fechaFormato = fecha.toLocaleDateString('es-ES') + ' ' + fecha.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
    } else if (ev.date) {
      fechaFormato = ev.date + ' ' + (ev.time || '');
    }
    
    const ubicacion = ev.ubicacion || ev.place || '';
    const tipo = ev.tipo || 'otro';
    
    meta.innerHTML = `<span class="meta-chip"><strong>Tipo:</strong> ${escapeHtml(tipo)}</span><span class="meta-chip"><strong>Fecha:</strong> ${fechaFormato}</span><span class="meta-chip"><strong>Lugar:</strong> ${escapeHtml(ubicacion)}</span>`;
    
    const desc = document.createElement('div'); 
    desc.className='desc'; 
    desc.textContent = ev.descripcion || ev.description || '';

    left.appendChild(title);
    left.appendChild(meta);
    left.appendChild(desc);

    const actions = document.createElement('div'); 
    actions.className='event-actions';
    const btnView = document.createElement('button'); 
    btnView.className='btn'; 
    btnView.textContent='Ver'; 
    btnView.addEventListener('click', ()=>openView(ev.id_evento || ev.idEvento || ev.id));
    
    const btnEdit = document.createElement('button'); 
    btnEdit.className='btn'; 
    btnEdit.textContent='Editar'; 
    btnEdit.addEventListener('click', ()=>openEdit(ev.id_evento || ev.idEvento || ev.id));
    
    const btnDel = document.createElement('button'); 
    btnDel.className='btn btn-danger'; 
    btnDel.textContent='Eliminar'; 
    btnDel.addEventListener('click', ()=>deleteEvent(ev.id_evento || ev.idEvento || ev.id));

    actions.appendChild(btnView); 
    actions.appendChild(btnEdit); 
    actions.appendChild(btnDel);

    cardContent.appendChild(left); 
    cardContent.appendChild(actions);
    
    card.appendChild(eventImage);
    card.appendChild(cardContent);
    eventsEl.appendChild(card);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  const input = document.getElementById('photo');
  const preview = document.getElementById('photo-preview');

  if (!input) {
    console.error('No se encontró el input de foto');
    return;
  }

  input.addEventListener('change', () => {
    const file = input.files[0];

    if (!file) return;

    preview.src = URL.createObjectURL(file);
    preview.style.display = 'block';
  });
});
// Modal helpers
function openModal(){ if(modal) modal.style.display='flex'; }
function closeModal(){ if(modal) modal.style.display='none'; }

function generateId(){ return 'ev_' + Math.random().toString(36).slice(2,9); }

function currentSearchValue(){ const s = document.getElementById('search'); return (s && s.value) ? s.value : ''; }

// API wrappers via preload/contextBridge (main process will call the HTTP API)
async function apiFetchEvents(){
  if (window.api && window.api.getEvents) return await window.api.getEvents();
  throw new Error('API bridge not available');
}

async function apiCreateEvent(payload){
  if (window.api && window.api.createEvent) return await window.api.createEvent(payload);
  throw new Error('API bridge not available');
}

async function apiUpdateEvent(payload){
  if (window.api && window.api.updateEvent) return await window.api.updateEvent(payload);
  throw new Error('API bridge not available');
}

async function apiDeleteEvent(id){
  if (window.api && window.api.deleteEvent) return await window.api.deleteEvent(id);
  throw new Error('API bridge not available');
}

// Función para subir imagen de evento
// Función para obtener URL de imagen de evento
function obtenerUrlImagenEvento(eventoId) {
  return `http://35.180.21.42:8080/api/eventos/${eventoId}/imagen`;
}

// UI actions
function openNew(){
  openModal();
  const title = document.getElementById('modal-title'); if(title) title.textContent='Nuevo evento';
  form.reset(); if(inputs.id) inputs.id.value='';
}

function openEdit(id){
  const ev = events.find(x => x.idEvento === parseInt(id) || x.id === id); 
  if(!ev) return showNotification('Evento no encontrado', 'error');
  
  openModal(); 
  const title = document.getElementById('modal-title'); 
  if(title) title.textContent='Editar evento';
  
  if(inputs.id) inputs.id.value = ev.idEvento || ev.id; 
  if(inputs.name) inputs.name.value = ev.nombre || ev.name || ''; 
  if(inputs.tipo) inputs.tipo.value = ev.tipo || 'otro';
  
  // Extraer fecha y hora de fecha_evento
  if (ev.fecha_evento) {
    const fecha = new Date(ev.fecha_evento);
    if(inputs.date) inputs.date.value = fecha.toISOString().split('T')[0];
    if(inputs.time) inputs.time.value = fecha.toTimeString().slice(0, 5);
  } else if (ev.fechaEvento) {
    const fecha = new Date(ev.fechaEvento);
    if(inputs.date) inputs.date.value = fecha.toISOString().split('T')[0];
    if(inputs.time) inputs.time.value = fecha.toTimeString().slice(0, 5);
  } else if (ev.date) {
    if(inputs.date) inputs.date.value = ev.date || '';
    if(inputs.time) inputs.time.value = ev.time || '';
  }
  
  if(inputs.place) inputs.place.value = ev.ubicacion || ev.place || ''; 
  if(inputs.description) inputs.description.value = ev.descripcion || ev.description || '';
}

function openView(id){
  const ev = events.find(x => x.idEvento === parseInt(id) || x.id === id); 
  if(!ev) return showNotification('Evento no encontrado', 'error');
  
  let fechaFormato = '';
  if (ev.fecha_evento) {
    const fecha = new Date(ev.fecha_evento);
    fechaFormato = fecha.toLocaleDateString('es-ES') + ' ' + fecha.toLocaleTimeString('es-ES');
  } else if (ev.fechaEvento) {
    const fecha = new Date(ev.fechaEvento);
    fechaFormato = fecha.toLocaleDateString('es-ES') + ' ' + fecha.toLocaleTimeString('es-ES');
  } else if (ev.date) {
    fechaFormato = ev.date + ' ' + (ev.time || '');
  }
  
  const text = `Nombre: ${ev.nombre || ev.name}\nTipo: ${ev.tipo || 'Otro'}\nFecha: ${fechaFormato}\nLugar: ${ev.ubicacion || ev.place}\n\nDescripción:\n${ev.descripcion || ev.description || ''}`;
  alert(text);
}

async function saveFromForm(){
  const id = inputs.id.value;
  const eventData = {
    nombre: inputs.name.value.trim(),
    tipo: inputs.tipo.value,
    date: inputs.date.value,
    time: inputs.time.value,
    ubicacion: inputs.place.value.trim(),
    descripcion: inputs.description.value.trim()
  };
  
  // Validar datos
  const validationErrors = validateEventData(eventData);
  if (validationErrors.length > 0) {
    showNotification(validationErrors.join('\n'), 'error');
    return;
  }

  if (!eventData.tipo) {
    showNotification('Debe seleccionar un tipo de evento', 'error');
    return;
  }

  // Obtener la falla del usuario
  const idFalla = localStorage.getItem('fallapp_user_idFalla');
  if (!idFalla) {
    showNotification('No tienes asignada una falla. No se puede crear el evento.', 'error');
    return;
  }

  // Obtener ID del usuario para creado_por
  const creadoPor = localStorage.getItem('fallapp_user_id');
  if (!creadoPor) {
    showNotification('No se puede identificar tu usuario. Por favor, inicia sesión de nuevo.', 'error');
    return;
  }

  // Crear fecha_evento combinando fecha y hora
  const fecha_evento = new Date(`${eventData.date}T${eventData.time || '00:00'}`).toISOString();

  // Preparar payload para el backend - mapear a los campos exactos de la tabla
  const payload = { 
    nombre: eventData.nombre,
    descripcion: eventData.descripcion,
    fecha_evento: fecha_evento,
    ubicacion: eventData.ubicacion,
    id_falla: parseInt(idFalla),
    tipo: eventData.tipo,
    creado_por: parseInt(creadoPor)
  };

  if(id){
    // Update
    try{
      payload.id_evento = id;
      await actualizarEvento(id, payload);
      
      // Actualizar en la lista local
      const idx = events.findIndex(x => x.id_evento === parseInt(id) || x.idEvento === parseInt(id) || x.id === id);
      if(idx >= 0) {
        events[idx] = { ...events[idx], ...payload };
      }
      saveLocal(); 
      renderList(currentSearchValue() || '');
      showNotification('Evento actualizado correctamente', 'success');
    }catch(e){
      console.error('Error updating event:', e);
      showNotification(`Error al actualizar evento: ${e.message}`, 'error');
    }
  } else {
    // Create
    try{
      const created = await crearEvento(payload);
      // Usar la respuesta del backend si tiene datos
      const nuevoEvento = created.datos || created;
      events.push(nuevoEvento); 
      saveLocal(); 
      renderList(currentSearchValue() || '');
      
      // Subir imagen si se seleccionó
      const photoInput = document.getElementById('photo');
      if (photoInput && photoInput.files.length > 0) {
        try {
          await uploadEventImage(nuevoEvento.id_evento || nuevoEvento.idEvento, photoInput.files[0]);
          showNotification('Evento creado y imagen subida correctamente', 'success');
        } catch (e) {
          console.warn('Error al subir imagen:', e);
          showNotification('Evento creado pero hubo error al subir la imagen', 'warning');
        }
      } else {
        showNotification('Evento creado correctamente', 'success');
      }
    }catch(e){
      console.error('Error creating event:', e);
      showNotification(`Error al crear evento: ${e.message}`, 'error');
    }
  }
  closeModal();
}

async function deleteEvent(id){
  if(!confirm('¿Eliminar este evento?')) return;
  try{
    await eliminarEvento(id);
    events = events.filter(x => x.id_evento !== parseInt(id) && x.idEvento !== parseInt(id) && x.id !== id); 
    saveLocal(); 
    renderList(currentSearchValue() || '');
    showNotification('Evento eliminado correctamente', 'success');
  }catch(e){
    console.error('Error deleting event:', e);
    showNotification(`Error al eliminar evento: ${e.message}`, 'error');
  }
}

// Subir imagen de evento
async function uploadEventImage(eventoId, archivo) {
  if (!archivo) return;
  
  try {
    const resultado = await subirImagenEvento(eventoId, archivo);
    console.log('Imagen de evento subida:', resultado);
    return resultado;
  } catch (e) {
    console.error('Error subiendo imagen:', e);
    throw e;
  }
}

// Init
async function init(){
  // Initialize DOM references (safer if script loads early)
  eventsEl = document.getElementById('events');
  emptyEl = document.getElementById('empty');
  modal = document.getElementById('modal');
  form = document.getElementById('event-form');
  inputs = {
    id: document.getElementById('event-id'),
    name: document.getElementById('name'),
    tipo: document.getElementById('tipo'),
    date: document.getElementById('date'),
    time: document.getElementById('time'),
    place: document.getElementById('place'),
    description: document.getElementById('description')
  };

  // Wire buttons (defensive checks for older runtimes)
  const btnNew = document.getElementById('btn-new'); if(btnNew) btnNew.addEventListener('click', openNew);
  const btnCancel = document.getElementById('btn-cancel'); if(btnCancel) btnCancel.addEventListener('click', closeModal);
  const modalClose = document.getElementById('modal-close'); if(modalClose) modalClose.addEventListener('click', closeModal);
  const modalEl = document.getElementById('modal'); if(modalEl) modalEl.addEventListener('click', function(ev){ if(ev.target && ev.target.id==='modal') closeModal(); });
  const searchEl = document.getElementById('search'); if(searchEl) searchEl.addEventListener('input', function(e){ renderList(e.target.value); });
  if(form) form.addEventListener('submit', function(e){ e.preventDefault(); saveFromForm(); });
  const backBtn = document.getElementById('events-back'); if(backBtn) backBtn.addEventListener('click', function(){ if(window.history.length>1) window.history.back(); else window.location.href='home.html'; });

  // Load from backend if possible
  try{
    const idFalla = localStorage.getItem('fallapp_user_idFalla');
    
    if (!idFalla) {
      showNotification('No tienes asignada una falla. Por favor, contacta al administrador.', 'warning');
      events = [];
    } else {
      // Obtener eventos de la falla asignada al usuario
      const response = await obtenerEventosPorFalla(idFalla);
      
      // El backend retorna un PagedResponse con content, así que extraemos el array
      if (response && response.content && Array.isArray(response.content)) {
        events = response.content;
      } else if (response && response.datos && Array.isArray(response.datos.content)) {
        events = response.datos.content;
      } else if (Array.isArray(response)) {
        events = response;
      } else {
        events = [];
      }
      
      saveLocal();
    }
  }catch(e){
    console.warn('Could not fetch events from backend, loading from cache:', e);
    showNotification(`Error cargando eventos: ${e.message}`, 'error');
    events = loadLocal();
  }

  renderList();
}

// Configurar botones del header
document.addEventListener('DOMContentLoaded', () => {
  const logoutBtn = document.getElementById('logout');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      try {
        localStorage.removeItem('fallapp_user');
      } catch (e) {
        console.error('Error removing user:', e);
      }
      window.location.href = '../js/index.html';
    });
  }
});

// Agregar animación de deslizamiento
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

init();
