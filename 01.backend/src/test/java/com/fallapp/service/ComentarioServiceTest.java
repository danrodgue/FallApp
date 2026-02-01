package com.fallapp.service;

import com.fallapp.dto.ComentarioDTO;
import com.fallapp.model.Comentario;
import com.fallapp.model.Falla;
import com.fallapp.model.Ninot;
import com.fallapp.model.Usuario;
import com.fallapp.repository.ComentarioRepository;
import com.fallapp.repository.FallaRepository;
import com.fallapp.repository.NinotRepository;
import com.fallapp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ComentarioService
 * 
 * Cobertura:
 * - Crear comentario con validación de idFalla
 * - Crear comentario con validación de idNinot
 * - Obtener comentarios por falla
 * - Obtener comentarios por ninot
 * - Actualizar comentario existente
 * - Eliminar comentario
 * - Manejo de excepciones (entidades no encontradas)
 * 
 * @version 0.4.0
 * @since 2026-02-01
 */
@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FallaRepository fallaRepository;

    @Mock
    private NinotRepository ninotRepository;

    @InjectMocks
    private ComentarioService comentarioService;

    private Usuario usuarioMock;
    private Falla fallaMock;
    private Ninot ninotMock;
    private Comentario comentarioMock;

    @BeforeEach
    void setUp() {
        // Usuario mock
        usuarioMock = new Usuario();
        usuarioMock.setIdUsuario(1L);
        usuarioMock.setNombreCompleto("Test User");
        usuarioMock.setEmail("test@example.com");

        // Falla mock
        fallaMock = new Falla();
        fallaMock.setIdFalla(1L);
        fallaMock.setNombre("Falla Plaza del Ayuntamiento");
        fallaMock.setSeccion("Especial");

        // Ninot mock
        ninotMock = new Ninot();
        ninotMock.setIdNinot(1L);
        ninotMock.setNombreNinot("Ninot Test");
        ninotMock.setFalla(fallaMock);

        // Comentario mock
        comentarioMock = new Comentario();
        comentarioMock.setIdComentario(1L);
        comentarioMock.setContenido("Este es un comentario de prueba");
        comentarioMock.setUsuario(usuarioMock);
        comentarioMock.setFalla(fallaMock);
        comentarioMock.setCreadoEn(LocalDateTime.now());
    }

    @Test
    void testCrearComentarioEnFalla_Success() {
        // Arrange
        ComentarioDTO dto = new ComentarioDTO();
        dto.setIdUsuario(1L);
        dto.setIdFalla(1L);
        dto.setContenido("Comentario de prueba en falla");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentarioMock);

        // Act
        ComentarioDTO resultado = comentarioService.crear(dto);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdComentario());
        assertEquals("Este es un comentario de prueba", resultado.getContenido());
        verify(comentarioRepository, times(1)).save(any(Comentario.class));
    }

    @Test
    void testCrearComentarioEnNinot_Success() {
        // Arrange
        ComentarioDTO dto = new ComentarioDTO();
        dto.setIdUsuario(1L);
        dto.setIdNinot(1L);
        dto.setContenido("Comentario de prueba en ninot");

        comentarioMock.setFalla(null);
        comentarioMock.setNinot(ninotMock);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(ninotRepository.findById(1L)).thenReturn(Optional.of(ninotMock));
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentarioMock);

        // Act
        ComentarioDTO resultado = comentarioService.crear(dto);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdComentario());
        verify(comentarioRepository, times(1)).save(any(Comentario.class));
    }

    @Test
    void testCrearComentario_UsuarioNoEncontrado() {
        // Arrange
        ComentarioDTO dto = new ComentarioDTO();
        dto.setIdUsuario(999L);
        dto.setIdFalla(1L);
        dto.setContenido("Comentario con usuario inexistente");

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            comentarioService.crear(dto);
        });

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        verify(comentarioRepository, never()).save(any(Comentario.class));
    }

    @Test
    void testCrearComentario_FallaNoEncontrada() {
        // Arrange
        ComentarioDTO dto = new ComentarioDTO();
        dto.setIdUsuario(1L);
        dto.setIdFalla(999L);
        dto.setContenido("Comentario con falla inexistente");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(fallaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            comentarioService.crear(dto);
        });

        assertTrue(exception.getMessage().contains("Falla no encontrada"));
        verify(comentarioRepository, never()).save(any(Comentario.class));
    }

    @Test
    void testObtenerPorFalla_Success() {
        // Arrange
        List<Comentario> comentarios = Arrays.asList(comentarioMock);
        when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));
        when(comentarioRepository.findByFallaOrderByCreadoEnDesc(fallaMock)).thenReturn(comentarios);

        // Act
        List<ComentarioDTO> resultado = comentarioService.obtenerPorFalla(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Este es un comentario de prueba", resultado.get(0).getContenido());
        verify(comentarioRepository, times(1)).findByFallaOrderByCreadoEnDesc(fallaMock);
    }

    @Test
    void testObtenerPorNinot_Success() {
        // Arrange
        comentarioMock.setFalla(null);
        comentarioMock.setNinot(ninotMock);
        List<Comentario> comentarios = Arrays.asList(comentarioMock);
        
        when(ninotRepository.findById(1L)).thenReturn(Optional.of(ninotMock));
        when(comentarioRepository.findByNinotOrderByCreadoEnDesc(ninotMock)).thenReturn(comentarios);

        // Act
        List<ComentarioDTO> resultado = comentarioService.obtenerPorNinot(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(comentarioRepository, times(1)).findByNinotOrderByCreadoEnDesc(ninotMock);
    }

    @Test
    void testActualizarComentario_Success() {
        // Arrange
        ComentarioDTO dto = new ComentarioDTO();
        dto.setIdUsuario(1L);
        dto.setIdFalla(1L);
        dto.setContenido("Comentario actualizado");

        when(comentarioRepository.findById(1L)).thenReturn(Optional.of(comentarioMock));
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentarioMock);

        // Act
        ComentarioDTO resultado = comentarioService.actualizar(1L, dto);

        // Assert
        assertNotNull(resultado);
        verify(comentarioRepository, times(1)).save(any(Comentario.class));
    }

    @Test
    void testEliminarComentario_Success() {
        // Arrange
        when(comentarioRepository.findById(1L)).thenReturn(Optional.of(comentarioMock));
        doNothing().when(comentarioRepository).delete(comentarioMock);

        // Act
        comentarioService.eliminar(1L);

        // Assert
        verify(comentarioRepository, times(1)).delete(comentarioMock);
    }

    @Test
    void testEliminarComentario_NoEncontrado() {
        // Arrange
        when(comentarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            comentarioService.eliminar(999L);
        });

        assertTrue(exception.getMessage().contains("Comentario no encontrado"));
        verify(comentarioRepository, never()).delete(any(Comentario.class));
    }

    @Test
    void testObtenerTodos_Success() {
        // Arrange
        List<Comentario> comentarios = Arrays.asList(comentarioMock);
        when(comentarioRepository.findAll()).thenReturn(comentarios);

        // Act
        List<ComentarioDTO> resultado = comentarioService.obtenerTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(comentarioRepository, times(1)).findAll();
    }
}
