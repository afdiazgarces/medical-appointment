package com.medical.appointment.application.port.in.command;

import com.medical.appointment.domain.model.EstadoCita;

import java.time.LocalDateTime;

/**
 * Filtros opcionales para el listado de citas (RF-06). Cualquier campo en {@code null}
 * se ignora en la búsqueda.
 */
public record FiltroCitas(
        Long medicoId,
        Long pacienteId,
        EstadoCita estado,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin) {
}
