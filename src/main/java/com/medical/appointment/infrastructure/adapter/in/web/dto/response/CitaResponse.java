package com.medical.appointment.infrastructure.adapter.in.web.dto.response;

import com.medical.appointment.domain.model.EstadoCita;

import java.time.LocalDateTime;

/** Representación de salida de una cita. */
public record CitaResponse(
        Long id,
        Long pacienteId,
        Long medicoId,
        LocalDateTime fechaHora,
        EstadoCita estado,
        LocalDateTime fechaCancelacion) {
}
