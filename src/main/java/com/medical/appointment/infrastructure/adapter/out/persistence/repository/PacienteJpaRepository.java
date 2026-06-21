package com.medical.appointment.infrastructure.adapter.out.persistence.repository;

import com.medical.appointment.infrastructure.adapter.out.persistence.entity.PacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteJpaRepository extends JpaRepository<PacienteEntity, Long> {

    boolean existsByDocumentoIdentidad(String documentoIdentidad);
}
