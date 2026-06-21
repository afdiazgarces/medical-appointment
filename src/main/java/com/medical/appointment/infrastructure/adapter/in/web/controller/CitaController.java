package com.medical.appointment.infrastructure.adapter.in.web.controller;

import com.medical.appointment.application.port.in.CitaUseCase;
import com.medical.appointment.application.port.in.command.FiltroCitas;
import com.medical.appointment.domain.model.Cita;
import com.medical.appointment.domain.model.EstadoCita;
import com.medical.appointment.infrastructure.adapter.in.web.dto.request.ReprogramarCitaRequest;
import com.medical.appointment.infrastructure.adapter.in.web.dto.request.ReservarCitaRequest;
import com.medical.appointment.infrastructure.adapter.in.web.dto.response.CitaResponse;
import com.medical.appointment.infrastructure.adapter.in.web.dto.response.FranjaDisponibleResponse;
import com.medical.appointment.infrastructure.adapter.in.web.mapper.CitaWebMapper;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

/** API REST de citas (RF-03, RF-04, RF-05, RF-06 y RN-06). */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaUseCase citaUseCase;
    private final CitaWebMapper mapper;

    public CitaController(CitaUseCase citaUseCase, CitaWebMapper mapper) {
        this.citaUseCase = citaUseCase;
        this.mapper = mapper;
    }

    /** RF-03: reservar cita → 201 Created. */
    @PostMapping
    public ResponseEntity<CitaResponse> reservar(@Valid @RequestBody ReservarCitaRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        Cita cita = citaUseCase.reservar(mapper.aComando(request));
        URI location = uriBuilder.path("/api/citas/{id}").buildAndExpand(cita.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.aRespuesta(cita));
    }

    /** RF-06: listar citas con filtros opcionales → 200 OK. */
    @GetMapping
    public ResponseEntity<List<CitaResponse>> listar(
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) EstadoCita estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        FiltroCitas filtro = new FiltroCitas(medicoId, pacienteId, estado, fechaInicio, fechaFin);
        List<CitaResponse> citas = citaUseCase.listar(filtro).stream().map(mapper::aRespuesta).toList();
        return ResponseEntity.ok(citas);
    }

    /** RF-04: franjas disponibles de un médico en un rango → 200 OK. */
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<FranjaDisponibleResponse>> disponibilidad(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        List<FranjaDisponibleResponse> franjas = citaUseCase
                .consultarDisponibilidad(medicoId, fechaInicio, fechaFin).stream()
                .map(mapper::aRespuesta).toList();
        return ResponseEntity.ok(franjas);
    }

    /** RF-05: cancelar cita → 200 OK. */
    @PostMapping("/{id}/cancelacion")
    public ResponseEntity<CitaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.aRespuesta(citaUseCase.cancelar(id)));
    }

    /** RN-06: reprogramar cita → 201 Created (se crea una nueva cita). */
    @PostMapping("/{id}/reprogramacion")
    public ResponseEntity<CitaResponse> reprogramar(@PathVariable Long id,
                                                    @Valid @RequestBody ReprogramarCitaRequest request,
                                                    UriComponentsBuilder uriBuilder) {
        Cita nueva = citaUseCase.reprogramar(id, request.nuevaFechaHora());
        URI location = uriBuilder.path("/api/citas/{id}").buildAndExpand(nueva.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.aRespuesta(nueva));
    }
}
