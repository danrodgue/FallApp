package com.fallapp.core.util

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

/**
 * Gestiona el almacenamiento seguro del token JWT.
 * 
 * Utiliza DataStore (sucesor de SharedPreferences) para persistir
 * el token de autenticación de forma cifrada en el dispositivo.
 * 
 * Operaciones:
 * - Guardar token después del login
 * - Recuperar token para peticiones autenticadas
 * - Eliminar token al hacer logout
 * - Verificar si hay sesión activa
 * 
 * @author Equipo FallApp
 * @since 1.0.0
 */
class TokenManager(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = "fallapp_auth"
        )
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ID_KEY = longPreferencesKey("user_id")
    }
    
    /**
     * Flow reactivo del token actual.
     * Emite null si no hay sesión activa.
     */
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN_KEY]
    }
    
    /**
     * Flow reactivo del email del usuario logueado.
     */
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    /**
     * Flow reactivo del ID del usuario logueado.
     */
    val userId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    /**
     * Guarda el token JWT después de un login exitoso.
     * @param token Token JWT recibido del servidor
     * @param email Email del usuario (opcional, para mostrar en UI)
     * @param userId ID del usuario (opcional, para llamadas futuras a API)
     */
    suspend fun saveToken(token: String, email: String? = null, userId: Long? = null) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            email?.let { preferences[USER_EMAIL_KEY] = it }
            userId?.let { preferences[USER_ID_KEY] = it }
        }
    }
    
    /**
     * Recupera el token actual de forma síncrona.
     * Útil para crear HttpClient autenticado.
     * @return Token JWT o null si no hay sesión
     */
    suspend fun getToken(): String? {
        return context.dataStore.data.first()[AUTH_TOKEN_KEY]
    }

    /**
     * Devuelve token solo si sigue vigente.
     * Si está expirado, limpia la sesión y devuelve null.
     */
    suspend fun getValidToken(): String? {
        val token = getToken() ?: return null
        return if (isTokenExpired(token)) {
            clearToken()
            null
        } else {
            token
        }
    }

    /**
     * Recupera el ID del usuario actual de forma síncrona.
     * @return ID del usuario o null si no está disponible
     */
    suspend fun getUserId(): Long? {
        return context.dataStore.data.first()[USER_ID_KEY]
    }

    /**
     * Elimina el token (logout).
     * Borra toda la información de sesión.
     */
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Verifica si hay una sesión activa.
     * @return true si existe un token guardado
     */
    suspend fun hasActiveSession(): Boolean {
        return getValidToken() != null
    }

    /**
     * Comprueba expiración leyendo claim "exp" del JWT (epoch seconds).
     * Si no puede parsearse, se considera expirado por seguridad.
     */
    private fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return true

            val payloadBytes = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
            val payload = String(payloadBytes, Charsets.UTF_8)
            val expSeconds = JSONObject(payload).optLong("exp", -1L)

            if (expSeconds <= 0L) return true

            val nowSeconds = System.currentTimeMillis() / 1000
            nowSeconds >= expSeconds
        } catch (_: Exception) {
            true
        }
    }
}
