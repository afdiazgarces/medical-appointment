package com.medical.appointment.application.port.in.command;

/** Datos necesarios para registrar un médico (RF-01). */
public record RegistrarMedicoCommand(
        String nombreCompleto,
        String especialidad,
        String telefono,
        String email) {
}
