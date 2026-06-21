package com.medical.appointment.application.service;

import com.medical.appointment.application.port.in.command.ReservarCitaCommand;
import com.medical.appointment.application.port.out.CitaRepositoryPort;
import com.medical.appointment.application.port.out.MedicoRepositoryPort;
import com.medical.appointment.application.port.out.PacienteRepositoryPort;
import com.medical.appointment.application.port.out.PenalizacionRepositoryPort;
import com.medical.appointment.domain.exception.ConflictoException;
import com.medical.appointment.domain.exception.RecursoNoEncontradoException;
import com.medical.appointment.domain.exception.ReglaNegocioException;
import com.medical.appointment.domain.model.Cita;
import com.medical.appointment.domain.model.EstadoCita;
import com.medical.appointment.domain.model.Paciente;
import com.medical.appointment.domain.policy.HorarioAtencion;
import com.medical.appointment.domain.policy.PoliticaAgendamiento;
import com.medical.appointment.domain.policy.PoliticaPenalizacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CitaService (RN-01 a RN-NEW-2)")
class CitaServiceTest {

    private static final ZoneId ZONA = ZoneOffset.UTC;
    // "Ahora" fijo: lunes 2026-06-22 08:00
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 6, 22, 8, 0);
    private static final Long MEDICO_ID = 1L;
    private static final Long OTRO_MEDICO_ID = 2L;
    private static final Long PACIENTE_ID = 1L;
    private static final int MAX_CITAS_POR_DIA = 3;

    @Mock private CitaRepositoryPort citaRepository;
    @Mock private MedicoRepositoryPort medicoRepository;
    @Mock private PacienteRepositoryPort pacienteRepository;
    @Mock private PenalizacionRepositoryPort penalizacionRepository;

    private CitaService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        service = new CitaService(
                citaRepository, medicoRepository, pacienteRepository, penalizacionRepository,
                new HorarioAtencion(Duration.ofMinutes(30)),
                new PoliticaPenalizacion(),
                new PoliticaAgendamiento(MAX_CITAS_POR_DIA),
                clock);

        // Stubs comunes (camino feliz); cada test sobrescribe lo necesario.
        when(medicoRepository.existePorId(MEDICO_ID)).thenReturn(true);
        when(medicoRepository.existePorId(OTRO_MEDICO_ID)).thenReturn(true);
        when(pacienteRepository.buscarPorId(PACIENTE_ID))
                .thenReturn(Optional.of(pacienteConNacimiento(LocalDate.of(1990, 1, 1))));
        when(penalizacionRepository.contarDePacienteDesde(eq(PACIENTE_ID), any())).thenReturn(0L);
        when(citaRepository.existeCitaProgramadaDeMedico(anyLong(), any())).thenReturn(false);
        when(citaRepository.existeCitaProgramadaDePacienteEnFranja(anyLong(), any())).thenReturn(false);
        when(citaRepository.contarCitasProgramadasDePacienteEnFecha(anyLong(), any(LocalDate.class))).thenReturn(0L);
        when(citaRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Paciente pacienteConNacimiento(LocalDate nacimiento) {
        return Paciente.reconstituir(PACIENTE_ID, "Juan Pérez", "1234567", "5551234", "juan@mail.com", nacimiento);
    }

    private ReservarCitaCommand comando(LocalDateTime fechaHora) {
        return new ReservarCitaCommand(PACIENTE_ID, MEDICO_ID, fechaHora);
    }

    private ReservarCitaCommand comandoConMedico(Long medicoId, LocalDateTime fechaHora) {
        return new ReservarCitaCommand(PACIENTE_ID, medicoId, fechaHora);
    }

    // martes 2026-06-23 09:00 → franja válida
    private final LocalDateTime franjaValida = LocalDateTime.of(2026, 6, 23, 9, 0);

    @Nested
    @DisplayName("RF-03 Reserva")
    class Reserva {

        @Test
        @DisplayName("reserva una cita válida en estado PROGRAMADA")
        void reservaOk() {
            Cita cita = service.reservar(comando(franjaValida));

            assertThat(cita.getEstado()).isEqualTo(EstadoCita.PROGRAMADA);
            assertThat(cita.getFechaHora()).isEqualTo(franjaValida);
            verify(citaRepository).guardar(any());
        }

        @Test
        @DisplayName("404 si el médico no existe")
        void medicoInexistente() {
            when(medicoRepository.existePorId(MEDICO_ID)).thenReturn(false);
            assertThatThrownBy(() -> service.reservar(comando(franjaValida)))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }

        @Test
        @DisplayName("404 si el paciente no existe")
        void pacienteInexistente() {
            when(pacienteRepository.buscarPorId(PACIENTE_ID)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.reservar(comando(franjaValida)))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }

        @Test
        @DisplayName("RN-01: 400 si la franja está fuera de la jornada (domingo)")
        void fueraDeHorario() {
            LocalDateTime domingo = LocalDateTime.of(2026, 6, 28, 9, 0);
            assertThatThrownBy(() -> service.reservar(comando(domingo)))
                    .isInstanceOf(ReglaNegocioException.class);
        }

        @Test
        @DisplayName("RN-01: 400 si la franja no está alineada a la duración configurada")
        void franjaNoAlineada() {
            assertThatThrownBy(() -> service.reservar(comando(LocalDateTime.of(2026, 6, 23, 9, 15))))
                    .isInstanceOf(ReglaNegocioException.class);
        }

        @Test
        @DisplayName("RN-03: 400 si la fecha de nacimiento es futura")
        void fechaNacimientoFutura() {
            when(pacienteRepository.buscarPorId(PACIENTE_ID))
                    .thenReturn(Optional.of(pacienteConNacimiento(LocalDate.of(2030, 1, 1))));
            assertThatThrownBy(() -> service.reservar(comando(franjaValida)))
                    .isInstanceOf(ReglaNegocioException.class);
        }

        @Test
        @DisplayName("RN-02: 409 si el médico ya tiene cita en la franja")
        void medicoOcupado() {
            when(citaRepository.existeCitaProgramadaDeMedico(MEDICO_ID, franjaValida)).thenReturn(true);
            assertThatThrownBy(() -> service.reservar(comando(franjaValida)))
                    .isInstanceOf(ConflictoException.class);
        }

        @Test
        @DisplayName("RN-05: 409 si el paciente acumula 3+ penalizaciones en 30 días")
        void pacienteBloqueado() {
            when(penalizacionRepository.contarDePacienteDesde(eq(PACIENTE_ID), any())).thenReturn(3L);
            assertThatThrownBy(() -> service.reservar(comando(franjaValida)))
                    .isInstanceOf(ConflictoException.class);
        }
    }

    @Nested
    @DisplayName("RN-NEW-1 Límite máximo de citas por día")
    class LimiteCitasPorDia {

        @Test
        @DisplayName("409 si el paciente ya tiene el máximo de citas en ese día")
        void pacienteAlcanzaLimiteDiario() {
            when(citaRepository.contarCitasProgramadasDePacienteEnFecha(eq(PACIENTE_ID), eq(franjaValida.toLocalDate())))
                    .thenReturn((long) MAX_CITAS_POR_DIA);

            assertThatThrownBy(() -> service.reservar(comando(franjaValida)))
                    .isInstanceOf(ConflictoException.class)
                    .hasMessageContaining(String.valueOf(MAX_CITAS_POR_DIA));
        }

        @Test
        @DisplayName("409 si el paciente supera el máximo de citas en ese día")
        void pacienteSuperaLimiteDiario() {
            when(citaRepository.contarCitasProgramadasDePacienteEnFecha(eq(PACIENTE_ID), eq(franjaValida.toLocalDate())))
                    .thenReturn((long) MAX_CITAS_POR_DIA + 1);

            assertThatThrownBy(() -> service.reservar(comando(franjaValida)))
                    .isInstanceOf(ConflictoException.class);
        }

        @Test
        @DisplayName("permite reservar cuando el paciente tiene citas en días diferentes")
        void reservaPermitidaEnOtroDia() {
            LocalDateTime otroDia = LocalDateTime.of(2026, 6, 24, 9, 0);
            when(citaRepository.contarCitasProgramadasDePacienteEnFecha(eq(PACIENTE_ID), eq(otroDia.toLocalDate())))
                    .thenReturn(0L);

            Cita cita = service.reservar(comando(otroDia));
            assertThat(cita.getEstado()).isEqualTo(EstadoCita.PROGRAMADA);
        }

        @Test
        @DisplayName("permite reservar cuando el paciente tiene citas pero sin alcanzar el límite")
        void reservaPermitidaBajoElLimite() {
            when(citaRepository.contarCitasProgramadasDePacienteEnFecha(eq(PACIENTE_ID), eq(franjaValida.toLocalDate())))
                    .thenReturn((long) MAX_CITAS_POR_DIA - 1);

            Cita cita = service.reservar(comando(franjaValida));
            assertThat(cita.getEstado()).isEqualTo(EstadoCita.PROGRAMADA);
        }
    }

    @Nested
    @DisplayName("RN-NEW-2 Conflicto horario del paciente con cualquier médico")
    class ConflictoHorarioPaciente {

        @Test
        @DisplayName("409 si el paciente ya tiene cita con otro médico en la misma franja")
        void pacienteYaTieneCitaConOtroMedico() {
            when(citaRepository.existeCitaProgramadaDePacienteEnFranja(PACIENTE_ID, franjaValida)).thenReturn(true);

            assertThatThrownBy(() -> service.reservar(comandoConMedico(OTRO_MEDICO_ID, franjaValida)))
                    .isInstanceOf(ConflictoException.class)
                    .hasMessageContaining(franjaValida.toString());
        }

        @Test
        @DisplayName("409 si el paciente ya tiene cita con el mismo médico en la misma franja")
        void pacienteYaTieneCitaConMismoMedico() {
            when(citaRepository.existeCitaProgramadaDePacienteEnFranja(PACIENTE_ID, franjaValida)).thenReturn(true);

            assertThatThrownBy(() -> service.reservar(comando(franjaValida)))
                    .isInstanceOf(ConflictoException.class);
        }

        @Test
        @DisplayName("permite reservar en una franja diferente aunque haya cita en otra hora")
        void reservaPermitidaEnFranjaDistinta() {
            LocalDateTime otraFranja = LocalDateTime.of(2026, 6, 23, 10, 0);
            when(citaRepository.existeCitaProgramadaDePacienteEnFranja(PACIENTE_ID, otraFranja)).thenReturn(false);

            Cita cita = service.reservar(new ReservarCitaCommand(PACIENTE_ID, MEDICO_ID, otraFranja));
            assertThat(cita.getEstado()).isEqualTo(EstadoCita.PROGRAMADA);
        }
    }

    @Nested
    @DisplayName("RF-05 Cancelación / RN-05 Penalización")
    class Cancelacion {

        @Test
        @DisplayName("404 si la cita no existe")
        void citaInexistente() {
            when(citaRepository.buscarPorId(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.cancelar(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }

        @Test
        @DisplayName("cancelación tardía (<2h) registra una penalización")
        void cancelacionTardiaPenaliza() {
            // cita hoy a las 09:00; ahora son las 08:00 → 1h de antelación
            Cita cita = Cita.reconstituir(10L, PACIENTE_ID, MEDICO_ID,
                    LocalDateTime.of(2026, 6, 22, 9, 0), EstadoCita.PROGRAMADA, null);
            when(citaRepository.buscarPorId(10L)).thenReturn(Optional.of(cita));

            Cita resultado = service.cancelar(10L);

            assertThat(resultado.getEstado()).isEqualTo(EstadoCita.CANCELADA);
            verify(penalizacionRepository, times(1)).guardar(any());
        }

        @Test
        @DisplayName("cancelación con antelación suficiente NO penaliza")
        void cancelacionConAntelacionNoPenaliza() {
            Cita cita = Cita.reconstituir(11L, PACIENTE_ID, MEDICO_ID,
                    LocalDateTime.of(2026, 6, 25, 9, 0), EstadoCita.PROGRAMADA, null);
            when(citaRepository.buscarPorId(11L)).thenReturn(Optional.of(cita));

            service.cancelar(11L);

            verify(penalizacionRepository, never()).guardar(any());
        }
    }

    @Nested
    @DisplayName("RN-06 Reprogramación")
    class Reprogramacion {

        @Test
        @DisplayName("cancela la cita original y crea una nueva en el nuevo horario")
        void reprogramaOk() {
            Cita original = Cita.reconstituir(20L, PACIENTE_ID, MEDICO_ID,
                    LocalDateTime.of(2026, 6, 25, 9, 0), EstadoCita.PROGRAMADA, null);
            when(citaRepository.buscarPorId(20L)).thenReturn(Optional.of(original));

            LocalDateTime nuevoHorario = LocalDateTime.of(2026, 6, 26, 10, 0); // viernes válido
            Cita nueva = service.reprogramar(20L, nuevoHorario);

            assertThat(original.getEstado()).isEqualTo(EstadoCita.CANCELADA);
            assertThat(nueva.getEstado()).isEqualTo(EstadoCita.PROGRAMADA);
            assertThat(nueva.getFechaHora()).isEqualTo(nuevoHorario);
            // se guarda dos veces: la cancelación de la original y la nueva cita
            verify(citaRepository, times(2)).guardar(any());
        }

        @Test
        @DisplayName("RN-02: si el nuevo horario está ocupado para el médico, falla con 409")
        void reprogramaConConflictoMedico() {
            Cita original = Cita.reconstituir(21L, PACIENTE_ID, MEDICO_ID,
                    LocalDateTime.of(2026, 6, 25, 9, 0), EstadoCita.PROGRAMADA, null);
            when(citaRepository.buscarPorId(21L)).thenReturn(Optional.of(original));
            LocalDateTime nuevoHorario = LocalDateTime.of(2026, 6, 26, 10, 0);
            when(citaRepository.existeCitaProgramadaDeMedico(MEDICO_ID, nuevoHorario)).thenReturn(true);

            assertThatThrownBy(() -> service.reprogramar(21L, nuevoHorario))
                    .isInstanceOf(ConflictoException.class);
        }
    }

    @Nested
    @DisplayName("RF-04 Disponibilidad")
    class Disponibilidad {

        @Test
        @DisplayName("excluye las franjas ya ocupadas por citas programadas")
        void excluyeOcupadas() {
            LocalDateTime inicio = LocalDateTime.of(2026, 6, 23, 8, 0);
            LocalDateTime fin = LocalDateTime.of(2026, 6, 23, 18, 0);
            Cita ocupada = Cita.reconstituir(30L, PACIENTE_ID, MEDICO_ID,
                    LocalDateTime.of(2026, 6, 23, 9, 0), EstadoCita.PROGRAMADA, null);
            when(citaRepository.buscarProgramadasDeMedicoEntre(MEDICO_ID, inicio, fin))
                    .thenReturn(java.util.List.of(ocupada));

            var franjas = service.consultarDisponibilidad(MEDICO_ID, inicio, fin);

            assertThat(franjas).isNotEmpty();
            assertThat(franjas).noneMatch(f -> f.inicio().equals(LocalDateTime.of(2026, 6, 23, 9, 0)));
        }

        @Test
        @DisplayName("400 si el rango de fechas es inválido")
        void rangoInvalido() {
            LocalDateTime inicio = LocalDateTime.of(2026, 6, 23, 18, 0);
            LocalDateTime fin = LocalDateTime.of(2026, 6, 23, 8, 0);
            assertThatThrownBy(() -> service.consultarDisponibilidad(MEDICO_ID, inicio, fin))
                    .isInstanceOf(ReglaNegocioException.class);
        }
    }
}
