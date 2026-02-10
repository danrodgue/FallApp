package com.example.fallapp.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(
        email: String,
        password: String,
        nombreCompleto: String,
        idFalla: Long?
    ): Result<Unit>
    suspend fun logout()
}

