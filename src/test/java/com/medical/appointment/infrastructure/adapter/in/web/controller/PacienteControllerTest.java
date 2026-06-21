package com.medical.appointment.infrastructure.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medical.appointment.application.port.in.PacienteUseCase;
import com.medical.appointment.domain.exception.ConflictoException;
import com.medical.appointment.domain.exception.RecursoNoEncontradoException;
import com.medical.appointment.domain.model.Paciente;
import com.medical.appointment.infrastructure.adapter.in.web.dto.request.CrearPacienteRequest;
import com.medical.appointment.infrastructure.adapter.in.web.mapper.PacienteWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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

@WebMvcTest(PacienteController.class)
@Import(PacienteWebMapper.class)
@DisplayName("PacienteController (RF-02: mapeo HTTP de pacientes)")
class PacienteControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private PacienteUseCase pacienteUseCase;

    private Paciente pacienteExistente() {
        return Paciente.reconstituir(1L, "Ana López", "9876543", "5559999", "ana@mail.com",
                LocalDate.of(1995, 5, 5));
    }

    private CrearPacienteRequest requestValido() {
        return new CrearPacienteRequest("Ana López", "9876543", "5559999", "ana@mail.com",
                LocalDate.of(1995, 5, 5));
    }

    @Test
    @DisplayName("POST /api/pacientes devuelve 201 y Location al registrar")
    void registrar201() throws Exception {
        when(pacienteUseCase.registrar(any())).thenReturn(pacienteExistente());

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/pacientes/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombreCompleto").value("Ana López"))
                .andExpect(jsonPath("$.documentoIdentidad").value("9876543"));
    }

    @Test
    @DisplayName("POST /api/pacientes devuelve 400 si faltan campos obligatorios")
    void registrar400() throws Exception {
        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/pacientes devuelve 409 si el documento ya existe")
    void registrar409() throws Exception {
        when(pacienteUseCase.registrar(any()))
                .thenThrow(new ConflictoException("Ya existe un paciente con ese documento"));

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/pacientes devuelve 200 con la lista de pacientes")
    void listar() throws Exception {
        when(pacienteUseCase.listar()).thenReturn(List.of(pacienteExistente()));

        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].documentoIdentidad").value("9876543"));
    }

    @Test
    @DisplayName("GET /api/pacientes/{id} devuelve 200 si el paciente existe")
    void obtenerOk() throws Exception {
        when(pacienteUseCase.obtenerPorId(1L)).thenReturn(pacienteExistente());

        mockMvc.perform(get("/api/pacientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ana@mail.com"));
    }

    @Test
    @DisplayName("GET /api/pacientes/{id} devuelve 404 si el paciente no existe")
    void obtener404() throws Exception {
        when(pacienteUseCase.obtenerPorId(eq(99L)))
                .thenThrow(new RecursoNoEncontradoException("Paciente", 99L));

        mockMvc.perform(get("/api/pacientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
