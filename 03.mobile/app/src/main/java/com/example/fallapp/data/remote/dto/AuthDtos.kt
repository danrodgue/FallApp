package com.example.fallapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    @SerialName("contrasena") val password: String
)

@Serializable
data class RegisterRequestDto(
    val email: String,
    @SerialName("contrasena") val password: String,
    val nombreCompleto: String,
    val idFalla: Long? = null
)

@Serializable
data class ApiResponseDto<T>(
    val exito: Boolean,
    val mensaje: String? = null,
    val datos: T? = null
)

@Serializable
data class LoginDataDto(
    val token: String,
    val tipo: String,
    val expiraEn: Long
)

