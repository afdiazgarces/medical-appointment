package com.medical.appointment.domain.model;

/**
 * Estados posibles de una cita.
 * <p>
 * Coincide exactamente con el CHECK de la columna {@code citas.estado} definido en
 * {@code schema.sql}: ('PROGRAMADA', 'CANCELADA', 'ATENDIDA').
 */
public enum EstadoCita {

    PROGRAMADA,
    CANCELADA,
    ATENDIDA;

    /**
     * Solo una cita PROGRAMADA puede cancelarse o reprogramarse (RF-05 / RN-06).
     */
    public boolean permiteCancelacion() {
        return this == PROGRAMADA;
    }
}
