package com.medical.appointment.infrastructure.adapter.in.web.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** Petición de reserva de cita (RF-03). La fecha/hora se espera en formato ISO 8601. */
public record ReservarCitaRequest(

        @NotNull(message = "El pacienteId es obligatorio")
        Long pacienteId,

        @NotNull(message = "El medicoId es obligatorio")
        Long medicoId,

        @NotNull(message = "La fechaHora es obligatoria (formato ISO 8601, ej: 2026-06-22T09:00:00)")
        LocalDateTime fechaHora) {
}
