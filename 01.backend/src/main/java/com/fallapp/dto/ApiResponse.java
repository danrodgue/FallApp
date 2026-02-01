package com.fallapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper genérico para respuestas de la API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private Boolean exito;
    private String mensaje;
    private T datos;

    public static <T> ApiResponse<T> success(T datos) {
        return ApiResponse.<T>builder()
                .exito(true)
                .datos(datos)
                .build();
    }

    public static <T> ApiResponse<T> success(String mensaje, T datos) {
        return ApiResponse.<T>builder()
                .exito(true)
                .mensaje(mensaje)
                .datos(datos)
                .build();
    }

    public static <T> ApiResponse<T> error(String mensaje) {
        return ApiResponse.<T>builder()
                .exito(false)
                .mensaje(mensaje)
                .build();
    }
}
