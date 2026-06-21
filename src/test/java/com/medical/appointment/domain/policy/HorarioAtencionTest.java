package com.medical.appointment.domain.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HorarioAtencion (RN-01: jornadas y franjas)")
class HorarioAtencionTest {

    private final HorarioAtencion horario = new HorarioAtencion(Duration.ofMinutes(30));

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
    @DisplayName("rechaza franjas no alineadas a la duración configurada (30 min)")
    void noAlineadas() {
        assertThat(horario.esInicioDeFranjaValido(LocalDateTime.of(2026, 6, 22, 9, 15))).isFalse();
    }

    @Test
    @DisplayName("genera 20 franjas entre semana, 10 el sábado y 0 el domingo con 30 min")
    void cantidadDeFranjas() {
        assertThat(horario.franjasDelDia(LocalDate.of(2026, 6, 22))).hasSize(20);
        assertThat(horario.franjasDelDia(LocalDate.of(2026, 6, 27))).hasSize(10);
        assertThat(horario.franjasDelDia(LocalDate.of(2026, 6, 28))).isEmpty();
    }

    @Test
    @DisplayName("con duración de 60 min genera 10 franjas entre semana y 5 el sábado")
    void cantidadDeFranjasConDuracion60min() {
        HorarioAtencion horario60 = new HorarioAtencion(Duration.ofMinutes(60));
        assertThat(horario60.franjasDelDia(LocalDate.of(2026, 6, 22))).hasSize(10);
        assertThat(horario60.franjasDelDia(LocalDate.of(2026, 6, 27))).hasSize(5);
    }

    @Test
    @DisplayName("con duración de 15 min rechaza inicio en :10 pero acepta :15")
    void alineacionConDuracion15min() {
        HorarioAtencion horario15 = new HorarioAtencion(Duration.ofMinutes(15));
        assertThat(horario15.esInicioDeFranjaValido(LocalDateTime.of(2026, 6, 22, 9, 15))).isTrue();
        assertThat(horario15.esInicioDeFranjaValido(LocalDateTime.of(2026, 6, 22, 9, 10))).isFalse();
    }
}
