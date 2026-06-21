package com.medical.appointment.application.service;

import com.medical.appointment.application.port.in.command.FiltroPenalizaciones;
import com.medical.appointment.application.port.out.PenalizacionRepositoryPort;
import com.medical.appointment.domain.model.Penalizacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PenalizacionService (RN-05: consulta de penalizaciones)")
class PenalizacionServiceTest {

    @Mock private PenalizacionRepositoryPort penalizacionRepository;

    private PenalizacionService service;

    private static final Long PACIENTE_ID = 1L;
    private static final Long CITA_ID = 10L;

    @BeforeEach
    void setUp() {
        service = new PenalizacionService(penalizacionRepository);
    }

    private Penalizacion penalizacion(Long id, Long pacienteId, Long citaId) {
        return Penalizacion.reconstituir(id, pacienteId, citaId,
                LocalDateTime.of(2026, 6, 20, 9, 30));
    }

    @Nested
    @DisplayName("Listado sin filtros")
    class SinFiltros {

        @Test
        @DisplayName("devuelve todas las penalizaciones cuando no hay filtros")
        void listarTodas() {
            List<Penalizacion> penalizaciones = List.of(
                    penalizacion(1L, PACIENTE_ID, CITA_ID),
                    penalizacion(2L, 2L, 20L));
            when(penalizacionRepository.listar(FiltroPenalizaciones.sinFiltros()))
                    .thenReturn(penalizaciones);

            List<Penalizacion> resultado = service.listar(FiltroPenalizaciones.sinFiltros());

            assertThat(resultado).hasSize(2);
            verify(penalizacionRepository).listar(FiltroPenalizaciones.sinFiltros());
        }

        @Test
        @DisplayName("devuelve lista vacía si no hay penalizaciones")
        void listarVacia() {
            when(penalizacionRepository.listar(FiltroPenalizaciones.sinFiltros())).thenReturn(List.of());

            assertThat(service.listar(FiltroPenalizaciones.sinFiltros())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Filtrado por pacienteId")
    class FiltroPaciente {

        @Test
        @DisplayName("delega el filtro de pacienteId al repositorio")
        void filtraPorPaciente() {
            FiltroPenalizaciones filtro = new FiltroPenalizaciones(PACIENTE_ID, null);
            List<Penalizacion> esperadas = List.of(penalizacion(1L, PACIENTE_ID, CITA_ID));
            when(penalizacionRepository.listar(filtro)).thenReturn(esperadas);

            List<Penalizacion> resultado = service.listar(filtro);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getPacienteId()).isEqualTo(PACIENTE_ID);
            verify(penalizacionRepository).listar(argThat(f ->
                    PACIENTE_ID.equals(f.pacienteId()) && f.citaId() == null));
        }

        @Test
        @DisplayName("devuelve vacío si el paciente no tiene penalizaciones")
        void sinPenalizacionesDePaciente() {
            FiltroPenalizaciones filtro = new FiltroPenalizaciones(99L, null);
            when(penalizacionRepository.listar(filtro)).thenReturn(List.of());

            assertThat(service.listar(filtro)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Filtrado por citaId")
    class FiltroCita {

        @Test
        @DisplayName("delega el filtro de citaId al repositorio")
        void filtraPorCita() {
            FiltroPenalizaciones filtro = new FiltroPenalizaciones(null, CITA_ID);
            List<Penalizacion> esperadas = List.of(penalizacion(1L, PACIENTE_ID, CITA_ID));
            when(penalizacionRepository.listar(filtro)).thenReturn(esperadas);

            List<Penalizacion> resultado = service.listar(filtro);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getCitaId()).isEqualTo(CITA_ID);
            verify(penalizacionRepository).listar(argThat(f ->
                    f.pacienteId() == null && CITA_ID.equals(f.citaId())));
        }
    }

    @Nested
    @DisplayName("Filtrado combinado")
    class FiltroCombinado {

        @Test
        @DisplayName("combina pacienteId y citaId en el filtro")
        void filtraCombinado() {
            FiltroPenalizaciones filtro = new FiltroPenalizaciones(PACIENTE_ID, CITA_ID);
            when(penalizacionRepository.listar(filtro))
                    .thenReturn(List.of(penalizacion(1L, PACIENTE_ID, CITA_ID)));

            List<Penalizacion> resultado = service.listar(filtro);

            assertThat(resultado).hasSize(1);
            verify(penalizacionRepository).listar(argThat(f ->
                    PACIENTE_ID.equals(f.pacienteId()) && CITA_ID.equals(f.citaId())));
        }
    }
}
