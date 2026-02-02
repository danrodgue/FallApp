package com.fallapp.repository;

import com.fallapp.model.Falla;
import com.fallapp.model.Ninot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para entidad Ninot (Versión Simplificada v2.0)
 */
@Repository
public interface NinotRepository extends JpaRepository<Ninot, Long> {

    /**
     * Buscar ninots por falla
     */
    Page<Ninot> findByFalla(Falla falla, Pageable pageable);

    /**
     * Contar ninots por falla
     */
    long countByFalla(Falla falla);
}
