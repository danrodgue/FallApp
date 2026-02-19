document.addEventListener('DOMContentLoaded', () => {
  const logoutBtn = document.getElementById('logout');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      try {
        localStorage.removeItem('fallapp_user');
        localStorage.removeItem('fallapp_token');
        localStorage.removeItem('fallapp_user_id');
        localStorage.removeItem('fallapp_user_idFalla');
      } catch (e) {
        console.error('Error removing user:', e);
      }
      window.location.href = '../js/index.html';
    });
  }

  const btnLoad = document.getElementById('btn-sentiment-load');
  const inputFallaId = document.getElementById('sentiment-falla-id');

  // Obtener la falla asociada al usuario logueado (casal)
  const storedFallaId = localStorage.getItem('fallapp_user_idFalla');
  if (storedFallaId && storedFallaId.trim() !== '') {
    window._fallaIdCasal = storedFallaId.trim();
    // Ocultar por completo el campo y botón de búsqueda por ID (no se usa ya)
    if (inputFallaId) {
      inputFallaId.style.display = 'none';
    }
    if (btnLoad) {
      btnLoad.style.display = 'none';
    }
    const label = document.querySelector('label[for=\"sentiment-falla-id\"]');
    if (label) {
      label.style.display = 'none';
    }

    // Cargar automáticamente el sentimiento de la falla del casal
    loadSentimentForFalla(storedFallaId.trim());
  } else {
    const emptyLabel = document.getElementById('sentiment-empty');
    if (emptyLabel) emptyLabel.style.display = 'none';
    showSentimentMessage(
      'No se ha podido cargar la información de tu falla. Inicia sesión de nuevo.',
      'warning'
    );
  }
});

function getApiBase() {
  const configured =
    window._API_URL ||
    window._apiBase ||
    (window._API_BASE ? `${window._API_BASE}/api` : null);

  const fallback = 'http://35.180.21.42:8080/api';
  const base = configured || fallback;
  return base.endsWith('/') ? base.slice(0, -1) : base;
}

async function loadSentimentForFalla(fallaId) {
  const resultContainer = document.getElementById('sentiment-result');
  const emptyLabel = document.getElementById('sentiment-empty');

  if (emptyLabel) emptyLabel.style.display = 'none';
  if (resultContainer) {
    resultContainer.innerHTML = '<div class="muted">Cargando análisis...</div>';
  }

  const url = `${getApiBase()}/admin/fallas/${encodeURIComponent(fallaId)}/sentimiento`;

  // Añadir JWT del usuario (casal) para acceder al endpoint /api/admin/**
  const token = localStorage.getItem('fallapp_token');
  if (!token) {
    showSentimentMessage('No hay sesión activa. Inicia sesión de nuevo.', 'warning');
    return;
  }

  try {
    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Accept': 'application/json'
      }
    });
    if (!response.ok) {
      // Intentar extraer mensaje del backend
      let backendMessage = `Error ${response.status}`;
      try {
        const errorJson = await response.json();
        if (errorJson && errorJson.mensaje) {
          backendMessage = errorJson.mensaje;
        }
      } catch (_) {
        // ignore parse error
      }
      console.error('Error HTTP al cargar sentimiento:', backendMessage);
      showSentimentMessage(backendMessage, 'error');
      return;
    }

    const json = await response.json();
    const datos = json.datos || json;

    renderSentiment(datos);
  } catch (err) {
    console.error('Error cargando sentimiento:', err);
    showSentimentMessage(`No se pudo cargar el análisis. Vuelve a intentarlo más tarde.`, 'error');
  }
}

function renderSentiment(data) {
  const resultContainer = document.getElementById('sentiment-result');
  if (!resultContainer) return;

  const sentimientos = data.sentimientos || {};
  const total = data.totalComentarios || 0;
  const totalFalla = data.totalComentariosFalla || total;
  const pendientes = data.totalPendientes || 0;
  const canReanalizar = !!window._fallaIdCasal;
  const actionHtml = canReanalizar
    ? `<button type="button" id="btn-reanalizar-sentimiento" class="btn-reanalizar">Actualizar análisis</button>`
    : '';
  const statusHtml = canReanalizar
    ? `<span id="reanalizar-status" class="muted reanalizar-status-inline"></span>`
    : '';

  if (total === 0) {
    resultContainer.innerHTML = `
      <div class="sentiment-card">
        <div class="sentiment-card-header">
          <h2>${data.nombreFalla || 'Tu falla'}</h2>
          <span class="falla-subtitle">Comentarios de los usuarios</span>
          ${actionHtml}
          ${statusHtml}
        </div>
        <div class="sentiment-stats-row">
          <span>💬 Total comentarios: <strong>${totalFalla}</strong></span>
          <span>·</span>
          <span>⏳ Por analizar: <strong>${pendientes}</strong></span>
        </div>
        <div class="sentiment-bars sentiment-empty-bars">
          <p class="muted">${totalFalla > 0
            ? pendientes > 0
              ? `Hay ${totalFalla} comentarios. Usa "Actualizar análisis" para procesarlos.`
              : 'No hay comentarios analizados todavía.'
            : 'Tu falla aún no tiene comentarios.'}</p>
        </div>
      </div>
    `;
    if (canReanalizar && window._fallaIdCasal) {
      const btn = document.getElementById('btn-reanalizar-sentimiento');
      if (btn) {
        btn.onclick = () => reanalizarSentimientoPendientes(window._fallaIdCasal);
      }
    }
    loadComentariosFalla(data.idFalla);
    return;
  }

  const positive = sentimientos.positive || 0;
  const neutral = sentimientos.neutral || 0;
  const negative = sentimientos.negative || 0;

  const pct = (value) => total > 0 ? ((value / total) * 100).toFixed(1) : '0.0';
  const pctNum = (v) => total > 0 ? (v / total) * 100 : 0;

  let alertHtml = '';
  const negativePct = pctNum(negative);
  if (negativePct >= 30) {
    alertHtml = `
      <div class="sentiment-alert">
        ⚠ Hay un porcentaje alto de comentarios negativos (${pct(negative)}%). Te recomendamos revisar el feedback.
      </div>
    `;
  }

  resultContainer.innerHTML = `
    <div class="sentiment-card">
      <div class="sentiment-card-header">
        <h2>${data.nombreFalla || 'Tu falla'}</h2>
        <span class="falla-subtitle">Comentarios de los usuarios</span>
        ${actionHtml}
        ${statusHtml}
      </div>
      <div class="sentiment-stats-row">
        <span>📊 Analizados: <strong>${total}</strong></span>
        <span>·</span>
        <span>💬 Total comentarios: <strong>${totalFalla}</strong></span>
        <span>·</span>
        <span>⏳ Por analizar: <strong>${pendientes}</strong></span>
      </div>
      <div class="sentiment-bars">
        <div class="sentiment-bar-item">
          <span class="sentiment-bar-label">Positivos</span>
          <div class="sentiment-bar-track">
            <div class="sentiment-bar-fill positive" style="width:${pctNum(positive)}%"></div>
          </div>
          <span class="sentiment-bar-value">${positive} (${pct(positive)}%)</span>
        </div>
        <div class="sentiment-bar-item">
          <span class="sentiment-bar-label">Neutros</span>
          <div class="sentiment-bar-track">
            <div class="sentiment-bar-fill neutral" style="width:${pctNum(neutral)}%"></div>
          </div>
          <span class="sentiment-bar-value">${neutral} (${pct(neutral)}%)</span>
        </div>
        <div class="sentiment-bar-item">
          <span class="sentiment-bar-label">Negativos</span>
          <div class="sentiment-bar-track">
            <div class="sentiment-bar-fill negative" style="width:${pctNum(negative)}%"></div>
          </div>
          <span class="sentiment-bar-value">${negative} (${pct(negative)}%)</span>
        </div>
      </div>
      ${alertHtml}
    </div>
  `;
  if (canReanalizar && window._fallaIdCasal) {
    const btn = document.getElementById('btn-reanalizar-sentimiento');
    if (btn) {
      btn.onclick = () => reanalizarSentimientoPendientes(window._fallaIdCasal);
    }
  }
  loadComentariosFalla(data.idFalla);
}

async function loadComentariosFalla(fallaId) {
  const container = document.getElementById('sentiment-comments');
  if (!container || !fallaId) return;

  const base = getApiBase();
  const token = localStorage.getItem('fallapp_token');
  const headers = { 'Accept': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  try {
    const res = await fetch(`${base}/comentarios?idFalla=${fallaId}`, { headers });
    const json = await res.json().catch(() => ({}));
    const lista = json.datos || json.content || json || [];
    const comentarios = Array.isArray(lista) ? lista : [];

    if (comentarios.length === 0) {
      container.innerHTML = `
        <div class="sentiment-comments-card">
          <h3>Comentarios de los usuarios</h3>
          <p class="muted">No hay comentarios todavía.</p>
        </div>
      `;
      return;
    }

    const items = comentarios.map(c => {
      const sent = (c.sentimiento || '').toLowerCase();
      const badge = sent === 'positive' ? '<span class="comment-badge positive">Positivo</span>' :
                    sent === 'negative' ? '<span class="comment-badge negative">Negativo</span>' :
                    sent === 'neutral' ? '<span class="comment-badge neutral">Neutro</span>' : '';
      const autor = c.nombreUsuario || 'Usuario';
      const fecha = c.fechaCreacion ? new Date(c.fechaCreacion).toLocaleDateString('es-ES', { day: 'numeric', month: 'short', year: 'numeric' }) : '';
      const texto = String(c.contenido || '').replace(/</g, '&lt;');
      return `
        <div class="comment-item">
          <div class="comment-meta">
            <strong>${autor}</strong> ${badge}
            ${fecha ? `<span class="comment-date">${fecha}</span>` : ''}
          </div>
          <p class="comment-text">${texto}</p>
        </div>
      `;
    }).join('');

    container.innerHTML = `
      <div class="sentiment-comments-card">
        <h3>Comentarios de los usuarios</h3>
        <div class="comment-list">${items}</div>
      </div>
    `;
  } catch (err) {
    container.innerHTML = `
      <div class="sentiment-comments-card">
        <h3>Comentarios de los usuarios</h3>
        <p class="muted">No se pudieron cargar los comentarios.</p>
      </div>
    `;
  }
}

/**
 * Llama al endpoint que reanaliza todos los comentarios con sentimiento NULL
 * y luego refresca la estadística de la falla tras unos segundos.
 */
async function reanalizarSentimientoPendientes(fallaId) {
  const btn = document.getElementById('btn-reanalizar-sentimiento');
  const statusEl = document.getElementById('reanalizar-status');
  const token = localStorage.getItem('fallapp_token');
  if (!token) {
    showSentimentMessage('No hay token de sesión. Inicia sesión de nuevo.', 'warning');
    return;
  }
  const url = `${getApiBase()}/admin/comentarios/reanalizar-sentimiento`;
  if (btn) btn.disabled = true;
  if (statusEl) statusEl.textContent = 'Procesando...';

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    });
    const json = await response.json().catch(() => ({}));
    const datos = json.datos || json;
    const encolados = datos.comentariosEncolados != null ? datos.comentariosEncolados : 0;
    const mensaje = datos.mensaje || (encolados > 0 ? 'Analizando ' + encolados + ' comentarios...' : 'No hay comentarios pendientes.');

    if (statusEl) statusEl.textContent = mensaje;
    if (encolados > 0 && fallaId) {
      loadSentimentForFalla(fallaId);
      if (statusEl) statusEl.textContent = '';
      if (btn) btn.disabled = false;
    } else {
      if (btn) btn.disabled = false;
      if (encolados === 0 && fallaId) loadSentimentForFalla(fallaId);
    }
  } catch (err) {
    console.error('Error reanalizando sentimiento:', err);
    if (statusEl) statusEl.textContent = 'Error: ' + (err.message || err);
    if (btn) btn.disabled = false;
  }
}

function showSentimentMessage(message, type) {
  const resultContainer = document.getElementById('sentiment-result');
  if (!resultContainer) return;

  const color =
    type === 'error' ? '#ef4444' :
    type === 'warning' ? '#f59e0b' :
    '#3b82f6';

  resultContainer.innerHTML = `
    <div style="
      padding: 12px 16px;
      border-radius: 8px;
      color: white;
      background: ${color};
      font-weight: 600;
      max-width: 480px;
    ">
      ${message}
    </div>
  `;
}

