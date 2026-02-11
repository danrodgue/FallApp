package com.fallapp.controller;

import com.fallapp.dto.ApiResponse;
import com.fallapp.dto.FallaDTO;
import com.fallapp.dto.PaginatedResponse;
import com.fallapp.dto.UbicacionDTO;
import com.fallapp.exception.GlobalExceptionHandler;
import com.fallapp.exception.ResourceNotFoundException;
import com.fallapp.service.FallaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para FallaController
 *
 * Usa MockMvc standalone (sin Spring context) para velocidad
 *
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FallaController Tests")
class FallaControllerTest {

    @Mock
    private FallaService fallaService;

    @InjectMocks
    private FallaController fallaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(fallaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    // ==========================================
    // GET /api/fallas
    // ==========================================

    @Nested
    @DisplayName("GET /api/fallas")
    class ListarTests {

        @Test
        @DisplayName("Listar fallas retorna 200 con datos paginados")
        void listar_retorna200ConPaginacion() throws Exception {
            // Given
            FallaDTO falla = FallaDTO.builder()
                    .idFalla(1L)
                    .nombre("Na Jordana")
                    .seccion("E")
                    .build();
            PaginatedResponse<FallaDTO> response = PaginatedResponse.<FallaDTO>builder()
                    .contenido(List.of(falla))
                    .paginaActual(0)
                    .elementosPorPagina(20)
                    .totalElementos(1L)
                    .totalPaginas(1)
                    .esUltimaPagina(true)
                    .build();

            when(fallaService.listar(0, 20)).thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/fallas")
                            .param("pagina", "0")
                            .param("tamano", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exito").value(true))
                    .andExpect(jsonPath("$.datos.contenido[0].nombre").value("Na Jordana"))
                    .andExpect(jsonPath("$.datos.totalElementos").value(1));
        }

        @Test
        @DisplayName("Listar con parámetros por defecto retorna 200")
        void listar_conParametrosDefecto_retorna200() throws Exception {
            // Given
            PaginatedResponse<FallaDTO> response = PaginatedResponse.<FallaDTO>builder()
                    .contenido(List.of())
                    .paginaActual(0)
                    .elementosPorPagina(20)
                    .totalElementos(0L)
                    .totalPaginas(0)
                    .esUltimaPagina(true)
                    .build();

            when(fallaService.listar(0, 20)).thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/fallas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exito").value(true));
        }
    }

    // ==========================================
    // GET /api/fallas/{id}
    // ==========================================

    @Nested
    @DisplayName("GET /api/fallas/{id}")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Obtener falla existente retorna 200")
        void obtenerPorId_conIdExistente_retorna200() throws Exception {
            // Given
            FallaDTO falla = FallaDTO.builder()
                    .idFalla(15L)
                    .nombre("Na Jordana")
                    .seccion("E")
                    .presidente("Carlos López")
                    .latitud(39.4738)
                    .longitud(-0.3753)
                    .build();

            when(fallaService.obtenerPorId(15L)).thenReturn(falla);

            // When & Then
            mockMvc.perform(get("/api/fallas/15"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exito").value(true))
                    .andExpect(jsonPath("$.datos.nombre").value("Na Jordana"))
                    .andExpect(jsonPath("$.datos.seccion").value("E"))
                    .andExpect(jsonPath("$.datos.latitud").value(39.4738));
        }

        @Test
        @DisplayName("Obtener falla inexistente retorna 404")
        void obtenerPorId_conIdInexistente_retorna404() throws Exception {
            // Given
            when(fallaService.obtenerPorId(999L))
                    .thenThrow(new ResourceNotFoundException("Falla", "id", 999L));

            // When & Then
            mockMvc.perform(get("/api/fallas/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.exito").value(false));
        }
    }

    // ==========================================
    // GET /api/fallas/{id}/ubicacion
    // ==========================================

    @Nested
    @DisplayName("GET /api/fallas/{id}/ubicacion")
    class ObtenerUbicacionTests {

        @Test
        @DisplayName("Obtener ubicación de falla con GPS retorna 200")
        void obtenerUbicacion_conGPS_retorna200() throws Exception {
            // Given
            UbicacionDTO ubicacion = UbicacionDTO.builder()
                    .idFalla(1L)
                    .nombre("Na Jordana")
                    .latitud(39.4738)
                    .longitud(-0.3753)
                    .tieneUbicacion(true)
                    .build();

            when(fallaService.obtenerUbicacion(1L)).thenReturn(ubicacion);

            // When & Then
            mockMvc.perform(get("/api/fallas/1/ubicacion"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.datos.tieneUbicacion").value(true))
                    .andExpect(jsonPath("$.datos.latitud").value(39.4738));
        }
    }

    // ==========================================
    // GET /api/fallas/buscar
    // ==========================================

    @Nested
    @DisplayName("GET /api/fallas/buscar")
    class BuscarTests {

        @Test
        @DisplayName("Buscar por texto retorna resultados")
        void buscar_conTexto_retornaResultados() throws Exception {
            // Given
            FallaDTO falla = FallaDTO.builder()
                    .idFalla(1L)
                    .nombre("Na Jordana")
                    .build();

            when(fallaService.buscar("Jordana")).thenReturn(List.of(falla));

            // When & Then
            mockMvc.perform(get("/api/fallas/buscar")
                            .param("texto", "Jordana"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.datos[0].nombre").value("Na Jordana"));
        }
    }

    // ==========================================
    // GET /api/fallas/cercanas
    // ==========================================

    @Nested
    @DisplayName("GET /api/fallas/cercanas")
    class BuscarCercanasTests {

        @Test
        @DisplayName("Buscar cercanas retorna fallas en radio")
        void buscarCercanas_retornaFallasEnRadio() throws Exception {
            // Given
            FallaDTO falla = FallaDTO.builder()
                    .idFalla(1L)
                    .nombre("Na Jordana")
                    .latitud(39.4738)
                    .longitud(-0.3753)
                    .build();

            when(fallaService.buscarCercanas(39.47, -0.37, 5.0))
                    .thenReturn(List.of(falla));

            // When & Then
            mockMvc.perform(get("/api/fallas/cercanas")
                            .param("latitud", "39.47")
                            .param("longitud", "-0.37")
                            .param("radio", "5.0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.datos[0].nombre").value("Na Jordana"));
        }
    }

    // ==========================================
    // POST /api/fallas
    // ==========================================

    @Nested
    @DisplayName("POST /api/fallas")
    class CrearTests {

        @Test
        @DisplayName("Crear falla válida retorna 201")
        void crear_conDatosValidos_retorna201() throws Exception {
            // Given
            FallaDTO request = FallaDTO.builder()
                    .nombre("Nueva Falla")
                    .seccion("1A")
                    .presidente("Ana García")
                    .anyoFundacion(2020)
                    .build();

            FallaDTO creada = FallaDTO.builder()
                    .idFalla(50L)
                    .nombre("Nueva Falla")
                    .seccion("1A")
                    .presidente("Ana García")
                    .anyoFundacion(2020)
                    .build();

            when(fallaService.crear(any(FallaDTO.class))).thenReturn(creada);

            // When & Then
            mockMvc.perform(post("/api/fallas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.exito").value(true))
                    .andExpect(jsonPath("$.datos.idFalla").value(50))
                    .andExpect(jsonPath("$.mensaje").value("Falla creada exitosamente"));
        }
    }

    // ==========================================
    // DELETE /api/fallas/{id}
    // ==========================================

    @Nested
    @DisplayName("DELETE /api/fallas/{id}")
    class EliminarTests {

        @Test
        @DisplayName("Eliminar falla existente retorna 200")
        void eliminar_conIdExistente_retorna200() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/fallas/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exito").value(true))
                    .andExpect(jsonPath("$.mensaje").value("Falla eliminada exitosamente"));
        }
    }
}
