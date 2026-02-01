package com.fallapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad Ninot
 * Tabla: ninots
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

    @Column(name = "nombre_ninot", nullable = false, length = 255)
    @NotBlank(message = "El nombre del ninot es obligatorio")
    private String nombreNinot;

    @Column(name = "titulo_obra", nullable = false, length = 255)
    @NotBlank(message = "El título de la obra es obligatorio")
    private String tituloObra;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "altura_metros", precision = 6, scale = 2)
    private BigDecimal alturaMetros;

    @Column(name = "ancho_metros", precision = 6, scale = 2)
    private BigDecimal anchoMetros;

    @Column(name = "material_principal", length = 100)
    private String materialPrincipal;

    @Column(name = "url_imagen_principal", length = 500)
    private String urlImagenPrincipal;

    @Column(name = "url_imagenes_adicionales", columnDefinition = "TEXT[]")
    private String[] urlImagenesAdicionales;

    @Column(name = "artista_constructor", length = 255)
    private String artistaConstructor;

    @Column(name = "anyo_construccion")
    private Integer anyoConstruccion;

    @Column(name = "premiado", nullable = false)
    private Boolean premiado = false;

    @Column(name = "categoria_premio", length = 100)
    private String categoriaPremio;

    @Column(name = "notas_tecnicas", columnDefinition = "TEXT")
    private String notasTecnicas;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    // Relaciones
    @OneToMany(mappedBy = "ninot", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Voto> votos;

    @OneToMany(mappedBy = "ninot", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Comentario> comentarios;
}
