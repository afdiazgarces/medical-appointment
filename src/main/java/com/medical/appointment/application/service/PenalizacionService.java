package com.medical.appointment.application.service;

import com.medical.appointment.application.port.in.PenalizacionUseCase;
import com.medical.appointment.application.port.in.command.FiltroPenalizaciones;
import com.medical.appointment.application.port.out.PenalizacionRepositoryPort;
import com.medical.appointment.domain.model.Penalizacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PenalizacionService implements PenalizacionUseCase {

    private final PenalizacionRepositoryPort penalizacionRepository;

    public PenalizacionService(PenalizacionRepositoryPort penalizacionRepository) {
        this.penalizacionRepository = penalizacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Penalizacion> listar(FiltroPenalizaciones filtro) {
        return penalizacionRepository.listar(filtro);
    }
}
