package com.medical.appointment.application.service;

import com.medical.appointment.application.port.in.MedicoUseCase;
import com.medical.appointment.application.port.in.command.RegistrarMedicoCommand;
import com.medical.appointment.application.port.out.MedicoRepositoryPort;
import com.medical.appointment.domain.exception.RecursoNoEncontradoException;
import com.medical.appointment.domain.model.Medico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicoService implements MedicoUseCase {

    private final MedicoRepositoryPort medicoRepository;

    public MedicoService(MedicoRepositoryPort medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    @Override
    @Transactional
    public Medico registrar(RegistrarMedicoCommand command) {
        Medico medico = Medico.crear(
                command.nombreCompleto(),
                command.especialidad(),
                command.telefono(),
                command.email());
        return medicoRepository.guardar(medico);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Medico> listar() {
        return medicoRepository.listar();
    }

    @Override
    @Transactional(readOnly = true)
    public Medico obtenerPorId(Long id) {
        return medicoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico", id));
    }
}
