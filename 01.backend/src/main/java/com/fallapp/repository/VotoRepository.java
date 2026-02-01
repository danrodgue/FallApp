package com.fallapp.repository;

import com.fallapp.model.Ninot;
import com.fallapp.model.Usuario;
import com.fallapp.model.Voto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para entidad Voto
 */
@Repository
public interface VotoRepository extends JpaRepository<Voto, Long> {

    /**
     * Buscar votos por usuario
     */
    Page<Voto> findByUsuario(Usuario usuario, Pageable pageable);

    /**
     * Buscar votos por ninot
     */
    Page<Voto> findByNinot(Ninot ninot, Pageable pageable);

    /**
     * Buscar votos por tipo
     */
    Page<Voto> findByTipoVoto(Voto.TipoVoto tipoVoto, Pageable pageable);

    /**
     * Buscar voto específico de usuario a ninot con tipo
     */
    Optional<Voto> findByUsuarioAndNinotAndTipoVoto(
            Usuario usuario, 
            Ninot ninot, 
            Voto.TipoVoto tipoVoto);

    /**
     * Verificar si usuario ya votó ninot con tipo específico
     */
    boolean existsByUsuarioAndNinotAndTipoVoto(
            Usuario usuario, 
            Ninot ninot, 
            Voto.TipoVoto tipoVoto);

    /**
     * Contar votos por ninot
     */
    long countByNinot(Ninot ninot);

    /**
     * Contar votos por tipo
     */
    long countByTipoVoto(Voto.TipoVoto tipoVoto);

    /**
     * Contar usuarios únicos que han votado
     */
    @Query("SELECT COUNT(DISTINCT v.usuario) FROM Voto v")
    long countUsuariosVotantes();

    /**
     * Obtener estadísticas de votos por tipo
     */
    @Query("SELECT v.tipoVoto, COUNT(v) FROM Voto v GROUP BY v.tipoVoto")
    List<Object[]> obtenerEstadisticasPorTipo();
}
