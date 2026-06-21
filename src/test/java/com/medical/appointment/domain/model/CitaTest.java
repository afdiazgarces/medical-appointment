package com.medical.appointment.domain.model;

import com.medical.appointment.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cita (RF-05 cancelación / RN-05 cancelación tardía)")
class CitaTest {

    private final LocalDateTime fechaHora = LocalDateTime.of(2026, 6, 23, 9, 0);

    private Cita citaProgramada() {
        return Cita.reconstituir(10L, 1L, 1L, fechaHora, EstadoCita.PROGRAMADA, null);
    }

    @Test
    @DisplayName("programar deja la cita en estado PROGRAMADA")
    void programar() {
        Cita cita = Cita.programar(1L, 1L, fechaHora);
        assertThat(cita.getEstado()).isEqualTo(EstadoCita.PROGRAMADA);
        assertThat(cita.getFechaCancelacion()).isNull();
    }

    @Test
    @DisplayName("cancelar cambia el estado y registra la fecha de cancelación")
    void cancelar() {
        Cita cita = citaProgramada();
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 22, 10, 0);

        cita.cancelar(ahora);

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.CANCELADA);
        assertThat(cita.getFechaCancelacion()).isEqualTo(ahora);
    }

    @Test
    @DisplayName("no se puede cancelar una cita que ya no está PROGRAMADA")
    void noCancelaDosVeces() {
        Cita cita = citaProgramada();
        cita.cancelar(LocalDateTime.of(2026, 6, 22, 10, 0));

        assertThatThrownBy(() -> cita.cancelar(LocalDateTime.of(2026, 6, 22, 11, 0)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("es cancelación tardía si ocurre con menos de 2 horas de antelación")
    void cancelacionTardia() {
        Cita cita = citaProgramada(); // fechaHora 09:00
        assertThat(cita.esCancelacionTardia(LocalDateTime.of(2026, 6, 23, 8, 0))).isTrue();  // 1h antes
        assertThat(cita.esCancelacionTardia(LocalDateTime.of(2026, 6, 23, 6, 0))).isFalse(); // 3h antes
    }
}
