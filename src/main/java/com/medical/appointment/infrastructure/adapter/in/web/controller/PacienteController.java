package com.medical.appointment.infrastructure.adapter.in.web.controller;

import com.medical.appointment.application.port.in.PacienteUseCase;
import com.medical.appointment.domain.model.Paciente;
import com.medical.appointment.infrastructure.adapter.in.web.dto.request.CrearPacienteRequest;
import com.medical.appointment.infrastructure.adapter.in.web.dto.response.PacienteResponse;
import com.medical.appointment.infrastructure.adapter.in.web.mapper.PacienteWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** API REST de pacientes (RF-02). */
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteUseCase pacienteUseCase;
    private final PacienteWebMapper mapper;

    public PacienteController(PacienteUseCase pacienteUseCase, PacienteWebMapper mapper) {
        this.pacienteUseCase = pacienteUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> registrar(@Valid @RequestBody CrearPacienteRequest request,
                                                      UriComponentsBuilder uriBuilder) {
        Paciente paciente = pacienteUseCase.registrar(mapper.aComando(request));
        URI location = uriBuilder.path("/api/pacientes/{id}").buildAndExpand(paciente.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.aRespuesta(paciente));
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponse>> listar() {
        List<PacienteResponse> pacientes = pacienteUseCase.listar().stream().map(mapper::aRespuesta).toList();
        return ResponseEntity.ok(pacientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.aRespuesta(pacienteUseCase.obtenerPorId(id)));
    }
}
