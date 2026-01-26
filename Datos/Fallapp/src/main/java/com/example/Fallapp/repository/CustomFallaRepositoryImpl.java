package com.example.Fallapp.repository;

import com.example.Fallapp.model.Falla;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class CustomFallaRepositoryImpl implements CustomFallaRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public CustomFallaRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void actualizarDistintivoPorIdFalla(Long idFalla, String nuevoDistintivo) {
        Query query = new Query(Criteria.where("idFalla").is(idFalla));
        Update update = new Update().set("distintivo", nuevoDistintivo);
        mongoTemplate.updateFirst(query, update, Falla.class);
    }
}

