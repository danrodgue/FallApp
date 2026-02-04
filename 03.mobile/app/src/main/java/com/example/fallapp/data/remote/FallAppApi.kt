package com.example.fallapp.data.remote

import com.example.fallapp.data.remote.dto.ApiResponseDto
import com.example.fallapp.data.remote.dto.FallaDto
import com.example.fallapp.data.remote.dto.FallasPageDto
import com.example.fallapp.data.remote.dto.LoginDataDto
import com.example.fallapp.data.remote.dto.LoginRequestDto
import com.example.fallapp.data.remote.dto.RegisterRequestDto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface FallAppApi {

    @POST("/api/auth/login")
    @Headers("Content-Type: application/json")
    suspend fun login(
        @Body body: LoginRequestDto
    ): ApiResponseDto<LoginDataDto>

    @POST("/api/auth/registro")
    @Headers("Content-Type: application/json")
    suspend fun register(
        @Body body: RegisterRequestDto
    ): ApiResponseDto<LoginDataDto>

    @GET("/api/fallas")
    @Headers("Content-Type: application/json")
    suspend fun getFallas(
        @Query("pagina") pagina: Int = 0,
        @Query("tamano") tamano: Int = 20
    ): ApiResponseDto<FallasPageDto>

    @GET("/api/fallas/buscar")
    @Headers("Content-Type: application/json")
    suspend fun buscarFallas(
        @Query("texto") texto: String
    ): ApiResponseDto<List<FallaDto>>

    companion object {
        const val DEFAULT_BASE_URL = "http://35.180.21.42:8080"
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun createFallAppRetrofitClient(baseUrl: String): Retrofit {
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .client(
            okhttp3.OkHttpClient.Builder()
                .addInterceptor(
                    okhttp3.logging.HttpLoggingInterceptor().apply {
                        level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
                    }
                )
                .build()
        )
        .build()
}

