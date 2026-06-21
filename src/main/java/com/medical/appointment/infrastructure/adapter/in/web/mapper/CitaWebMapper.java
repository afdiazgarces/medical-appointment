package com.medical.appointment.infrastructure.adapter.in.web.mapper;

import com.medical.appointment.application.port.in.command.ReservarCitaCommand;
import com.medical.appointment.domain.model.Cita;
import com.medical.appointment.domain.model.FranjaHoraria;
import com.medical.appointment.infrastructure.adapter.in.web.dto.request.ReservarCitaRequest;
import com.medical.appointment.infrastructure.adapter.in.web.dto.response.CitaResponse;
import com.medical.appointment.infrastructure.adapter.in.web.dto.response.FranjaDisponibleResponse;
import org.springframework.stereotype.Component;

@Component
public class CitaWebMapper {

    public ReservarCitaCommand aComando(ReservarCitaRequest request) {
        return new ReservarCitaCommand(
                request.pacienteId(),
                request.medicoId(),
                request.fechaHora());
    }

    public CitaResponse aRespuesta(Cita cita) {
        return new CitaResponse(
                cita.getId(),
                cita.getPacienteId(),
                cita.getMedicoId(),
                cita.getFechaHora(),
                cita.getEstado(),
                cita.getFechaCancelacion());
    }

    public FranjaDisponibleResponse aRespuesta(FranjaHoraria franja) {
        return new FranjaDisponibleResponse(franja.inicio(), franja.fin());
    }
}
