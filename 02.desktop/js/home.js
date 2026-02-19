let cachedUserData = null;

document.addEventListener('DOMContentLoaded', async () => {
  // Cargar datos del usuario al iniciar
  await loadUserDataForModal();
  await loadProfileAvatarImage();

  // Configurar botones de logout
  const logoutBtn = document.getElementById('logout');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', handleLogout);
  }

  // Configurar modal de perfil
  const profileBtn = document.getElementById('profileBtn');
  const profileModal = document.getElementById('profileModal');
  const closeProfileBtn = document.getElementById('closeProfileBtn');
  const closeProfileBtnFooter = document.getElementById('closeProfileBtnFooter');
  const profileModalOverlay = document.querySelector('.profile-modal-overlay');

  if (profileBtn) {
    profileBtn.addEventListener('click', async () => {
      // Recargar datos frescos cuando se abre el modal
      await loadUserDataForModal();
      await loadProfileAvatarImage();
      profileModal.classList.add('active');
      document.body.style.overflow = 'hidden';
    });
  }

  if (closeProfileBtn) {
    closeProfileBtn.addEventListener('click', closeModal);
  }

  if (closeProfileBtnFooter) {
    closeProfileBtnFooter.addEventListener('click', closeModal);
  }

  if (profileModalOverlay) {
    profileModalOverlay.addEventListener('click', closeModal);
  }

  function closeModal() {
    profileModal.classList.remove('active');
    document.body.style.overflow = 'auto';
  }
});

// Cargar datos del usuario para el modal directamente desde la API
async function loadUserDataForModal() {
  try {
    const idUsuario = localStorage.getItem('fallapp_user_id');
    
    if (!idUsuario) {
      console.warn('No user ID in localStorage');
      return;
    }

    // Usar obtenerUsuario de api.js que incluye el token
    const response = await obtenerUsuario(idUsuario);
    
    // Manejar diferentes formatos de respuesta
    const userData = response.datos || response.data || response;
    
    if (userData) {
      cachedUserData = userData;
      populateProfileModal(userData);
    }
  } catch (error) {
    console.error('Error loading user data:', error);
  }
}

// Rellenar el modal con datos del usuario
function populateProfileModal(userData) {
  if (!userData) return;

  // Avatar con iniciales
  const initials = (userData.nombreCompleto || userData.email || 'U')
    .split(' ')
    .map(word => word[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
  
  document.getElementById('profileAvatarInitials').textContent = initials;

  // Información del usuario
  document.getElementById('modalUserEmail').textContent = userData.email || '-';
  document.getElementById('modalUserEmailDisplay').textContent = userData.email || '-';
  document.getElementById('modalUserNombreCompleto').textContent = userData.nombreCompleto || '-';
  document.getElementById('modalUserTelefono').textContent = userData.telefono || '-';
  document.getElementById('modalUserRol').textContent = userData.rol || '-';
  document.getElementById('modalUserEstado').textContent = userData.activo ? 'Activo' : 'Inactivo';
  document.getElementById('modalUserDireccion').textContent = userData.direccion || '-';
  document.getElementById('modalUserCiudad').textContent = userData.ciudad || '-';
  document.getElementById('modalUserCodigoPostal').textContent = userData.codigoPostal || '-';
}

// Cargar imagen de perfil para el avatar del modal
async function loadProfileAvatarImage() {
  try {
    const idUsuario = localStorage.getItem('fallapp_user_id');
    if (!idUsuario) return;

    const token = localStorage.getItem('fallapp_token');
    const headers = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const base = (typeof API_BASE_URL !== 'undefined')
      ? API_BASE_URL
      : 'http://35.180.21.42:8080/api';
    const urlImagen = `${base}/usuarios/${idUsuario}/imagen`;

    const response = await fetch(urlImagen, { headers });
    const imgEl = document.getElementById('profileAvatarImg');
    const initialsEl = document.getElementById('profileAvatarInitials');

    if (!imgEl || !initialsEl) return;

    if (response.ok) {
      const blob = await response.blob();
      const objectUrl = URL.createObjectURL(blob);
      imgEl.src = objectUrl;
      imgEl.style.display = 'block';
      initialsEl.style.display = 'none';
    } else {
      // Si no hay imagen, mostramos iniciales
      imgEl.style.display = 'none';
      imgEl.src = '';
      initialsEl.style.display = 'block';
    }
  } catch (e) {
    console.debug('No se pudo cargar la imagen de perfil para el modal:', e);
  }
}

// Manejar cierre de sesión
function handleLogout() {
  try {
    localStorage.removeItem('fallapp_user');
  } catch (e) {
    console.error('Error removing user:', e);
  }
  window.location.href = '../js/index.html';
}
