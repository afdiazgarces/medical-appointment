package com.medical.appointment.infrastructure.adapter.out.persistence.mapper;

import com.medical.appointment.domain.model.Paciente;
import com.medical.appointment.infrastructure.adapter.out.persistence.entity.PacienteEntity;
import org.springframework.stereotype.Component;

/** Conversión entre {@link Paciente} (dominio) y {@link PacienteEntity} (JPA). */
@Component
public class PacientePersistenceMapper {

    public Paciente aDominio(PacienteEntity entity) {
        return Paciente.reconstituir(
                entity.getId(),
                entity.getNombreCompleto(),
                entity.getDocumentoIdentidad(),
                entity.getTelefono(),
                entity.getEmail(),
                entity.getFechaNacimiento());
    }

    public PacienteEntity aEntidad(Paciente paciente) {
        return PacienteEntity.builder()
                .id(paciente.getId())
                .nombreCompleto(paciente.getNombreCompleto())
                .documentoIdentidad(paciente.getDocumentoIdentidad())
                .telefono(paciente.getTelefono())
                .email(paciente.getEmail())
                .fechaNacimiento(paciente.getFechaNacimiento())
                .build();
    }
}
