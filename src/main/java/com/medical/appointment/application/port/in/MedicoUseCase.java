package com.medical.appointment.application.port.in;

import com.medical.appointment.application.port.in.command.RegistrarMedicoCommand;
import com.medical.appointment.domain.model.Medico;

import java.util.List;

/** Casos de uso de médicos (RF-01). */
public interface MedicoUseCase {

    Medico registrar(RegistrarMedicoCommand command);

    List<Medico> listar();

    Medico obtenerPorId(Long id);
}
