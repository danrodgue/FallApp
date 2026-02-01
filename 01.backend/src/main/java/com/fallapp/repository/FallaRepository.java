package com.fallapp.repository;

import com.fallapp.model.Falla;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para entidad Falla
 */
@Repository
public interface FallaRepository extends JpaRepository<Falla, Long> {

    /**
     * Buscar falla por nombre
     */
    Optional<Falla> findByNombre(String nombre);

    /**
     * Verificar si existe falla con nombre
     */
    boolean existsByNombre(String nombre);

    /**
     * Buscar fallas por sección
     */
    Page<Falla> findBySeccion(String seccion, Pageable pageable);

    /**
     * Buscar fallas por categoría
     */
    Page<Falla> findByCategoria(Falla.CategoriaFalla categoria, Pageable pageable);

    /**
     * Búsqueda full-text en nombre, lema, artista
     * Usa la búsqueda de texto completo de PostgreSQL
     */
    @Query(value = "SELECT * FROM fallas f WHERE " +
            "to_tsvector('spanish', COALESCE(f.nombre, '') || ' ' || " +
            "COALESCE(f.lema, '') || ' ' || COALESCE(f.artista, '')) " +
            "@@ plainto_tsquery('spanish', :texto)", 
            nativeQuery = true)
    List<Falla> buscarPorTexto(@Param("texto") String texto);

    /**
     * Buscar fallas cercanas a una ubicación
     * Usa fórmula de Haversine para calcular distancia
     */
    @Query(value = "SELECT *, " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(ubicacion_lat)) * " +
            "cos(radians(ubicacion_lon) - radians(:lon)) + " +
            "sin(radians(:lat)) * sin(radians(ubicacion_lat)))) AS distancia " +
            "FROM fallas " +
            "WHERE ubicacion_lat IS NOT NULL AND ubicacion_lon IS NOT NULL " +
            "HAVING distancia < :radioKm " +
            "ORDER BY distancia",
            nativeQuery = true)
    List<Falla> buscarFallasCercanas(
            @Param("lat") BigDecimal latitud,
            @Param("lon") BigDecimal longitud,
            @Param("radioKm") BigDecimal radioKm);

    /**
     * Contar fallas por categoría
     */
    long countByCategoria(Falla.CategoriaFalla categoria);
}
