package com.medical.appointment.domain.policy;

import java.time.Duration;

/**
 * Reglas de penalización por cancelación tardía (RN-05).
 * <ul>
 *   <li>Cancelar con menos de 2 horas de antelación genera una penalización.</li>
 *   <li>Un paciente con 3 o más penalizaciones en los últimos 30 días no puede
 *       agendar nuevas citas.</li>
 * </ul>
 * Centraliza los umbrales numéricos de la regla para evitar "números mágicos"
 * dispersos por la aplicación. Clase de dominio pura y sin estado.
 */
public final class PoliticaPenalizacion {

    /** Antelación mínima para que una cancelación NO sea penalizada. */
    public static final Duration ANTELACION_MINIMA = Duration.ofHours(2);

    /** Número de penalizaciones que bloquea al paciente. */
    public static final int MAX_PENALIZACIONES = 3;

    /** Ventana móvil (en días) sobre la que se cuentan las penalizaciones. */
    public static final int VENTANA_DIAS = 30;

    /** {@code true} si la cantidad de penalizaciones recientes bloquea el agendamiento. */
    public boolean superaLimite(long penalizacionesEnVentana) {
        return penalizacionesEnVentana >= MAX_PENALIZACIONES;
    }
}
