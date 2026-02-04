package com.fallapp.features.fallas.data.api

import com.fallapp.core.network.ApiResponse
import com.fallapp.features.fallas.data.dto.VotoDto
import com.fallapp.features.fallas.data.dto.VotoRequestDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Servicio API para gestión de votos.
 * 
 * Endpoints:
 * - POST /api/votos - Crear voto
 * - GET /api/votos/usuario/{idUsuario} - Obtener votos de un usuario
 * - DELETE /api/votos/{idVoto} - Eliminar voto
 * 
 * @property client Cliente HTTP de Ktor configurado
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class VotosApiService(
    private val client: HttpClient
) {
    /**
     * Crea un nuevo voto para una falla (a través de ninot).
     * 
     * @param request Datos del voto (idNinot, tipoVoto)
     * @return Respuesta con el voto creado
     */
    suspend fun crearVoto(request: VotoRequestDto): ApiResponse<VotoDto> {
        return client.post("/api/votos") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    /**
     * Obtiene los votos de un usuario específico.
     * 
     * @param idUsuario ID del usuario
     * @return Lista de votos del usuario
     */
    suspend fun getVotosUsuario(idUsuario: Long): ApiResponse<List<VotoDto>> {
        return client.get("/api/votos/usuario/$idUsuario").body()
    }
    
    /**
     * Elimina un voto existente.
     * 
     * Solo el autor del voto puede eliminarlo.
     * 
     * @param idVoto ID del voto a eliminar
     * @return Respuesta de confirmación
     */
    suspend fun eliminarVoto(idVoto: Long): ApiResponse<Unit> {
        return client.delete("/api/votos/$idVoto").body()
    }
    
    /**
     * Obtiene los votos de una falla específica.
     * 
     * @param idFalla ID de la falla
     * @return Lista de votos de la falla
     */
    suspend fun getVotosFalla(idFalla: Long): ApiResponse<List<VotoDto>> {
        return client.get("/api/votos/falla/$idFalla").body()
    }
}
