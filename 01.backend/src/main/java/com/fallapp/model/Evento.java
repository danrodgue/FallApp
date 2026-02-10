package com.fallapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad Evento
 * Tabla: eventos
 */
@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long idEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_falla", nullable = false)
    @NotNull(message = "La falla es obligatoria")
    private Falla falla;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20, columnDefinition = "tipo_evento")
    private TipoEvento tipo;

    @Column(name = "nombre", nullable = false, length = 255)
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_evento", nullable = false)
    @NotNull(message = "La fecha del evento es obligatoria")
    private LocalDateTime fechaEvento;

    @Column(name = "ubicacion", length = 255)
    private String ubicacion;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "url_imagen", length = 500)
    private String urlImagen;

    /**
     * Imagen principal del evento almacenada como binario en la base de datos.
     * Se mantiene el campo urlImagen para compatibilidad con clientes que usen URLs.
     *
     * Tipo en BD recomendado: BYTEA
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "imagen")
    private byte[] imagen;

    /**
     * Content-Type original de la imagen del evento (image/jpeg, image/png, etc.)
     */
    @Column(name = "imagen_content_type", length = 100)
    private String imagenContentType;

    @Column(name = "participantes_estimado")
    private Integer participantesEstimado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    /**
     * Enum para tipos de evento
     */
    public enum TipoEvento {
        planta,
        crema,
        ofrenda,
        infantil,
        concierto,
        exposicion,
        encuentro,
        cena,
        teatro,
        otro
    }
}
