package com.medical.appointment.infrastructure.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** Petición de reprogramación de cita (RN-06). */
public record ReprogramarCitaRequest(

        @NotNull(message = "La nuevaFechaHora es obligatoria (formato ISO 8601)")
        LocalDateTime nuevaFechaHora) {
}
