package com.example.Fallapp.repository;

import com.example.Fallapp.model.Falla;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface FallaRepository extends MongoRepository<Falla, String>, CustomFallaRepository {

    List<Falla> findBySeccion(String seccion);

    List<Falla> findByNombre(String nombre);

    List<Falla> findByArtistaId(String artistaId);

    @Query("{ 'anyoFundacion' : { $gte: ?0, $lte: ?1 } }")
    List<Falla> findByAnyoFundacionBetween(Integer desde, Integer hasta);

    List<Falla> findByLemaContainingIgnoreCase(String lema);
}

