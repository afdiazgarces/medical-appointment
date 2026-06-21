package com.medical.appointment.infrastructure.adapter.out.persistence.repository;

import com.medical.appointment.domain.model.EstadoCita;
import com.medical.appointment.infrastructure.adapter.out.persistence.entity.CitaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaJpaRepository extends JpaRepository<CitaEntity, Long> {

    /** RN-02: médico ocupado en esa franja. */
    boolean existsByMedicoIdAndFechaHoraAndEstado(Long medicoId, LocalDateTime fechaHora, EstadoCita estado);

    /** RN-NEW-2: paciente ya tiene una cita en esa franja con cualquier médico. */
    boolean existsByPacienteIdAndFechaHoraAndEstado(Long pacienteId, LocalDateTime fechaHora, EstadoCita estado);

    /** RN-NEW-1: cuenta citas programadas de un paciente dentro de un rango de fechas. */
    long countByPacienteIdAndEstadoAndFechaHoraBetween(
            Long pacienteId, EstadoCita estado, LocalDateTime desde, LocalDateTime hasta);

    List<CitaEntity> findByMedicoIdAndEstadoAndFechaHoraBetween(
            Long medicoId, EstadoCita estado, LocalDateTime desde, LocalDateTime hasta);

    /** RF-06: filtros opcionales; cualquier parámetro {@code null} se ignora. */
    @Query("""
            SELECT c FROM CitaEntity c
            WHERE (:medicoId   IS NULL OR c.medicoId   = :medicoId)
              AND (:pacienteId IS NULL OR c.pacienteId = :pacienteId)
              AND (:estado     IS NULL OR c.estado     = :estado)
              AND (:desde      IS NULL OR c.fechaHora >= :desde)
              AND (:hasta      IS NULL OR c.fechaHora <= :hasta)
            ORDER BY c.fechaHora ASC
            """)
    List<CitaEntity> buscarConFiltros(
            @Param("medicoId") Long medicoId,
            @Param("pacienteId") Long pacienteId,
            @Param("estado") EstadoCita estado,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
