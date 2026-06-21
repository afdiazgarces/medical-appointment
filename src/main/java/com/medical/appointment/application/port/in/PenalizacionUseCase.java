package com.medical.appointment.application.port.in;

import com.medical.appointment.application.port.in.command.FiltroPenalizaciones;
import com.medical.appointment.domain.model.Penalizacion;

import java.util.List;

/** Consultas de penalizaciones (RN-05). */
public interface PenalizacionUseCase {

    /** Devuelve todas las penalizaciones, con filtros opcionales por paciente o cita. */
    List<Penalizacion> listar(FiltroPenalizaciones filtro);
}
