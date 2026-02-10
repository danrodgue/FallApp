package com.example.fallapp.domain.model

data class Falla(
    val id: Long,
    val nombre: String,
    val seccion: String?,
    val presidente: String?,
    val lema: String?,
    val categoria: String?,
    val imagenUrl: String?,
    val latitud: Double?,
    val longitud: Double?
)

