package com.example.fallapp.domain.repository

import com.example.fallapp.domain.model.Falla

interface FallaRepository {
    suspend fun getFallas(): Result<List<Falla>>
    suspend fun searchFallas(texto: String): Result<List<Falla>>
}

