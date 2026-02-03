package com.example.fallapp.data.remote

import com.example.fallapp.data.remote.dto.ApiResponseDto
import com.example.fallapp.data.remote.dto.LoginDataDto
import com.example.fallapp.data.remote.dto.LoginRequestDto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FallAppApi {

    @POST("/api/auth/login")
    @Headers("Content-Type: application/json")
    suspend fun login(
        @Body body: LoginRequestDto
    ): ApiResponseDto<LoginDataDto>

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

