// API endpoint para obtener información del usuario
const API_USER_URL = 'http://35.180.21.42:8080/api/usuarios';

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
    // Obtener el idUsuario desde localStorage (guardado en el login)
    const idUsuario = localStorage.getItem('fallapp_user_id');
    
    if (!idUsuario) {
      console.error('No hay idUsuario en localStorage');
      redirectToLogin();
      return;
    }

    currentUserId = idUsuario;

    // Obtener los datos del usuario específico desde la API
    const response = await obtenerUsuario(idUsuario);
    
    // Manejo de diferentes formatos de respuesta
    const userData = response.datos || response.data || response;
    
    if (!userData) {
      throw new Error('Usuario no encontrado');
    }
    
    populateUserForm(userData);
  } catch (error) {
    console.error('Error loading user data:', error);
    showErrorMessage(`Error al cargar datos: ${error.message}`);
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
  document.getElementById('userIdFalla').value = userData.idFalla || '';
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
    // Los campos no editables son: id, rol, idFalla, estado, fechaRegistro
    const nonEditableFields = ['userId', 'userRol', 'userIdFalla', 'userEstado', 'userFechaRegistro'];
    
    fields.forEach((field) => {
      if (!nonEditableFields.includes(field.id)) {
        field.disabled = false;
      } else {
        field.disabled = true;
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

// Validar datos del usuario
function validateUserData(formData) {
  const errors = [];
  
  // Validar email
  if (formData.email && !formData.email.includes('@')) {
    errors.push('El email no es válido');
  }
  
  // Validar teléfono (opcional pero si se proporciona debe tener formato)
  if (formData.telefono && formData.telefono.length > 0) {
    if (formData.telefono.replace(/\D/g, '').length < 9) {
      errors.push('El teléfono debe tener al menos 9 dígitos');
    }
  }
  
  // Validar código postal (opcional pero si se proporciona)
  if (formData.codigoPostal && formData.codigoPostal.length > 0) {
    if (!/^\d{5}$/.test(formData.codigoPostal)) {
      errors.push('El código postal debe tener 5 dígitos');
    }
  }
  
  return errors;
}

// Guardar cambios del usuario
async function saveUserData() {
  try {
    if (!currentUserId) {
      showErrorMessage('No se puede identificar al usuario.');
      return;
    }

    const formData = {
      nombreCompleto: document.getElementById('userNombreCompleto').value.trim(),
      email: document.getElementById('userEmail').value.trim(),
      telefono: document.getElementById('userTelefono').value.trim(),
      direccion: document.getElementById('userDireccion').value.trim(),
      ciudad: document.getElementById('userCiudad').value.trim(),
      codigoPostal: document.getElementById('userCodigoPostal').value.trim(),
    };

    // Validar datos
    const validationErrors = validateUserData(formData);
    if (validationErrors.length > 0) {
      showErrorMessage(validationErrors.join('\n'));
      return;
    }

    // Mostrar indicador de carga
    const saveBtn = document.getElementById('saveBtn');
    const originalText = saveBtn.textContent;
    saveBtn.textContent = 'Guardando...';
    saveBtn.disabled = true;

    // Enviar actualización utilizando la función mejorada de api.js
    const result = await actualizarUsuario(currentUserId, formData);

    // Deshabilitar modo edición
    isEditing = true;
    toggleEditMode();

    showSuccessMessage('Perfil actualizado correctamente.');
  } catch (error) {
    console.error('Error saving user data:', error);
    showErrorMessage(`Error al guardar: ${error.message}`);
  } finally {
    // Restaurar botón de guardar
    const saveBtn = document.getElementById('saveBtn');
    if (saveBtn) {
      saveBtn.textContent = 'Guardar';
      saveBtn.disabled = false;
    }
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
  setTimeout(() => messageDiv.remove(), 5000);
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
    max-width: 400px;
    word-wrap: break-word;
    white-space: pre-wrap;
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
    // Limpiar todos los datos de sesión
    localStorage.removeItem('fallapp_token');
    localStorage.removeItem('fallapp_user_id');
    localStorage.removeItem('fallapp_user_email');
    localStorage.removeItem('fallapp_user_nombre');
    localStorage.removeItem('fallapp_user_rol');
    localStorage.removeItem('fallapp_user_idFalla');
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


