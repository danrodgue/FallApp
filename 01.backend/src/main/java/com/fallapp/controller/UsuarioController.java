package com.fallapp.controller;

import com.fallapp.dto.ApiResponse;
import com.fallapp.dto.UsuarioDTO;
import com.fallapp.service.FileUploadService;
import com.fallapp.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador para gestión de usuarios
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final FileUploadService fileUploadService;

    @GetMapping
    @Operation(summary = "Listar usuarios activos")
    public ResponseEntity<ApiResponse<List<UsuarioDTO>>> listar() {
        List<UsuarioDTO> usuarios = usuarioService.listarActivos();
        return ResponseEntity.ok(ApiResponse.success(usuarios));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<ApiResponse<UsuarioDTO>> obtenerPorId(@PathVariable Long id) {
        UsuarioDTO usuario = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.success(usuario));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<ApiResponse<UsuarioDTO>> actualizar(
            @PathVariable Long id,
            @RequestBody UsuarioDTO dto) {
        UsuarioDTO actualizado = usuarioService.actualizar(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Usuario actualizado", actualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar usuario")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.success("Usuario desactivado", null));
    }

    /**
     * POST /api/usuarios/{id}/imagen - Subir imagen de perfil del usuario
     * 
     * Guarda la imagen en el servidor y asocia el nombre del archivo al usuario
     */
    @PostMapping(path = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir imagen de perfil del usuario")
    public ResponseEntity<ApiResponse<UsuarioDTO>> subirImagen(
            @PathVariable Long id,
            @RequestPart("imagen") MultipartFile imagen) {
        
        try {
            UsuarioDTO usuarioActualizado = usuarioService.guardarImagen(id, imagen);
            return ResponseEntity.ok(ApiResponse.success("Imagen de perfil subida correctamente", usuarioActualizado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/usuarios/{id}/imagen - Descargar imagen de perfil del usuario
     * 
     * Retorna la imagen en su formato original
     */
    @GetMapping("/{id}/imagen")
    @Operation(summary = "Obtener imagen de perfil del usuario")
    public ResponseEntity<byte[]> descargarImagen(@PathVariable Long id) {
        try {
            byte[] imagenBytes = usuarioService.obtenerImagen(id);
            String imagenNombre = usuarioService.obtenerNombreImagen(id);
            
            String mimeType = fileUploadService.obtenerMimeType(imagenNombre);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            headers.setContentDisposition(org.springframework.http.ContentDisposition.builder("inline")
                    .filename(imagenNombre)
                    .build());
            
            return new ResponseEntity<>(imagenBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * DELETE /api/usuarios/{id}/imagen - Eliminar imagen de perfil del usuario
     */
    @DeleteMapping("/{id}/imagen")
    @Operation(summary = "Eliminar imagen de perfil del usuario")
    public ResponseEntity<ApiResponse<Void>> eliminarImagen(@PathVariable Long id) {
        try {
            usuarioService.eliminarImagen(id);
            return ResponseEntity.ok(ApiResponse.success("Imagen de perfil eliminada", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/usuarios/{id}/foto - Actualizar foto de perfil del usuario.
     *
     * NOTA: Endpoint deshabilitado - la BD no tiene columnas foto_perfil/foto_perfil_content_type.
     *
     * Formato: multipart/form-data con un campo "foto" que contiene la imagen.
     * La imagen se almacena como binario (BYTEA) en la base de datos.
     */
    /*
    @PutMapping(path = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar foto de perfil del usuario")
    public ResponseEntity<ApiResponse<Void>> actualizarFotoPerfil(
            @PathVariable Long id,
            @RequestPart("foto") MultipartFile foto) {

        usuarioService.actualizarFotoPerfil(id, foto);
        return ResponseEntity.ok(ApiResponse.success("Foto de perfil actualizada", null));
    }
    */

    /**
     * GET /api/usuarios/{id}/foto - Obtener la foto de perfil del usuario.
     *
     * NOTA: Endpoint deshabilitado - la BD no tiene columnas foto_perfil/foto_perfil_content_type.
     *
     * Devuelve directamente los bytes de la imagen con el Content-Type original.
     * Si el usuario no tiene foto, devuelve 404.
     */
    /*
    @GetMapping("/{id}/foto")
    @Operation(summary = "Obtener foto de perfil del usuario")
    public ResponseEntity<byte[]> obtenerFotoPerfil(@PathVariable Long id) {
        var usuario = usuarioService.obtenerEntidadPorId(id);

        byte[] foto = usuario.getFotoPerfil();
        if (foto == null || foto.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String contentType = usuario.getFotoPerfilContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.IMAGE_PNG_VALUE;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));

        return new ResponseEntity<>(foto, headers, HttpStatus.OK);
    }
    */
}
