package com.fallapp.repository;

import com.fallapp.model.Comentario;
import com.fallapp.model.Falla;
import com.fallapp.model.Ninot;
import com.fallapp.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para entidad Comentario
 */
@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    /**
     * Buscar comentarios por usuario
     */
    Page<Comentario> findByUsuario(Usuario usuario, Pageable pageable);

    /**
     * Buscar comentarios por falla ordenados por fecha
     */
    List<Comentario> findByFallaOrderByCreadoEnDesc(Falla falla);

    /**
     * Buscar comentarios por falla con paginación
     */
    Page<Comentario> findByFalla(Falla falla, Pageable pageable);

    /**
     * Buscar comentarios por ninot ordenados por fecha
     */
    List<Comentario> findByNinotOrderByCreadoEnDesc(Ninot ninot);

    /**
     * Buscar comentarios por ninot con paginación
     */
    Page<Comentario> findByNinot(Ninot ninot, Pageable pageable);

    /**
     * Contar comentarios por falla
     */
    long countByFalla(Falla falla);

    /**
     * Contar comentarios por ninot
     */
    long countByNinot(Ninot ninot);
}
