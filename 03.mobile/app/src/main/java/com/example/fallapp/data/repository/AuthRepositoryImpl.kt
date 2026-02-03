package com.example.fallapp.data.repository

import android.content.SharedPreferences
import com.example.fallapp.data.remote.FallAppApi
import com.example.fallapp.data.remote.dto.LoginRequestDto
import com.example.fallapp.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: FallAppApi,
    private val prefs: SharedPreferences
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = api.login(LoginRequestDto(email = email, password = password))
            if (response.exito && response.datos != null) {
                // Guardamos el token para futuras llamadas autenticadas
                prefs.edit()
                    .putString(KEY_JWT_TOKEN, response.datos.token)
                    .apply()
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException(response.mensaje ?: "Error de login"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val KEY_JWT_TOKEN = "jwt_token"
    }
}

