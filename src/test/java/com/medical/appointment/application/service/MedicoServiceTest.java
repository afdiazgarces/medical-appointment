package com.medical.appointment.application.service;

import com.medical.appointment.application.port.in.command.RegistrarMedicoCommand;
import com.medical.appointment.application.port.out.MedicoRepositoryPort;
import com.medical.appointment.domain.exception.RecursoNoEncontradoException;
import com.medical.appointment.domain.exception.ReglaNegocioException;
import com.medical.appointment.domain.model.Medico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicoService (RF-01)")
class MedicoServiceTest {

    @Mock private MedicoRepositoryPort medicoRepository;

    private MedicoService service;

    @BeforeEach
    void setUp() {
        service = new MedicoService(medicoRepository);
    }

    private RegistrarMedicoCommand comando() {
        return new RegistrarMedicoCommand("Dra. García", "Cardiología", "3001234567", "garcia@hospital.com");
    }

    private Medico medicoExistente() {
        return Medico.reconstituir(1L, "Dra. García", "Cardiología", "3001234567", "garcia@hospital.com");
    }

    @Nested
    @DisplayName("RF-01 Registro")
    class Registro {

        @Test
        @DisplayName("registra un médico y lo persiste")
        void registraOk() {
            when(medicoRepository.guardar(any())).thenAnswer(inv -> {
                Medico m = inv.getArgument(0);
                return Medico.reconstituir(1L, m.getNombreCompleto(), m.getEspecialidad(),
                        m.getTelefono(), m.getEmail());
            });

            Medico resultado = service.registrar(comando());

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombreCompleto()).isEqualTo("Dra. García");
            assertThat(resultado.getEspecialidad()).isEqualTo("Cardiología");
            verify(medicoRepository).guardar(any());
        }

        @Test
        @DisplayName("400 si el nombre es demasiado corto")
        void nombreCorto() {
            RegistrarMedicoCommand cmd = new RegistrarMedicoCommand("AB", "Cardiología", null, null);
            assertThatThrownBy(() -> service.registrar(cmd))
                    .isInstanceOf(ReglaNegocioException.class);
        }

        @Test
        @DisplayName("400 si la especialidad está en blanco")
        void especialidadBlanco() {
            RegistrarMedicoCommand cmd = new RegistrarMedicoCommand("Dr. López", "  ", null, null);
            assertThatThrownBy(() -> service.registrar(cmd))
                    .isInstanceOf(ReglaNegocioException.class);
        }
    }

    @Nested
    @DisplayName("Listado y búsqueda")
    class Consulta {

        @Test
        @DisplayName("listar devuelve todos los médicos")
        void listar() {
            List<Medico> medicos = List.of(medicoExistente());
            when(medicoRepository.listar()).thenReturn(medicos);

            assertThat(service.listar()).hasSize(1);
            verify(medicoRepository).listar();
        }

        @Test
        @DisplayName("obtenerPorId devuelve el médico cuando existe")
        void obtenerPorIdOk() {
            when(medicoRepository.buscarPorId(1L)).thenReturn(Optional.of(medicoExistente()));

            Medico resultado = service.obtenerPorId(1L);

            assertThat(resultado.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("404 si el médico no existe")
        void obtenerPorIdNoEncontrado() {
            when(medicoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.obtenerPorId(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }
}
