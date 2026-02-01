package com.fallapp.config;

import com.fallapp.model.Usuario.RolUsuario;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter para manejar el tipo ENUM rol_usuario de PostgreSQL
 */
@Converter(autoApply = true)
public class RolUsuarioConverter implements AttributeConverter<RolUsuario, String> {

    @Override
    public String convertToDatabaseColumn(RolUsuario attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public RolUsuario convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return RolUsuario.valueOf(dbData);
    }
}
