package com.fallapp.features.profile.domain.model

/**
 * Modelo de dominio para el perfil del usuario.
 *
 * Contiene solo los datos que deben mostrarse en la UI,
 * excluyendo campos sensibles según especificación.
 *
 * Campos incluidos:
 * - email: Email del usuario
 * - nombreCompleto: Nombre completo
 * - rol: Rol del usuario (fallero, admin, casal)
 * - telefono: Teléfono de contacto (opcional)
 * - direccion: Dirección (opcional)
 * - ciudad: Ciudad (opcional)
 * - codigoPostal: Código postal (opcional)
 * - fechaCreacion: Fecha de creación (no editable)
 *
 * Campos EXCLUIDOS (según especificación):
 * - idFalla, nombreFalla, activo, fechaActualizacion, idUsuario
 *
 * @property idUsuario ID único del usuario (solo internamente, no mostrado)
 * @property email Email del usuario
 * @property nombreCompleto Nombre completo del usuario
 * @property rol Rol del usuario
 * @property telefono Teléfono de contacto (opcional)
 * @property direccion Dirección (opcional)
 * @property ciudad Ciudad (opcional)
 * @property codigoPostal Código postal (opcional)
 * @property fechaCreacion Fecha de creación (no editable)
 */
data class UsuarioPerfil(
    val idUsuario: Long,
    val email: String,
    val nombreCompleto: String,
    val rol: String,
    val telefono: String? = null,
    val direccion: String? = null,
    val ciudad: String? = null,
    val codigoPostal: String? = null,
    val fechaCreacion: String
)

