package com.example.Fallapp.repository;

import com.example.Fallapp.model.Falla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FallaRepository extends JpaRepository<Falla, Long>, CustomFallaRepository {

    List<Falla> findBySeccion(String seccion);

    List<Falla> findByNombre(String nombre);

    List<Falla> findByArtistaId(String artistaId);

    @Query("SELECT f FROM Falla f WHERE f.anyo_fundacion >= :desde AND f.anyo_fundacion <= :hasta")
    List<Falla> findByAnyoFundacionBetween(@Param("desde") Integer desde, @Param("hasta") Integer hasta);

    List<Falla> findByLemaContainingIgnoreCase(String lema);
}

