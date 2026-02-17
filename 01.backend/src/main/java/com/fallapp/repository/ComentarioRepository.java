package com.fallapp.repository;

import com.fallapp.model.Comentario;
import com.fallapp.model.Falla;
import com.fallapp.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * Contar comentarios por falla
     */
    long countByFalla(Falla falla);

    /**
     * Agrupar comentarios por sentimiento para una falla concreta.
     * Devuelve pares (sentimiento, total).
     */
    @Query("SELECT c.sentimiento, COUNT(c) " +
            "FROM Comentario c " +
            "WHERE c.falla = :falla AND c.sentimiento IS NOT NULL " +
            "GROUP BY c.sentimiento")
    List<Object[]> countByFallaGroupBySentimiento(@Param("falla") Falla falla);
}
