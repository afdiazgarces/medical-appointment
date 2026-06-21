package com.medical.appointment.infrastructure.adapter.out.persistence.repository;

import com.medical.appointment.infrastructure.adapter.out.persistence.entity.PenalizacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PenalizacionJpaRepository extends JpaRepository<PenalizacionEntity, Long> {

    long countByPacienteIdAndFechaPenalizacionGreaterThanEqual(Long pacienteId, LocalDateTime desde);
}
