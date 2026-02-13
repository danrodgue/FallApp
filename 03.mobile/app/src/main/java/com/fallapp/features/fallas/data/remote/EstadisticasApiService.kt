package com.fallapp.features.fallas.data.remote

import com.fallapp.core.config.ApiConfig
import com.fallapp.core.network.ApiResponse
import com.fallapp.features.fallas.data.remote.dto.EstadisticasVotosDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Servicio remoto para consumir los endpoints de estadísticas.
 *
 * En particular, GET /api/estadisticas/votos para rankings de fallas.
 */
class EstadisticasApiService(
    private val client: HttpClient
) {

    /**
     * GET /api/estadisticas/votos
     *
     * @param limite número máximo de fallas en el ranking (por defecto 10)
     * @param tipoVoto filtro opcional: EXPERIMENTAL, INGENIO_Y_GRACIA, MONUMENTO
     */
    suspend fun getEstadisticasVotos(
        limite: Int = 10,
        tipoVoto: String? = null
    ): ApiResponse<EstadisticasVotosDto> {
        return client.get("${ApiConfig.API_PATH}/estadisticas/votos") {
            parameter("limite", limite)
            if (tipoVoto != null) {
                parameter("tipoVoto", tipoVoto)
            }
        }.body()
    }
}

