package com.example.fallapp.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.fallapp.data.local.FallAppDatabase
import com.example.fallapp.data.remote.FallAppApi
import com.example.fallapp.data.remote.FallAppApi.Companion.DEFAULT_BASE_URL
import com.example.fallapp.data.remote.createFallAppRetrofitClient
import com.example.fallapp.data.repository.AuthRepositoryImpl
import com.example.fallapp.data.repository.FallaRepositoryImpl
import com.example.fallapp.domain.repository.AuthRepository
import com.example.fallapp.domain.repository.FallaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences =
        context.getSharedPreferences("fallapp_prefs", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideRetrofit(): retrofit2.Retrofit =
        createFallAppRetrofitClient(DEFAULT_BASE_URL)

    @Provides
    @Singleton
    fun provideFallAppApi(
        retrofit: retrofit2.Retrofit
    ): FallAppApi = retrofit.create(FallAppApi::class.java)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): FallAppDatabase =
        Room.databaseBuilder(
            context,
            FallAppDatabase::class.java,
            "fallapp.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: FallAppApi,
        prefs: SharedPreferences
    ): AuthRepository = AuthRepositoryImpl(api, prefs)

    @Provides
    @Singleton
    fun provideFallaRepository(
        api: FallAppApi
    ): FallaRepository = FallaRepositoryImpl(api)
}

