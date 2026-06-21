package com.medical.appointment.infrastructure.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.appointment.application.port.in.MedicoUseCase;
import com.medical.appointment.domain.exception.RecursoNoEncontradoException;
import com.medical.appointment.domain.model.Medico;
import com.medical.appointment.infrastructure.adapter.in.web.dto.request.CrearMedicoRequest;
import com.medical.appointment.infrastructure.adapter.in.web.mapper.MedicoWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicoController.class)
@Import(MedicoWebMapper.class)
@DisplayName("MedicoController (RF-01: mapeo HTTP de médicos)")
class MedicoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private MedicoUseCase medicoUseCase;

    private Medico medicoExistente() {
        return Medico.reconstituir(1L, "Dra. García", "Cardiología", "3001234567", "garcia@hospital.com");
    }

    @Test
    @DisplayName("POST /api/medicos devuelve 201 y Location al registrar")
    void registrar201() throws Exception {
        when(medicoUseCase.registrar(any())).thenReturn(medicoExistente());
        CrearMedicoRequest request = new CrearMedicoRequest("Dra. García", "Cardiología", "3001234567", "garcia@hospital.com");

        mockMvc.perform(post("/api/medicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/medicos/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombreCompleto").value("Dra. García"))
                .andExpect(jsonPath("$.especialidad").value("Cardiología"));
    }

    @Test
    @DisplayName("POST /api/medicos devuelve 400 si faltan campos obligatorios")
    void registrar400() throws Exception {
        mockMvc.perform(post("/api/medicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/medicos devuelve 200 con la lista de médicos")
    void listar() throws Exception {
        when(medicoUseCase.listar()).thenReturn(List.of(medicoExistente()));

        mockMvc.perform(get("/api/medicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].especialidad").value("Cardiología"));
    }

    @Test
    @DisplayName("GET /api/medicos/{id} devuelve 200 si el médico existe")
    void obtenerOk() throws Exception {
        when(medicoUseCase.obtenerPorId(1L)).thenReturn(medicoExistente());

        mockMvc.perform(get("/api/medicos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombreCompleto").value("Dra. García"));
    }

    @Test
    @DisplayName("GET /api/medicos/{id} devuelve 404 si el médico no existe")
    void obtener404() throws Exception {
        when(medicoUseCase.obtenerPorId(eq(99L)))
                .thenThrow(new RecursoNoEncontradoException("Médico", 99L));

        mockMvc.perform(get("/api/medicos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
