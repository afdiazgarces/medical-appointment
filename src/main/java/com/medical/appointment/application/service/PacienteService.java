package com.medical.appointment.application.service;

import com.medical.appointment.application.port.in.PacienteUseCase;
import com.medical.appointment.application.port.in.command.RegistrarPacienteCommand;
import com.medical.appointment.application.port.out.PacienteRepositoryPort;
import com.medical.appointment.domain.exception.ConflictoException;
import com.medical.appointment.domain.exception.RecursoNoEncontradoException;
import com.medical.appointment.domain.model.Paciente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PacienteService implements PacienteUseCase {

    private final PacienteRepositoryPort pacienteRepository;

    public PacienteService(PacienteRepositoryPort pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    @Transactional
    public Paciente registrar(RegistrarPacienteCommand command) {
        Paciente paciente = Paciente.crear(
                command.nombreCompleto(),
                command.documentoIdentidad(),
                command.telefono(),
                command.email(),
                command.fechaNacimiento());

        // Unicidad del documento de identidad (también garantizada por el UNIQUE de schema.sql).
        if (pacienteRepository.existePorDocumento(paciente.getDocumentoIdentidad())) {
            throw new ConflictoException(
                    "Ya existe un paciente con el documento " + paciente.getDocumentoIdentidad());
        }
        return pacienteRepository.guardar(paciente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Paciente> listar() {
        return pacienteRepository.listar();
    }

    @Override
    @Transactional(readOnly = true)
    public Paciente obtenerPorId(Long id) {
        return pacienteRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente", id));
    }
}
