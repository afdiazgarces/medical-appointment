package com.medical.appointment.application.port.out;

import com.medical.appointment.domain.model.Paciente;

import java.util.List;
import java.util.Optional;

/** Puerto de salida para la persistencia de pacientes. */
public interface PacienteRepositoryPort {

    Paciente guardar(Paciente paciente);

    Optional<Paciente> buscarPorId(Long id);

    List<Paciente> listar();

    boolean existePorDocumento(String documentoIdentidad);
}
