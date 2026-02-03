package com.example.fallapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ninots")
data class NinotEntity(
    @PrimaryKey val id: Long,
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

