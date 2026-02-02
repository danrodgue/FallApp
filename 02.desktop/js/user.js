// API endpoint para obtener información del usuario
const API_USER_URL = 'http://localhost:8080/api/usuarios';

// Variable para rastrear si estamos en modo edición
let isEditing = false;
let currentUserId = null;

document.addEventListener('DOMContentLoaded', async () => {
  // Cargar información del usuario
  await loadUserData();

  // Configurar botones
  const editBtn = document.getElementById('editBtn');
  const saveBtn = document.getElementById('saveBtn');
  const cancelBtn = document.getElementById('cancelBtn');
  const logoutBtn = document.getElementById('logout');

  if (editBtn) editBtn.addEventListener('click', toggleEditMode);
  if (saveBtn) saveBtn.addEventListener('click', saveUserData);
  if (cancelBtn) cancelBtn.addEventListener('click', toggleEditMode);
  if (logoutBtn) logoutBtn.addEventListener('click', handleLogout);
});

// Cargar datos del usuario desde el backend
async function loadUserData() {
  try {
    // Obtener el usuario logueado del localStorage (email)
    const loggedUser = localStorage.getItem('fallapp_user');
    
    if (!loggedUser) {
      redirectToLogin();
      return;
    }

    // Obtener todos los usuarios activos del backend
    const response = await fetch(API_USER_URL);
    
    if (!response.ok) {
      throw new Error(`Error al obtener datos: ${response.status}`);
    }

    const result = await response.json();
    const usersData = result.data || [];
    
    // Buscar el usuario logueado por email
    const userData = Array.isArray(usersData) 
      ? usersData.find(u => u.email === loggedUser)
      : usersData;
    
    if (!userData) {
      throw new Error('Usuario no encontrado');
    }
    
    currentUserId = userData.idUsuario;
    populateUserForm(userData);
  } catch (error) {
    console.error('Error loading user data:', error);
    showErrorMessage('No se pudieron cargar los datos del usuario. Intenta de nuevo.');
  }
}

// Rellenar el formulario con datos del usuario
function populateUserForm(userData) {
  if (!userData) {
    showErrorMessage('No se encontraron datos del usuario.');
    return;
  }

  // Establecer valores en los campos
  document.getElementById('userId').value = userData.idUsuario || '';
  document.getElementById('userName').value = userData.email || '';
  document.getElementById('userEmail').value = userData.email || '';
  document.getElementById('userNombreCompleto').value = userData.nombreCompleto || '';
  document.getElementById('userTelefono').value = userData.telefono || '';
  document.getElementById('userRol').value = userData.rol || '';
  document.getElementById('userDireccion').value = userData.direccion || '';
  document.getElementById('userCiudad').value = userData.ciudad || '';
  document.getElementById('userCodigoPostal').value = userData.codigoPostal || '';
  document.getElementById('userEstado').value = userData.activo ? 'Activo' : 'Inactivo';

  // Formatear fecha de creación si existe
  if (userData.fechaCreacion) {
    const fecha = new Date(userData.fechaCreacion);
    document.getElementById('userFechaRegistro').value = fecha.toLocaleDateString('es-ES');
  }

  // Actualizar avatar con iniciales del usuario
  const initials = (userData.nombreCompleto || userData.email || 'U')
    .split(' ')
    .map(word => word[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
  
  document.getElementById('avatarInitials').textContent = initials;

  // Actualizar información en la barra lateral
  const sideStatus = document.getElementById('sideStatus');
  const estadoTexto = userData.activo ? 'activa' : 'inactiva';
  const fecha = userData.fechaCreacion 
    ? new Date(userData.fechaCreacion).toLocaleDateString('es-ES') 
    : 'desconocida';
  sideStatus.textContent = `Tu cuenta está ${estadoTexto}. Registrado desde ${fecha}.`;
}

// Alternar modo de edición
function toggleEditMode() {
  isEditing = !isEditing;

  const editBtn = document.getElementById('editBtn');
  const saveBtn = document.getElementById('saveBtn');
  const cancelBtn = document.getElementById('cancelBtn');
  const fields = document.querySelectorAll('.field input');

  if (isEditing) {
    // Habilitar campos editables (excepto algunos)
    fields.forEach((field) => {
      const fieldName = field.name;
      const nonEditableFields = ['id', 'username', 'rol', 'estado', 'fechaRegistro'];
      
      if (!nonEditableFields.includes(fieldName)) {
        field.disabled = false;
      }
    });

    editBtn.style.display = 'none';
    saveBtn.style.display = 'inline-flex';
    cancelBtn.style.display = 'inline-flex';
  } else {
    // Deshabilitar campos
    fields.forEach((field) => {
      field.disabled = true;
    });

    editBtn.style.display = 'inline-flex';
    saveBtn.style.display = 'none';
    cancelBtn.style.display = 'none';

    // Recargar datos para descartar cambios
    loadUserData();
  }
}

// Guardar cambios del usuario
async function saveUserData() {
  try {
    if (!currentUserId) {
      showErrorMessage('No se puede identificar al usuario.');
      return;
    }

    const formData = {
      nombreCompleto: document.getElementById('userNombreCompleto').value,
      email: document.getElementById('userEmail').value,
      telefono: document.getElementById('userTelefono').value,
      direccion: document.getElementById('userDireccion').value,
      ciudad: document.getElementById('userCiudad').value,
      codigoPostal: document.getElementById('userCodigoPostal').value,
    };

    // Enviar actualización al backend
    const response = await fetch(`${API_USER_URL}/${currentUserId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(formData),
    });

    if (!response.ok) {
      const errorResult = await response.json();
      throw new Error(errorResult.message || `Error al guardar: ${response.status}`);
    }

    // Deshabilitar modo edición
    isEditing = true;
    toggleEditMode();

    showSuccessMessage('Perfil actualizado correctamente.');
  } catch (error) {
    console.error('Error saving user data:', error);
    showErrorMessage(error.message || 'No se pudieron guardar los cambios. Intenta de nuevo.');
  }
}

// Mostrar mensaje de éxito
function showSuccessMessage(message) {
  const messageDiv = createMessageElement(message, 'success');
  document.body.appendChild(messageDiv);
  setTimeout(() => messageDiv.remove(), 3000);
}

// Mostrar mensaje de error
function showErrorMessage(message) {
  const messageDiv = createMessageElement(message, 'error');
  document.body.appendChild(messageDiv);
  setTimeout(() => messageDiv.remove(), 4000);
}

// Crear elemento de mensaje
function createMessageElement(message, type) {
  const div = document.createElement('div');
  div.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 16px 24px;
    border-radius: 8px;
    font-weight: 600;
    font-size: 14px;
    z-index: 9999;
    animation: slideIn 0.3s ease-out;
    ${type === 'success' 
      ? 'background: #10b981; color: white;' 
      : 'background: #ef4444; color: white;'}
  `;
  div.textContent = message;
  return div;
}

// Manejar cierre de sesión
function handleLogout() {
  try {
    localStorage.removeItem('fallapp_user');
  } catch (e) {
    console.error('Error removing user from localStorage:', e);
  }
  // Redirigir a login
  window.location.href = '../js/index.html';
}

// Redirigir a login si no hay usuario
function redirectToLogin() {
  window.location.href = '../js/index.html';
}

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

// Rellenar el formulario con datos del usuario
function populateUserForm(userData) {
  // Manejar tanto arrays como objeto único
  const user = Array.isArray(userData) ? userData[0] : userData;
  
  if (!user) {
    showErrorMessage('No se encontraron datos del usuario.');
    return;
  }

  // Establecer valores en los campos
  document.getElementById('userId').value = user.id || '';
  document.getElementById('userName').value = user.username || '';
  document.getElementById('userEmail').value = user.email || '';
  document.getElementById('userNombreCompleto').value = user.nombreCompleto || '';
  document.getElementById('userTelefono').value = user.telefono || '';
  document.getElementById('userRol').value = user.rol || '';
  document.getElementById('userDireccion').value = user.direccion || '';
  document.getElementById('userCiudad').value = user.ciudad || '';
  document.getElementById('userCodigoPostal').value = user.codigoPostal || '';
  document.getElementById('userEstado').value = user.estado || 'Activo';

  // Formatear fecha de registro si existe
  if (user.fechaRegistro) {
    const fecha = new Date(user.fechaRegistro);
    document.getElementById('userFechaRegistro').value = fecha.toLocaleDateString('es-ES');
  }

  // Actualizar avatar con iniciales del usuario
  const initials = (user.nombreCompleto || user.username || 'U')
    .split(' ')
    .map(word => word[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
  
  document.getElementById('avatarInitials').textContent = initials;

  // Actualizar información en la barra lateral
  const sideStatus = document.getElementById('sideStatus');
  sideStatus.textContent = `Tu cuenta está ${user.estado?.toLowerCase() === 'activo' ? 'activa' : 'inactiva'}. 
                            Registrado desde ${new Date(user.fechaRegistro).toLocaleDateString('es-ES')}.`;
}

// Alternar modo de edición
function toggleEditMode() {
  isEditing = !isEditing;

  const editBtn = document.getElementById('editBtn');
  const saveBtn = document.getElementById('saveBtn');
  const cancelBtn = document.getElementById('cancelBtn');
  const fields = document.querySelectorAll('.field input');

  if (isEditing) {
    // Habilitar campos editables (excepto algunos)
    fields.forEach((field) => {
      const fieldName = field.name;
      const nonEditableFields = ['id', 'username', 'rol', 'estado', 'fechaRegistro'];
      
      if (!nonEditableFields.includes(fieldName)) {
        field.disabled = false;
      }
    });

    editBtn.style.display = 'none';
    saveBtn.style.display = 'inline-flex';
    cancelBtn.style.display = 'inline-flex';
  } else {
    // Deshabilitar campos
    fields.forEach((field) => {
      field.disabled = true;
    });

    editBtn.style.display = 'inline-flex';
    saveBtn.style.display = 'none';
    cancelBtn.style.display = 'none';

    // Recargar datos para descartar cambios
    loadUserData();
  }
}

// Guardar cambios del usuario
async function saveUserData() {
  try {
    const formData = {
      id: document.getElementById('userId').value,
      email: document.getElementById('userEmail').value,
      nombreCompleto: document.getElementById('userNombreCompleto').value,
      telefono: document.getElementById('userTelefono').value,
      direccion: document.getElementById('userDireccion').value,
      ciudad: document.getElementById('userCiudad').value,
      codigoPostal: document.getElementById('userCodigoPostal').value,
    };

    // Enviar actualización al backend
    const response = await fetch(`${API_USER_URL}/${formData.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(formData),
    });

    if (!response.ok) {
      throw new Error(`Error al guardar: ${response.status}`);
    }

    // Deshabilitar modo edición
    isEditing = true;
    toggleEditMode();

    showSuccessMessage('Perfil actualizado correctamente.');
  } catch (error) {
    console.error('Error saving user data:', error);
    showErrorMessage('No se pudieron guardar los cambios. Intenta de nuevo.');
  }
}

// Mostrar mensaje de éxito
function showSuccessMessage(message) {
  const messageDiv = createMessageElement(message, 'success');
  document.body.appendChild(messageDiv);
  setTimeout(() => messageDiv.remove(), 3000);
}

// Mostrar mensaje de error
function showErrorMessage(message) {
  const messageDiv = createMessageElement(message, 'error');
  document.body.appendChild(messageDiv);
  setTimeout(() => messageDiv.remove(), 4000);
}

// Crear elemento de mensaje
function createMessageElement(message, type) {
  const div = document.createElement('div');
  div.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 16px 24px;
    border-radius: 8px;
    font-weight: 600;
    font-size: 14px;
    z-index: 9999;
    animation: slideIn 0.3s ease-out;
    ${type === 'success' 
      ? 'background: #10b981; color: white;' 
      : 'background: #ef4444; color: white;'}
  `;
  div.textContent = message;
  return div;
}

// Manejar cierre de sesión
function handleLogout() {
  try {
    localStorage.removeItem('fallapp_user');
  } catch (e) {
    console.error('Error removing user from localStorage:', e);
  }
  // Redirigir a login
  window.location.href = '../js/index.html';
}

// Redirigir a login si no hay usuario
function redirectToLogin() {
  window.location.href = '../js/index.html';
}

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
