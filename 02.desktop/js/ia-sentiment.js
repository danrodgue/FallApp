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
  if (inputFallaId) {
    if (storedFallaId && storedFallaId.trim() !== '') {
      // Prefijar y bloquear el campo: el casal solo ve su propia falla
      inputFallaId.value = storedFallaId;
      inputFallaId.disabled = true;

      const label = document.querySelector('label[for=\"sentiment-falla-id\"]');
      if (label) {
        label.textContent = `ID de tu falla (asociada a esta cuenta): ${storedFallaId}`;
      }
    }
  }

  if (btnLoad) {
    btnLoad.addEventListener('click', () => {
      // Si hay idFalla asociado en sesión, úsalo siempre
      const fallaId = storedFallaId && storedFallaId.trim() !== ''
        ? storedFallaId.trim()
        : (inputFallaId ? inputFallaId.value.trim() : '');

      if (!fallaId) {
        showSentimentMessage('No se ha podido determinar tu falla. Inicia sesión de nuevo.', 'warning');
        return;
      }

      loadSentimentForFalla(fallaId);
    });
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

  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Error ${response.status}`);
    }
    const json = await response.json();
    const datos = json.datos || json;

    renderSentiment(datos);
  } catch (err) {
    console.error('Error cargando sentimiento:', err);
    showSentimentMessage('No se pudo cargar el sentimiento para esta falla.', 'error');
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

