package com.medical.appointment.application.port.in.command;

/** Filtros opcionales para la consulta de penalizaciones. */
public record FiltroPenalizaciones(Long pacienteId, Long citaId) {

    /** Sin filtros: devuelve todas las penalizaciones. */
    public static FiltroPenalizaciones sinFiltros() {
        return new FiltroPenalizaciones(null, null);
    }
}
