package com.fallapp.service;

import com.fallapp.dto.FallaDTO;
import com.fallapp.dto.PaginatedResponse;
import com.fallapp.dto.UbicacionDTO;
import com.fallapp.exception.ResourceNotFoundException;
import com.fallapp.model.Falla;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para FallaService
 *
 * Cobertura: obtenerPorId, listar, buscar, buscarCercanas,
 *            obtenerPorSeccion, obtenerPorCategoria, obtenerUbicacion,
 *            crear, actualizar, eliminar
 *
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FallaService Tests")
class FallaServiceTest {

    @Mock
    private FallaRepository fallaRepository;

    @InjectMocks
    private FallaService fallaService;

    private Falla fallaMock;

    @BeforeEach
    void setUp() {
        fallaMock = new Falla();
        fallaMock.setIdFalla(1L);
        fallaMock.setNombre("Falla Na Jordana");
        fallaMock.setSeccion("E");
        fallaMock.setPresidente("Carlos López");
        fallaMock.setArtista("Pere Baenas");
        fallaMock.setLema("València en festes");
        fallaMock.setAnyoFundacion(1942);
        fallaMock.setExperim(false);
        fallaMock.setUbicacionLat(BigDecimal.valueOf(39.4738));
        fallaMock.setUbicacionLon(BigDecimal.valueOf(-0.3753));
        fallaMock.setCategoria(Falla.CategoriaFalla.especial);
        fallaMock.setCreadoEn(LocalDateTime.now());
        fallaMock.setActualizadoEn(LocalDateTime.now());
        fallaMock.setEventos(new ArrayList<>());
        fallaMock.setUsuarios(new ArrayList<>());
        fallaMock.setComentarios(new ArrayList<>());
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @Nested
    @DisplayName("obtenerPorId()")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Obtener falla por ID existente retorna DTO")
        void obtenerPorId_conIdExistente_retornaDTO() {
            // Given
            when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));

            // When
            FallaDTO resultado = fallaService.obtenerPorId(1L);

            // Then
            assertNotNull(resultado);
            assertEquals(1L, resultado.getIdFalla());
            assertEquals("Falla Na Jordana", resultado.getNombre());
            assertEquals("E", resultado.getSeccion());
            assertEquals("Carlos López", resultado.getPresidente());
            assertEquals("especial", resultado.getCategoria());
        }

        @Test
        @DisplayName("Obtener falla por ID incluye coordenadas GPS")
        void obtenerPorId_incluyeCoordenadasGPS() {
            // Given
            when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));

            // When
            FallaDTO resultado = fallaService.obtenerPorId(1L);

            // Then
            assertNotNull(resultado.getLatitud());
            assertNotNull(resultado.getLongitud());
            assertEquals(39.4738, resultado.getLatitud(), 0.0001);
            assertEquals(-0.3753, resultado.getLongitud(), 0.0001);
        }

        @Test
        @DisplayName("Obtener falla por ID inexistente lanza excepción")
        void obtenerPorId_conIdInexistente_lanzaExcepcion() {
            // Given
            when(fallaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> {
                fallaService.obtenerPorId(999L);
            });
        }

        @Test
        @DisplayName("Obtener falla sin coordenadas retorna null en lat/lon")
        void obtenerPorId_sinCoordenadas_retornaNullEnLatLon() {
            // Given
            fallaMock.setUbicacionLat(null);
            fallaMock.setUbicacionLon(null);
            when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));

            // When
            FallaDTO resultado = fallaService.obtenerPorId(1L);

            // Then
            assertNull(resultado.getLatitud());
            assertNull(resultado.getLongitud());
        }
    }

    // ==========================================
    // LISTAR
    // ==========================================

    @Nested
    @DisplayName("listar()")
    class ListarTests {

        @Test
        @DisplayName("Listar fallas paginadas retorna respuesta correcta")
        void listar_retornaRespuestaPaginada() {
            // Given
            Page<Falla> page = new PageImpl<>(List.of(fallaMock));
            when(fallaRepository.findAll(any(Pageable.class))).thenReturn(page);

            // When
            PaginatedResponse<FallaDTO> resultado = fallaService.listar(0, 20);

            // Then
            assertNotNull(resultado);
            assertEquals(1, resultado.getContenido().size());
            assertEquals(0, resultado.getPaginaActual());
            assertEquals(1, resultado.getTotalElementos());
            assertTrue(resultado.getEsUltimaPagina());
        }

        @Test
        @DisplayName("Listar sin fallas retorna lista vacía")
        void listar_sinFallas_retornaListaVacia() {
            // Given
            Page<Falla> emptyPage = new PageImpl<>(List.of());
            when(fallaRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            // When
            PaginatedResponse<FallaDTO> resultado = fallaService.listar(0, 20);

            // Then
            assertTrue(resultado.getContenido().isEmpty());
            assertEquals(0, resultado.getTotalElementos());
        }
    }

    // ==========================================
    // BUSCAR
    // ==========================================

    @Nested
    @DisplayName("buscar()")
    class BuscarTests {

        @Test
        @DisplayName("Buscar por texto encuentra fallas")
        void buscar_conTextoExistente_retornaFallas() {
            // Given
            when(fallaRepository.buscarPorTexto("Jordana")).thenReturn(List.of(fallaMock));

            // When
            List<FallaDTO> resultado = fallaService.buscar("Jordana");

            // Then
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals("Falla Na Jordana", resultado.get(0).getNombre());
        }

        @Test
        @DisplayName("Buscar por texto sin resultados retorna lista vacía")
        void buscar_sinResultados_retornaListaVacia() {
            // Given
            when(fallaRepository.buscarPorTexto("inexistente")).thenReturn(List.of());

            // When
            List<FallaDTO> resultado = fallaService.buscar("inexistente");

            // Then
            assertTrue(resultado.isEmpty());
        }
    }

    // ==========================================
    // BUSCAR CERCANAS
    // ==========================================

    @Nested
    @DisplayName("buscarCercanas()")
    class BuscarCercanasTests {

        @Test
        @DisplayName("Buscar fallas cercanas con radio 5km retorna resultados")
        void buscarCercanas_conRadio5km_retornaResultados() {
            // Given
            when(fallaRepository.buscarFallasCercanas(
                    any(BigDecimal.class), any(BigDecimal.class), any(BigDecimal.class)))
                    .thenReturn(List.of(fallaMock));

            // When
            List<FallaDTO> resultado = fallaService.buscarCercanas(39.47, -0.37, 5.0);

            // Then
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("Buscar cercanas sin fallas en radio retorna vacío")
        void buscarCercanas_sinFallasEnRadio_retornaVacio() {
            // Given
            when(fallaRepository.buscarFallasCercanas(
                    any(BigDecimal.class), any(BigDecimal.class), any(BigDecimal.class)))
                    .thenReturn(List.of());

            // When
            List<FallaDTO> resultado = fallaService.buscarCercanas(40.0, -3.0, 1.0);

            // Then
            assertTrue(resultado.isEmpty());
        }
    }

    // ==========================================
    // OBTENER POR SECCION
    // ==========================================

    @Nested
    @DisplayName("obtenerPorSeccion()")
    class ObtenerPorSeccionTests {

        @Test
        @DisplayName("Obtener por sección existente retorna fallas")
        void obtenerPorSeccion_conSeccionExistente_retornaFallas() {
            // Given
            Page<Falla> page = new PageImpl<>(List.of(fallaMock));
            when(fallaRepository.findBySeccion(eq("E"), any(Pageable.class))).thenReturn(page);

            // When
            List<FallaDTO> resultado = fallaService.obtenerPorSeccion("E");

            // Then
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals("E", resultado.get(0).getSeccion());
        }
    }

    // ==========================================
    // OBTENER UBICACION
    // ==========================================

    @Nested
    @DisplayName("obtenerUbicacion()")
    class ObtenerUbicacionTests {

        @Test
        @DisplayName("Obtener ubicación de falla con GPS retorna tieneUbicacion=true")
        void obtenerUbicacion_conGPS_retornaTieneUbicacionTrue() {
            // Given
            when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));

            // When
            UbicacionDTO resultado = fallaService.obtenerUbicacion(1L);

            // Then
            assertNotNull(resultado);
            assertEquals(1L, resultado.getIdFalla());
            assertTrue(resultado.getTieneUbicacion());
            assertNotNull(resultado.getLatitud());
            assertNotNull(resultado.getLongitud());
        }

        @Test
        @DisplayName("Obtener ubicación de falla sin GPS retorna tieneUbicacion=false")
        void obtenerUbicacion_sinGPS_retornaTieneUbicacionFalse() {
            // Given
            fallaMock.setUbicacionLat(null);
            fallaMock.setUbicacionLon(null);
            when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));

            // When
            UbicacionDTO resultado = fallaService.obtenerUbicacion(1L);

            // Then
            assertFalse(resultado.getTieneUbicacion());
            assertNull(resultado.getLatitud());
            assertNull(resultado.getLongitud());
        }

        @Test
        @DisplayName("Obtener ubicación de falla inexistente lanza excepción")
        void obtenerUbicacion_conFallaInexistente_lanzaExcepcion() {
            // Given
            when(fallaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> {
                fallaService.obtenerUbicacion(999L);
            });
        }
    }

    // ==========================================
    // CREAR
    // ==========================================

    @Nested
    @DisplayName("crear()")
    class CrearTests {

        @Test
        @DisplayName("Crear falla con nombre único debe guardar correctamente")
        void crear_conNombreUnico_guardaCorrectamente() {
            // Given
            FallaDTO dto = FallaDTO.builder()
                    .nombre("Nueva Falla")
                    .seccion("1A")
                    .presidente("Ana Martínez")
                    .anyoFundacion(2020)
                    .categoria("primera")
                    .latitud(39.47)
                    .longitud(-0.37)
                    .build();

            when(fallaRepository.existsByNombre("Nueva Falla")).thenReturn(false);
            when(fallaRepository.save(any(Falla.class))).thenAnswer(inv -> {
                Falla falla = inv.getArgument(0);
                falla.setIdFalla(50L);
                falla.setCreadoEn(LocalDateTime.now());
                falla.setEventos(new ArrayList<>());
                falla.setUsuarios(new ArrayList<>());
                return falla;
            });

            // When
            FallaDTO resultado = fallaService.crear(dto);

            // Then
            assertNotNull(resultado);
            assertEquals(50L, resultado.getIdFalla());
            verify(fallaRepository).save(any(Falla.class));
        }

        @Test
        @DisplayName("Crear falla con nombre duplicado lanza excepción")
        void crear_conNombreDuplicado_lanzaExcepcion() {
            // Given
            FallaDTO dto = FallaDTO.builder()
                    .nombre("Falla Na Jordana")
                    .seccion("E")
                    .presidente("Carlos")
                    .anyoFundacion(1942)
                    .build();

            when(fallaRepository.existsByNombre("Falla Na Jordana")).thenReturn(true);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                fallaService.crear(dto);
            });

            verify(fallaRepository, never()).save(any(Falla.class));
        }

        @Test
        @DisplayName("Crear falla sin coordenadas no setea ubicación")
        void crear_sinCoordenadas_noSeteaUbicacion() {
            // Given
            FallaDTO dto = FallaDTO.builder()
                    .nombre("Falla sin GPS")
                    .seccion("2B")
                    .presidente("María García")
                    .anyoFundacion(2000)
                    .build();

            when(fallaRepository.existsByNombre("Falla sin GPS")).thenReturn(false);
            when(fallaRepository.save(any(Falla.class))).thenAnswer(inv -> {
                Falla falla = inv.getArgument(0);
                falla.setIdFalla(51L);
                falla.setEventos(new ArrayList<>());
                falla.setUsuarios(new ArrayList<>());
                assertNull(falla.getUbicacionLat(), "No debe tener latitud");
                assertNull(falla.getUbicacionLon(), "No debe tener longitud");
                return falla;
            });

            // When
            fallaService.crear(dto);

            // Then
            verify(fallaRepository).save(any(Falla.class));
        }
    }

    // ==========================================
    // ACTUALIZAR
    // ==========================================

    @Nested
    @DisplayName("actualizar()")
    class ActualizarTests {

        @Test
        @DisplayName("Actualizar falla existente guarda cambios")
        void actualizar_conFallaExistente_guardaCambios() {
            // Given
            FallaDTO dto = FallaDTO.builder()
                    .nombre("Falla Na Jordana Actualizada")
                    .seccion("E")
                    .presidente("Nuevo Presidente")
                    .anyoFundacion(1942)
                    .build();

            when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));
            when(fallaRepository.existsByNombre("Falla Na Jordana Actualizada")).thenReturn(false);
            when(fallaRepository.save(any(Falla.class))).thenReturn(fallaMock);

            // When
            FallaDTO resultado = fallaService.actualizar(1L, dto);

            // Then
            assertNotNull(resultado);
            verify(fallaRepository).save(any(Falla.class));
        }

        @Test
        @DisplayName("Actualizar con mismo nombre no valida duplicado")
        void actualizar_conMismoNombre_noValidaDuplicado() {
            // Given
            FallaDTO dto = FallaDTO.builder()
                    .nombre("Falla Na Jordana")
                    .seccion("E")
                    .presidente("Presidente Actualizado")
                    .anyoFundacion(1942)
                    .build();

            when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));
            when(fallaRepository.save(any(Falla.class))).thenReturn(fallaMock);

            // When
            fallaService.actualizar(1L, dto);

            // Then
            verify(fallaRepository, never()).existsByNombre(anyString());
            verify(fallaRepository).save(any(Falla.class));
        }

        @Test
        @DisplayName("Actualizar con nombre que ya existe lanza excepción")
        void actualizar_conNombreDuplicado_lanzaExcepcion() {
            // Given
            FallaDTO dto = FallaDTO.builder()
                    .nombre("Otra Falla Existente")
                    .seccion("E")
                    .presidente("Carlos")
                    .anyoFundacion(1942)
                    .build();

            when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));
            when(fallaRepository.existsByNombre("Otra Falla Existente")).thenReturn(true);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                fallaService.actualizar(1L, dto);
            });
        }

        @Test
        @DisplayName("Actualizar falla inexistente lanza excepción")
        void actualizar_conFallaInexistente_lanzaExcepcion() {
            // Given
            FallaDTO dto = FallaDTO.builder().nombre("Test").build();
            when(fallaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> {
                fallaService.actualizar(999L, dto);
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
        @DisplayName("Eliminar falla existente la borra de BD")
        void eliminar_conFallaExistente_borraDeBD() {
            // Given
            when(fallaRepository.findById(1L)).thenReturn(Optional.of(fallaMock));
            doNothing().when(fallaRepository).delete(fallaMock);

            // When
            fallaService.eliminar(1L);

            // Then
            verify(fallaRepository).delete(fallaMock);
        }

        @Test
        @DisplayName("Eliminar falla inexistente lanza excepción")
        void eliminar_conFallaInexistente_lanzaExcepcion() {
            // Given
            when(fallaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> {
                fallaService.eliminar(999L);
            });

            verify(fallaRepository, never()).delete(any(Falla.class));
        }
    }
}
