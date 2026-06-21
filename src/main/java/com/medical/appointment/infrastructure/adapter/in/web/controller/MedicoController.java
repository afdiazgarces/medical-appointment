package com.medical.appointment.infrastructure.adapter.in.web.controller;

import com.medical.appointment.application.port.in.MedicoUseCase;
import com.medical.appointment.domain.model.Medico;
import com.medical.appointment.infrastructure.adapter.in.web.dto.request.CrearMedicoRequest;
import com.medical.appointment.infrastructure.adapter.in.web.dto.response.MedicoResponse;
import com.medical.appointment.infrastructure.adapter.in.web.mapper.MedicoWebMapper;
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

/** API REST de médicos (RF-01). */
@RestController
@RequestMapping("/api/medicos")
public class MedicoController {

    private final MedicoUseCase medicoUseCase;
    private final MedicoWebMapper mapper;

    public MedicoController(MedicoUseCase medicoUseCase, MedicoWebMapper mapper) {
        this.medicoUseCase = medicoUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<MedicoResponse> registrar(@Valid @RequestBody CrearMedicoRequest request,
                                                    UriComponentsBuilder uriBuilder) {
        Medico medico = medicoUseCase.registrar(mapper.aComando(request));
        URI location = uriBuilder.path("/api/medicos/{id}").buildAndExpand(medico.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.aRespuesta(medico));
    }

    @GetMapping
    public ResponseEntity<List<MedicoResponse>> listar() {
        List<MedicoResponse> medicos = medicoUseCase.listar().stream().map(mapper::aRespuesta).toList();
        return ResponseEntity.ok(medicos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.aRespuesta(medicoUseCase.obtenerPorId(id)));
    }
}
