package com.fallapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad Usuario
 * Tabla: usuarios
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email debe tener formato válido")
    private String email;

    @Column(name = "contraseña_hash", nullable = false, length = 255)
    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasenaHash;

    @Column(name = "nombre_completo", nullable = false, length = 255)
    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @Convert(converter = com.fallapp.config.RolUsuarioConverter.class)
    @Column(name = "rol", nullable = false, length = 20, columnDefinition = "VARCHAR")
    private RolUsuario rol = RolUsuario.usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_falla")
    private Falla falla;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(name = "imagen_nombre", length = 255)
    private String imagenNombre;

    @Transient
    private byte[] fotoPerfil;

    @Transient
    private String fotoPerfilContentType;

    /**
     * Enum para roles de usuario
     */
    public enum RolUsuario {
        admin,
        casal,
        usuario
    }
}
