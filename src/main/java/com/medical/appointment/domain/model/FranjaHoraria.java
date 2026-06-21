package com.medical.appointment.domain.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Value Object que representa una franja de atención de 30 minutos (RN-01 / RF-04).
 * <p>
 * Es inmutable y se identifica por su instante de inicio. El inicio debe estar
 * alineado a los minutos {@code :00} o {@code :30}, sin segundos ni nanosegundos.
 */
public record FranjaHoraria(LocalDateTime inicio) {

    /** Duración fija de cada franja. */
    public static final Duration DURACION = Duration.ofMinutes(30);

    public FranjaHoraria {
        Objects.requireNonNull(inicio, "El inicio de la franja es obligatorio");
        if (inicio.getSecond() != 0 || inicio.getNano() != 0) {
            throw new IllegalArgumentException("La franja no admite segundos ni nanosegundos: " + inicio);
        }
        int minuto = inicio.getMinute();
        if (minuto != 0 && minuto != 30) {
            throw new IllegalArgumentException("La franja debe iniciar en :00 o :30, no en :" + minuto);
        }
    }

    /** Crea la franja de 30 min que contiene/inicia en el instante dado. */
    public static FranjaHoraria de(LocalDateTime inicio) {
        return new FranjaHoraria(inicio);
    }

    /** Instante de fin (exclusivo) de la franja. */
    public LocalDateTime fin() {
        return inicio.plus(DURACION);
    }

    /** Hora de inicio (sin la parte de fecha). */
    public LocalTime horaInicio() {
        return inicio.toLocalTime();
    }
}
