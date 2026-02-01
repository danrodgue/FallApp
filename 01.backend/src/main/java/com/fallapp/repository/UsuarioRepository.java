package com.fallapp.repository;

import com.fallapp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para entidad Usuario
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Buscar usuario por email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verificar si existe usuario con email
     */
    boolean existsByEmail(String email);

    /**
     * Buscar usuario por email y que esté activo
     */
    Optional<Usuario> findByEmailAndActivoTrue(String email);

    /**
     * Contar usuarios por rol
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol = :rol")
    long countByRol(@Param("rol") Usuario.RolUsuario rol);

    /**
     * Contar usuarios activos
     */
    long countByActivoTrue();
    
    /**
     * Buscar todos los usuarios activos
     */
    java.util.List<Usuario> findByActivoTrue();
}
