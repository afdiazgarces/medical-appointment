package com.medical.appointment.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FranjaHoraria (RN-01: franjas de 30 min)")
class FranjaHorariaTest {

    @Test
    @DisplayName("calcula el fin sumando 30 minutos al inicio")
    void calculaFin() {
        FranjaHoraria franja = FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 0));
        assertThat(franja.fin()).isEqualTo(LocalDateTime.of(2026, 6, 22, 9, 30));
    }

    @Test
    @DisplayName("acepta inicios alineados a :00 y :30")
    void aceptaAlineados() {
        assertThat(FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 0))).isNotNull();
        assertThat(FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 30))).isNotNull();
    }

    @Test
    @DisplayName("rechaza inicios no alineados a la media hora")
    void rechazaNoAlineados() {
        assertThatThrownBy(() -> FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 15)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rechaza inicios con segundos")
    void rechazaSegundos() {
        assertThatThrownBy(() -> FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 0, 30)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
