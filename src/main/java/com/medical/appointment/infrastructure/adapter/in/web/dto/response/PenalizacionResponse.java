package com.medical.appointment.infrastructure.adapter.in.web.dto.response;

import java.time.LocalDateTime;

/** Representación de salida de una penalización (RN-05). */
public record PenalizacionResponse(
        Long id,
        Long pacienteId,
        Long citaId,
        LocalDateTime fechaPenalizacion) {
}
