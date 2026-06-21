package com.medical.appointment.application.port.in.command;

import java.time.LocalDateTime;

/** Datos necesarios para reservar una cita (RF-03). */
public record ReservarCitaCommand(
        Long pacienteId,
        Long medicoId,
        LocalDateTime fechaHora) {
}
