package com.medical.appointment.infrastructure.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.appointment.application.port.in.CitaUseCase;
import com.medical.appointment.domain.exception.ConflictoException;
import com.medical.appointment.domain.exception.RecursoNoEncontradoException;
import com.medical.appointment.domain.model.Cita;
import com.medical.appointment.domain.model.EstadoCita;
import com.medical.appointment.infrastructure.adapter.in.web.dto.request.ReservarCitaRequest;
import com.medical.appointment.infrastructure.adapter.in.web.mapper.CitaWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CitaController.class)
@Import(CitaWebMapper.class)
@DisplayName("CitaController (mapeo HTTP y manejo de errores)")
class CitaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CitaUseCase citaUseCase;

    private final LocalDateTime franja = LocalDateTime.of(2026, 6, 23, 9, 0);

    @Test
    @DisplayName("POST /api/citas devuelve 201 al reservar")
    void reservar201() throws Exception {
        Cita cita = Cita.reconstituir(1L, 1L, 1L, franja, EstadoCita.PROGRAMADA, null);
        when(citaUseCase.reservar(any())).thenReturn(cita);

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReservarCitaRequest(1L, 1L, franja))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"));
    }

    @Test
    @DisplayName("POST /api/citas devuelve 400 si faltan campos obligatorios")
    void reservar400() throws Exception {
        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/citas devuelve 409 ante conflicto de negocio")
    void reservar409() throws Exception {
        when(citaUseCase.reservar(any())).thenThrow(new ConflictoException("franja ocupada"));

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReservarCitaRequest(1L, 1L, franja))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/citas/{id}/cancelacion devuelve 404 si la cita no existe")
    void cancelar404() throws Exception {
        when(citaUseCase.cancelar(eq(99L))).thenThrow(new RecursoNoEncontradoException("Cita", 99L));

        mockMvc.perform(post("/api/citas/99/cancelacion"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
