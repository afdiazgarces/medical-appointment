package com.medical.appointment.infrastructure.adapter.in.web.mapper;

import com.medical.appointment.domain.model.Penalizacion;
import com.medical.appointment.infrastructure.adapter.in.web.dto.response.PenalizacionResponse;
import org.springframework.stereotype.Component;

@Component
public class PenalizacionWebMapper {

    public PenalizacionResponse aRespuesta(Penalizacion penalizacion) {
        return new PenalizacionResponse(
                penalizacion.getId(),
                penalizacion.getPacienteId(),
                penalizacion.getCitaId(),
                penalizacion.getFechaPenalizacion());
    }
}
