package com.medical.appointment.infrastructure.adapter.out.persistence.mapper;

import com.medical.appointment.domain.model.Penalizacion;
import com.medical.appointment.infrastructure.adapter.out.persistence.entity.PenalizacionEntity;
import org.springframework.stereotype.Component;

/** Conversión entre {@link Penalizacion} (dominio) y {@link PenalizacionEntity} (JPA). */
@Component
public class PenalizacionPersistenceMapper {

    public Penalizacion aDominio(PenalizacionEntity entity) {
        return Penalizacion.reconstituir(
                entity.getId(),
                entity.getPacienteId(),
                entity.getCitaId(),
                entity.getFechaPenalizacion());
    }

    public PenalizacionEntity aEntidad(Penalizacion penalizacion) {
        return PenalizacionEntity.builder()
                .id(penalizacion.getId())
                .pacienteId(penalizacion.getPacienteId())
                .citaId(penalizacion.getCitaId())
                .fechaPenalizacion(penalizacion.getFechaPenalizacion())
                .build();
    }
}
