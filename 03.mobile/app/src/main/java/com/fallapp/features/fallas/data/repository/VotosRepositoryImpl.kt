package com.fallapp.features.fallas.data.repository

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.data.mapper.toDomain
import com.fallapp.features.fallas.data.mapper.toDto
import com.fallapp.features.fallas.data.remote.VotosApiService
import com.fallapp.features.fallas.domain.model.Voto
import com.fallapp.features.fallas.domain.model.VotoRequest
import com.fallapp.features.fallas.domain.repository.VotosRepository

/**
 * Implementación del repositorio de votos.
 * 
 * Gestiona las llamadas a la API REST y manejo de errores.
 * 
 * @property apiService Servicio de API de votos
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class VotosRepositoryImpl(
    private val apiService: VotosApiService
) : VotosRepository {
    
    override suspend fun crearVoto(request: VotoRequest): Result<Voto> {
        return try {
            val response = apiService.crearVoto(request.toDto())
            
            if (response.exito && response.datos != null) {
                Result.Success(response.datos.toDomain())
            } else {
                Result.Error(
                    exception = Exception(response.mensaje ?: "Error al crear voto"),
                    message = response.mensaje
                )
            }
        } catch (e: Exception) {
            Result.Error(
                exception = e,
                message = e.message ?: "Error de conexión al crear voto"
            )
        }
    }
    
    override suspend fun getVotosUsuario(idUsuario: Long): Result<List<Voto>> {
        return try {
            val response = apiService.getVotosUsuario(idUsuario)
            
            if (response.exito && response.datos != null) {
                Result.Success(response.datos.map { it.toDomain() })
            } else {
                Result.Error(
                    exception = Exception(response.mensaje ?: "Error al obtener votos"),
                    message = response.mensaje
                )
            }
        } catch (e: Exception) {
            Result.Error(
                exception = e,
                message = e.message ?: "Error de conexión al obtener votos"
            )
        }
    }
    
    override suspend fun eliminarVoto(idVoto: Long): Result<Unit> {
        return try {
            val response = apiService.eliminarVoto(idVoto)
            
            if (response.exito) {
                Result.Success(Unit)
            } else {
                Result.Error(
                    exception = Exception(response.mensaje ?: "Error al eliminar voto"),
                    message = response.mensaje
                )
            }
        } catch (e: Exception) {
            Result.Error(
                exception = e,
                message = e.message ?: "Error de conexión al eliminar voto"
            )
        }
    }
    
    override suspend fun getVotosFalla(idFalla: Long): Result<List<Voto>> {
        return try {
            val response = apiService.getVotosFalla(idFalla)
            
            if (response.exito && response.datos != null) {
                Result.Success(response.datos.map { it.toDomain() })
            } else {
                Result.Error(
                    exception = Exception(response.mensaje ?: "Error al obtener votos de la falla"),
                    message = response.mensaje
                )
            }
        } catch (e: Exception) {
            Result.Error(
                exception = e,
                message = e.message ?: "Error de conexión al obtener votos de la falla"
            )
        }
    }
}
