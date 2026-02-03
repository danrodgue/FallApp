package com.example.fallapp.domain.model

data class Ninot(
    val id: Long,
    val fallaId: Long,
    val fallaNombre: String,
    val nombre: String,
    val tituloObra: String,
    val altura: Double?,
    val ancho: Double?,
    val imagenPrincipal: String?,
    val premiado: Boolean,
    val totalVotos: Int
)

