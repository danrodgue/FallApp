// Use window.fetch when available (renderer), otherwise fall back to node-fetch
let nf;
try { nf = (typeof window !== 'undefined' && window.fetch) ? window.fetch.bind(window) : require('node-fetch'); } catch(e) { nf = (typeof window !== 'undefined' && window.fetch) ? window.fetch.bind(window) : null; }

// Externalized events UI logic. Tries backend, falls back to localStorage.
const STORAGE_KEY = 'fallapp_events_v1';
window._eventsResource = window._eventsResource || 'http://127.0.0.1:8080/api/events';

let events = [];
let eventsEl, emptyEl, modal, form, inputs;

function escapeHtml(s){ return String(s||'').replace(/[&<>\"]/g, c=>({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c])); }

function saveLocal(){ try{ localStorage.setItem(STORAGE_KEY, JSON.stringify(events)); }catch(e){} }
function loadLocal(){ try{ const s = localStorage.getItem(STORAGE_KEY); return s? JSON.parse(s): [] }catch(e){ return [] } }

// Render list
function renderList(filter=''){
  if(!eventsEl) return;
  eventsEl.innerHTML = '';
  const q = (filter||'').trim().toLowerCase();
  const filtered = events.filter(ev => {
    if(!q) return true;
    return (ev.name||'').toLowerCase().includes(q) || (ev.place||'').toLowerCase().includes(q) || (ev.creator||'').toLowerCase().includes(q);
  });

  if(filtered.length===0){ if(emptyEl) emptyEl.style.display='block'; return } else { if(emptyEl) emptyEl.style.display='none' }

  filtered.sort((a,b)=> new Date(a.date + ' ' + (a.time||'00:00')) - new Date(b.date + ' ' + (b.time||'00:00')));

  for(const ev of filtered){
    const card = document.createElement('div'); card.className='event-card';
    const left = document.createElement('div');
    const title = document.createElement('strong'); title.textContent = ev.name || '(sin nombre)';
    const meta = document.createElement('div'); meta.className='event-meta';
    meta.innerHTML = `<span class="meta-chip"><strong>Creador:</strong> ${escapeHtml(ev.creator||'')}</span><span class="meta-chip"><strong>Fecha:</strong> ${ev.date||''} ${ev.time||''}</span><span class="meta-chip"><strong>Lugar:</strong> ${escapeHtml(ev.place||'')}</span>`;
    const desc = document.createElement('div'); desc.className='desc'; desc.textContent = ev.description || '';

    left.appendChild(title);
    left.appendChild(meta);
    left.appendChild(desc);

    const actions = document.createElement('div'); actions.className='event-actions';
    const btnView = document.createElement('button'); btnView.className='btn'; btnView.textContent='Ver'; btnView.addEventListener('click', ()=>openView(ev.id));
    const btnEdit = document.createElement('button'); btnEdit.className='btn'; btnEdit.textContent='Editar'; btnEdit.addEventListener('click', ()=>openEdit(ev.id));
    const btnDel = document.createElement('button'); btnDel.className='btn btn-danger'; btnDel.textContent='Eliminar'; btnDel.addEventListener('click', ()=>deleteEvent(ev.id));

    actions.appendChild(btnView); actions.appendChild(btnEdit); actions.appendChild(btnDel);

    card.appendChild(left); card.appendChild(actions);
    eventsEl.appendChild(card);
  }
}

// Modal helpers
function openModal(){ if(modal) modal.style.display='flex'; }
function closeModal(){ if(modal) modal.style.display='none'; }

function generateId(){ return 'ev_' + Math.random().toString(36).slice(2,9); }

function currentSearchValue(){ const s = document.getElementById('search'); return (s && s.value) ? s.value : ''; }

// API wrappers (use node-fetch to follow librosexpress pattern)
async function apiFetchEvents(){
  try{
    const res = await nf(window._eventsResource);
    if(!res.ok) throw new Error('No backend');
    const json = await res.json();
    return json;
  }catch(e){ throw e }
}

async function apiCreateEvent(payload){
  const res = await nf(window._eventsResource, { method: 'post', body: JSON.stringify(payload), headers: {'Content-Type':'application/json'} });
  if(!res.ok) throw new Error('create failed');
  return await res.json();
}
async function apiUpdateEvent(payload){
  const res = await nf(window._eventsResource + '/' + payload.id, { method: 'put', body: JSON.stringify(payload), headers: {'Content-Type':'application/json'} });
  if(!res.ok) throw new Error('update failed');
  return await res.json();
}
async function apiDeleteEvent(id){
  const res = await nf(window._eventsResource + '/' + id, { method: 'delete' });
  if(!res.ok) throw new Error('delete failed');
  return true;
}

// UI actions
function openNew(){
  openModal();
  const title = document.getElementById('modal-title'); if(title) title.textContent='Nuevo evento';
  form.reset(); if(inputs.id) inputs.id.value='';
  const current = localStorage.getItem('fallapp_user') || '';
  if(inputs.creator) inputs.creator.value = current;
}

function openEdit(id){
  const ev = events.find(x=>x.id===id); if(!ev) return alert('Evento no encontrado');
  openModal(); const title = document.getElementById('modal-title'); if(title) title.textContent='Editar evento';
  if(inputs.id) inputs.id.value = ev.id; if(inputs.name) inputs.name.value=ev.name||''; if(inputs.date) inputs.date.value=ev.date||''; if(inputs.time) inputs.time.value=ev.time||''; if(inputs.place) inputs.place.value=ev.place||''; if(inputs.description) inputs.description.value=ev.description||'';
  if(inputs.creator) inputs.creator.value = ev.creator || localStorage.getItem('fallapp_user') || '';
}

function openView(id){
  const ev = events.find(x=>x.id===id); if(!ev) return alert('Evento no encontrado');
  const text = `Nombre: ${ev.name}\nCreador: ${ev.creator}\nFecha: ${ev.date} ${ev.time||''}\nLugar: ${ev.place}\n\nDescripción:\n${ev.description||''}`;
  alert(text);
}

async function saveFromForm(){
  const id = inputs.id.value;
  const currentUser = localStorage.getItem('fallapp_user') || inputs.creator.value.trim();
  const payload = { id: id || generateId(), name: inputs.name.value.trim(), creator: currentUser, date: inputs.date.value, time: inputs.time.value, place: inputs.place.value.trim(), description: inputs.description.value.trim() };

  if(id){
    // Update: try backend then local
    try{
      await apiUpdateEvent(payload);
      const idx = events.findIndex(x=>x.id===id);
      if(idx>=0) events[idx]=payload;
      saveLocal(); renderList(currentSearchValue() || '');
    }catch(e){
      // fallback local
      const idx = events.findIndex(x=>x.id===id);
          if(idx>=0){ events[idx]=payload; saveLocal(); renderList(currentSearchValue() || ''); }
    }
  } else {
    // Create
    try{
      const created = await apiCreateEvent(payload);
      // use created if backend returned an id
      if(created && created.id) payload.id = created.id;
      events.push(payload); saveLocal(); renderList(currentSearchValue() || '');
    }catch(e){
      events.push(payload); saveLocal(); renderList(currentSearchValue() || '');
    }
  }
  closeModal();
}

async function deleteEvent(id){
  if(!confirm('¿Eliminar este evento?')) return;
  try{
    await apiDeleteEvent(id);
    events = events.filter(x=>x.id!==id); saveLocal(); renderList(currentSearchValue() || '');
  }catch(e){
    // fallback local
    events = events.filter(x=>x.id!==id); saveLocal(); renderList(currentSearchValue() || '');
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
    creator: document.getElementById('creator'),
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
    const list = await apiFetchEvents();
    if(Array.isArray(list) && list.length>=0){ events = list; saveLocal(); }
  }catch(e){
    events = loadLocal();
  }

  renderList();
}

init();
