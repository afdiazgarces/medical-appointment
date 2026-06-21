package com.medical.appointment.infrastructure.adapter.out.persistence.mapper;

import com.medical.appointment.domain.model.Cita;
import com.medical.appointment.infrastructure.adapter.out.persistence.entity.CitaEntity;
import org.springframework.stereotype.Component;

/** Conversión entre {@link Cita} (dominio) y {@link CitaEntity} (JPA). */
@Component
public class CitaPersistenceMapper {

    public Cita aDominio(CitaEntity entity) {
        return Cita.reconstituir(
                entity.getId(),
                entity.getPacienteId(),
                entity.getMedicoId(),
                entity.getFechaHora(),
                entity.getEstado(),
                entity.getFechaCancelacion());
    }

    public CitaEntity aEntidad(Cita cita) {
        return CitaEntity.builder()
                .id(cita.getId())
                .pacienteId(cita.getPacienteId())
                .medicoId(cita.getMedicoId())
                .fechaHora(cita.getFechaHora())
                .estado(cita.getEstado())
                .fechaCancelacion(cita.getFechaCancelacion())
                .build();
    }
}
