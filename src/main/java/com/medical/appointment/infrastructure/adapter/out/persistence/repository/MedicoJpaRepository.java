package com.medical.appointment.infrastructure.adapter.out.persistence.repository;

import com.medical.appointment.infrastructure.adapter.out.persistence.entity.MedicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicoJpaRepository extends JpaRepository<MedicoEntity, Long> {
}
