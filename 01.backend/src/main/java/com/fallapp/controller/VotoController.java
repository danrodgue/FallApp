package com.fallapp.controller;

import com.fallapp.dto.ApiResponse;
import com.fallapp.dto.CrearVotoRequest;
import com.fallapp.dto.VotoDTO;
import com.fallapp.exception.BadRequestException;
import com.fallapp.exception.ResourceNotFoundException;
import com.fallapp.model.Usuario;
import com.fallapp.repository.UsuarioRepository;
import com.fallapp.service.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión de votos
 */
@RestController
@RequestMapping("/api/votos")
@RequiredArgsConstructor
@Tag(name = "Votos", description = "Gestión de votos a fallas")
public class VotoController {

    private final VotoService votoService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    @Operation(summary = "Crear un nuevo voto")
    public ResponseEntity<ApiResponse<VotoDTO>> votar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CrearVotoRequest request) {
        // Obtener usuario autenticado desde JWT (username es el email)
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        VotoDTO voto = votoService.votar(usuario.getIdUsuario(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Voto registrado", voto));
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Obtener votos de un usuario")
    public ResponseEntity<ApiResponse<List<VotoDTO>>> obtenerVotosUsuario(
            @PathVariable Long idUsuario,
            @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuarioAutenticado = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!usuarioAutenticado.getIdUsuario().equals(idUsuario) && 
            !usuarioAutenticado.getRol().name().equals("ADMIN")) {
            throw new BadRequestException("No tienes permisos para ver los votos de otro usuario");
        }
        List<VotoDTO> votos = votoService.obtenerVotosUsuario(idUsuario);
        return ResponseEntity.ok(ApiResponse.success(votos));
    }

    @GetMapping("/falla/{idFalla}")
    @Operation(summary = "Obtener votos de una falla")
    public ResponseEntity<ApiResponse<List<VotoDTO>>> obtenerVotosFalla(@PathVariable Long idFalla) {
        List<VotoDTO> votos = votoService.obtenerVotosFalla(idFalla);
        return ResponseEntity.ok(ApiResponse.success(votos));
    }

    @DeleteMapping("/{idVoto}")
    @Operation(summary = "Eliminar un voto")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long idVoto,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Obtener usuario autenticado (username es el email)
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        votoService.eliminar(idVoto, usuario.getIdUsuario());
        return ResponseEntity.ok(ApiResponse.success("Voto eliminado", null));
    }
}

