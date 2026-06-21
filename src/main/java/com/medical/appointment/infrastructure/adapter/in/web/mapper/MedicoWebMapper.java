package com.medical.appointment.infrastructure.adapter.in.web.mapper;

import com.medical.appointment.application.port.in.command.RegistrarMedicoCommand;
import com.medical.appointment.domain.model.Medico;
import com.medical.appointment.infrastructure.adapter.in.web.dto.request.CrearMedicoRequest;
import com.medical.appointment.infrastructure.adapter.in.web.dto.response.MedicoResponse;
import org.springframework.stereotype.Component;

@Component
public class MedicoWebMapper {

    public RegistrarMedicoCommand aComando(CrearMedicoRequest request) {
        return new RegistrarMedicoCommand(
                request.nombreCompleto(),
                request.especialidad(),
                request.telefono(),
                request.email());
    }

    public MedicoResponse aRespuesta(Medico medico) {
        return new MedicoResponse(
                medico.getId(),
                medico.getNombreCompleto(),
                medico.getEspecialidad(),
                medico.getTelefono(),
                medico.getEmail());
    }
}
