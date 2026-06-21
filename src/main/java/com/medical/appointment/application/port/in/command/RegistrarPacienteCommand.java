package com.medical.appointment.application.port.in.command;

import java.time.LocalDate;

/** Datos necesarios para registrar un paciente (RF-02). */
public record RegistrarPacienteCommand(
        String nombreCompleto,
        String documentoIdentidad,
        String telefono,
        String email,
        LocalDate fechaNacimiento) {
}
