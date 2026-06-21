package com.medical.appointment.infrastructure.adapter.in.web.controller;

import com.medical.appointment.application.port.in.PenalizacionUseCase;
import com.medical.appointment.application.port.in.command.FiltroPenalizaciones;
import com.medical.appointment.domain.model.Penalizacion;
import com.medical.appointment.infrastructure.adapter.in.web.mapper.PenalizacionWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PenalizacionController.class)
@Import(PenalizacionWebMapper.class)
@DisplayName("PenalizacionController (RN-05: mapeo HTTP de penalizaciones)")
class PenalizacionControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private PenalizacionUseCase penalizacionUseCase;

    private Penalizacion penalizacion(Long id, Long pacienteId, Long citaId) {
        return Penalizacion.reconstituir(id, pacienteId, citaId,
                LocalDateTime.of(2026, 6, 20, 9, 30));
    }

    @Test
    @DisplayName("GET /api/penalizaciones devuelve 200 con todas las penalizaciones")
    void listarTodas() throws Exception {
        when(penalizacionUseCase.listar(FiltroPenalizaciones.sinFiltros()))
                .thenReturn(List.of(penalizacion(1L, 1L, 10L), penalizacion(2L, 2L, 20L)));

        mockMvc.perform(get("/api/penalizaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].pacienteId").value(1))
                .andExpect(jsonPath("$[0].citaId").value(10));
    }

    @Test
    @DisplayName("GET /api/penalizaciones devuelve 200 con lista vacía si no hay penalizaciones")
    void listarVacia() throws Exception {
        when(penalizacionUseCase.listar(FiltroPenalizaciones.sinFiltros())).thenReturn(List.of());

        mockMvc.perform(get("/api/penalizaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/penalizaciones?pacienteId=1 filtra por paciente")
    void filtrarPorPaciente() throws Exception {
        when(penalizacionUseCase.listar(argThat(f -> Long.valueOf(1L).equals(f.pacienteId()) && f.citaId() == null)))
                .thenReturn(List.of(penalizacion(1L, 1L, 10L)));

        mockMvc.perform(get("/api/penalizaciones").param("pacienteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].pacienteId").value(1));
    }

    @Test
    @DisplayName("GET /api/penalizaciones?citaId=10 filtra por cita")
    void filtrarPorCita() throws Exception {
        when(penalizacionUseCase.listar(argThat(f -> f.pacienteId() == null && Long.valueOf(10L).equals(f.citaId()))))
                .thenReturn(List.of(penalizacion(1L, 1L, 10L)));

        mockMvc.perform(get("/api/penalizaciones").param("citaId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].citaId").value(10));
    }

    @Test
    @DisplayName("GET /api/penalizaciones?pacienteId=1&citaId=10 filtra por ambos")
    void filtrarCombinado() throws Exception {
        when(penalizacionUseCase.listar(
                argThat(f -> Long.valueOf(1L).equals(f.pacienteId()) && Long.valueOf(10L).equals(f.citaId()))))
                .thenReturn(List.of(penalizacion(1L, 1L, 10L)));

        mockMvc.perform(get("/api/penalizaciones").param("pacienteId", "1").param("citaId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].pacienteId").value(1))
                .andExpect(jsonPath("$[0].citaId").value(10));
    }

    @Test
    @DisplayName("GET /api/penalizaciones?pacienteId=abc devuelve 400 si el parámetro es inválido")
    void parametroInvalido() throws Exception {
        mockMvc.perform(get("/api/penalizaciones").param("pacienteId", "abc"))
                .andExpect(status().isBadRequest());
    }
}
