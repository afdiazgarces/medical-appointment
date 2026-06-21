package com.medical.appointment.infrastructure.adapter.in.web.dto.response;

import java.time.LocalDateTime;

/** Franja de 30 minutos disponible (RF-04). */
public record FranjaDisponibleResponse(
        LocalDateTime inicio,
        LocalDateTime fin) {
}
