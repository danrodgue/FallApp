package com.fallapp.service;

import com.fallapp.dto.RegistroRequest;
import com.fallapp.dto.UsuarioDTO;
import com.fallapp.exception.BadRequestException;
import com.fallapp.exception.ResourceNotFoundException;
import com.fallapp.model.Falla;
import com.fallapp.model.Usuario;
import com.fallapp.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UsuarioService
 *
 * Cobertura: registrar, obtenerPorId, obtenerPorEmail,
 *            listarActivos, actualizar, desactivar
 *
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService Tests")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = new Usuario();
        usuarioMock.setIdUsuario(1L);
        usuarioMock.setEmail("juan@example.com");
        usuarioMock.setNombreCompleto("Juan García");
        usuarioMock.setContrasenaHash("$2a$10$hashedPassword");
        usuarioMock.setRol(Usuario.RolUsuario.usuario);
        usuarioMock.setActivo(true);
        usuarioMock.setFechaRegistro(LocalDateTime.now());
    }

    // ==========================================
    // REGISTRAR
    // ==========================================

    @Nested
    @DisplayName("registrar()")
    class RegistrarTests {

        @Test
        @DisplayName("Registrar usuario con email único debe crear usuario")
        void registrar_conEmailUnico_creaUsuario() {
            // Given
            RegistroRequest request = new RegistroRequest();
            request.setEmail("nuevo@example.com");
            request.setContrasena("password123");
            request.setNombreCompleto("Nuevo Usuario");

            when(usuarioRepository.existsByEmail("nuevo@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedHash");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                u.setIdUsuario(2L);
                u.setFechaRegistro(LocalDateTime.now());
                return u;
            });

            // When
            UsuarioDTO resultado = usuarioService.registrar(request);

            // Then
            assertNotNull(resultado);
            assertEquals("nuevo@example.com", resultado.getEmail());
            assertEquals("Nuevo Usuario", resultado.getNombreCompleto());
            assertEquals("usuario", resultado.getRol());
            assertTrue(resultado.getActivo());
            verify(passwordEncoder).encode("password123");
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Registrar con email duplicado lanza BadRequestException")
        void registrar_conEmailDuplicado_lanzaBadRequestException() {
            // Given
            RegistroRequest request = new RegistroRequest();
            request.setEmail("juan@example.com");
            request.setContrasena("password123");
            request.setNombreCompleto("Otro Juan");

            when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(true);

            // When & Then
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                usuarioService.registrar(request);
            });

            assertTrue(exception.getMessage().contains("email ya está registrado"));
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Registrar usuario siempre asigna rol 'usuario'")
        void registrar_siempreAsignaRolUsuario() {
            // Given
            RegistroRequest request = new RegistroRequest();
            request.setEmail("test@example.com");
            request.setContrasena("test123");
            request.setNombreCompleto("Test");

            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hash");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                u.setIdUsuario(3L);
                u.setFechaRegistro(LocalDateTime.now());
                assertEquals(Usuario.RolUsuario.usuario, u.getRol(),
                        "El rol debe ser 'usuario' para nuevos registros");
                return u;
            });

            // When
            usuarioService.registrar(request);

            // Then
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Registrar usuario encripta contraseña con BCrypt")
        void registrar_encriptaContrasena() {
            // Given
            RegistroRequest request = new RegistroRequest();
            request.setEmail("crypt@example.com");
            request.setContrasena("mi_password_seguro");
            request.setNombreCompleto("Crypto User");

            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("mi_password_seguro")).thenReturn("$2a$10$encrypted");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                u.setIdUsuario(4L);
                u.setFechaRegistro(LocalDateTime.now());
                assertEquals("$2a$10$encrypted", u.getContrasenaHash(),
                        "La contraseña debe estar encriptada");
                return u;
            });

            // When
            usuarioService.registrar(request);

            // Then
            verify(passwordEncoder).encode("mi_password_seguro");
        }

        @Test
        @DisplayName("Registrar usuario activa la cuenta por defecto")
        void registrar_activaCuentaPorDefecto() {
            // Given
            RegistroRequest request = new RegistroRequest();
            request.setEmail("active@example.com");
            request.setContrasena("pass123");
            request.setNombreCompleto("Active User");

            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hash");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                u.setIdUsuario(5L);
                u.setFechaRegistro(LocalDateTime.now());
                assertTrue(u.getActivo(), "La cuenta debe estar activa por defecto");
                return u;
            });

            // When
            usuarioService.registrar(request);

            // Then
            verify(usuarioRepository).save(any(Usuario.class));
        }
    }

    // ==========================================
    // OBTENER POR ID
    // ==========================================

    @Nested
    @DisplayName("obtenerPorId()")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Obtener usuario existente retorna DTO completo")
        void obtenerPorId_conIdExistente_retornaDTO() {
            // Given
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));

            // When
            UsuarioDTO resultado = usuarioService.obtenerPorId(1L);

            // Then
            assertNotNull(resultado);
            assertEquals(1L, resultado.getIdUsuario());
            assertEquals("juan@example.com", resultado.getEmail());
            assertEquals("Juan García", resultado.getNombreCompleto());
        }

        @Test
        @DisplayName("Obtener usuario inexistente lanza excepción")
        void obtenerPorId_conIdInexistente_lanzaExcepcion() {
            // Given
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> {
                usuarioService.obtenerPorId(999L);
            });
        }

        @Test
        @DisplayName("Obtener usuario con falla asociada incluye datos de falla")
        void obtenerPorId_conFallaAsociada_incluyeDatosFalla() {
            // Given
            Falla falla = new Falla();
            falla.setIdFalla(10L);
            falla.setNombre("Mi Falla");
            usuarioMock.setFalla(falla);
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));

            // When
            UsuarioDTO resultado = usuarioService.obtenerPorId(1L);

            // Then
            assertEquals(10L, resultado.getIdFalla());
            assertEquals("Mi Falla", resultado.getNombreFalla());
        }
    }

    // ==========================================
    // OBTENER POR EMAIL
    // ==========================================

    @Nested
    @DisplayName("obtenerPorEmail()")
    class ObtenerPorEmailTests {

        @Test
        @DisplayName("Obtener por email existente retorna usuario")
        void obtenerPorEmail_conEmailExistente_retornaUsuario() {
            // Given
            when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuarioMock));

            // When
            UsuarioDTO resultado = usuarioService.obtenerPorEmail("juan@example.com");

            // Then
            assertNotNull(resultado);
            assertEquals("juan@example.com", resultado.getEmail());
        }

        @Test
        @DisplayName("Obtener por email inexistente lanza excepción")
        void obtenerPorEmail_conEmailInexistente_lanzaExcepcion() {
            // Given
            when(usuarioRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> {
                usuarioService.obtenerPorEmail("noexiste@example.com");
            });
        }
    }

    // ==========================================
    // LISTAR ACTIVOS
    // ==========================================

    @Nested
    @DisplayName("listarActivos()")
    class ListarActivosTests {

        @Test
        @DisplayName("Listar usuarios activos retorna lista")
        void listarActivos_retornaListaDeActivos() {
            // Given
            Usuario usuario2 = new Usuario();
            usuario2.setIdUsuario(2L);
            usuario2.setEmail("maria@example.com");
            usuario2.setNombreCompleto("María López");
            usuario2.setRol(Usuario.RolUsuario.usuario);
            usuario2.setActivo(true);
            usuario2.setFechaRegistro(LocalDateTime.now());

            when(usuarioRepository.findByActivoTrue()).thenReturn(Arrays.asList(usuarioMock, usuario2));

            // When
            List<UsuarioDTO> resultado = usuarioService.listarActivos();

            // Then
            assertEquals(2, resultado.size());
        }

        @Test
        @DisplayName("Listar sin usuarios activos retorna lista vacía")
        void listarActivos_sinActivos_retornaListaVacia() {
            // Given
            when(usuarioRepository.findByActivoTrue()).thenReturn(List.of());

            // When
            List<UsuarioDTO> resultado = usuarioService.listarActivos();

            // Then
            assertTrue(resultado.isEmpty());
        }
    }

    // ==========================================
    // ACTUALIZAR
    // ==========================================

    @Nested
    @DisplayName("actualizar()")
    class ActualizarTests {

        @Test
        @DisplayName("Actualizar nombre completo guarda cambio")
        void actualizar_nombreCompleto_guardaCambio() {
            // Given
            UsuarioDTO dto = UsuarioDTO.builder()
                    .nombreCompleto("Juan García Actualizado")
                    .build();

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

            // When
            UsuarioDTO resultado = usuarioService.actualizar(1L, dto);

            // Then
            assertNotNull(resultado);
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Actualizar teléfono y dirección guarda cambios")
        void actualizar_telefonoYDireccion_guardaCambios() {
            // Given
            UsuarioDTO dto = UsuarioDTO.builder()
                    .telefono("666123456")
                    .direccion("Calle Mayor 1")
                    .ciudad("Valencia")
                    .codigoPostal("46001")
                    .build();

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                assertEquals("666123456", u.getTelefono());
                assertEquals("Calle Mayor 1", u.getDireccion());
                assertEquals("Valencia", u.getCiudad());
                assertEquals("46001", u.getCodigoPostal());
                return u;
            });

            // When
            usuarioService.actualizar(1L, dto);

            // Then
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Actualizar usuario inexistente lanza excepción")
        void actualizar_conUsuarioInexistente_lanzaExcepcion() {
            // Given
            UsuarioDTO dto = UsuarioDTO.builder().nombreCompleto("Test").build();
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> {
                usuarioService.actualizar(999L, dto);
            });
        }

        @Test
        @DisplayName("Actualizar con campos null no modifica esos campos")
        void actualizar_conCamposNull_noModificaCampos() {
            // Given
            usuarioMock.setTelefono("666000000");
            UsuarioDTO dto = UsuarioDTO.builder().build(); // todo null

            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                // El nombre original debe mantenerse
                assertEquals("Juan García", u.getNombreCompleto());
                return u;
            });

            // When
            usuarioService.actualizar(1L, dto);

            // Then
            verify(usuarioRepository).save(any(Usuario.class));
        }
    }

    // ==========================================
    // DESACTIVAR
    // ==========================================

    @Nested
    @DisplayName("desactivar()")
    class DesactivarTests {

        @Test
        @DisplayName("Desactivar usuario existente marca activo=false")
        void desactivar_conUsuarioExistente_marcaActivoFalse() {
            // Given
            when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                assertFalse(u.getActivo(), "El usuario debe quedar desactivado");
                return u;
            });

            // When
            usuarioService.desactivar(1L);

            // Then
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Desactivar usuario inexistente lanza excepción")
        void desactivar_conUsuarioInexistente_lanzaExcepcion() {
            // Given
            when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> {
                usuarioService.desactivar(999L);
            });
        }
    }
}
