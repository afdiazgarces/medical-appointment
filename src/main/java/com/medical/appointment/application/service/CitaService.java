package com.medical.appointment.application.service;

import com.medical.appointment.application.port.in.CitaUseCase;
import com.medical.appointment.application.port.in.command.FiltroCitas;
import com.medical.appointment.application.port.in.command.ReservarCitaCommand;
import com.medical.appointment.application.port.out.CitaRepositoryPort;
import com.medical.appointment.application.port.out.MedicoRepositoryPort;
import com.medical.appointment.application.port.out.PacienteRepositoryPort;
import com.medical.appointment.application.port.out.PenalizacionRepositoryPort;
import com.medical.appointment.domain.exception.ConflictoException;
import com.medical.appointment.domain.exception.RecursoNoEncontradoException;
import com.medical.appointment.domain.exception.ReglaNegocioException;
import com.medical.appointment.domain.model.Cita;
import com.medical.appointment.domain.model.FranjaHoraria;
import com.medical.appointment.domain.model.Paciente;
import com.medical.appointment.domain.model.Penalizacion;
import com.medical.appointment.domain.policy.HorarioAtencion;
import com.medical.appointment.domain.policy.PoliticaAgendamiento;
import com.medical.appointment.domain.policy.PoliticaPenalizacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CitaService implements CitaUseCase {

    private final CitaRepositoryPort citaRepository;
    private final MedicoRepositoryPort medicoRepository;
    private final PacienteRepositoryPort pacienteRepository;
    private final PenalizacionRepositoryPort penalizacionRepository;
    private final HorarioAtencion horarioAtencion;
    private final PoliticaPenalizacion politicaPenalizacion;
    private final PoliticaAgendamiento politicaAgendamiento;
    private final Clock clock;

    public CitaService(CitaRepositoryPort citaRepository,
                       MedicoRepositoryPort medicoRepository,
                       PacienteRepositoryPort pacienteRepository,
                       PenalizacionRepositoryPort penalizacionRepository,
                       HorarioAtencion horarioAtencion,
                       PoliticaPenalizacion politicaPenalizacion,
                       PoliticaAgendamiento politicaAgendamiento,
                       Clock clock) {
        this.citaRepository = citaRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.penalizacionRepository = penalizacionRepository;
        this.horarioAtencion = horarioAtencion;
        this.politicaPenalizacion = politicaPenalizacion;
        this.politicaAgendamiento = politicaAgendamiento;
        this.clock = clock;
    }

    // ---------------------------------------------------------------------
    // RF-03: Reserva de citas
    // ---------------------------------------------------------------------
    @Override
    @Transactional
    public Cita reservar(ReservarCitaCommand command) {
        return crearCitaValidada(command.pacienteId(), command.medicoId(), command.fechaHora(), true);
    }

    // ---------------------------------------------------------------------
    // RF-05: Cancelación de citas
    // ---------------------------------------------------------------------
    @Override
    @Transactional
    public Cita cancelar(Long citaId) {
        Cita cita = citaRepository.buscarPorId(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita", citaId));
        return cancelarYPenalizar(cita);
    }

    // ---------------------------------------------------------------------
    // RN-06: Reprogramación (transaccional: cancelar + crear)
    // ---------------------------------------------------------------------
    @Override
    @Transactional
    public Cita reprogramar(Long citaId, LocalDateTime nuevaFechaHora) {
        Cita original = citaRepository.buscarPorId(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita", citaId));

        // 1. Cancelar la cita anterior (aplica RN-05 si corresponde).
        cancelarYPenalizar(original);

        // 2 y 3. Crear la nueva cita validando disponibilidad (RN-01/RN-02/RN-NEW).
        // No se re-evalúa el bloqueo por penalizaciones (RN-05) para no penalizar la
        // propia reprogramación. Si la creación falla, la transacción revierte la
        // cancelación, dejando el sistema consistente.
        return crearCitaValidada(original.getPacienteId(), original.getMedicoId(), nuevaFechaHora, false);
    }

    // ---------------------------------------------------------------------
    // RF-04: Consulta de disponibilidad
    // ---------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<FranjaHoraria> consultarDisponibilidad(Long medicoId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (!medicoRepository.existePorId(medicoId)) {
            throw new RecursoNoEncontradoException("Médico", medicoId);
        }
        if (fechaInicio == null || fechaFin == null || fechaInicio.isAfter(fechaFin)) {
            throw new ReglaNegocioException("El rango de fechas es inválido: fechaInicio debe ser <= fechaFin");
        }

        Set<LocalDateTime> ocupadas = citaRepository
                .buscarProgramadasDeMedicoEntre(medicoId, fechaInicio, fechaFin).stream()
                .map(Cita::getFechaHora)
                .collect(Collectors.toSet());

        return horarioAtencion.franjasEnRango(fechaInicio.toLocalDate(), fechaFin.toLocalDate()).stream()
                .filter(franja -> !franja.inicio().isBefore(fechaInicio))
                .filter(franja -> !franja.inicio().isAfter(fechaFin))
                .filter(franja -> !ocupadas.contains(franja.inicio()))
                .toList();
    }

    // ---------------------------------------------------------------------
    // RF-06: Listado con filtros
    // ---------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<Cita> listar(FiltroCitas filtro) {
        return citaRepository.buscarConFiltros(filtro);
    }

    // ---------------------------------------------------------------------
    // Helpers privados
    // ---------------------------------------------------------------------

    /**
     * Crea y persiste una cita validando todas las reglas de agendamiento.
     *
     * @param verificarBloqueo si {@code true} aplica el bloqueo por penalizaciones (RN-05).
     */
    private Cita crearCitaValidada(Long pacienteId, Long medicoId, LocalDateTime fechaHora, boolean verificarBloqueo) {
        Paciente paciente = pacienteRepository.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente", pacienteId));
        if (!medicoRepository.existePorId(medicoId)) {
            throw new RecursoNoEncontradoException("Médico", medicoId);
        }

        LocalDateTime ahora = LocalDateTime.now(clock);

        // RN-03: edad mínima / fecha de nacimiento no futura.
        paciente.validarAptoParaAgendar(ahora.toLocalDate());

        // RN-05: bloqueo por acumulación de penalizaciones.
        if (verificarBloqueo) {
            verificarPacienteNoBloqueado(pacienteId, ahora);
        }

        // RN-01: la franja debe estar dentro de la jornada laboral y alineada a la duración configurada.
        if (!horarioAtencion.esInicioDeFranjaValido(fechaHora)) {
            throw new ReglaNegocioException(
                    "La fecha/hora " + fechaHora + " no corresponde a una franja válida de atención (RN-01)");
        }

        // RN-NEW-1: el paciente no puede superar el límite diario de citas.
        verificarMaximoCitasPorDia(pacienteId, fechaHora.toLocalDate());

        // RN-02: el médico no puede tener otra cita en esa franja.
        if (citaRepository.existeCitaProgramadaDeMedico(medicoId, fechaHora)) {
            throw new ConflictoException(
                    "El médico ya tiene una cita programada en la franja " + fechaHora + " (RN-02)");
        }

        // RN-NEW-2: el paciente no puede tener otra cita en esa misma franja, aunque sea con otro médico.
        if (citaRepository.existeCitaProgramadaDePacienteEnFranja(pacienteId, fechaHora)) {
            throw new ConflictoException(
                    "El paciente ya tiene una cita programada en la franja " + fechaHora
                            + ". No puede tener dos citas en el mismo horario (RN-NEW-2)");
        }

        return citaRepository.guardar(Cita.programar(pacienteId, medicoId, fechaHora));
    }

    /** Cancela la cita y registra penalización si la cancelación es tardía (RN-05). */
    private Cita cancelarYPenalizar(Cita cita) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        boolean tardia = cita.esCancelacionTardia(ahora);

        cita.cancelar(ahora); // valida que estuviera PROGRAMADA
        Cita cancelada = citaRepository.guardar(cita);

        if (tardia) {
            penalizacionRepository.guardar(
                    Penalizacion.registrar(cita.getPacienteId(), cancelada.getId(), ahora));
        }
        return cancelada;
    }

    /** RN-05: lanza conflicto si el paciente acumula el máximo de penalizaciones en la ventana. */
    private void verificarPacienteNoBloqueado(Long pacienteId, LocalDateTime ahora) {
        LocalDateTime inicioVentana = ahora.minusDays(PoliticaPenalizacion.VENTANA_DIAS);
        long penalizaciones = penalizacionRepository.contarDePacienteDesde(pacienteId, inicioVentana);
        if (politicaPenalizacion.superaLimite(penalizaciones)) {
            throw new ConflictoException(
                    "El paciente está bloqueado: %d o más penalizaciones en los últimos %d días (RN-05)"
                            .formatted(PoliticaPenalizacion.MAX_PENALIZACIONES, PoliticaPenalizacion.VENTANA_DIAS));
        }
    }

    /** RN-NEW-1: lanza conflicto si el paciente alcanza el límite diario de citas. */
    private void verificarMaximoCitasPorDia(Long pacienteId, LocalDate fecha) {
        long citasEnElDia = citaRepository.contarCitasProgramadasDePacienteEnFecha(pacienteId, fecha);
        if (politicaAgendamiento.superaMaximoDiario(citasEnElDia)) {
            throw new ConflictoException(
                    "El paciente ha alcanzado el máximo de %d citas permitidas para el día %s (RN-NEW-1)"
                            .formatted(politicaAgendamiento.getMaxCitasPorPacientePorDia(), fecha));
        }
    }
}
