package com.fallapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad Voto
 * Tabla: votos
 */
@Entity
@Table(name = "votos", 
       uniqueConstraints = @UniqueConstraint(
           name = "uk_usuario_ninot_tipo",
           columnNames = {"id_usuario", "id_ninot", "tipo_voto"}
       ))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_voto")
    private Long idVoto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @NotNull(message = "El usuario es obligatorio")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_falla", nullable = false)
    @NotNull(message = "La falla es obligatoria")
    private Falla falla;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_voto", nullable = false, length = 20, columnDefinition = "tipo_voto")
    private TipoVoto tipoVoto;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    /**
     * Enum para tipos de voto
     */
    public enum TipoVoto {
        favorito,
        ingenioso,
        critico,
        artistico,
        rating
    }
}
