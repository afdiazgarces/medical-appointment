package com.medical.appointment.infrastructure.adapter.in.web.controller;

import com.medical.appointment.application.port.in.PenalizacionUseCase;
import com.medical.appointment.application.port.in.command.FiltroPenalizaciones;
import com.medical.appointment.infrastructure.adapter.in.web.dto.response.PenalizacionResponse;
import com.medical.appointment.infrastructure.adapter.in.web.mapper.PenalizacionWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API REST de penalizaciones (RN-05). */
@RestController
@RequestMapping("/api/penalizaciones")
@Tag(name = "Penalizaciones", description = "Consulta de penalizaciones por cancelación tardía (RN-05)")
public class PenalizacionController {

    private final PenalizacionUseCase penalizacionUseCase;
    private final PenalizacionWebMapper mapper;

    public PenalizacionController(PenalizacionUseCase penalizacionUseCase, PenalizacionWebMapper mapper) {
        this.penalizacionUseCase = penalizacionUseCase;
        this.mapper = mapper;
    }

    /**
     * Lista todas las penalizaciones con filtros opcionales.
     * Si no se proporcionan filtros, devuelve todas las penalizaciones del sistema.
     */
    @GetMapping
    @Operation(
            summary = "Listar penalizaciones",
            description = "Devuelve las penalizaciones registradas por cancelación tardía. "
                    + "Se puede filtrar por paciente, por cita, o por ambos. "
                    + "Sin filtros devuelve todas las penalizaciones.",
            parameters = {
                    @Parameter(name = "pacienteId", description = "ID del paciente (opcional)"),
                    @Parameter(name = "citaId",     description = "ID de la cita que originó la penalización (opcional)")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de penalizaciones",
                            content = @Content(schema = @Schema(implementation = PenalizacionResponse.class)))
            }
    )
    public ResponseEntity<List<PenalizacionResponse>> listar(
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) Long citaId) {

        FiltroPenalizaciones filtro = new FiltroPenalizaciones(pacienteId, citaId);
        List<PenalizacionResponse> respuesta = penalizacionUseCase.listar(filtro)
                .stream().map(mapper::aRespuesta).toList();
        return ResponseEntity.ok(respuesta);
    }
}
