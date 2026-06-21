package com.medical.appointment.infrastructure.adapter.in.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Petición de registro de paciente (RF-02). */
public record CrearPacienteRequest(

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El documento de identidad es obligatorio")
        @Size(min = 7, max = 50, message = "El documento debe tener entre 7 y 50 caracteres")
        String documentoIdentidad,

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
        String telefono,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 255)
        String email,

        // Opcional en el registro (RN-03: la edad se valida al agendar, no al registrar).
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fechaNacimiento) {
}
