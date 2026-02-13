/**
 * Tests unitarios para api.js - Cliente API del Desktop
 *
 * Cobertura: getAuthHeaders, parseErrorResponse,
 *            CRUD de fallas, usuarios, eventos
 *
 * Patrón: Given-When-Then
 * @version 1.0.0
 */

// Cargamos las funciones directamente evaluando el archivo
// ya que api.js usa variables globales, no module.exports
const fs = require('fs');
const path = require('path');

// Cargar el código de api.js
const apiCode = fs.readFileSync(path.join(__dirname, '..', 'api.js'), 'utf8');

// Inyectar en contexto global
eval(apiCode);

describe('API Client - api.js', () => {

    // ==========================================
    // getAuthHeaders()
    // ==========================================
    describe('getAuthHeaders()', () => {

        test('retorna headers con token cuando existe en localStorage', () => {
            // Given
            localStorage.setItem('fallapp_token', 'jwt-token-abc123');

            // When
            const headers = getAuthHeaders();

            // Then
            expect(headers['Content-Type']).toBe('application/json');
            expect(headers['Authorization']).toBe('Bearer jwt-token-abc123');
        });

        test('retorna headers sin Authorization cuando no hay token', () => {
            // Given - localStorage vacío

            // When
            const headers = getAuthHeaders();

            // Then
            expect(headers['Content-Type']).toBe('application/json');
            expect(headers['Authorization']).toBeUndefined();
        });

        test('siempre incluye Content-Type application/json', () => {
            // When
            const headers = getAuthHeaders();

            // Then
            expect(headers['Content-Type']).toBe('application/json');
        });
    });

    // ==========================================
    // parseErrorResponse()
    // ==========================================
    describe('parseErrorResponse()', () => {

        test('extrae message del JSON de error', async () => {
            // Given
            const response = {
                status: 400,
                json: jest.fn().mockResolvedValue({ message: 'Datos inválidos' })
            };

            // When
            const error = await parseErrorResponse(response);

            // Then
            expect(error).toBe('Datos inválidos');
        });

        test('extrae error del JSON cuando no hay message', async () => {
            // Given
            const response = {
                status: 500,
                json: jest.fn().mockResolvedValue({ error: 'Internal Server Error' })
            };

            // When
            const error = await parseErrorResponse(response);

            // Then
            expect(error).toBe('Internal Server Error');
        });

        test('retorna error genérico cuando JSON parsing falla', async () => {
            // Given
            const response = {
                status: 503,
                json: jest.fn().mockRejectedValue(new Error('Invalid JSON'))
            };

            // When
            const error = await parseErrorResponse(response);

            // Then
            expect(error).toBe('Error del servidor (503)');
        });

        test('retorna Error {status} cuando no hay message ni error en JSON', async () => {
            // Given
            const response = {
                status: 422,
                json: jest.fn().mockResolvedValue({})
            };

            // When
            const error = await parseErrorResponse(response);

            // Then
            expect(error).toBe('Error 422');
        });
    });

    // ==========================================
    // obtenerFallas()
    // ==========================================
    describe('obtenerFallas()', () => {

        test('retorna lista de fallas cuando API responde OK', async () => {
            // Given
            const fallasResponse = {
                exito: true,
                datos: {
                    contenido: [
                        { idFalla: 1, nombre: 'Na Jordana' },
                        { idFalla: 2, nombre: 'Convento Jerusalén' }
                    ]
                }
            };

            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: jest.fn().mockResolvedValue(fallasResponse)
            });

            // When
            const resultado = await obtenerFallas();

            // Then
            expect(resultado).toEqual(fallasResponse);
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/fallas'),
                expect.objectContaining({
                    headers: expect.objectContaining({
                        'Content-Type': 'application/json'
                    })
                })
            );
        });

        test('lanza error cuando API responde con error', async () => {
            // Given
            global.fetch.mockResolvedValueOnce({
                ok: false,
                status: 500,
                json: jest.fn().mockResolvedValue({ message: 'Server error' })
            });

            // When & Then
            await expect(obtenerFallas()).rejects.toThrow('No se pudieron obtener las fallas');
        });

        test('lanza error cuando fetch falla por red', async () => {
            // Given
            global.fetch.mockRejectedValueOnce(new Error('Network error'));

            // When & Then
            await expect(obtenerFallas()).rejects.toThrow('No se pudieron obtener las fallas');
        });
    });

    // ==========================================
    // obtenerFalla()
    // ==========================================
    describe('obtenerFalla()', () => {

        test('retorna falla por ID cuando API responde OK', async () => {
            // Given
            const fallaResponse = {
                exito: true,
                datos: { idFalla: 15, nombre: 'Na Jordana', seccion: 'E' }
            };

            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: jest.fn().mockResolvedValue(fallaResponse)
            });

            // When
            const resultado = await obtenerFalla(15);

            // Then
            expect(resultado).toEqual(fallaResponse);
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/fallas/15'),
                expect.any(Object)
            );
        });

        test('lanza error cuando falla no existe', async () => {
            // Given
            global.fetch.mockResolvedValueOnce({
                ok: false,
                status: 404,
                json: jest.fn().mockResolvedValue({ message: 'Falla no encontrada' })
            });

            // When & Then
            await expect(obtenerFalla(999)).rejects.toThrow('No se pudo obtener la falla');
        });
    });

    // ==========================================
    // crearFalla()
    // ==========================================
    describe('crearFalla()', () => {

        test('crea falla con datos válidos', async () => {
            // Given
            const datos = {
                nombre: 'Nueva Falla',
                seccion: '1A',
                presidente: 'Ana García'
            };

            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: jest.fn().mockResolvedValue({
                    exito: true,
                    datos: { idFalla: 50, ...datos }
                })
            });

            // When
            const resultado = await crearFalla(datos);

            // Then
            expect(resultado.datos.idFalla).toBe(50);
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/fallas'),
                expect.objectContaining({
                    method: 'POST',
                    body: JSON.stringify(datos)
                })
            );
        });

        test('incluye token en headers cuando hay sesión', async () => {
            // Given
            localStorage.setItem('fallapp_token', 'my-jwt-token');
            const datos = { nombre: 'Test' };

            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: jest.fn().mockResolvedValue({ exito: true, datos: {} })
            });

            // When
            await crearFalla(datos);

            // Then
            expect(fetch).toHaveBeenCalledWith(
                expect.any(String),
                expect.objectContaining({
                    headers: expect.objectContaining({
                        'Authorization': 'Bearer my-jwt-token'
                    })
                })
            );
        });
    });

    // ==========================================
    // actualizarFalla()
    // ==========================================
    describe('actualizarFalla()', () => {

        test('actualiza falla existente con PUT', async () => {
            // Given
            const datos = { nombre: 'Falla Actualizada', seccion: 'E' };

            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: jest.fn().mockResolvedValue({ exito: true, datos: { idFalla: 1, ...datos } })
            });

            // When
            const resultado = await actualizarFalla(1, datos);

            // Then
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/fallas/1'),
                expect.objectContaining({
                    method: 'PUT',
                    body: JSON.stringify(datos)
                })
            );
        });
    });

    // ==========================================
    // eliminarFalla()
    // ==========================================
    describe('eliminarFalla()', () => {

        test('elimina falla con DELETE', async () => {
            // Given
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: jest.fn().mockResolvedValue({ exito: true })
            });

            // When
            await eliminarFalla(5);

            // Then
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/fallas/5'),
                expect.objectContaining({
                    method: 'DELETE'
                })
            );
        });

        test('lanza error cuando eliminación falla', async () => {
            // Given
            global.fetch.mockResolvedValueOnce({
                ok: false,
                status: 403,
                json: jest.fn().mockResolvedValue({ message: 'No autorizado' })
            });

            // When & Then
            await expect(eliminarFalla(1)).rejects.toThrow('No se pudo eliminar la falla');
        });
    });

    // ==========================================
    // obtenerUsuario()
    // ==========================================
    describe('obtenerUsuario()', () => {

        test('retorna datos del usuario por ID', async () => {
            // Given
            const userData = {
                exito: true,
                datos: {
                    idUsuario: 1,
                    email: 'juan@example.com',
                    nombreCompleto: 'Juan García'
                }
            };

            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: jest.fn().mockResolvedValue(userData)
            });

            // When
            const resultado = await obtenerUsuario(1);

            // Then
            expect(resultado.datos.email).toBe('juan@example.com');
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/usuarios/1'),
                expect.any(Object)
            );
        });
    });

    // ==========================================
    // actualizarUsuario()
    // ==========================================
    describe('actualizarUsuario()', () => {

        test('actualiza usuario con PUT', async () => {
            // Given
            const datos = { nombreCompleto: 'Juan Actualizado', telefono: '666123456' };

            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: jest.fn().mockResolvedValue({ exito: true, datos: {} })
            });

            // When
            await actualizarUsuario(1, datos);

            // Then
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/usuarios/1'),
                expect.objectContaining({
                    method: 'PUT',
                    body: JSON.stringify(datos)
                })
            );
        });
    });

    // ==========================================
    // CRUD Eventos
    // ==========================================
    describe('Eventos CRUD', () => {

        test('obtenerEvento retorna evento por ID', async () => {
            // Given
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: jest.fn().mockResolvedValue({
                    exito: true,
                    datos: { idEvento: 10, nombre: 'Plantà 2026' }
                })
            });

            // When
            const resultado = await obtenerEvento(10);

            // Then
            expect(resultado.datos.nombre).toBe('Plantà 2026');
        });

        test('crearEvento envía POST con datos', async () => {
            // Given
            const datos = { idFalla: 1, nombre: 'Cremà', tipo: 'crema' };

            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: jest.fn().mockResolvedValue({ exito: true, datos: { idEvento: 11 } })
            });

            // When
            await crearEvento(datos);

            // Then
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/eventos'),
                expect.objectContaining({
                    method: 'POST',
                    body: JSON.stringify(datos)
                })
            );
        });

        test('actualizarEvento envía PUT con datos', async () => {
            // Given
            const datos = { nombre: 'Evento Actualizado' };

            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: jest.fn().mockResolvedValue({ exito: true })
            });

            // When
            await actualizarEvento(10, datos);

            // Then
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/eventos/10'),
                expect.objectContaining({ method: 'PUT' })
            );
        });

        test('eliminarEvento envía DELETE', async () => {
            // Given
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: jest.fn().mockResolvedValue({ exito: true })
            });

            // When
            await eliminarEvento(10);

            // Then
            expect(fetch).toHaveBeenCalledWith(
                expect.stringContaining('/eventos/10'),
                expect.objectContaining({ method: 'DELETE' })
            );
        });
    });
});
