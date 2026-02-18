package com.fallapp.service;

import com.fallapp.dto.ComentarioDTO;
import com.fallapp.model.Comentario;
import com.fallapp.model.Falla;
import com.fallapp.model.Usuario;
import com.fallapp.repository.ComentarioRepository;
import com.fallapp.repository.FallaRepository;
import com.fallapp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para gestión de Comentarios
 * 
 * Responsabilidades:
 * - CRUD completo de comentarios (crear, leer, actualizar, eliminar)
 * - Validación de reglas de negocio (comentario requiere falla O ninot)
 * - Filtrado por falla o ninot
 * - Conversión entre entidades y DTOs
 * - Gestión de relaciones con Usuario, Falla y Ninot
 * 
 * Validaciones de Negocio:
 * - Comentario debe tener idFalla XOR idNinot (no ambos, no ninguno)
 * - Usuario debe existir en BD
 * - Falla o Ninot referenciado debe existir
 * - Contenido: 3-500 caracteres (validado en DTO con @Size)
 * 
 * Transaccionalidad:
 * - @Transactional(readOnly=true) por defecto para queries
 * - @Transactional en métodos de escritura (crear, actualizar, eliminar)
 * - Rollback automático en caso de excepción
 * 
 * Ordenamiento:
 * - Comentarios retornados ordenados por fecha DESC (más recientes primero)
 * - Implementado en repository con custom queries
 * 
 * @see ComentarioRepository Queries personalizadas findByFalla, findByNinot
 * @see ComentarioDTO DTO con validaciones Bean Validation
 * @see ComentarioController REST controller que consume este servicio
 * 
 * @author FallApp Development Team
 * @version 0.4.0
 * @since 2026-02-01
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComentarioService {
    
    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final FallaRepository fallaRepository;
    private final SentimentAnalysisService sentimentAnalysisService;
    /**
     * Obtener todos los comentarios del sistema
     * 
     * Sin filtros, retorna todos los comentarios existentes.
     * Útil para administración o visualización completa.
     * 
     * Performance: O(n) - No usar en producción con >1000 comentarios.
     * Considerar paginación si el volumen crece.
     * 
     * @return Lista de todos los ComentarioDTO
     */
    public List<ComentarioDTO> obtenerTodos() {
        return comentarioRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener comentarios asociados a una falla específica
     * 
     * Retorna solo comentarios donde comentario.falla.id = idFalla
     * Ordenados por fecha de creación DESC (más recientes primero)
     * 
     * Ejemplo de uso:
     * - Pantalla de detalle de falla: mostrar comentarios de usuarios
     * - Timeline de feedback sobre una falla
     * 
     * @param idFalla ID de la falla
     * @return Lista de ComentarioDTO asociados a la falla
     * @throws RuntimeException Si la falla no existe
     */
    public List<ComentarioDTO> obtenerPorFalla(Long idFalla) {
        Falla falla = fallaRepository.findById(idFalla)
                .orElseThrow(() -> new RuntimeException("Falla no encontrada con ID: " + idFalla));
        
        // Repository custom query: findByFallaOrderByCreadoEnDesc
        return comentarioRepository.findByFallaOrderByCreadoEnDesc(falla).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
        // Método obtenerPorNinot eliminado: los comentarios se asocian únicamente a `falla`.
    
    /**
     * Obtener comentario por ID
     */
    public ComentarioDTO obtenerPorId(Long id) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado con ID: " + id));
        return convertirADTO(comentario);
    }
    
    /**
     * Crear nuevo comentario
     */
    @Transactional
    public ComentarioDTO crear(ComentarioDTO comentarioDTO) {
        // Validar que la falla esté presente
        if (comentarioDTO.getIdFalla() == null) {
            throw new IllegalArgumentException("Debe especificar idFalla");
        }
        
        Usuario usuario = usuarioRepository.findById(comentarioDTO.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + comentarioDTO.getIdUsuario()));
        
        Comentario comentario = new Comentario();
        comentario.setUsuario(usuario);
        comentario.setContenido(comentarioDTO.getContenido());
        
        if (comentarioDTO.getIdFalla() != null) {
            Falla falla = fallaRepository.findById(comentarioDTO.getIdFalla())
                    .orElseThrow(() -> new RuntimeException("Falla no encontrada con ID: " + comentarioDTO.getIdFalla()));
            comentario.setFalla(falla);
        }
        
        Comentario comentarioSaved = comentarioRepository.save(comentario);

        // Lanzar análisis de sentimiento en segundo plano DESPUÉS del commit
        // para evitar condiciones de carrera (el hilo async podría no ver el registro aún).
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sentimentAnalysisService.analizarComentarioAsync(
                            comentarioSaved.getIdComentario(),
                            comentarioSaved.getContenido()
                    );
                }
            });
        } else {
            sentimentAnalysisService.analizarComentarioAsync(
                    comentarioSaved.getIdComentario(),
                    comentarioSaved.getContenido()
            );
        }

        return convertirADTO(comentarioSaved);
    }
    
    /**
     * Actualizar comentario existente
     */
    @Transactional
    public ComentarioDTO actualizar(Long id, ComentarioDTO comentarioDTO) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado con ID: " + id));
        
        // Solo actualizar el contenido
        comentario.setContenido(comentarioDTO.getContenido());
        
        Comentario comentarioActualizado = comentarioRepository.save(comentario);
        return convertirADTO(comentarioActualizado);
    }
    
    /**
     * Eliminar comentario
     */
    @Transactional
    public void eliminar(Long id) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado con ID: " + id));
        
        comentarioRepository.delete(comentario);
    }
    
    /**
     * Reanaliza el sentimiento de todos los comentarios que tienen sentimiento NULL.
     * Útil para comentarios creados antes de tener el token de Hugging Face configurado.
     * Lanza el análisis en segundo plano para cada uno (no bloquea).
     *
     * @return Número de comentarios encolados para reanálisis
     */
    @Transactional(readOnly = true)
    public int reanalizarSentimientoPendientes() {
        List<Comentario> pendientes = comentarioRepository.findBySentimientoIsNull();
        for (Comentario c : pendientes) {
            if (c.getContenido() != null && !c.getContenido().isBlank()) {
                sentimentAnalysisService.analizarComentarioAsync(c.getIdComentario(), c.getContenido());
            }
        }
        return pendientes.size();
    }

    /**
     * Convertir entidad a DTO
     */
    private ComentarioDTO convertirADTO(Comentario comentario) {
        return ComentarioDTO.builder()
                .idComentario(comentario.getIdComentario())
                .idUsuario(comentario.getUsuario() != null ? comentario.getUsuario().getIdUsuario() : null)
                .nombreUsuario(comentario.getUsuario() != null ? comentario.getUsuario().getNombreCompleto() : null)
                .idFalla(comentario.getFalla() != null ? comentario.getFalla().getIdFalla() : null)
                .nombreFalla(comentario.getFalla() != null ? comentario.getFalla().getNombre() : null)
                .contenido(comentario.getContenido())
                .fechaCreacion(comentario.getCreadoEn())
                .fechaActualizacion(comentario.getActualizadoEn())
                .sentimiento(comentario.getSentimiento())
                .build();
    }
}
