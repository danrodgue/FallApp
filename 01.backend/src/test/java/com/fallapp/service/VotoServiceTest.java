package com.fallapp.service;

import com.fallapp.dto.CrearVotoRequest;
import com.fallapp.dto.VotoDTO;
import com.fallapp.exception.BadRequestException;
import com.fallapp.exception.ResourceNotFoundException;
import com.fallapp.model.Falla;
import com.fallapp.model.Usuario;
import com.fallapp.model.Voto;
import com.fallapp.repository.FallaRepository;
import com.fallapp.repository.UsuarioRepository;
import com.fallapp.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para VotoService
 *
 * Cobertura: votar, obtenerVotosUsuario, obtenerVotosFalla, eliminar
 * Patrón: Microtest (Google) - 1 test = 1 comportamiento
 *
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VotoService Tests")
class VotoServiceTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FallaRepository fallaRepository;

    @InjectMocks
    private VotoService votoService;

    private Usuario usuarioMock;
    private Falla fallaMock;
    private Voto votoMock;

    @BeforeEach
    void setUp() {
        usuarioMock = new Usuario();
        usuarioMock.setIdUsuario(1L);
        usuarioMock.setNombreCompleto("Juan García");
        usuarioMock.setEmail("juan@example.com");

        fallaMock = new Falla();
        fallaMock.setIdFalla(15L);
        fallaMock.setNombre("Falla Na Jordana");
        fallaMock.setSeccion("E");

        votoMock = new Voto();
        votoMock.setIdVoto(100L);
        votoMock.setUsuario(usuarioMock);
        votoMock.setFalla(fallaMock);
        votoMock.setTipoVoto(Voto.TipoVoto.EXPERIMENTAL);
        votoMock.setValor(1);
        votoMock.setCreadoEn(LocalDateTime.now());
    }

    // ==========================================
    // VOTAR
    // ==========================================

    @Nested
    @DisplayName("votar()")
    class VotarTests {

        @Test
        @DisplayName("Votar con tipo EXPERIMENTAL debe crear voto exitosamente")
        void votar_conTipoExperimental_creaVotoExitosamente() {
            // Given
            CrearVotoRequest request = new CrearVotoRequest();
            request.setIdFalla(15L);
            request.setTipoVoto("EXPERIMENTAL");

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(fallaRepository.findById(15L)).thenReturn(Optional.of(fallaMock));
            when(votoRepository.existsByUsuarioAndFallaAndTipoVoto(
                    eq(usuarioMock), eq(fallaMock), eq(Voto.TipoVoto.EXPERIMENTAL))).thenReturn(false);
            when(votoRepository.save(any(Voto.class))).thenReturn(votoMock);

            // When
            VotoDTO resultado = votoService.votar(1L, request);

            // Then
            assertNotNull(resultado, "El resultado no debe ser null");
            assertEquals(100L, resultado.getIdVoto());
            assertEquals(1L, resultado.getIdUsuario());
            assertEquals(15L, resultado.getIdFalla());
            assertEquals("EXPERIMENTAL", resultado.getTipoVoto());
            verify(votoRepository).save(any(Voto.class));
        }

        @Test
        @DisplayName("Votar con tipo INGENIO_Y_GRACIA debe crear voto exitosamente")
        void votar_conTipoIngenioYGracia_creaVotoExitosamente() {
            // Given
            CrearVotoRequest request = new CrearVotoRequest();
            request.setIdFalla(15L);
            request.setTipoVoto("INGENIO_Y_GRACIA");

            Voto votoIngenio = new Voto();
            votoIngenio.setIdVoto(101L);
            votoIngenio.setUsuario(usuarioMock);
            votoIngenio.setFalla(fallaMock);
            votoIngenio.setTipoVoto(Voto.TipoVoto.INGENIO_Y_GRACIA);
            votoIngenio.setValor(1);
            votoIngenio.setCreadoEn(LocalDateTime.now());

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(fallaRepository.findById(15L)).thenReturn(Optional.of(fallaMock));
            when(votoRepository.existsByUsuarioAndFallaAndTipoVoto(
                    any(), any(), eq(Voto.TipoVoto.INGENIO_Y_GRACIA))).thenReturn(false);
            when(votoRepository.save(any(Voto.class))).thenReturn(votoIngenio);

            // When
            VotoDTO resultado = votoService.votar(1L, request);

            // Then
            assertNotNull(resultado);
            assertEquals("INGENIO_Y_GRACIA", resultado.getTipoVoto());
        }

        @Test
        @DisplayName("Votar con tipo MONUMENTO debe crear voto exitosamente")
        void votar_conTipoMonumento_creaVotoExitosamente() {
            // Given
            CrearVotoRequest request = new CrearVotoRequest();
            request.setIdFalla(15L);
            request.setTipoVoto("MONUMENTO");

            Voto votoMonumento = new Voto();
            votoMonumento.setIdVoto(102L);
            votoMonumento.setUsuario(usuarioMock);
            votoMonumento.setFalla(fallaMock);
            votoMonumento.setTipoVoto(Voto.TipoVoto.MONUMENTO);
            votoMonumento.setValor(1);
            votoMonumento.setCreadoEn(LocalDateTime.now());

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(fallaRepository.findById(15L)).thenReturn(Optional.of(fallaMock));
            when(votoRepository.existsByUsuarioAndFallaAndTipoVoto(
                    any(), any(), eq(Voto.TipoVoto.MONUMENTO))).thenReturn(false);
            when(votoRepository.save(any(Voto.class))).thenReturn(votoMonumento);

            // When
            VotoDTO resultado = votoService.votar(1L, request);

            // Then
            assertNotNull(resultado);
            assertEquals("MONUMENTO", resultado.getTipoVoto());
        }

        @Test
        @DisplayName("Votar debe setear valor a 1 siempre")
        void votar_siempreSeteaValorA1() {
            // Given
            CrearVotoRequest request = new CrearVotoRequest();
            request.setIdFalla(15L);
            request.setTipoVoto("EXPERIMENTAL");

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(fallaRepository.findById(15L)).thenReturn(Optional.of(fallaMock));
            when(votoRepository.existsByUsuarioAndFallaAndTipoVoto(any(), any(), any())).thenReturn(false);
            when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> {
                Voto voto = invocation.getArgument(0);
                assertEquals(1, voto.getValor(), "El valor del voto debe ser siempre 1");
                voto.setIdVoto(100L);
                voto.setCreadoEn(LocalDateTime.now());
                return voto;
            });

            // When
            votoService.votar(1L, request);

            // Then
            verify(votoRepository).save(any(Voto.class));
        }

        @Test
        @DisplayName("Votar falla duplicada debe lanzar BadRequestException")
        void votar_conVotoDuplicado_lanzaBadRequestException() {
            // Given
            CrearVotoRequest request = new CrearVotoRequest();
            request.setIdFalla(15L);
            request.setTipoVoto("EXPERIMENTAL");

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(fallaRepository.findById(15L)).thenReturn(Optional.of(fallaMock));
            when(votoRepository.existsByUsuarioAndFallaAndTipoVoto(
                    eq(usuarioMock), eq(fallaMock), eq(Voto.TipoVoto.EXPERIMENTAL))).thenReturn(true);

            // When & Then
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                votoService.votar(1L, request);
            });

            assertTrue(exception.getMessage().contains("Ya has votado"));
            verify(votoRepository, never()).save(any(Voto.class));
        }

        @Test
        @DisplayName("Votar con usuario diferente tipo permite voto")
        void votar_mismaFallaDiferenteTipo_permiteVoto() {
            // Given - usuario ya votó EXPERIMENTAL, ahora vota MONUMENTO
            CrearVotoRequest request = new CrearVotoRequest();
            request.setIdFalla(15L);
            request.setTipoVoto("MONUMENTO");

            Voto nuevoVoto = new Voto();
            nuevoVoto.setIdVoto(103L);
            nuevoVoto.setUsuario(usuarioMock);
            nuevoVoto.setFalla(fallaMock);
            nuevoVoto.setTipoVoto(Voto.TipoVoto.MONUMENTO);
            nuevoVoto.setValor(1);
            nuevoVoto.setCreadoEn(LocalDateTime.now());

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(fallaRepository.findById(15L)).thenReturn(Optional.of(fallaMock));
            when(votoRepository.existsByUsuarioAndFallaAndTipoVoto(
                    any(), any(), eq(Voto.TipoVoto.MONUMENTO))).thenReturn(false);
            when(votoRepository.save(any(Voto.class))).thenReturn(nuevoVoto);

            // When
            VotoDTO resultado = votoService.votar(1L, request);

            // Then
            assertNotNull(resultado);
            assertEquals("MONUMENTO", resultado.getTipoVoto());
        }

        @Test
        @DisplayName("Votar con usuario inexistente debe lanzar ResourceNotFoundException")
        void votar_conUsuarioInexistente_lanzaResourceNotFoundException() {
            // Given
            CrearVotoRequest request = new CrearVotoRequest();
            request.setIdFalla(15L);
            request.setTipoVoto("EXPERIMENTAL");

            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> {
                votoService.votar(999L, request);
            });

            verify(votoRepository, never()).save(any(Voto.class));
        }

        @Test
        @DisplayName("Votar con falla inexistente debe lanzar ResourceNotFoundException")
        void votar_conFallaInexistente_lanzaResourceNotFoundException() {
            // Given
            CrearVotoRequest request = new CrearVotoRequest();
            request.setIdFalla(999L);
            request.setTipoVoto("EXPERIMENTAL");

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(fallaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> {
                votoService.votar(1L, request);
            });

            verify(votoRepository, never()).save(any(Voto.class));
        }

        @Test
        @DisplayName("Votar con tipo inválido debe lanzar IllegalArgumentException")
        void votar_conTipoInvalido_lanzaIllegalArgumentException() {
            // Given
            CrearVotoRequest request = new CrearVotoRequest();
            request.setIdFalla(15L);
            request.setTipoVoto("TIPO_INVALIDO");

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                votoService.votar(1L, request);
            });

            verify(votoRepository, never()).save(any(Voto.class));
        }

        @Test
        @DisplayName("El DTO retornado contiene nombre del usuario y falla")
        void votar_retornaDTOConNombresCorrectos() {
            // Given
            CrearVotoRequest request = new CrearVotoRequest();
            request.setIdFalla(15L);
            request.setTipoVoto("EXPERIMENTAL");

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(fallaRepository.findById(15L)).thenReturn(Optional.of(fallaMock));
            when(votoRepository.existsByUsuarioAndFallaAndTipoVoto(any(), any(), any())).thenReturn(false);
            when(votoRepository.save(any(Voto.class))).thenReturn(votoMock);

            // When
            VotoDTO resultado = votoService.votar(1L, request);

            // Then
            assertEquals("Juan García", resultado.getNombreUsuario());
            assertEquals("Falla Na Jordana", resultado.getNombreFalla());
        }
    }

    // ==========================================
    // OBTENER VOTOS USUARIO
    // ==========================================

    @Nested
    @DisplayName("obtenerVotosUsuario()")
    class ObtenerVotosUsuarioTests {

        @Test
        @DisplayName("Obtener votos de usuario existente retorna lista")
        void obtenerVotosUsuario_conUsuarioExistente_retornaLista() {
            // Given
            Page<Voto> page = new PageImpl<>(List.of(votoMock));
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(votoRepository.findByUsuario(eq(usuarioMock), any(Pageable.class))).thenReturn(page);

            // When
            List<VotoDTO> resultado = votoService.obtenerVotosUsuario(1L);

            // Then
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals("EXPERIMENTAL", resultado.get(0).getTipoVoto());
        }

        @Test
        @DisplayName("Obtener votos de usuario sin votos retorna lista vacía")
        void obtenerVotosUsuario_sinVotos_retornaListaVacia() {
            // Given
            Page<Voto> emptyPage = new PageImpl<>(List.of());
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(votoRepository.findByUsuario(eq(usuarioMock), any(Pageable.class))).thenReturn(emptyPage);

            // When
            List<VotoDTO> resultado = votoService.obtenerVotosUsuario(1L);

            // Then
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Obtener votos de usuario inexistente lanza excepción")
        void obtenerVotosUsuario_conUsuarioInexistente_lanzaExcepcion() {
            // Given
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> {
                votoService.obtenerVotosUsuario(999L);
            });
        }

        @Test
        @DisplayName("Obtener votos con múltiples tipos retorna todos")
        void obtenerVotosUsuario_conMultiplesTipos_retornaTodos() {
            // Given
            Voto voto2 = new Voto();
            voto2.setIdVoto(101L);
            voto2.setUsuario(usuarioMock);
            voto2.setFalla(fallaMock);
            voto2.setTipoVoto(Voto.TipoVoto.MONUMENTO);
            voto2.setValor(1);
            voto2.setCreadoEn(LocalDateTime.now());

            Page<Voto> page = new PageImpl<>(Arrays.asList(votoMock, voto2));
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(votoRepository.findByUsuario(eq(usuarioMock), any(Pageable.class))).thenReturn(page);

            // When
            List<VotoDTO> resultado = votoService.obtenerVotosUsuario(1L);

            // Then
            assertEquals(2, resultado.size());
        }
    }

    // ==========================================
    // OBTENER VOTOS FALLA
    // ==========================================

    @Nested
    @DisplayName("obtenerVotosFalla()")
    class ObtenerVotosFallaTests {

        @Test
        @DisplayName("Obtener votos de falla existente retorna lista")
        void obtenerVotosFalla_conFallaExistente_retornaLista() {
            // Given
            Page<Voto> page = new PageImpl<>(List.of(votoMock));
            when(fallaRepository.findById(15L)).thenReturn(Optional.of(fallaMock));
            when(votoRepository.findByFalla(eq(fallaMock), any(Pageable.class))).thenReturn(page);

            // When
            List<VotoDTO> resultado = votoService.obtenerVotosFalla(15L);

            // Then
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals(15L, resultado.get(0).getIdFalla());
        }

        @Test
        @DisplayName("Obtener votos de falla inexistente lanza excepción")
        void obtenerVotosFalla_conFallaInexistente_lanzaExcepcion() {
            // Given
            when(fallaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> {
                votoService.obtenerVotosFalla(999L);
            });
        }

        @Test
        @DisplayName("Obtener votos de falla sin votos retorna lista vacía")
        void obtenerVotosFalla_sinVotos_retornaListaVacia() {
            // Given
            Page<Voto> emptyPage = new PageImpl<>(List.of());
            when(fallaRepository.findById(15L)).thenReturn(Optional.of(fallaMock));
            when(votoRepository.findByFalla(eq(fallaMock), any(Pageable.class))).thenReturn(emptyPage);

            // When
            List<VotoDTO> resultado = votoService.obtenerVotosFalla(15L);

            // Then
            assertTrue(resultado.isEmpty());
        }
    }

    // ==========================================
    // ELIMINAR
    // ==========================================

    @Nested
    @DisplayName("eliminar()")
    class EliminarTests {

        @Test
        @DisplayName("Eliminar voto propio debe funcionar correctamente")
        void eliminar_votoPropio_eliminaCorrectamente() {
            // Given
            when(votoRepository.findById(100L)).thenReturn(Optional.of(votoMock));
            doNothing().when(votoRepository).delete(votoMock);

            // When
            votoService.eliminar(100L, 1L);

            // Then
            verify(votoRepository).delete(votoMock);
        }

        @Test
        @DisplayName("Eliminar voto de otro usuario lanza BadRequestException")
        void eliminar_votoDeOtroUsuario_lanzaBadRequestException() {
            // Given
            when(votoRepository.findById(100L)).thenReturn(Optional.of(votoMock));

            // When & Then - usuario 2 intenta eliminar voto de usuario 1
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                votoService.eliminar(100L, 2L);
            });

            assertTrue(exception.getMessage().contains("No puedes eliminar"));
            verify(votoRepository, never()).delete(any(Voto.class));
        }

        @Test
        @DisplayName("Eliminar voto inexistente lanza ResourceNotFoundException")
        void eliminar_votoInexistente_lanzaResourceNotFoundException() {
            // Given
            when(votoRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> {
                votoService.eliminar(999L, 1L);
            });

            verify(votoRepository, never()).delete(any(Voto.class));
        }
    }
}
