package com.medical.appointment.infrastructure.adapter.out.persistence.mapper;

import com.medical.appointment.domain.model.Medico;
import com.medical.appointment.infrastructure.adapter.out.persistence.entity.MedicoEntity;
import org.springframework.stereotype.Component;

/** Conversión entre {@link Medico} (dominio) y {@link MedicoEntity} (JPA). */
@Component
public class MedicoPersistenceMapper {

    public Medico aDominio(MedicoEntity entity) {
        return Medico.reconstituir(
                entity.getId(),
                entity.getNombreCompleto(),
                entity.getEspecialidad(),
                entity.getTelefono(),
                entity.getEmail());
    }

    public MedicoEntity aEntidad(Medico medico) {
        return MedicoEntity.builder()
                .id(medico.getId())
                .nombreCompleto(medico.getNombreCompleto())
                .especialidad(medico.getEspecialidad())
                .telefono(medico.getTelefono())
                .email(medico.getEmail())
                .build();
    }
}
