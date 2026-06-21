package com.medical.appointment.infrastructure.adapter.in.web.dto.response;

import java.time.LocalDate;

/** Representación de salida de un paciente. */
public record PacienteResponse(
        Long id,
        String nombreCompleto,
        String documentoIdentidad,
        String telefono,
        String email,
        LocalDate fechaNacimiento) {
}
