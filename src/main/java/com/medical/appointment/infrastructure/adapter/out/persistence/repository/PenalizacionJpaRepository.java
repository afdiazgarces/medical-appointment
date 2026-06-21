package com.medical.appointment.infrastructure.adapter.out.persistence.repository;

import com.medical.appointment.infrastructure.adapter.out.persistence.entity.PenalizacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PenalizacionJpaRepository extends JpaRepository<PenalizacionEntity, Long> {

    long countByPacienteIdAndFechaPenalizacionGreaterThanEqual(Long pacienteId, LocalDateTime desde);

    /** Filtros opcionales; cualquier parámetro {@code null} se ignora. */
    @Query("""
            SELECT p FROM PenalizacionEntity p
            WHERE (:pacienteId IS NULL OR p.pacienteId = :pacienteId)
              AND (:citaId     IS NULL OR p.citaId     = :citaId)
            ORDER BY p.fechaPenalizacion DESC
            """)
    List<PenalizacionEntity> buscarConFiltros(
            @Param("pacienteId") Long pacienteId,
            @Param("citaId") Long citaId);
}
