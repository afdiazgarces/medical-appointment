package com.medical.appointment.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades configurables del módulo de citas médicas.
 * <p>
 * Centraliza los parámetros de negocio que deben ser ajustables sin recompilar:
 * <ul>
 *   <li>{@code appointment.duracion-minutos}: duración de cada franja (por defecto 30 min).</li>
 *   <li>{@code appointment.max-citas-por-paciente-por-dia}: límite diario de citas por paciente.</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "appointment")
public record AppointmentProperties(int duracionMinutos, int maxCitasPorPacientePorDia) {

    public AppointmentProperties {
        if (duracionMinutos <= 0 || duracionMinutos > 60) {
            throw new IllegalArgumentException(
                    "appointment.duracion-minutos debe estar entre 1 y 60; valor: " + duracionMinutos);
        }
        if (maxCitasPorPacientePorDia <= 0) {
            throw new IllegalArgumentException(
                    "appointment.max-citas-por-paciente-por-dia debe ser positivo; valor: " + maxCitasPorPacientePorDia);
        }
    }
}