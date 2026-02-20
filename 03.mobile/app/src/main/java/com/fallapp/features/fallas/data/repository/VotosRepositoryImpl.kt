package com.fallapp.features.fallas.data.repository

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.data.mapper.toDomain
import com.fallapp.features.fallas.data.mapper.toDto
import com.fallapp.features.fallas.data.remote.NinotsApiService
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
    private val apiService: VotosApiService,
    private val ninotsApiService: NinotsApiService
) : VotosRepository {
    
    override suspend fun crearVoto(request: VotoRequest): Result<Voto> {
        return try {
            // Ahora el dominio usa idFalla y la API acepta directamente idFalla,
            // por lo que no necesitamos resolver ninots.
            val dtoRequest = request.toDto()
            val response = apiService.crearVoto(dtoRequest)

            if (!response.exito) {
                return Result.Error(
                    exception = Exception(response.mensaje ?: "Error al crear voto"),
                    message = response.mensaje ?: "No se pudo registrar tu voto"
                )
            }

            // El backend puede no devolver un VotoDTO completo en `datos`,
            // pero para la UI solo necesitamos saber que se ha registrado el voto.
            // El listado real de votos se vuelve a cargar desde /votos/mis-votos.
            val dummyVoto = Voto(
                idVoto = -1L,
                idUsuario = -1L,
                nombreUsuario = "",
                idFalla = request.idFalla,
                nombreFalla = "",
                tipoVoto = request.tipoVoto,
                fechaCreacion = null
            )
            Result.Success(dummyVoto)
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("nn not found", ignoreCase = true) == true ->
                    "Error al procesar tu voto. Por favor intenta de nuevo."
                e.message?.contains("Sesión expirada", ignoreCase = true) == true ->
                    "Tu sesión ha expirado. Por favor inicia sesión de nuevo."
                else -> e.message ?: "Error de conexión al votar"
            }
            Result.Error(
                exception = e,
                message = errorMsg
            )
        }
    }
    
    override suspend fun getMisVotos(): Result<List<Voto>> {
        return try {
            val response = apiService.getMisVotos()
            if (response.exito && response.datos != null) {
                Result.Success(response.datos.map { it.toDomain() })
            } else {
                Result.Error(
                    exception = Exception(response.mensaje ?: "Error al obtener votos"),
                    message = response.mensaje ?: "Error desconocido al obtener votos"
                )
            }
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("no soporta este endpoint", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intenta más tarde."
                e.message?.contains("nn not found", ignoreCase = true) == true ->
                    "Error al sincronizar tus votos. Por favor recarga la pantalla."
                e.message?.contains("Sesión expirada", ignoreCase = true) == true ->
                    e.message ?: "Tu sesión ha expirado"
                e.message?.contains("405", ignoreCase = true) == true ->
                    "El servidor no está respondiendo correctamente. Intenta más tarde."
                else -> e.message ?: "Error de conexión al obtener votos"
            }
            Result.Error(
                exception = e,
                message = errorMsg
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
                    message = response.mensaje ?: "Error desconocido al obtener votos"
                )
            }
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("nn not found", ignoreCase = true) == true ->
                    "Error al parsear votos del usuario. Intenta recargar."
                else -> e.message ?: "Error de conexión"
            }
            Result.Error(
                exception = e,
                message = errorMsg
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
                    message = response.mensaje ?: "No se pudo eliminar el voto"
                )
            }
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("nn not found", ignoreCase = true) == true ->
                    "Error al procesar la eliminación del voto. Intenta de nuevo."
                else -> e.message ?: "Error de conexión"
            }
            Result.Error(
                exception = e,
                message = errorMsg
            )
        }
    }
    
    override suspend fun getVotosFalla(idFalla: Long): Result<List<Voto>> {
        return try {
            // Resolver el idNinot real asociado a esta falla
            // A partir de v2, los votos están asociados directamente a la falla,
            // por lo que podemos llamar a /api/votos/falla/{idFalla} sin resolver ninots.
            val response = apiService.getVotosFalla(idFalla)

            if (response.exito && response.datos != null) {
                val votosDto = response.datos
                Result.Success(votosDto.map { it.toDomain() })
            } else {
                Result.Error(
                    exception = Exception(response.mensaje ?: "Error al obtener votos de la falla"),
                    message = response.mensaje ?: "Error desconocido"
                )
            }
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("nn not found", ignoreCase = true) == true ->
                    "Error al parsear votos de la falla. Intenta recargar."
                else -> e.message ?: "Error de conexión"
            }
            Result.Error(
                exception = e,
                message = errorMsg
            )
        }
    }
}
