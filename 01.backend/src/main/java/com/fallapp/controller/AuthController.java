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
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Controlador para autenticación y registro
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación y registro")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<ApiResponse<LoginResponse>> registrar(@Valid @RequestBody RegistroRequest request) {
        // Crear usuario (se encargará de encriptar la contraseña)
        UsuarioDTO usuario = usuarioService.registrar(request);
        
        // Enviar email de bienvenida
        boolean emailEnviado = true;
        try {
            emailService.sendWelcomeEmail(usuario.getEmail(), usuario.getNombreCompleto());
        } catch (Exception e) {
            emailEnviado = false;
            logger.error("Error al enviar email de bienvenida a {}", usuario.getEmail(), e);
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
                    emailEnviado
                        ? "Usuario registrado correctamente. Te hemos enviado un correo de bienvenida."
                        : "Usuario registrado correctamente, pero no se pudo enviar el correo de bienvenida.",
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
}
