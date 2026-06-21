package com.medical.appointment.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FranjaHoraria (RN-01: franjas con duración configurable)")
class FranjaHorariaTest {

    @Test
    @DisplayName("calcula el fin sumando 30 minutos al inicio (factory de un argumento)")
    void calculaFinConDefecto() {
        FranjaHoraria franja = FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 0));
        assertThat(franja.fin()).isEqualTo(LocalDateTime.of(2026, 6, 22, 9, 30));
    }

    @Test
    @DisplayName("calcula el fin con duración explícita de 15 minutos")
    void calculaFinConDuracion15() {
        FranjaHoraria franja = FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 0), Duration.ofMinutes(15));
        assertThat(franja.fin()).isEqualTo(LocalDateTime.of(2026, 6, 22, 9, 15));
    }

    @Test
    @DisplayName("acepta inicios alineados a :00 y :30 con duración de 30 min")
    void aceptaAlineadosDuracion30() {
        assertThat(FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 0))).isNotNull();
        assertThat(FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 30))).isNotNull();
    }

    @Test
    @DisplayName("acepta :00, :15, :30 y :45 con duración de 15 min")
    void aceptaAlineadosDuracion15() {
        Duration d15 = Duration.ofMinutes(15);
        assertThat(FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 0), d15)).isNotNull();
        assertThat(FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 15), d15)).isNotNull();
        assertThat(FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 30), d15)).isNotNull();
        assertThat(FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 45), d15)).isNotNull();
    }

    @Test
    @DisplayName("rechaza inicios no alineados a la duración (30 min: :15 es inválido)")
    void rechazaNoAlineados() {
        assertThatThrownBy(() -> FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 15)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rechaza :10 con duración de 15 min")
    void rechazaNoAlineadosDuracion15() {
        assertThatThrownBy(() -> FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 10), Duration.ofMinutes(15)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rechaza inicios con segundos")
    void rechazaSegundos() {
        assertThatThrownBy(() -> FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 0, 30)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rechaza duración no positiva")
    void rechazaDuracionCero() {
        assertThatThrownBy(() -> FranjaHoraria.de(LocalDateTime.of(2026, 6, 22, 9, 0), Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
