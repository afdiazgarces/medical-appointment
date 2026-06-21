package com.medical.appointment.application.port.out;

import com.medical.appointment.domain.model.Medico;

import java.util.List;
import java.util.Optional;

/** Puerto de salida para la persistencia de médicos. */
public interface MedicoRepositoryPort {

    Medico guardar(Medico medico);

    Optional<Medico> buscarPorId(Long id);

    List<Medico> listar();

    boolean existePorId(Long id);
}
