package com.fallapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad Falla
 * Tabla: fallas
 */
@Entity
@Table(name = "fallas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Falla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_falla")
    private Long idFalla;

    @Column(name = "nombre", nullable = false, unique = true, length = 255)
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Column(name = "seccion", nullable = false, length = 5)
    @NotBlank(message = "La sección es obligatoria")
    private String seccion;

    @Column(name = "fallera", length = 255)
    private String fallera;

    @Column(name = "presidente", nullable = false, length = 255)
    @NotBlank(message = "El presidente es obligatorio")
    private String presidente;

    @Column(name = "artista", length = 255)
    private String artista;

    @Column(name = "lema", columnDefinition = "TEXT")
    private String lema;

    @Column(name = "anyo_fundacion", nullable = false)
    @NotNull(message = "El año de fundación es obligatorio")
    private Integer anyoFundacion;

    @Column(name = "distintivo", length = 100)
    private String distintivo;

    @Column(name = "url_boceto", length = 500)
    private String urlBoceto;

    @Column(name = "experim", nullable = false)
    private Boolean experim = false;

    @Column(name = "ubicacion_lat", precision = 10, scale = 8)
    @DecimalMin(value = "-90.0", message = "Latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "Latitud debe estar entre -90 y 90")
    private BigDecimal ubicacionLat;

    @Column(name = "ubicacion_lon", precision = 11, scale = 8)
    @DecimalMin(value = "-180.0", message = "Longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "Longitud debe estar entre -180 y 180")
    private BigDecimal ubicacionLon;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "web_oficial", length = 255)
    private String webOficial;

    @Column(name = "telefono_contacto", length = 20)
    private String telefonoContacto;

    @Column(name = "email_contacto", length = 120)
    @Email(message = "Email de contacto debe ser válido")
    private String emailContacto;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 50)
    private CategoriaFalla categoria = CategoriaFalla.sin_categoria;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @Column(name = "datos_json", columnDefinition = "JSONB")
    private String datosJson;

    // Relaciones
    @OneToMany(mappedBy = "falla", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Evento> eventos;

    @OneToMany(mappedBy = "falla", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Usuario> usuarios;

    @OneToMany(mappedBy = "falla", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Comentario> comentarios;

    /**
     * Enum para categorías de falla
     */
    public enum CategoriaFalla {
        especial,
        primera,
        segunda,
        tercera,
        cuarta,
        quinta,
        sin_categoria
    }
}
