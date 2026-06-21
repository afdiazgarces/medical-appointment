package com.medical.appointment.application.port.out;

import com.medical.appointment.domain.model.Penalizacion;

import java.time.LocalDateTime;

/** Puerto de salida para la persistencia de penalizaciones (RN-05). */
public interface PenalizacionRepositoryPort {

    Penalizacion guardar(Penalizacion penalizacion);

    /** Cuenta las penalizaciones de un paciente a partir de un instante (ventana de 30 días). */
    long contarDePacienteDesde(Long pacienteId, LocalDateTime desde);
}
