package com.medical.appointment.application.port.in;

import com.medical.appointment.application.port.in.command.FiltroCitas;
import com.medical.appointment.application.port.in.command.ReservarCitaCommand;
import com.medical.appointment.domain.model.Cita;
import com.medical.appointment.domain.model.FranjaHoraria;

import java.time.LocalDateTime;
import java.util.List;

/** Casos de uso de citas (RF-03, RF-04, RF-05, RF-06 y RN-06). */
public interface CitaUseCase {

    /** RF-03: reserva una cita aplicando RN-01..RN-05. */
    Cita reservar(ReservarCitaCommand command);

    /** RF-05: cancela una cita y aplica la penalización si corresponde (RN-05). */
    Cita cancelar(Long citaId);

    /** RN-06: reprograma una cita (cancela la anterior y crea una nueva, transaccional). */
    Cita reprogramar(Long citaId, LocalDateTime nuevaFechaHora);

    /** RF-04: franjas de 30 min disponibles de un médico en un rango. */
    List<FranjaHoraria> consultarDisponibilidad(Long medicoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /** RF-06: listado de citas con filtros opcionales. */
    List<Cita> listar(FiltroCitas filtro);
}
