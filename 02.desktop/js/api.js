const API_BASE_URL = 'http://35.180.21.42:8080/api';
const API_URL = API_BASE_URL + '/fallas';
const API_USUARIOS_URL = API_BASE_URL + '/usuarios';
const API_EVENTOS_URL = API_BASE_URL + '/eventos';

// ============================================
// OBTENER TOKEN DE AUTENTICACIÓN
// ============================================
function getAuthHeaders() {
    const token = localStorage.getItem('fallapp_token');
    const headers = { 'Content-Type': 'application/json' };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
}

// ============================================
// MANEJO DE ERRORES MEJORADO
// ============================================
async function parseErrorResponse(response) {
    try {
        const data = await response.json();
        return data.message || data.error || `Error ${response.status}`;
    } catch (e) {
        return `Error del servidor (${response.status})`;
    }
}

// ============================================
// FUNCIONES DE FALLA
// ============================================

// Obtener todas las fallas
async function obtenerFallas() {
    try {
        const respuesta = await fetch(API_URL, {
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudieron obtener las fallas: ${error.message}`);
    }
}

// Obtener una falla por ID
async function obtenerFalla(id) {
    try {
        const respuesta = await fetch(`${API_URL}/${id}`, {
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo obtener la falla: ${error.message}`);
    }
}

// Crear una falla
async function crearFalla(datos) {
    try {
        const respuesta = await fetch(API_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(datos)
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo crear la falla: ${error.message}`);
    }
}

// Actualizar una falla
async function actualizarFalla(id, datos) {
    try {
        const respuesta = await fetch(`${API_URL}/${id}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(datos)
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo actualizar la falla: ${error.message}`);
    }
}

// Eliminar una falla
async function eliminarFalla(id) {
    try {
        const respuesta = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo eliminar la falla: ${error.message}`);
    }
}

// ============================================
// FUNCIONES DE USUARIO
// ============================================

// Obtener un usuario por ID
async function obtenerUsuario(id) {
    try {
        const respuesta = await fetch(`${API_USUARIOS_URL}/${id}`, {
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo obtener el usuario: ${error.message}`);
    }
}

// Actualizar un usuario
async function actualizarUsuario(id, datos) {
    try {
        const respuesta = await fetch(`${API_USUARIOS_URL}/${id}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(datos)
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo actualizar el usuario: ${error.message}`);
    }
}

// ============================================
// FUNCIONES DE EVENTO
// ============================================

// Obtener un evento por ID
async function obtenerEvento(id) {
    try {
        const respuesta = await fetch(`${API_EVENTOS_URL}/${id}`, {
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo obtener el evento: ${error.message}`);
    }
}

// Crear un evento
async function crearEvento(datos) {
    try {
        const respuesta = await fetch(API_EVENTOS_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(datos)
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo crear el evento: ${error.message}`);
    }
}

// Actualizar un evento
async function actualizarEvento(id, datos) {
    try {
        const respuesta = await fetch(`${API_EVENTOS_URL}/${id}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(datos)
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo actualizar el evento: ${error.message}`);
    }
}

// Eliminar un evento
async function eliminarEvento(id) {
    try {
        const respuesta = await fetch(`${API_EVENTOS_URL}/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (!respuesta.ok) {
            const error = await parseErrorResponse(respuesta);
            throw new Error(error);
        }
        return await respuesta.json();
    } catch (error) {
        throw new Error(`No se pudo eliminar el evento: ${error.message}`);
    }
}