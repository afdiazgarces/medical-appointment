package com.medical.appointment.infrastructure.config;

import com.medical.appointment.domain.policy.HorarioAtencion;
import com.medical.appointment.domain.policy.PoliticaAgendamiento;
import com.medical.appointment.domain.policy.PoliticaPenalizacion;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

/**
 * Expone como beans las piezas de dominio sin anotaciones de framework. Esto mantiene
 * el dominio puro y, a la vez, inyectable y testeable (el {@link Clock} permite fijar
 * el tiempo en pruebas de RN-05). Los parámetros de negocio provienen de
 * {@link AppointmentProperties} para evitar valores quemados.
 */
@Configuration
@EnableConfigurationProperties(AppointmentProperties.class)
public class DomainBeansConfig {

    @Bean
    public HorarioAtencion horarioAtencion(AppointmentProperties props) {
        return new HorarioAtencion(Duration.ofMinutes(props.duracionMinutos()));
    }

    @Bean
    public PoliticaPenalizacion politicaPenalizacion() {
        return new PoliticaPenalizacion();
    }

    @Bean
    public PoliticaAgendamiento politicaAgendamiento(AppointmentProperties props) {
        return new PoliticaAgendamiento(props.maxCitasPorPacientePorDia());
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
