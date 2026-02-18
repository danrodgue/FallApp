package com.fallapp.service;

import com.fallapp.dto.*;
import com.fallapp.model.Usuario;
import com.fallapp.model.Falla;
import com.fallapp.exception.BadRequestException;
import com.fallapp.exception.ResourceNotFoundException;
import com.fallapp.repository.UsuarioRepository;
import com.fallapp.repository.FallaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final FallaRepository fallaRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

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
        
        // Asignar campos opcionales si se proporcionan
        if (request.getTelefono() != null && !request.getTelefono().isBlank()) {
            usuario.setTelefono(request.getTelefono());
        }
        if (request.getDireccion() != null && !request.getDireccion().isBlank()) {
            usuario.setDireccion(request.getDireccion());
        }
        if (request.getCiudad() != null && !request.getCiudad().isBlank()) {
            usuario.setCiudad(request.getCiudad());
        }
        if (request.getCodigoPostal() != null && !request.getCodigoPostal().isBlank()) {
            usuario.setCodigoPostal(request.getCodigoPostal());
        }
        
        // Determinar el rol: usar el proporcionado por la solicitud, o "usuario" por defecto
        Usuario.RolUsuario rolFinal = Usuario.RolUsuario.usuario;
        if (request.getRol() != null && !request.getRol().isBlank()) {
            try {
                rolFinal = Usuario.RolUsuario.valueOf(request.getRol().toLowerCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Rol inválido. Valores permitidos: admin, casal, usuario");
            }
        }
        usuario.setRol(rolFinal);
        
        // Si es casal, debe tener una falla asociada
        if (rolFinal == Usuario.RolUsuario.casal) {
            if (request.getIdFalla() == null || request.getIdFalla() <= 0) {
                throw new BadRequestException("Los casales deben tener una falla asociada (idFalla requerido)");
            }
            
            // Obtener y asociar la falla
            Falla falla = fallaRepository.findById(request.getIdFalla())
                    .orElseThrow(() -> new ResourceNotFoundException("Falla", "id", request.getIdFalla()));
            usuario.setFalla(falla);
        } else {
            // Para usuarios regulares, opcionalmente asociar falla si se proporciona
            if (request.getIdFalla() != null && request.getIdFalla() > 0) {
                Falla falla = fallaRepository.findById(request.getIdFalla())
                        .orElseThrow(() -> new ResourceNotFoundException("Falla", "id", request.getIdFalla()));
                usuario.setFalla(falla);
            }
        }
        
        usuario.setActivo(true);
        usuario.setVerificado(true);
        usuario.setTokenVerificacion(null);
        usuario.setTokenVerificacionExpira(null);

        Usuario guardado = usuarioRepository.save(usuario);
        return convertirADTO(guardado);
    }

    /**
     * Obtener usuario por ID
     */
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(Long id) {
        Usuario usuario = obtenerEntidadPorId(id);
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

        // Actualizar campos editables
        if (dto.getNombreCompleto() != null && !dto.getNombreCompleto().isBlank()) {
            usuario.setNombreCompleto(dto.getNombreCompleto());
        }
        
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            usuario.setEmail(dto.getEmail());
        }
        
        // Para campos opcionales: guardar si no está vacío, null si está vacío
        if (dto.getTelefono() != null) {
            usuario.setTelefono(dto.getTelefono().isBlank() ? null : dto.getTelefono());
        }
        
        if (dto.getDireccion() != null) {
            usuario.setDireccion(dto.getDireccion().isBlank() ? null : dto.getDireccion());
        }
        
        if (dto.getCiudad() != null) {
            usuario.setCiudad(dto.getCiudad().isBlank() ? null : dto.getCiudad());
        }
        
        if (dto.getCodigoPostal() != null) {
            usuario.setCodigoPostal(dto.getCodigoPostal().isBlank() ? null : dto.getCodigoPostal());
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return convertirADTO(actualizado);
    }

    /**
     * Desactivar usuario
     */
    public void desactivar(Long id) {
        Usuario usuario = obtenerEntidadPorId(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Devuelve la entidad Usuario completa (incluyendo campos perezosos como la foto de perfil).
     * Uso interno de la capa de servicio y controladores que necesiten acceso a la entidad.
     */
    @Transactional(readOnly = true)
    public Usuario obtenerEntidadPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
    }

    /**
     * Actualiza la foto de perfil del usuario almacenando la imagen como binario en BD.
     *
     * NOTA: Funcionalidad deshabilitada - la BD no tiene columnas foto_perfil/foto_perfil_content_type.
     *
     * Responsabilidades:
     * - Validar tamaño máximo razonable de la imagen
     * - Guardar bytes en campo BYTEA
     * - Guardar el Content-Type para poder servir la imagen correctamente
     *
     * La conversión a binario se hace en backend a partir del MultipartFile recibido.
     */
    /*
    public void actualizarFotoPerfil(Long id, MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            throw new BadRequestException("La imagen de perfil no puede estar vacía");
        }

        // Límite defensivo: 2 MB por foto de perfil
        long maxBytes = 2 * 1024 * 1024;
        if (foto.getSize() > maxBytes) {
            throw new BadRequestException("La imagen de perfil supera el tamaño máximo permitido (2 MB)");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        try {
            usuario.setFotoPerfil(foto.getBytes());
        } catch (java.io.IOException e) {
            throw new BadRequestException("No se ha podido leer la imagen de perfil");
        }

        String contentType = foto.getContentType();
        if (contentType == null || contentType.isBlank()) {
            // Por defecto, asumimos PNG si no viene content-type
            contentType = MediaType.IMAGE_PNG_VALUE;
        }
        usuario.setFotoPerfilContentType(contentType);

        usuarioRepository.save(usuario);
    }
    */

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
                .imagenNombre(usuario.getImagenNombre())
                .fechaCreacion(usuario.getFechaRegistro())
                .fechaActualizacion(usuario.getUltimoAcceso())
                .build();
    }

    /**
     * Guardar imagen de perfil del usuario
     * 
     * @param id ID del usuario
     * @param imagen Archivo de imagen a guardar
     * @return DTO del usuario actualizado
     */
    public UsuarioDTO guardarImagen(Long id, MultipartFile imagen) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        // Eliminar imagen anterior si existe
        if (usuario.getImagenNombre() != null && !usuario.getImagenNombre().isBlank()) {
            try {
                fileUploadService.eliminarArchivo(usuario.getImagenNombre(), "usuarios");
            } catch (Exception e) {
                // Continuar aunque falle la eliminación anterior
            }
        }

        // Guardar nueva imagen
        String nombreArchivo = fileUploadService.guardarArchivo(imagen, "usuarios");
        usuario.setImagenNombre(nombreArchivo);
        
        usuarioRepository.save(usuario);
        return convertirADTO(usuario);
    }

    /**
     * Obtener imagen de perfil del usuario
     * 
     * @param id ID del usuario
     * @return Bytes de la imagen
     */
    public byte[] obtenerImagen(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (usuario.getImagenNombre() == null || usuario.getImagenNombre().isBlank()) {
            throw new ResourceNotFoundException("El usuario no tiene imagen de perfil");
        }

        return fileUploadService.obtenerArchivo(usuario.getImagenNombre(), "usuarios");
    }

    /**
     * Obtener nombre de la imagen del usuario
     * 
     * @param id ID del usuario
     * @return Nombre del archivo de imagen
     */
    public String obtenerNombreImagen(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (usuario.getImagenNombre() == null || usuario.getImagenNombre().isBlank()) {
            throw new ResourceNotFoundException("El usuario no tiene imagen de perfil");
        }

        return usuario.getImagenNombre();
    }

    /**
     * Eliminar imagen de perfil del usuario
     * 
     * @param id ID del usuario
     */
    public void eliminarImagen(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (usuario.getImagenNombre() != null && !usuario.getImagenNombre().isBlank()) {
            fileUploadService.eliminarArchivo(usuario.getImagenNombre(), "usuarios");
            usuario.setImagenNombre(null);
            usuarioRepository.save(usuario);
        }
    }
}
