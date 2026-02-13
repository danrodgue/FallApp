package com.fallapp.core.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Wrapper para resultados de operaciones que pueden fallar.
 * 
 * Uso en toda la aplicación para manejo consistente de estados:
 * - Loading: Operación en progreso
 * - Success: Operación exitosa con datos
 * - Error: Operación fallida con información del error
 * 
 * @param T Tipo de datos en caso de éxito
 * 
 * @author Equipo FallApp
 * @since 1.0
 */
sealed class Result<out T> {
    
    /**
     * Estado de éxito con datos.
     * @param data Los datos resultantes de la operación
     */
    data class Success<T>(val data: T) : Result<T>()
    
    /**
     * Estado de error.
     * @param exception La excepción que causó el error
     * @param message Mensaje de error opcional (más legible que exception.message)
     */
    data class Error(
        val exception: Throwable,
        val message: String? = null
    ) : Result<Nothing>()
    
    /**
     * Estado de carga.
     */
    data object Loading : Result<Nothing>()
    
    // ============================================================
    // PROPIEDADES DE CONVENIENCIA
    // ============================================================
    
    /** Retorna true si el resultado es Success */
    val isSuccess: Boolean get() = this is Success
    
    /** Retorna true si el resultado es Error */
    val isError: Boolean get() = this is Error
    
    /** Retorna true si el resultado es Loading */
    val isLoading: Boolean get() = this is Loading
    
    // ============================================================
    // MÉTODOS DE EXTRACCIÓN DE DATOS
    // ============================================================
    
    /**
     * Retorna los datos si es Success, null en caso contrario.
     */
    fun getOrNull(): T? = (this as? Success)?.data
    
    /**
     * Retorna los datos si es Success, lanza excepción si es Error o Loading.
     * @throws Throwable si es Error
     * @throws IllegalStateException si es Loading
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
    }
    
    /**
     * Retorna los datos si es Success, o el valor por defecto si no.
     * @param default Valor por defecto a retornar
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> default
    }
    
    /**
     * Retorna los datos si es Success, o ejecuta el bloque para obtener un valor.
     * @param block Bloque que retorna el valor alternativo
     */
    inline fun getOrElse(block: (Throwable?) -> @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> block(exception)
        is Loading -> block(null)
    }
    
    // ============================================================
    // MÉTODOS DE TRANSFORMACIÓN
    // ============================================================
    
    /**
     * Transforma los datos si es Success.
     * @param transform Función de transformación
     * @return Nuevo Result con datos transformados
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }
    
    /**
     * Transforma los datos si es Success, permitiendo retornar otro Result.
     * @param transform Función que retorna un Result
     * @return Result resultante de la transformación
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> this
    }
    
    /**
     * Transforma el error si es Error.
     * @param transform Función de transformación del error
     * @return Nuevo Result con error transformado
     */
    inline fun mapError(transform: (Throwable) -> Throwable): Result<T> = when (this) {
        is Success -> this
        is Error -> Error(transform(exception), message)
        is Loading -> this
    }
    
    // ============================================================
    // MÉTODOS DE ACCIÓN
    // ============================================================
    
    /**
     * Ejecuta una acción si es Success.
     * @param action Acción a ejecutar con los datos
     * @return El mismo Result (para encadenamiento)
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * Ejecuta una acción si es Error.
     * @param action Acción a ejecutar con la excepción y mensaje
     * @return El mismo Result (para encadenamiento)
     */
    inline fun onError(action: (Throwable, String?) -> Unit): Result<T> {
        if (this is Error) action(exception, message)
        return this
    }
    
    /**
     * Ejecuta una acción si es Loading.
     * @param action Acción a ejecutar
     * @return El mismo Result (para encadenamiento)
     */
    inline fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) action()
        return this
    }
    
    /**
     * Ejecuta una acción para cualquier estado.
     * @param onSuccess Acción para Success
     * @param onError Acción para Error
     * @param onLoading Acción para Loading
     */
    inline fun fold(
        onSuccess: (T) -> Unit,
        onError: (Throwable, String?) -> Unit,
        onLoading: () -> Unit = {}
    ) {
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(exception, message)
            is Loading -> onLoading()
        }
    }
    
    companion object {
        /**
         * Crea un Result.Success con los datos.
         */
        fun <T> success(data: T): Result<T> = Success(data)
        
        /**
         * Crea un Result.Error con la excepción y mensaje opcional.
         */
        fun error(exception: Throwable, message: String? = null): Result<Nothing> = 
            Error(exception, message)
        
        /**
         * Crea un Result.Loading.
         */
        fun loading(): Result<Nothing> = Loading
        
        /**
         * Ejecuta un bloque y envuelve el resultado en Result.
         * @param block Bloque a ejecutar
         * @return Result.Success con el resultado o Result.Error si falla
         */
        inline fun <T> runCatching(block: () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Throwable) {
                Error(e, e.message)
            }
        }
    }
}

// ============================================================
// EXTENSIONES PARA FLOW
// ============================================================

/**
 * Envuelve un Flow en Result, emitiendo Loading al inicio y Error en caso de excepción.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> = this
    .map<T, Result<T>> { Result.Success(it) }
    .onStart { emit(Result.Loading) }
    .catch { emit(Result.Error(it, it.message)) }

/**
 * Transforma los datos dentro de un Flow<Result<T>>.
 */
fun <T, R> Flow<Result<T>>.mapResult(transform: (T) -> R): Flow<Result<R>> = 
    map { it.map(transform) }
