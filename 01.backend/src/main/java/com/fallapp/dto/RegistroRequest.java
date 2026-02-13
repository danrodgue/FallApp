package com.fallapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registro de nuevo usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequest {
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String contrasena;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    private String nombreCompleto;

    private Long idFalla;  // Opcional: para asociar a una falla
    
    /**
     * Rol del usuario a crear. Valores: "admin", "casal", "usuario"
     * Por defecto es "usuario" si no se especifica.
     * Si se especifica "casal", entonces idFalla es obligatorio.
     */
    private String rol;
    
    // Campos opcionales adicionales
    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String telefono;
    
    @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
    private String direccion;
    
    @Size(max = 100, message = "La ciudad no puede superar 100 caracteres")
    private String ciudad;
    
    @Size(max = 10, message = "El código postal no puede superar 10 caracteres")
    private String codigoPostal;
}
