package com.fallapp.features.profile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO para la respuesta del endpoint GET /api/usuarios/{id}
 * 
 * Contiene toda la información del perfil del usuario.
 * 
 * Formato de respuesta de la API:
 * ```json
 * {
 *   "exito": true,
 *   "mensaje": "Usuario recuperado",
 *   "datos": {
 *     "idUsuario": 5,
 *     "email": "demo@fallapp.es",
 *     "nombreCompleto": "Usuario Demostración",
 *     "rol": "usuario",
 *     "idFalla": null,
 *     "nombreFalla": null,
 *     "activo": true,
 *     "telefono": null,
 *     "direccion": null,
 *     "ciudad": null,
 *     "codigoPostal": null,
 *     "fechaCreacion": "2026-02-11T08:33:43.917722",
 *     "fechaActualizacion": "2026-02-11T08:35:37.412343"
 *   },
 *   "timestamp": "2026-02-01T18:30:00"
 * }
 * ```
 * 
 * Campos a OCULTAR en UI (según especificación):
 * - idFalla
 * - nombreFalla
 * - activo
 * - fechaActualizacion
 * - idUsuario
 * 
 * Campos NO EDITABLES:
 * - fechaCreacion
 * 
 * @property idUsuario ID único del usuario (NO MOSTRAR)
 * @property email Email del usuario
 * @property nombreCompleto Nombre completo
 * @property rol Rol del usuario (fallero, admin, casal)
 * @property idFalla ID de la falla asociada (NO MOSTRAR)
 * @property nombreFalla Nombre de la falla asociada (NO MOSTRAR)
 * @property activo Estado del usuario (NO MOSTRAR)
 * @property telefono Teléfono de contacto (opcional, editable)
 * @property direccion Dirección (opcional, editable)
 * @property ciudad Ciudad (opcional, editable)
 * @property codigoPostal Código postal (opcional, editable)
 * @property fechaCreacion Fecha de creación (NO EDITABLE)
 * @property fechaActualizacion Fecha de última actualización (NO MOSTRAR)
 */
@Serializable
data class UsuarioPerfilDto(
    @SerialName("idUsuario")
    val idUsuario: Long,
    
    @SerialName("email")
    val email: String,
    
    @SerialName("nombreCompleto")
    val nombreCompleto: String,
    
    @SerialName("rol")
    val rol: String,
    
    @SerialName("idFalla")
    val idFalla: Long? = null,
    
    @SerialName("nombreFalla")
    val nombreFalla: String? = null,
    
    @SerialName("activo")
    val activo: Boolean = true,
    
    @SerialName("telefono")
    val telefono: String? = null,
    
    @SerialName("direccion")
    val direccion: String? = null,
    
    @SerialName("ciudad")
    val ciudad: String? = null,
    
    @SerialName("codigoPostal")
    val codigoPostal: String? = null,
    
    @SerialName("fechaCreacion")
    val fechaCreacion: String,
    
    @SerialName("fechaActualizacion")
    val fechaActualizacion: String? = null
)

