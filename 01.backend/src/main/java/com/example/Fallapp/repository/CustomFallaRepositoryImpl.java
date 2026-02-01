package com.example.Fallapp.repository;

import com.example.Fallapp.model.Falla;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CustomFallaRepositoryImpl implements CustomFallaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void actualizarDistintivoPorIdFalla(Long idFalla, String nuevoDistintivo) {
        Falla falla = entityManager.find(Falla.class, idFalla);
        if (falla != null) {
            falla.setDistintivo(nuevoDistintivo);
            entityManager.merge(falla);
        }
    }
}

