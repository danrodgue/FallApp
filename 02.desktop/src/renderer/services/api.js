const API_URL = 'http://localhost:8080/api/fallas';

// Obtener todas las fallas
async function obtenerFallas() {
    const respuesta = await fetch(API_URL);
    return await respuesta.json();
}

// Obtener una falla por ID
async function obtenerFalla(id) {
    const respuesta = await fetch(`${API_URL}/${id}`);
    return await respuesta.json();
}

// Crear una falla
async function crearFalla(datos) {
    const respuesta = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datos)
    });
    return await respuesta.json();
}