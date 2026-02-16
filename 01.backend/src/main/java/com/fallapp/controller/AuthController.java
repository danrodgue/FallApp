package com.fallapp.controller;

import com.fallapp.dto.*;
import com.fallapp.model.Usuario;
import com.fallapp.repository.UsuarioRepository;
import com.fallapp.security.JwtTokenProvider;
import com.fallapp.service.UsuarioService;
import com.fallapp.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Controlador para autenticación y registro
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación y registro")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<ApiResponse<LoginResponse>> registrar(@Valid @RequestBody RegistroRequest request) {
        // Crear usuario (se encargará de encriptar la contraseña)
        UsuarioDTO usuario = usuarioService.registrar(request);
        
        // Obtener usuario de BD para generar token de verificación
        Usuario usuarioEntidad = usuarioRepository.findByEmail(usuario.getEmail())
                .orElseThrow(() -> new RuntimeException("Error al obtener usuario registrado"));
        
        // Generar token de verificación (UUID sin guiones)
        String tokenVerificacion = UUID.randomUUID().toString().replace("-", "");
        usuarioEntidad.setTokenVerificacion(tokenVerificacion);
        usuarioEntidad.setTokenVerificacionExpira(LocalDateTime.now().plusHours(24));
        usuarioEntidad.setVerificado(false);
        usuarioRepository.save(usuarioEntidad);
        
        // Enviar email de verificación
        try {
            emailService.sendVerificationEmail(usuario.getEmail(), tokenVerificacion);
        } catch (Exception e) {
            // Log error pero no falla el registro
            System.err.println("Error al enviar email de verificación: " + e.getMessage());
        }
        
        // Generar token JWT (usando email como username)
        String token = jwtTokenProvider.generateTokenFromUsername(usuario.getEmail());
        
        // Preparar respuesta
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .expiraEn(86400L)  // 24 horas
                .usuario(usuario)
                .build();
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                    "Usuario registrado. Por favor verifica tu email para activar tu cuenta.", 
                    response
                ));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Autenticar usuario por email
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getContrasena()
                    )
            );

            // Establecer autenticación en el contexto
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generar token JWT
            String token = jwtTokenProvider.generateToken(authentication);

            // Obtener usuario de la BD
            Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Actualizar último acceso (ADR-008 RESUELTO: columna rol migrada a VARCHAR)
            usuario.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(usuario);

            // Convertir a DTO
            UsuarioDTO usuarioDTO = usuarioService.convertirADTO(usuario);

            // Preparar respuesta
            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .tipo("Bearer")
                    .expiraEn(86400L)  // 24 horas
                    .usuario(usuarioDTO)
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Login exitoso", response));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Credenciales inválidas"));
        }
    }

    @GetMapping("/verificar")
    @Operation(summary = "Verificar email con token")
    public ResponseEntity<ApiResponse<String>> verificarEmail(@RequestParam String token) {
        // Buscar usuario por token
        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> token.equals(u.getTokenVerificacion()))
                .findFirst()
                .orElse(null);
        
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Token de verificación inválido"));
        }
        
        // Verificar si el token ha expirado
        if (usuario.getTokenVerificacionExpira() != null 
            && usuario.getTokenVerificacionExpira().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Token de verificación expirado"));
        }
        
        // Marcar como verificado
        usuario.setVerificado(true);
        usuario.setTokenVerificacion(null);
        usuario.setTokenVerificacionExpira(null);
        usuarioRepository.save(usuario);
        
        return ResponseEntity.ok(
            ApiResponse.success("Email verificado correctamente. Ya puedes usar tu cuenta.", null)
        );
    }

    @PostMapping("/reenviar-verificacion")
    @Operation(summary = "Reenviar email de verificación")
    public ResponseEntity<ApiResponse<String>> reenviarVerificacion(@RequestParam String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElse(null);
        
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado"));
        }
        
        if (usuario.getVerificado()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("La cuenta ya está verificada"));
        }
        
        // Generar nuevo token
        String tokenVerificacion = UUID.randomUUID().toString().replace("-", "");
        usuario.setTokenVerificacion(tokenVerificacion);
        usuario.setTokenVerificacionExpira(LocalDateTime.now().plusHours(24));
        usuarioRepository.save(usuario);
        
        // Reenviar email
        try {
            emailService.sendVerificationEmail(email, tokenVerificacion);
            return ResponseEntity.ok(
                ApiResponse.success("Email de verificación reenviado correctamente", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al enviar email: " + e.getMessage()));
        }
    }
}
