package com.fallapp.repository;

import com.fallapp.model.Evento;
import com.fallapp.model.Falla;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para entidad Evento
 */
@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    /**
     * Buscar eventos por falla
     */
    Page<Evento> findByFalla(Falla falla, Pageable pageable);

    /**
     * Buscar eventos por tipo
     */
    Page<Evento> findByTipo(Evento.TipoEvento tipo, Pageable pageable);

    /**
     * Buscar eventos por falla y tipo
     */
    Page<Evento> findByFallaAndTipo(Falla falla, Evento.TipoEvento tipo, Pageable pageable);

    /**
     * Buscar eventos futuros (desde hoy)
     */
    @Query("SELECT e FROM Evento e WHERE e.fechaEvento >= :fechaDesde ORDER BY e.fechaEvento ASC")
    List<Evento> findEventosFuturos(@Param("fechaDesde") LocalDateTime fechaDesde);

    /**
     * Buscar eventos en un rango de fechas
     */
    @Query("SELECT e FROM Evento e WHERE e.fechaEvento BETWEEN :desde AND :hasta ORDER BY e.fechaEvento ASC")
    Page<Evento> findByFechaEventoBetween(
            @Param("desde") LocalDateTime desde, 
            @Param("hasta") LocalDateTime hasta, 
            Pageable pageable);

    /**
     * Contar eventos por tipo
     */
    long countByTipo(Evento.TipoEvento tipo);

    /**
     * Buscar próximos N eventos
     */
    @Query("SELECT e FROM Evento e WHERE e.fechaEvento >= CURRENT_TIMESTAMP ORDER BY e.fechaEvento ASC")
    List<Evento> findProximosEventos(Pageable pageable);

    /**
     * Contar eventos por falla
     */
    long countByFalla(Falla falla);
}
