package com.medical.appointment.infrastructure.adapter.in.web.dto.response;

/** Representación de salida de un médico. */
public record MedicoResponse(
        Long id,
        String nombreCompleto,
        String especialidad,
        String telefono,
        String email) {
}
