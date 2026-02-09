let cachedUserData = null;

document.addEventListener('DOMContentLoaded', async () => {
  // Cargar datos del usuario al iniciar
  await loadUserDataForModal();

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

// Manejar cierre de sesión
function handleLogout() {
  try {
    localStorage.removeItem('fallapp_user');
  } catch (e) {
    console.error('Error removing user:', e);
  }
  window.location.href = '../js/index.html';
}
