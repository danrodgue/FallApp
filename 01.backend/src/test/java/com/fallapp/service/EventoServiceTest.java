package com.fallapp.service;

import com.fallapp.dto.EventoDTO;
import com.fallapp.model.Evento;
import com.fallapp.model.Falla;
import com.fallapp.repository.EventoRepository;
import com.fallapp.repository.FallaRepository;
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
 * Tests unitarios para EventoService
 *
 * Cobertura: obtenerEventosFuturos, obtenerProximosEventos,
 *            obtenerPorFalla, obtenerPorId, crear, actualizar, eliminar
 *
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventoService Tests")
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private FallaRepository fallaRepository;

    @InjectMocks
    private EventoService eventoService;

    private Falla fallaMock;
    private Evento eventoMock;

    @BeforeEach
    void setUp() {
        fallaMock = new Falla();
        fallaMock.setIdFalla(1L);
        fallaMock.setNombre("Falla Na Jordana");

        eventoMock = new Evento();
        eventoMock.setIdEvento(10L);
        eventoMock.setFalla(fallaMock);
        eventoMock.setTipo(Evento.TipoEvento.planta);
        eventoMock.setNombre("Plantà 2026");
        eventoMock.setDescripcion("Plantà de la falla mayor");
        eventoMock.setFechaEvento(LocalDateTime.of(2026, 3, 15, 0, 0));
        eventoMock.setUbicacion("Plaza del Ayuntamiento");
        eventoMock.setParticipantesEstimado(500);
        eventoMock.setCreadoEn(LocalDateTime.now());
    }

    // ==========================================
    // OBTENER EVENTOS FUTUROS
    // ==========================================

    @Nested
    @DisplayName("obtenerEventosFuturos()")
    class ObtenerEventosFuturosTests {

        @Test
        @DisplayName("Retorna lista de eventos futuros")
        void obtenerEventosFuturos_retornaListaDeEventos() {
            // Given
            when(eventoRepository.findEventosFuturos(any(LocalDateTime.class)))
                    .thenReturn(List.of(eventoMock));

            // When
            List<EventoDTO> resultado = eventoService.obtenerEventosFuturos();

            // Then
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals("Plantà 2026", resultado.get(0).getNombre());
            assertEquals("planta", resultado.get(0).getTipo());
        }

        @Test
        @DisplayName("Sin eventos futuros retorna lista vacía")
        void obtenerEventosFuturos_sinEventos_retornaListaVacia() {
            // Given
            when(eventoRepository.findEventosFuturos(any(LocalDateTime.class)))
                    .thenReturn(List.of());

            // When
            List<EventoDTO> resultado = eventoService.obtenerEventosFuturos();

            // Then
            assertTrue(resultado.isEmpty());
        }
    }

    // ==========================================
    // OBTENER PROXIMOS EVENTOS
    // ==========================================

    @Nested
    @DisplayName("obtenerProximosEventos()")
    class ObtenerProximosEventosTests {

        @Test
        @DisplayName("Retorna lista limitada de próximos eventos")
        void obtenerProximosEventos_conLimite_retornaListaLimitada() {
            // Given
            when(eventoRepository.findProximosEventos(any(Pageable.class)))
                    .thenReturn(List.of(eventoMock));

            // When
            List<EventoDTO> resultado = eventoService.obtenerProximosEventos(5);

            // Then
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
        }
    }

    // ==========================================
    // OBTENER POR FALLA
    // ==========================================

    @Nested
    @DisplayName("obtenerPorFalla()")
    class ObtenerPorFallaTests {

        @Test
        @DisplayName("Obtener eventos de falla existente retorna página")
        void obtenerPorFalla_conFallaExistente_retornaPagina() {
            // Given
            Page<Evento> page = new PageImpl<>(List.of(eventoMock));
            when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));
            when(eventoRepository.findByFalla(eq(fallaMock), any(Pageable.class))).thenReturn(page);

            // When
            Page<EventoDTO> resultado = eventoService.obtenerPorFalla(1L, Pageable.unpaged());

            // Then
            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            assertEquals("Plantà 2026", resultado.getContent().get(0).getNombre());
        }

        @Test
        @DisplayName("Obtener eventos de falla inexistente lanza excepción")
        void obtenerPorFalla_conFallaInexistente_lanzaExcepcion() {
            // Given
            when(fallaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                eventoService.obtenerPorFalla(999L, Pageable.unpaged());
            });
        }
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @Nested
    @DisplayName("obtenerPorId()")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Obtener evento existente retorna DTO completo")
        void obtenerPorId_conIdExistente_retornaDTO() {
            // Given
            when(eventoRepository.findById(10L)).thenReturn(Optional.of(eventoMock));

            // When
            EventoDTO resultado = eventoService.obtenerPorId(10L);

            // Then
            assertNotNull(resultado);
            assertEquals(10L, resultado.getIdEvento());
            assertEquals("Plantà 2026", resultado.getNombre());
            assertEquals(1L, resultado.getIdFalla());
            assertEquals("Falla Na Jordana", resultado.getNombreFalla());
            assertEquals("planta", resultado.getTipo());
            assertEquals(500, resultado.getParticipantesEstimado());
        }

        @Test
        @DisplayName("Obtener evento inexistente lanza excepción")
        void obtenerPorId_conIdInexistente_lanzaExcepcion() {
            // Given
            when(eventoRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                eventoService.obtenerPorId(999L);
            });
            assertTrue(ex.getMessage().contains("Evento no encontrado"));
        }
    }

    // ==========================================
    // CREAR
    // ==========================================

    @Nested
    @DisplayName("crear()")
    class CrearTests {

        @Test
        @DisplayName("Crear evento con datos válidos guarda correctamente")
        void crear_conDatosValidos_guardaCorrectamente() {
            // Given
            EventoDTO dto = new EventoDTO();
            dto.setIdFalla(1L);
            dto.setNombre("Cremà 2026");
            dto.setDescripcion("Nit de la Cremà");
            dto.setTipo("crema");
            dto.setFechaEvento(LocalDateTime.of(2026, 3, 19, 0, 0));
            dto.setUbicacion("Plaza del Ayuntamiento");
            dto.setParticipantesEstimado(1000);

            Evento eventoCreado = new Evento();
            eventoCreado.setIdEvento(11L);
            eventoCreado.setFalla(fallaMock);
            eventoCreado.setTipo(Evento.TipoEvento.crema);
            eventoCreado.setNombre("Cremà 2026");
            eventoCreado.setDescripcion("Nit de la Cremà");
            eventoCreado.setFechaEvento(dto.getFechaEvento());
            eventoCreado.setUbicacion("Plaza del Ayuntamiento");
            eventoCreado.setParticipantesEstimado(1000);

            when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));
            when(eventoRepository.save(any(Evento.class))).thenReturn(eventoCreado);

            // When
            EventoDTO resultado = eventoService.crear(dto);

            // Then
            assertNotNull(resultado);
            assertEquals(11L, resultado.getIdEvento());
            assertEquals("Cremà 2026", resultado.getNombre());
            assertEquals("crema", resultado.getTipo());
            verify(eventoRepository).save(any(Evento.class));
        }

        @Test
        @DisplayName("Crear evento con falla inexistente lanza excepción")
        void crear_conFallaInexistente_lanzaExcepcion() {
            // Given
            EventoDTO dto = new EventoDTO();
            dto.setIdFalla(999L);
            dto.setNombre("Evento Test");

            when(fallaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                eventoService.crear(dto);
            });

            verify(eventoRepository, never()).save(any(Evento.class));
        }

        @Test
        @DisplayName("Crear evento con todos los tipos válidos")
        void crear_conTiposDeEventoValidos() {
            // Given
            String[] tiposValidos = {"planta", "crema", "ofrenda", "infantil", "concierto",
                    "exposicion", "encuentro", "cena", "teatro", "otro"};

            for (String tipo : tiposValidos) {
                EventoDTO dto = new EventoDTO();
                dto.setIdFalla(1L);
                dto.setNombre("Evento " + tipo);
                dto.setTipo(tipo);
                dto.setFechaEvento(LocalDateTime.now().plusDays(1));

                Evento eventoCreado = new Evento();
                eventoCreado.setIdEvento(20L);
                eventoCreado.setFalla(fallaMock);
                eventoCreado.setTipo(Evento.TipoEvento.valueOf(tipo));
                eventoCreado.setNombre("Evento " + tipo);
                eventoCreado.setFechaEvento(dto.getFechaEvento());

                when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));
                when(eventoRepository.save(any(Evento.class))).thenReturn(eventoCreado);

                // When & Then - no debe lanzar excepción
                assertDoesNotThrow(() -> eventoService.crear(dto),
                        "El tipo de evento '" + tipo + "' debe ser válido");
            }
        }
    }

    // ==========================================
    // ACTUALIZAR
    // ==========================================

    @Nested
    @DisplayName("actualizar()")
    class ActualizarTests {

        @Test
        @DisplayName("Actualizar evento existente guarda cambios")
        void actualizar_conEventoExistente_guardaCambios() {
            // Given
            EventoDTO dto = new EventoDTO();
            dto.setIdFalla(1L);
            dto.setNombre("Plantà 2026 - Actualizada");
            dto.setDescripcion("Descripción actualizada");
            dto.setTipo("planta");
            dto.setFechaEvento(LocalDateTime.of(2026, 3, 15, 8, 0));

            when(eventoRepository.findById(10L)).thenReturn(Optional.of(eventoMock));
            when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));
            when(eventoRepository.save(any(Evento.class))).thenReturn(eventoMock);

            // When
            EventoDTO resultado = eventoService.actualizar(10L, dto);

            // Then
            assertNotNull(resultado);
            verify(eventoRepository).save(any(Evento.class));
        }

        @Test
        @DisplayName("Actualizar evento inexistente lanza excepción")
        void actualizar_conEventoInexistente_lanzaExcepcion() {
            // Given
            EventoDTO dto = new EventoDTO();
            dto.setIdFalla(1L);
            when(eventoRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                eventoService.actualizar(999L, dto);
            });
        }

        @Test
        @DisplayName("Actualizar evento con falla inexistente lanza excepción")
        void actualizar_conFallaInexistente_lanzaExcepcion() {
            // Given
            EventoDTO dto = new EventoDTO();
            dto.setIdFalla(999L);
            when(eventoRepository.findById(10L)).thenReturn(Optional.of(eventoMock));
            when(fallaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                eventoService.actualizar(10L, dto);
            });
        }
    }

    // ==========================================
    // ELIMINAR
    // ==========================================

    @Nested
    @DisplayName("eliminar()")
    class EliminarTests {

        @Test
        @DisplayName("Eliminar evento existente lo borra de BD")
        void eliminar_conEventoExistente_borraDeBD() {
            // Given
            when(eventoRepository.findById(10L)).thenReturn(Optional.of(eventoMock));
            doNothing().when(eventoRepository).delete(eventoMock);

            // When
            eventoService.eliminar(10L);

            // Then
            verify(eventoRepository).delete(eventoMock);
        }

        @Test
        @DisplayName("Eliminar evento inexistente lanza excepción")
        void eliminar_conEventoInexistente_lanzaExcepcion() {
            // Given
            when(eventoRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                eventoService.eliminar(999L);
            });
            verify(eventoRepository, never()).delete(any(Evento.class));
        }
    }

    // ==========================================
    // CONVERSIÓN DTO
    // ==========================================

    @Nested
    @DisplayName("Conversión DTO")
    class ConversionDTOTests {

        @Test
        @DisplayName("DTO incluye datos de la falla asociada")
        void convertirADTO_incluyeDatosFalla() {
            // Given
            when(eventoRepository.findById(10L)).thenReturn(Optional.of(eventoMock));

            // When
            EventoDTO resultado = eventoService.obtenerPorId(10L);

            // Then
            assertEquals(1L, resultado.getIdFalla());
            assertEquals("Falla Na Jordana", resultado.getNombreFalla());
        }

        @Test
        @DisplayName("DTO con evento sin falla maneja null correctamente")
        void convertirADTO_sinFalla_manejaNull() {
            // Given
            eventoMock.setFalla(null);
            when(eventoRepository.findById(10L)).thenReturn(Optional.of(eventoMock));

            // When
            EventoDTO resultado = eventoService.obtenerPorId(10L);

            // Then
            assertNull(resultado.getIdFalla());
            assertNull(resultado.getNombreFalla());
        }
    }
}
