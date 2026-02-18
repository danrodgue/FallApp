package com.fallapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de Usuario
 * No incluye información sensible como contraseñas
 * Actualizado: 2026-02-04 - Añadidos campos de dirección
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long idUsuario;
    private String email;
    private String nombreCompleto;
    private String rol;
    private Long idFalla;
    private String nombreFalla;
    private Boolean activo;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String codigoPostal;
    private String imagenNombre;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
