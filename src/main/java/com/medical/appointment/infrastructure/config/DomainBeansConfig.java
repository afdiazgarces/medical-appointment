package com.medical.appointment.infrastructure.config;

import com.medical.appointment.domain.policy.HorarioAtencion;
import com.medical.appointment.domain.policy.PoliticaPenalizacion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Expone como beans las piezas de dominio sin anotaciones de framework. Esto mantiene
 * el dominio puro y, a la vez, inyectable y testeable (el {@link Clock} permite fijar
 * el tiempo en pruebas de RN-05).
 */
@Configuration
public class DomainBeansConfig {

    @Bean
    public HorarioAtencion horarioAtencion() {
        return new HorarioAtencion();
    }

    @Bean
    public PoliticaPenalizacion politicaPenalizacion() {
        return new PoliticaPenalizacion();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
