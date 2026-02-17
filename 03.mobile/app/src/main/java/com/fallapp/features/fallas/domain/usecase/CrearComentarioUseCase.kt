package com.fallapp.features.fallas.domain.usecase

import com.fallapp.core.util.Result
import com.fallapp.features.auth.domain.usecase.GetCurrentUserUseCase
import com.fallapp.features.fallas.data.remote.ComentariosApiService
import com.fallapp.features.fallas.data.remote.dto.ComentarioRequestDto

/**
 * Caso de uso: crear comentario en una falla.
 * El backend analiza el sentimiento con IA (HuggingFace) automáticamente.
 */
class CrearComentarioUseCase(
    private val comentariosApiService: ComentariosApiService,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {

    suspend operator fun invoke(idFalla: Long, contenido: String): Result<Unit> {
        val trimmed = contenido.trim()
        if (trimmed.length < 3) {
            return Result.error(
                exception = IllegalArgumentException("Mínimo 3 caracteres"),
                message = "El comentario debe tener entre 3 y 500 caracteres"
            )
        }
        if (trimmed.length > 500) {
            return Result.error(
                exception = IllegalArgumentException("Máximo 500 caracteres"),
                message = "El comentario no puede exceder 500 caracteres"
            )
        }

        val user = getCurrentUserUseCase()
            ?: return Result.error(
                exception = IllegalStateException("No hay sesión"),
                message = "Inicia sesión para poder comentar"
            )

        return try {
            val response = comentariosApiService.crearComentario(
                ComentarioRequestDto(
                    idUsuario = user.idUsuario,
                    idFalla = idFalla,
                    contenido = trimmed
                )
            )
            if (response.exito) {
                Result.success(Unit)
            } else {
                Result.error(
                    exception = Exception(response.mensaje),
                    message = response.mensaje ?: "Error al enviar comentario"
                )
            }
        } catch (e: Exception) {
            Result.error(
                exception = e,
                message = e.message ?: "Error de conexión al enviar comentario"
            )
        }
    }
}
