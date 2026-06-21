package com.medical.appointment.domain.model;

import com.medical.appointment.domain.exception.ReglaNegocioException;
import com.medical.appointment.domain.policy.PoliticaPenalizacion;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Cita médica (RF-03). Agregado raíz que encapsula su ciclo de vida.
 * <p>
 * Referencia a paciente y médico por id (no por objeto) para mantener los agregados
 * desacoplados. El comportamiento de cancelación (RF-05) y la detección de cancelación
 * tardía (RN-05) viven aquí, no en el servicio.
 */
@Getter
public class Cita {

    private final Long id;
    private final Long pacienteId;
    private final Long medicoId;
    private final LocalDateTime fechaHora;
    private EstadoCita estado;
    private LocalDateTime fechaCancelacion;

    private Cita(Long id, Long pacienteId, Long medicoId, LocalDateTime fechaHora,
                 EstadoCita estado, LocalDateTime fechaCancelacion) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.fechaCancelacion = fechaCancelacion;
    }

    /** Crea una cita nueva en estado PROGRAMADA (sin id). */
    public static Cita programar(Long pacienteId, Long medicoId, LocalDateTime fechaHora) {
        Objects.requireNonNull(pacienteId, "pacienteId es obligatorio");
        Objects.requireNonNull(medicoId, "medicoId es obligatorio");
        Objects.requireNonNull(fechaHora, "fechaHora es obligatoria");
        return new Cita(null, pacienteId, medicoId, fechaHora, EstadoCita.PROGRAMADA, null);
    }

    /** Reconstruye una cita existente desde persistencia. */
    public static Cita reconstituir(Long id, Long pacienteId, Long medicoId, LocalDateTime fechaHora,
                                    EstadoCita estado, LocalDateTime fechaCancelacion) {
        return new Cita(id, pacienteId, medicoId, fechaHora, estado, fechaCancelacion);
    }

    /** Franja de 30 minutos que ocupa esta cita. */
    public FranjaHoraria franja() {
        return FranjaHoraria.de(fechaHora);
    }

    /**
     * Cancela la cita (RF-05). Solo es válido sobre una cita PROGRAMADA.
     *
     * @param ahora instante de la cancelación (se registra como {@code fechaCancelacion}).
     * @throws ReglaNegocioException si la cita no está en estado PROGRAMADA.
     */
    public void cancelar(LocalDateTime ahora) {
        if (!estado.permiteCancelacion()) {
            throw new ReglaNegocioException(
                    "Solo se puede cancelar una cita en estado PROGRAMADA; estado actual: " + estado);
        }
        this.estado = EstadoCita.CANCELADA;
        this.fechaCancelacion = ahora;
    }

    /**
     * Indica si la cancelación es tardía según RN-05: ocurre con menos de 2 horas de
     * antelación respecto a la hora programada (incluye cancelaciones posteriores a ella).
     */
    public boolean esCancelacionTardia(LocalDateTime ahora) {
        Duration antelacion = Duration.between(ahora, fechaHora);
        return antelacion.compareTo(PoliticaPenalizacion.ANTELACION_MINIMA) < 0;
    }
}
