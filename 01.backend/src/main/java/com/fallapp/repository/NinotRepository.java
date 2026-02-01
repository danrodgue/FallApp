package com.fallapp.repository;

import com.fallapp.model.Falla;
import com.fallapp.model.Ninot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para entidad Ninot
 */
@Repository
public interface NinotRepository extends JpaRepository<Ninot, Long> {

    /**
     * Buscar ninots por falla
     */
    Page<Ninot> findByFalla(Falla falla, Pageable pageable);

    /**
     * Buscar ninots premiados
     */
    Page<Ninot> findByPremiadoTrue(Pageable pageable);

    /**
     * Clasificación de ninots por número de votos
     */
    @Query("SELECT n FROM Ninot n LEFT JOIN n.votos v " +
           "GROUP BY n.idNinot " +
           "ORDER BY COUNT(v) DESC")
    List<Ninot> findClasificacionPorVotos(Pageable pageable);

    /**
     * Clasificación de ninots por votos con tipo específico
     */
    @Query("SELECT n FROM Ninot n LEFT JOIN n.votos v " +
           "WHERE v.tipoVoto = :tipoVoto " +
           "GROUP BY n.idNinot " +
           "ORDER BY COUNT(v) DESC")
    List<Ninot> findClasificacionPorTipoVoto(
            @Param("tipoVoto") com.fallapp.model.Voto.TipoVoto tipoVoto, 
            Pageable pageable);

    /**
     * Contar ninots por falla
     */
    long countByFalla(Falla falla);

    /**
     * Contar ninots premiados
     */
    long countByPremiadoTrue();
}
