package com.medical.appointment.domain.policy;

import com.medical.appointment.domain.model.FranjaHoraria;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Política de horarios de atención de la clínica (RN-01).
 * <ul>
 *   <li>Lunes a Viernes: 08:00 - 18:00</li>
 *   <li>Sábados: 08:00 - 13:00</li>
 *   <li>Domingos: sin atención</li>
 * </ul>
 * Las citas solo pueden iniciar en franjas de 30 minutos completamente contenidas
 * dentro de la jornada. Clase de dominio pura (sin dependencias de framework) y
 * sin estado, por lo que es segura para reutilizar como singleton.
 */
public final class HorarioAtencion {

    public static final LocalTime APERTURA = LocalTime.of(8, 0);
    public static final LocalTime CIERRE_ENTRE_SEMANA = LocalTime.of(18, 0);
    public static final LocalTime CIERRE_SABADO = LocalTime.of(13, 0);

    /** Hora de cierre del día indicado, o vacío si la clínica no atiende ese día. */
    public Optional<LocalTime> horaCierre(DayOfWeek dia) {
        return switch (dia) {
            case SATURDAY -> Optional.of(CIERRE_SABADO);
            case SUNDAY -> Optional.empty();
            default -> Optional.of(CIERRE_ENTRE_SEMANA);
        };
    }

    /** {@code true} si la clínica atiende ese día de la semana. */
    public boolean esDiaLaborable(DayOfWeek dia) {
        return horaCierre(dia).isPresent();
    }

    /**
     * Indica si una franja puede iniciar en el instante dado: día laborable, alineada
     * a :00/:30, no antes de la apertura y terminando a más tardar al cierre.
     */
    public boolean esInicioDeFranjaValido(LocalDateTime instante) {
        Optional<LocalTime> cierre = horaCierre(instante.getDayOfWeek());
        if (cierre.isEmpty()) {
            return false;
        }
        FranjaHoraria franja;
        try {
            franja = FranjaHoraria.de(instante);
        } catch (IllegalArgumentException noAlineada) {
            return false;
        }
        LocalTime inicio = franja.horaInicio();
        return !inicio.isBefore(APERTURA)
                && !franja.fin().toLocalTime().isAfter(cierre.get());
    }

    /** Todas las franjas de 30 minutos de un día concreto (vacío si no es laborable). */
    public List<FranjaHoraria> franjasDelDia(LocalDate dia) {
        Optional<LocalTime> cierre = horaCierre(dia.getDayOfWeek());
        if (cierre.isEmpty()) {
            return List.of();
        }
        List<FranjaHoraria> franjas = new ArrayList<>();
        LocalDateTime inicio = dia.atTime(APERTURA);
        LocalDateTime ultimoFin = dia.atTime(cierre.get());
        while (!inicio.plus(FranjaHoraria.DURACION).isAfter(ultimoFin)) {
            franjas.add(FranjaHoraria.de(inicio));
            inicio = inicio.plus(FranjaHoraria.DURACION);
        }
        return franjas;
    }

    /**
     * Todas las franjas laborables dentro de un rango [desde, hasta] (ambos inclusive
     * a nivel de día). Recorre día por día aplicando la jornada de cada uno.
     */
    public List<FranjaHoraria> franjasEnRango(LocalDate desde, LocalDate hasta) {
        List<FranjaHoraria> franjas = new ArrayList<>();
        for (LocalDate dia = desde; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
            franjas.addAll(franjasDelDia(dia));
        }
        return franjas;
    }
}
