package com.medical.appointment.domain.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HorarioAtencion (RN-01: jornadas y franjas)")
class HorarioAtencionTest {

    private final HorarioAtencion horario = new HorarioAtencion();

    // 2026-06-22 = lunes, 2026-06-27 = sábado, 2026-06-28 = domingo

    @Test
    @DisplayName("entre semana acepta 08:00 y la última franja 17:30")
    void entreSemanaLimites() {
        assertThat(horario.esInicioDeFranjaValido(LocalDateTime.of(2026, 6, 22, 8, 0))).isTrue();
        assertThat(horario.esInicioDeFranjaValido(LocalDateTime.of(2026, 6, 22, 17, 30))).isTrue();
    }

    @Test
    @DisplayName("entre semana rechaza antes de abrir y la franja que excede el cierre")
    void entreSemanaFueraDeRango() {
        assertThat(horario.esInicioDeFranjaValido(LocalDateTime.of(2026, 6, 22, 7, 30))).isFalse();
        assertThat(horario.esInicioDeFranjaValido(LocalDateTime.of(2026, 6, 22, 18, 0))).isFalse();
    }

    @Test
    @DisplayName("sábado solo hasta las 13:00 (última franja 12:30)")
    void sabado() {
        assertThat(horario.esInicioDeFranjaValido(LocalDateTime.of(2026, 6, 27, 12, 30))).isTrue();
        assertThat(horario.esInicioDeFranjaValido(LocalDateTime.of(2026, 6, 27, 13, 0))).isFalse();
    }

    @Test
    @DisplayName("domingo no hay atención")
    void domingo() {
        assertThat(horario.esInicioDeFranjaValido(LocalDateTime.of(2026, 6, 28, 9, 0))).isFalse();
    }

    @Test
    @DisplayName("rechaza franjas no alineadas a 30 min")
    void noAlineadas() {
        assertThat(horario.esInicioDeFranjaValido(LocalDateTime.of(2026, 6, 22, 9, 15))).isFalse();
    }

    @Test
    @DisplayName("genera 20 franjas entre semana, 10 el sábado y 0 el domingo")
    void cantidadDeFranjas() {
        assertThat(horario.franjasDelDia(LocalDate.of(2026, 6, 22))).hasSize(20);
        assertThat(horario.franjasDelDia(LocalDate.of(2026, 6, 27))).hasSize(10);
        assertThat(horario.franjasDelDia(LocalDate.of(2026, 6, 28))).isEmpty();
    }
}
