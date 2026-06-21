package com.medical.appointment.infrastructure.adapter.in.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Petición de registro de médico (RF-01). */
public record CrearMedicoRequest(

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombreCompleto,

        @NotBlank(message = "La especialidad es obligatoria")
        @Size(max = 100, message = "La especialidad admite máximo 100 caracteres")
        String especialidad,

        @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
        String telefono,

        @Email(message = "El email no tiene un formato válido")
        @Size(max = 255)
        String email) {
}
