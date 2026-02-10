package com.example.fallapp.data.repository

import com.example.fallapp.data.remote.FallAppApi
import com.example.fallapp.domain.model.Falla
import com.example.fallapp.domain.repository.FallaRepository
import javax.inject.Inject

class FallaRepositoryImpl @Inject constructor(
    private val api: FallAppApi
) : FallaRepository {

    override suspend fun getFallas(): Result<List<Falla>> {
        return try {
            val response = api.getFallas()
            if (response.exito && response.datos != null) {
                Result.success(response.datos.contenido.map { it.toDomain() })
            } else {
                Result.failure(IllegalStateException(response.mensaje ?: "Error obteniendo fallas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchFallas(texto: String): Result<List<Falla>> {
        return try {
            val response = api.buscarFallas(texto)
            if (response.exito && response.datos != null) {
                Result.success(response.datos.map { it.toDomain() })
            } else {
                Result.failure(IllegalStateException(response.mensaje ?: "Error buscando fallas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun com.example.fallapp.data.remote.dto.FallaDto.toDomain(): Falla =
    Falla(
        id = id,
        nombre = nombre,
        seccion = seccion,
        presidente = presidente,
        lema = lema,
        categoria = categoria,
        imagenUrl = urlBoceto,
        latitud = latitud,
        longitud = longitud
    )

