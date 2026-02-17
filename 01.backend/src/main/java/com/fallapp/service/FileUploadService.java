package com.fallapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Servicio para gestión de archivos subidos
 */
@Service
public class FileUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Guardar un archivo subido
     * @param file Archivo a guardar
     * @param subfolder Subcarpeta dentro del directorio de uploads
     * @return Nombre del archivo guardado
     */
    public String guardarArchivo(MultipartFile file, String subfolder) {
        try {
            // Crear directorio si no existe
            Path uploadPath = Paths.get(uploadDir, subfolder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generar nombre único
            String extension = getExtension(file.getOriginalFilename());
            String nombreArchivo = UUID.randomUUID().toString() + extension;

            // Guardar archivo
            Path filePath = uploadPath.resolve(nombreArchivo);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return nombreArchivo;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Eliminar un archivo
     * @param nombreArchivo Nombre del archivo a eliminar
     * @param subfolder Subcarpeta donde está el archivo
     */
    public void eliminarArchivo(String nombreArchivo, String subfolder) {
        try {
            Path filePath = Paths.get(uploadDir, subfolder, nombreArchivo);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener un archivo
     * @param nombreArchivo Nombre del archivo
     * @param subfolder Subcarpeta donde está el archivo
     * @return Bytes del archivo
     */
    public byte[] obtenerArchivo(String nombreArchivo, String subfolder) {
        try {
            Path filePath = Paths.get(uploadDir, subfolder, nombreArchivo);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener el tipo MIME de un archivo
     * @param nombreArchivo Nombre del archivo
     * @return Tipo MIME
     */
    public String obtenerMimeType(String nombreArchivo) {
        try {
            String extension = getExtension(nombreArchivo).toLowerCase();
            return switch (extension) {
                case ".jpg", ".jpeg" -> "image/jpeg";
                case ".png" -> "image/png";
                case ".gif" -> "image/gif";
                case ".pdf" -> "application/pdf";
                default -> "application/octet-stream";
            };
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

    /**
     * Obtener la extensión de un archivo
     */
    private String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
}
