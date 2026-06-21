package com.medical.appointment.application.service;

import com.medical.appointment.application.port.in.command.RegistrarPacienteCommand;
import com.medical.appointment.application.port.out.PacienteRepositoryPort;
import com.medical.appointment.domain.exception.ConflictoException;
import com.medical.appointment.domain.model.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PacienteService (RF-02)")
class PacienteServiceTest {

    @Mock private PacienteRepositoryPort pacienteRepository;
    private PacienteService service;

    @BeforeEach
    void setUp() {
        service = new PacienteService(pacienteRepository);
    }

    private RegistrarPacienteCommand comando() {
        return new RegistrarPacienteCommand("Ana López", "9876543", "5559999", "ana@mail.com",
                LocalDate.of(1995, 5, 5));
    }

    @Test
    @DisplayName("registra un paciente cuando el documento no existe")
    void registraOk() {
        when(pacienteRepository.existePorDocumento("9876543")).thenReturn(false);
        when(pacienteRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Paciente paciente = service.registrar(comando());

        assertThat(paciente.getDocumentoIdentidad()).isEqualTo("9876543");
        verify(pacienteRepository).guardar(any());
    }

    @Test
    @DisplayName("409 si el documento de identidad ya existe")
    void documentoDuplicado() {
        when(pacienteRepository.existePorDocumento("9876543")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(comando()))
                .isInstanceOf(ConflictoException.class);
        verify(pacienteRepository, never()).guardar(any());
    }
}
