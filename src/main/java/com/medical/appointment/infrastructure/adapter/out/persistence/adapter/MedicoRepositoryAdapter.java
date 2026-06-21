package com.medical.appointment.infrastructure.adapter.out.persistence.adapter;

import com.medical.appointment.application.port.out.MedicoRepositoryPort;
import com.medical.appointment.domain.model.Medico;
import com.medical.appointment.infrastructure.adapter.out.persistence.mapper.MedicoPersistenceMapper;
import com.medical.appointment.infrastructure.adapter.out.persistence.repository.MedicoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MedicoRepositoryAdapter implements MedicoRepositoryPort {

    private final MedicoJpaRepository jpaRepository;
    private final MedicoPersistenceMapper mapper;

    public MedicoRepositoryAdapter(MedicoJpaRepository jpaRepository, MedicoPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Medico guardar(Medico medico) {
        return mapper.aDominio(jpaRepository.save(mapper.aEntidad(medico)));
    }

    @Override
    public Optional<Medico> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::aDominio);
    }

    @Override
    public List<Medico> listar() {
        return jpaRepository.findAll().stream().map(mapper::aDominio).toList();
    }

    @Override
    public boolean existePorId(Long id) {
        return jpaRepository.existsById(id);
    }
}
