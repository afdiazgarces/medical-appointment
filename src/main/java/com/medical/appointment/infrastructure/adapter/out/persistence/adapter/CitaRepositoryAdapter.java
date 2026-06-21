package com.medical.appointment.infrastructure.adapter.out.persistence.adapter;

import com.medical.appointment.application.port.in.command.FiltroCitas;
import com.medical.appointment.application.port.out.CitaRepositoryPort;
import com.medical.appointment.domain.model.Cita;
import com.medical.appointment.domain.model.EstadoCita;
import com.medical.appointment.infrastructure.adapter.out.persistence.mapper.CitaPersistenceMapper;
import com.medical.appointment.infrastructure.adapter.out.persistence.repository.CitaJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class CitaRepositoryAdapter implements CitaRepositoryPort {

    private final CitaJpaRepository jpaRepository;
    private final CitaPersistenceMapper mapper;

    public CitaRepositoryAdapter(CitaJpaRepository jpaRepository, CitaPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Cita guardar(Cita cita) {
        return mapper.aDominio(jpaRepository.save(mapper.aEntidad(cita)));
    }

    @Override
    public Optional<Cita> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::aDominio);
    }

    @Override
    public boolean existeCitaProgramadaDeMedico(Long medicoId, LocalDateTime fechaHora) {
        return jpaRepository.existsByMedicoIdAndFechaHoraAndEstado(medicoId, fechaHora, EstadoCita.PROGRAMADA);
    }

    @Override
    public boolean existeCitaProgramadaDePacienteConMedico(Long pacienteId, Long medicoId, LocalDateTime fechaHora) {
        return jpaRepository.existsByPacienteIdAndMedicoIdAndFechaHoraAndEstado(
                pacienteId, medicoId, fechaHora, EstadoCita.PROGRAMADA);
    }

    @Override
    public List<Cita> buscarProgramadasDeMedicoEntre(Long medicoId, LocalDateTime desde, LocalDateTime hasta) {
        return jpaRepository
                .findByMedicoIdAndEstadoAndFechaHoraBetween(medicoId, EstadoCita.PROGRAMADA, desde, hasta)
                .stream().map(mapper::aDominio).toList();
    }

    @Override
    public List<Cita> buscarConFiltros(FiltroCitas filtro) {
        return jpaRepository.buscarConFiltros(
                filtro.medicoId(),
                filtro.pacienteId(),
                filtro.estado(),
                filtro.fechaInicio(),
                filtro.fechaFin()).stream().map(mapper::aDominio).toList();
    }
}
