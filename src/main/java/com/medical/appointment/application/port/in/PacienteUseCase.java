package com.medical.appointment.application.port.in;

import com.medical.appointment.application.port.in.command.RegistrarPacienteCommand;
import com.medical.appointment.domain.model.Paciente;

import java.util.List;

/** Casos de uso de pacientes (RF-02). */
public interface PacienteUseCase {

    Paciente registrar(RegistrarPacienteCommand command);

    List<Paciente> listar();

    Paciente obtenerPorId(Long id);
}
