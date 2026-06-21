package com.medical.appointment.infrastructure.adapter.in.web.mapper;

import com.medical.appointment.application.port.in.command.RegistrarPacienteCommand;
import com.medical.appointment.domain.model.Paciente;
import com.medical.appointment.infrastructure.adapter.in.web.dto.request.CrearPacienteRequest;
import com.medical.appointment.infrastructure.adapter.in.web.dto.response.PacienteResponse;
import org.springframework.stereotype.Component;

@Component
public class PacienteWebMapper {

    public RegistrarPacienteCommand aComando(CrearPacienteRequest request) {
        return new RegistrarPacienteCommand(
                request.nombreCompleto(),
                request.documentoIdentidad(),
                request.telefono(),
                request.email(),
                request.fechaNacimiento());
    }

    public PacienteResponse aRespuesta(Paciente paciente) {
        return new PacienteResponse(
                paciente.getId(),
                paciente.getNombreCompleto(),
                paciente.getDocumentoIdentidad(),
                paciente.getTelefono(),
                paciente.getEmail(),
                paciente.getFechaNacimiento());
    }
}
