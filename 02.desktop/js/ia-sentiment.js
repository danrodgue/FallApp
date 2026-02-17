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
    // Si no hay falla asociada, mostrar mensaje claro
    showSentimentMessage(
      'No se ha podido determinar tu falla (idFalla vacío). Inicia sesión de nuevo con un usuario de casal.',
      'warning'
    );
  }
});

function getApiBase() {
  // Reutiliza la misma base de API usada en otras pantallas
  return window._apiBase || 'http://35.180.21.42:8080/api';
}

async function loadSentimentForFalla(fallaId) {
  const resultContainer = document.getElementById('sentiment-result');
  const emptyLabel = document.getElementById('sentiment-empty');

  if (emptyLabel) emptyLabel.style.display = 'none';
  if (resultContainer) {
    resultContainer.innerHTML = '<div class="muted">Cargando sentimiento...</div>';
  }

  const url = `${getApiBase()}/admin/fallas/${encodeURIComponent(fallaId)}/sentimiento`;

  // Añadir JWT del usuario (casal) para acceder al endpoint /api/admin/**
  const token = localStorage.getItem('fallapp_token');
  if (!token) {
    showSentimentMessage('No hay token de sesión. Inicia sesión de nuevo en el panel.', 'warning');
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
    showSentimentMessage(`No se pudo cargar el sentimiento para esta falla: ${err.message || err}`, 'error');
  }
}

function renderSentiment(data) {
  const resultContainer = document.getElementById('sentiment-result');
  if (!resultContainer) return;

  const sentimientos = data.sentimientos || {};
  const total = data.totalComentarios || 0;

  if (total === 0) {
    resultContainer.innerHTML = '<div class="muted">Esta falla todavía no tiene comentarios analizados.</div>';
    return;
  }

  const positive = sentimientos.positive || 0;
  const neutral = sentimientos.neutral || 0;
  const negative = sentimientos.negative || 0;

  const pct = (value) => total > 0 ? ((value / total) * 100).toFixed(1) : '0.0';

  let alertHtml = '';
  const negativePct = total > 0 ? (negative / total) * 100 : 0;
  if (negativePct >= 30) {
    alertHtml = `
      <div class="muted" style="color:#b91c1c; margin-bottom:8px; font-weight:600;">
        ⚠ Muchos comentarios negativos (${pct(negative)}%). Revisa qué está ocurriendo con esta falla.
      </div>
    `;
  }

  resultContainer.innerHTML = `
    <div class="sentiment-card">
      <h2>Falla #${data.idFalla} - ${data.nombreFalla || ''}</h2>
      <p class="muted">Total comentarios analizados: ${total}</p>
      ${alertHtml}
      <ul class="sentiment-list">
        <li><strong>Positivos</strong>: ${positive} (${pct(positive)}%)</li>
        <li><strong>Neutros</strong>: ${neutral} (${pct(neutral)}%)</li>
        <li><strong>Negativos</strong>: ${negative} (${pct(negative)}%)</li>
      </ul>
    </div>
  `;
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

