package com.fallapp.service;

import com.fallapp.dto.*;
import com.fallapp.model.Usuario;
import com.fallapp.exception.BadRequestException;
import com.fallapp.exception.ResourceNotFoundException;
import com.fallapp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de usuarios
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registrar un nuevo usuario
     */
    public UsuarioDTO registrar(RegistroRequest request) {
        // Verificar que el email no esté en uso
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setRol(Usuario.RolUsuario.usuario);
        usuario.setActivo(true);

        // TODO: Asociar falla si se proporciona idFalla

        Usuario guardado = usuarioRepository.save(usuario);
        return convertirADTO(guardado);
    }

    /**
     * Obtener usuario por ID
     */
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return convertirADTO(usuario);
    }

    /**
     * Obtener usuario por email
     */
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));
        return convertirADTO(usuario);
    }

    /**
     * Listar todos los usuarios activos
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarActivos() {
        return usuarioRepository.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar perfil de usuario
     */
    public UsuarioDTO actualizar(Long id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (dto.getNombreCompleto() != null) {
            usuario.setNombreCompleto(dto.getNombreCompleto());
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return convertirADTO(actualizado);
    }

    /**
     * Desactivar usuario
     */
    public void desactivar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Convertir entidad a DTO (público para uso en controllers)
     * Actualizado: 2026-02-04 - Incluye nuevos campos de dirección
     */
    public UsuarioDTO convertirADTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol().name())
                .idFalla(usuario.getFalla() != null ? usuario.getFalla().getIdFalla() : null)
                .nombreFalla(usuario.getFalla() != null ? usuario.getFalla().getNombre() : null)
                .activo(usuario.getActivo())
                .telefono(usuario.getTelefono())
                .direccion(usuario.getDireccion())
                .ciudad(usuario.getCiudad())
                .codigoPostal(usuario.getCodigoPostal())
                .fechaCreacion(usuario.getFechaRegistro())
                .fechaActualizacion(usuario.getUltimoAcceso())
                .build();
    }
}
