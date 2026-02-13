package com.fallapp.features.fallas.domain.repository

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.domain.model.Voto
import com.fallapp.features.fallas.domain.model.VotoRequest

/**
 * Interfaz del repositorio de votos.
 * 
 * Define las operaciones disponibles para la gestión de votos.
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
interface VotosRepository {
    /**
     * Crea un nuevo voto para una falla (a través de ninot).
     * 
     * @param request Datos del voto (idNinot, tipoVoto)
     * @return Result con el voto creado o error
     */
    suspend fun crearVoto(request: VotoRequest): Result<Voto>
    
    /**
     * Obtiene los votos de un usuario específico.
     * 
     * @param idUsuario ID del usuario
     * @return Result con lista de votos o error
     */
    suspend fun getVotosUsuario(idUsuario: Long): Result<List<Voto>>
    
    /**
     * Elimina un voto existente.
     * 
     * Solo el autor del voto puede eliminarlo.
     * 
     * @param idVoto ID del voto a eliminar
     * @return Result con confirmación o error
     */
    suspend fun eliminarVoto(idVoto: Long): Result<Unit>
    
    /**
     * Obtiene los votos de una falla específica.
     * 
     * @param idFalla ID de la falla
     * @return Result con lista de votos o error
     */
    suspend fun getVotosFalla(idFalla: Long): Result<List<Voto>>
}
