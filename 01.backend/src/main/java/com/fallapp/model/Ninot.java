package com.fallapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad Ninot (Versión Simplificada v2.0)
 * Tabla: ninots
 * 
 * Propósito: Almacenar imágenes de ninots/bocetos asociados a fallas
 * Nota: Solo contiene campos con datos reales disponibles
 * 
 * @version 2.0
 * @since 2026-02-02
 */
@Entity
@Table(name = "ninots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ninot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ninot")
    private Long idNinot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_falla", nullable = false)
    @NotNull(message = "La falla es obligatoria")
    private Falla falla;

    @Column(name = "nombre", length = 255)
    private String nombre;  // Opcional

    @Column(name = "url_imagen", nullable = false, length = 500)
    @NotBlank(message = "La URL de la imagen es obligatoria")
    private String urlImagen;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}
