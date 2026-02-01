package com.fallapp.exception;

/**
 * Excepción para peticiones inválidas (HTTP 400)
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String mensaje) {
        super(mensaje);
    }
}
