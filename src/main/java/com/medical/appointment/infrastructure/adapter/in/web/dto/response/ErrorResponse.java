package com.medical.appointment.infrastructure.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Payload de error uniforme para todas las respuestas no exitosas.
 * Los campos nulos (p. ej. {@code detalles}) se omiten de la serialización.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> detalles) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ErrorResponse of(int status, String error, String message, String path, List<String> detalles) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, detalles);
    }
}
