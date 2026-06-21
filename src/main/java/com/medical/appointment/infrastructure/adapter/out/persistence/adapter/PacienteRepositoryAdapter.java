package com.medical.appointment.infrastructure.adapter.out.persistence.adapter;

import com.medical.appointment.application.port.out.PacienteRepositoryPort;
import com.medical.appointment.domain.model.Paciente;
import com.medical.appointment.infrastructure.adapter.out.persistence.mapper.PacientePersistenceMapper;
import com.medical.appointment.infrastructure.adapter.out.persistence.repository.PacienteJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PacienteRepositoryAdapter implements PacienteRepositoryPort {

    private final PacienteJpaRepository jpaRepository;
    private final PacientePersistenceMapper mapper;

    public PacienteRepositoryAdapter(PacienteJpaRepository jpaRepository, PacientePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Paciente guardar(Paciente paciente) {
        return mapper.aDominio(jpaRepository.save(mapper.aEntidad(paciente)));
    }

    @Override
    public Optional<Paciente> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::aDominio);
    }

    @Override
    public List<Paciente> listar() {
        return jpaRepository.findAll().stream().map(mapper::aDominio).toList();
    }

    @Override
    public boolean existePorDocumento(String documentoIdentidad) {
        return jpaRepository.existsByDocumentoIdentidad(documentoIdentidad);
    }
}
