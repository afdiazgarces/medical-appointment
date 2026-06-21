package com.medical.appointment.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Penalización registrada a un paciente por una cancelación tardía (RN-05).
 * <p>
 * Se asocia siempre a la cita que la originó y al paciente penalizado. La acumulación
 * de penalizaciones en una ventana de 30 días bloquea el agendamiento.
 */
@Getter
public class Penalizacion {

    private final Long id;
    private final Long pacienteId;
    private final Long citaId;
    private final LocalDateTime fechaPenalizacion;

    private Penalizacion(Long id, Long pacienteId, Long citaId, LocalDateTime fechaPenalizacion) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.citaId = citaId;
        this.fechaPenalizacion = fechaPenalizacion;
    }

    /** Registra una penalización nueva (sin id) para el paciente y cita indicados. */
    public static Penalizacion registrar(Long pacienteId, Long citaId, LocalDateTime fechaPenalizacion) {
        return new Penalizacion(
                null,
                Objects.requireNonNull(pacienteId, "pacienteId es obligatorio"),
                Objects.requireNonNull(citaId, "citaId es obligatorio"),
                Objects.requireNonNull(fechaPenalizacion, "fechaPenalizacion es obligatoria"));
    }

    /** Reconstruye una penalización existente desde persistencia. */
    public static Penalizacion reconstituir(Long id, Long pacienteId, Long citaId, LocalDateTime fechaPenalizacion) {
        return new Penalizacion(id, pacienteId, citaId, fechaPenalizacion);
    }
}
