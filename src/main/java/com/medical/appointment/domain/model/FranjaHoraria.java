package com.medical.appointment.domain.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Value Object que representa una franja de atención (RN-01 / RF-04).
 * <p>
 * Es inmutable y se identifica por su instante de inicio y su duración. El inicio
 * debe estar alineado al múltiplo de {@code duracion} dentro de la hora
 * (p. ej. :00 y :30 para 30 min), sin segundos ni nanosegundos.
 * <p>
 * La constante {@link #DURACION} conserva el valor original de 30 minutos como
 * referencia para el factory de un solo argumento. La duración real de cada instancia
 * proviene del campo {@code duracion}.
 */
public record FranjaHoraria(LocalDateTime inicio, Duration duracion) {

    /** Duración por defecto para compatibilidad con código existente. */
    public static final Duration DURACION = Duration.ofMinutes(30);

    public FranjaHoraria {
        Objects.requireNonNull(inicio, "El inicio de la franja es obligatorio");
        Objects.requireNonNull(duracion, "La duración de la franja es obligatoria");
        if (inicio.getSecond() != 0 || inicio.getNano() != 0) {
            throw new IllegalArgumentException("La franja no admite segundos ni nanosegundos: " + inicio);
        }
        long duracionMinutos = duracion.toMinutes();
        if (duracionMinutos <= 0) {
            throw new IllegalArgumentException("La duración de la franja debe ser positiva: " + duracion);
        }
        int minuto = inicio.getMinute();
        if (minuto % duracionMinutos != 0) {
            throw new IllegalArgumentException(
                    "La franja debe iniciar alineada a " + duracionMinutos + " min, no en :" + minuto);
        }
    }

    /**
     * Crea una franja con la duración por defecto de 30 minutos.
     * Mantiene compatibilidad con el código existente.
     */
    public static FranjaHoraria de(LocalDateTime inicio) {
        return new FranjaHoraria(inicio, DURACION);
    }

    /** Crea una franja con la duración configurada. */
    public static FranjaHoraria de(LocalDateTime inicio, Duration duracion) {
        return new FranjaHoraria(inicio, duracion);
    }

    /** Instante de fin (exclusivo) de la franja. */
    public LocalDateTime fin() {
        return inicio.plus(duracion);
    }

    /** Hora de inicio (sin la parte de fecha). */
    public LocalTime horaInicio() {
        return inicio.toLocalTime();
    }
}
