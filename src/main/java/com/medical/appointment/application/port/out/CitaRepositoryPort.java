package com.medical.appointment.application.port.out;

import com.medical.appointment.application.port.in.command.FiltroCitas;
import com.medical.appointment.domain.model.Cita;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Puerto de salida para la persistencia y consulta de citas. */
public interface CitaRepositoryPort {

    Cita guardar(Cita cita);

    Optional<Cita> buscarPorId(Long id);

    /** RN-02: ¿el médico ya tiene una cita PROGRAMADA en esa franja exacta? */
    boolean existeCitaProgramadaDeMedico(Long medicoId, LocalDateTime fechaHora);

    /** RN-04: ¿el paciente ya tiene una cita PROGRAMADA con ese médico en esa franja? */
    boolean existeCitaProgramadaDePacienteConMedico(Long pacienteId, Long medicoId, LocalDateTime fechaHora);

    /** RF-04: citas PROGRAMADAS de un médico dentro de un rango (para excluir franjas ocupadas). */
    List<Cita> buscarProgramadasDeMedicoEntre(Long medicoId, LocalDateTime desde, LocalDateTime hasta);

    /** RF-06: listado con filtros opcionales. */
    List<Cita> buscarConFiltros(FiltroCitas filtro);
}
