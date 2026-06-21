package com.medical.appointment.domain.model;

import com.medical.appointment.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Paciente (RN-03: edad)")
class PacienteTest {

    private Paciente conNacimiento(LocalDate nacimiento) {
        return Paciente.reconstituir(1L, "Juan Pérez", "1234567", "5551234", "juan@mail.com", nacimiento);
    }

    @Test
    @DisplayName("sin fecha de nacimiento asume edad 0")
    void edadCeroSinFecha() {
        assertThat(conNacimiento(null).edadEn(LocalDate.of(2026, 6, 22))).isZero();
    }

    @Test
    @DisplayName("calcula la edad en años cumplidos")
    void calculaEdad() {
        assertThat(conNacimiento(LocalDate.of(2000, 6, 22)).edadEn(LocalDate.of(2026, 6, 22))).isEqualTo(26);
    }

    @Test
    @DisplayName("rechaza fecha de nacimiento futura al agendar")
    void rechazaFechaFutura() {
        Paciente paciente = conNacimiento(LocalDate.of(2030, 1, 1));
        assertThatThrownBy(() -> paciente.validarAptoParaAgendar(LocalDate.of(2026, 6, 22)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("permite agendar con fecha de nacimiento válida o nula")
    void permiteAgendar() {
        assertThatCode(() -> conNacimiento(LocalDate.of(1990, 1, 1)).validarAptoParaAgendar(LocalDate.of(2026, 6, 22)))
                .doesNotThrowAnyException();
        assertThatCode(() -> conNacimiento(null).validarAptoParaAgendar(LocalDate.of(2026, 6, 22)))
                .doesNotThrowAnyException();
    }
}
