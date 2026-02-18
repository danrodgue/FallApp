const API_BASE_URL = 'http://35.180.21.42:8080/api';
const API_URL = API_BASE_URL + '/fallas';
const API_USUARIOS_URL = API_BASE_URL + '/usuarios';
const API_EVENTOS_URL = API_BASE_URL + '/eventos';

function getAuthHeaders() {
  const token = localStorage.getItem('fallapp_token');
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return headers;
}

async function parseErrorResponse(response) {
  try {
    const data = await response.json();
    return data.message || data.error || `Error ${response.status}`;
  } catch (e) {
    return `Error del servidor (${response.status})`;
  }
}

async function obtenerFallas() {
  try {
    const respuesta = await fetch(API_URL, { headers: getAuthHeaders() });
    if (!respuesta.ok) {
      const error = await parseErrorResponse(respuesta);
      throw new Error(error);
    }
    return await respuesta.json();
  } catch (error) {
    throw new Error(`No se pudieron obtener las fallas: ${error.message}`);
  }
}

async function obtenerFalla(id) {
  try {
    const respuesta = await fetch(`${API_URL}/${id}`, { headers: getAuthHeaders() });
    if (!respuesta.ok) {
      const error = await parseErrorResponse(respuesta);
      throw new Error(error);
    }
    return await respuesta.json();
  } catch (error) {
    throw new Error(`No se pudo obtener la falla: ${error.message}`);
  }
}

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

async function obtenerUsuario(id) {
  try {
    const respuesta = await fetch(`${API_USUARIOS_URL}/${id}`, { headers: getAuthHeaders() });
    if (!respuesta.ok) {
      const error = await parseErrorResponse(respuesta);
      throw new Error(error);
    }
    return await respuesta.json();
  } catch (error) {
    throw new Error(`No se pudo obtener el usuario: ${error.message}`);
  }
}

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

async function obtenerEventosPorFalla(idFalla, page = 0, size = 100) {
  try {
    const url = `${API_EVENTOS_URL}/falla/${idFalla}?page=${page}&size=${size}`;
    const respuesta = await fetch(url, { headers: getAuthHeaders() });
    if (!respuesta.ok) {
      const error = await parseErrorResponse(respuesta);
      throw new Error(error);
    }
    return await respuesta.json();
  } catch (error) {
    throw new Error(`No se pudieron obtener los eventos de la falla: ${error.message}`);
  }
}

async function obtenerEvento(id) {
  try {
    const respuesta = await fetch(`${API_EVENTOS_URL}/${id}`, { headers: getAuthHeaders() });
    if (!respuesta.ok) {
      const error = await parseErrorResponse(respuesta);
      throw new Error(error);
    }
    return await respuesta.json();
  } catch (error) {
    throw new Error(`No se pudo obtener el evento: ${error.message}`);
  }
}

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

async function subirImagenEvento(idEvento, archivo) {
  try {
    const formData = new FormData();
    formData.append('imagen', archivo);

    const headers = {};
    const token = localStorage.getItem('fallapp_token');
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const respuesta = await fetch(`${API_EVENTOS_URL}/${idEvento}/imagen`, {
      method: 'POST',
      headers,
      body: formData
    });

    if (!respuesta.ok) {
      const error = await parseErrorResponse(respuesta);
      throw new Error(error);
    }
    return await respuesta.json();
  } catch (error) {
    throw new Error(`No se pudo subir la imagen del evento: ${error.message}`);
  }
}

async function descargarImagenEvento(idEvento) {
  try {
    const respuesta = await fetch(`${API_EVENTOS_URL}/${idEvento}/imagen`, {
      headers: getAuthHeaders()
    });
    if (!respuesta.ok) {
      throw new Error(`Error HTTP ${respuesta.status}`);
    }
    return await respuesta.blob();
  } catch (error) {
    throw new Error(`No se pudo descargar la imagen del evento: ${error.message}`);
  }
}

async function eliminarImagenEvento(idEvento) {
  try {
    const respuesta = await fetch(`${API_EVENTOS_URL}/${idEvento}/imagen`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    if (!respuesta.ok) {
      const error = await parseErrorResponse(respuesta);
      throw new Error(error);
    }
    return await respuesta.json();
  } catch (error) {
    throw new Error(`No se pudo eliminar la imagen del evento: ${error.message}`);
  }
}

async function subirImagenUsuario(idUsuario, archivo) {
  try {
    const formData = new FormData();
    formData.append('imagen', archivo);

    const headers = {};
    const token = localStorage.getItem('fallapp_token');
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const respuesta = await fetch(`${API_USUARIOS_URL}/${idUsuario}/imagen`, {
      method: 'POST',
      headers,
      body: formData
    });

    if (!respuesta.ok) {
      const error = await parseErrorResponse(respuesta);
      throw new Error(error);
    }
    return await respuesta.json();
  } catch (error) {
    throw new Error(`No se pudo subir la imagen del usuario: ${error.message}`);
  }
}

async function descargarImagenUsuario(idUsuario) {
  try {
    const respuesta = await fetch(`${API_USUARIOS_URL}/${idUsuario}/imagen`, {
      headers: getAuthHeaders()
    });
    if (!respuesta.ok) {
      throw new Error(`Error HTTP ${respuesta.status}`);
    }
    return await respuesta.blob();
  } catch (error) {
    throw new Error(`No se pudo descargar la imagen del usuario: ${error.message}`);
  }
}

async function eliminarImagenUsuario(idUsuario) {
  try {
    const respuesta = await fetch(`${API_USUARIOS_URL}/${idUsuario}/imagen`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    if (!respuesta.ok) {
      const error = await parseErrorResponse(respuesta);
      throw new Error(error);
    }
    return await respuesta.json();
  } catch (error) {
    throw new Error(`No se pudo eliminar la imagen del usuario: ${error.message}`);
  }
}
