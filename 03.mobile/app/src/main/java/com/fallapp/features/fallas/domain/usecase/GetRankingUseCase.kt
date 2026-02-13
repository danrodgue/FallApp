package com.fallapp.features.fallas.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.fallas.data.remote.EstadisticasApiService
import com.fallapp.features.fallas.domain.model.FallaRanking

/**
 * Caso de uso para obtener el ranking de fallas desde /api/estadisticas/votos.
 */
class GetRankingUseCase(
    private val apiService: EstadisticasApiService
) {

    /**
     * @param tipoVotoApi null para todos, o uno de:
     *  - "EXPERIMENTAL"
     *  - "INGENIO_Y_GRACIA"
     *  - "MONUMENTO"
     */
    suspend operator fun invoke(
        tipoVotoApi: String?,
        limite: Int = 10
    ): Result<List<FallaRanking>> {
        return try {
            val response = apiService.getEstadisticasVotos(limite = limite, tipoVoto = tipoVotoApi)
            if (response.exito && response.datos != null) {
                val ranking = response.datos.topFallas.map {
                    FallaRanking(
                        idFalla = it.idFalla,
                        nombre = it.nombre,
                        seccion = it.seccion,
                        votos = it.votos.toInt()
                    )
                }
                Result.Success(ranking)
            } else {
                Result.Error(
                    exception = Exception(response.mensaje ?: "Error al obtener ranking"),
                    message = response.mensaje
                )
            }
        } catch (e: Exception) {
            Result.Error(
                exception = e,
                message = e.message ?: "Error de red al obtener ranking"
            )
        }
    }
}

