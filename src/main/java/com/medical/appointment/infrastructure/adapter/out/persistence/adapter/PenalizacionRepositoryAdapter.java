package com.medical.appointment.infrastructure.adapter.out.persistence.adapter;

import com.medical.appointment.application.port.in.command.FiltroPenalizaciones;
import com.medical.appointment.application.port.out.PenalizacionRepositoryPort;
import com.medical.appointment.domain.model.Penalizacion;
import com.medical.appointment.infrastructure.adapter.out.persistence.mapper.PenalizacionPersistenceMapper;
import com.medical.appointment.infrastructure.adapter.out.persistence.repository.PenalizacionJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PenalizacionRepositoryAdapter implements PenalizacionRepositoryPort {

    private final PenalizacionJpaRepository jpaRepository;
    private final PenalizacionPersistenceMapper mapper;

    public PenalizacionRepositoryAdapter(PenalizacionJpaRepository jpaRepository,
                                         PenalizacionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Penalizacion guardar(Penalizacion penalizacion) {
        return mapper.aDominio(jpaRepository.save(mapper.aEntidad(penalizacion)));
    }

    @Override
    public long contarDePacienteDesde(Long pacienteId, LocalDateTime desde) {
        return jpaRepository.countByPacienteIdAndFechaPenalizacionGreaterThanEqual(pacienteId, desde);
    }

    @Override
    public List<Penalizacion> listar(FiltroPenalizaciones filtro) {
        return jpaRepository.buscarConFiltros(filtro.pacienteId(), filtro.citaId())
                .stream().map(mapper::aDominio).toList();
    }
}
