package com.medical.appointment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración end-to-end: HTTP → Service → JPA → H2 real.
 * No se mockea ninguna capa de infraestructura; cada test ejerce el stack completo.
 *
 * <p>Reloj fijo: lunes 2026-06-22 08:00 UTC.
 * <ul>
 *   <li>Franjas del mismo día (08:30–09:30) quedan dentro del umbral de 2 h
 *       → cancelación tardía → penalización (RN-05).</li>
 *   <li>Franjas de días futuros (≥ 24 h de antelación) → cancelación a tiempo,
 *       sin penalización.</li>
 *   <li>Domingo 2026-06-28 → sin atención (RN-01).</li>
 * </ul>
 *
 * <p><b>Por qué @MockitoBean Clock en lugar de @TestConfiguration:</b> usar una
 * @TestConfiguration inner class altera la clave de caché del contexto de Spring
 * TestContext, forzando a crear un segundo ApplicationContext para las clases
 * @Nested y ejecutando schema.sql dos veces (lo que falla en H2 porque las tablas
 * ya existen). Con @MockitoBean la clave de caché es idéntica en todas las clases
 * @Nested y el contexto se comparte en toda la suite.
 */
@SpringBootTest(properties = {
        // Base de datos H2 aislada para esta suite: evita que el schema.sql
        // colisione con el contexto de MedicalAppointmentApplicationTests,
        // que usa jdbc:h2:mem:medisalud en el mismo proceso JVM.
        "spring.datasource.url=jdbc:h2:mem:flujos;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
@AutoConfigureMockMvc
@DisplayName("Flujos de negocio — integración completa (HTTP → JPA → H2)")
class FlujosNegocioIT {

    // ─────────────────────────── reloj fijo ───────────────────────────────────

    /** Lunes 2026-06-22 08:00 — día laborable, inicio de jornada. */
    private static final LocalDateTime CLOCK_FIXED = LocalDateTime.of(2026, 6, 22, 8, 0, 0);

    // ─────────────────────────── dependencias ─────────────────────────────────

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbc;

    /**
     * Reemplaza el bean Clock de DomainBeansConfig con un mock de Mockito.
     * Se reconfigura en cada @BeforeEach para apuntar al instante fijo.
     */
    @MockitoBean
    Clock clock;

    /** IDs de médicos precargados por data.sql — estables en toda la suite. */
    private static final long MEDICO_1 = 1L;
    private static final long MEDICO_2 = 2L;
    private static final long MEDICO_3 = 3L;

    // ─────────────────────────── setup ────────────────────────────────────────

    /**
     * Antes de cada test:
     * <ol>
     *   <li>Configura el reloj fijo en el mock de Clock.</li>
     *   <li>Borra datos de prueba en el orden que respeta las FK del schema.sql:
     *       penalizaciones → citas → pacientes. Los médicos no se tocan.</li>
     * </ol>
     */
    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(CLOCK_FIXED.toInstant(ZoneOffset.UTC));
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        jdbc.execute("DELETE FROM penalizaciones");
        jdbc.execute("DELETE FROM citas");
        jdbc.execute("DELETE FROM pacientes");
    }

    // ─────────────────────────── helpers HTTP ─────────────────────────────────

    private long crearPaciente(String nombre, String documento) throws Exception {
        String body = """
                {"nombreCompleto":"%s","documentoIdentidad":"%s",\
                "telefono":"5550001","email":"p@test.com","fechaNacimiento":"1990-06-01"}
                """.formatted(nombre, documento);
        String resp = mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asLong();
    }

    /** Reserva exitosa — el test falla con AssertionError si no devuelve 201. */
    private long reservar(long pacienteId, long medicoId, String fechaHora) throws Exception {
        String body = """
                {"pacienteId":%d,"medicoId":%d,"fechaHora":"%s"}
                """.formatted(pacienteId, medicoId, fechaHora);
        String resp = mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("id").asLong();
    }

    /** Intento de reserva — devuelve ResultActions para aserciones de error. */
    private ResultActions intentarReservar(long pacienteId, long medicoId, String fechaHora) throws Exception {
        String body = """
                {"pacienteId":%d,"medicoId":%d,"fechaHora":"%s"}
                """.formatted(pacienteId, medicoId, fechaHora);
        return mockMvc.perform(post("/api/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions cancelar(long citaId) throws Exception {
        return mockMvc.perform(post("/api/citas/" + citaId + "/cancelacion"));
    }

    private ResultActions reprogramar(long citaId, String nuevaFechaHora) throws Exception {
        String body = """
                {"nuevaFechaHora":"%s"}
                """.formatted(nuevaFechaHora);
        return mockMvc.perform(post("/api/citas/" + citaId + "/reprogramacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions listarCitasDe(long pacienteId) throws Exception {
        return mockMvc.perform(get("/api/citas")
                .param("pacienteId", String.valueOf(pacienteId)));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FLUJO 1 — Penalización y bloqueo (RN-05)
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("RN-05 — Penalización y bloqueo del paciente")
    class FlujoPenalizacionYBloqueo {

        @Test
        @DisplayName("Cancelar 3 veces con < 2 h de antelación bloquea al paciente en la 4ª reserva")
        void tresPenalizacionesBloqueaAlPaciente() throws Exception {
            long pid = crearPaciente("Pedro Bloqueado", "DOC-BLQ-001");

            // Franjas 08:30, 09:00 y 09:30 → 30, 60 y 90 min desde el reloj fijo (08:00)
            // → las 3 cancelaciones son tardías → se registra una penalización por cada una.
            long c1 = reservar(pid, MEDICO_1, "2026-06-22T08:30:00");
            cancelar(c1).andExpect(status().isOk());

            long c2 = reservar(pid, MEDICO_2, "2026-06-22T09:00:00");
            cancelar(c2).andExpect(status().isOk());

            long c3 = reservar(pid, MEDICO_3, "2026-06-22T09:30:00");
            cancelar(c3).andExpect(status().isOk());

            // La 4ª reserva → 409: 3 penalizaciones acumuladas en los últimos 30 días.
            intentarReservar(pid, MEDICO_1, "2026-06-23T08:30:00")
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", containsString("bloqueado")));
        }

        @Test
        @DisplayName("Cada cancelación tardía persiste una penalización consultable por API")
        void penalizacionesQuedaronPersistidas() throws Exception {
            long pid = crearPaciente("Ana Penalizada", "DOC-BLQ-002");

            long c1 = reservar(pid, MEDICO_1, "2026-06-22T08:30:00");
            cancelar(c1).andExpect(status().isOk());

            long c2 = reservar(pid, MEDICO_2, "2026-06-22T09:00:00");
            cancelar(c2).andExpect(status().isOk());

            // Las penalizaciones existen en H2 y son visibles vía el endpoint.
            mockMvc.perform(get("/api/penalizaciones")
                            .param("pacienteId", String.valueOf(pid)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].pacienteId", is((int) pid)))
                    .andExpect(jsonPath("$[1].pacienteId", is((int) pid)));
        }

        @Test
        @DisplayName("Cancelar con >= 2 h de antelación NO genera penalización")
        void cancelarATiempoNoGeneraPenalizacion() throws Exception {
            long pid = crearPaciente("Luis Puntual", "DOC-BLQ-003");

            // Mañana 08:30 → 24 h 30 min de antelación → cancelación a tiempo.
            long cita = reservar(pid, MEDICO_1, "2026-06-23T08:30:00");
            cancelar(cita).andExpect(status().isOk());

            mockMvc.perform(get("/api/penalizaciones")
                            .param("pacienteId", String.valueOf(pid)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Con 2 penalizaciones el paciente NO está bloqueado (umbral = 3)")
        void dosPenalizacionesNoBloquean() throws Exception {
            long pid = crearPaciente("María Parcial", "DOC-BLQ-004");

            long c1 = reservar(pid, MEDICO_1, "2026-06-22T08:30:00");
            cancelar(c1).andExpect(status().isOk());

            long c2 = reservar(pid, MEDICO_2, "2026-06-22T09:00:00");
            cancelar(c2).andExpect(status().isOk());

            // Solo 2 penalizaciones → por debajo del umbral → puede seguir reservando.
            intentarReservar(pid, MEDICO_3, "2026-06-23T08:30:00")
                    .andExpect(status().isCreated());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FLUJO 2 — Reprogramación atómica (RN-06)
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("RN-06 — Reprogramación atómica (cancelar + crear en una sola transacción)")
    class FlujoReprogramacionAtomica {

        @Test
        @DisplayName("Si la nueva franja está ocupada por el médico, la cita original permanece PROGRAMADA")
        void franjaOcupadaRevierteYDejaOriginalProgramada() throws Exception {
            long pid     = crearPaciente("Carlos Reprogramado", "DOC-REP-001");
            long otroPid = crearPaciente("Otro Paciente",       "DOC-REP-002");

            long citaA = reservar(pid,     MEDICO_1, "2026-06-23T08:30:00");
            reservar(otroPid, MEDICO_1, "2026-06-23T09:00:00"); // ocupa la franja objetivo

            // Intento de reprogramar A → 09:00 → falla por RN-02 (médico ocupado).
            // El rollback de la transacción revierte TAMBIÉN la cancelación de A.
            reprogramar(citaA, "2026-06-23T09:00:00")
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", containsString("RN-02")));

            // Verificación clave: A sigue en su estado original.
            listarCitasDe(pid)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id",     is((int) citaA)))
                    .andExpect(jsonPath("$[0].estado", is("PROGRAMADA")));
        }

        @Test
        @DisplayName("Reprogramación exitosa: la cita original queda CANCELADA y la nueva PROGRAMADA")
        void reprogramarExitosoActualizaAmbosEstados() throws Exception {
            long pid = crearPaciente("Roberto Exitoso", "DOC-REP-003");
            long citaOriginal = reservar(pid, MEDICO_1, "2026-06-23T08:30:00");

            reprogramar(citaOriginal, "2026-06-23T09:00:00")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.estado",    is("PROGRAMADA")))
                    .andExpect(jsonPath("$.fechaHora", is("2026-06-23T09:00:00")));

            // El paciente tiene 2 citas: la original CANCELADA y la nueva PROGRAMADA.
            listarCitasDe(pid)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[?(@.estado == 'CANCELADA')]",  hasSize(1)))
                    .andExpect(jsonPath("$[?(@.estado == 'PROGRAMADA')]", hasSize(1)));
        }

        @Test
        @DisplayName("Si la nueva franja es inválida (domingo, fuera de jornada), la cita original no se cancela")
        void franjaFueraDeJornadaNoAlteraOriginal() throws Exception {
            long pid = crearPaciente("Sofía Inválida", "DOC-REP-004");
            long citaId = reservar(pid, MEDICO_1, "2026-06-23T08:30:00");

            // Domingo 2026-06-28 → sin atención (RN-01) → la reprogramación falla
            // ANTES de cancelar la cita original → el sistema queda intacto.
            reprogramar(citaId, "2026-06-28T08:30:00")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("RN-01")));

            listarCitasDe(pid)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].estado", is("PROGRAMADA")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FLUJO 3 — Límite diario de citas por paciente (RN-NEW-1)
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("RN-NEW-1 — Límite diario de citas por paciente (máx. 3 configurado en application.yaml)")
    class FlujoLimiteDiario {

        @Test
        @DisplayName("3 citas en el mismo día (el límite exacto) se permiten todas")
        void tresCitasMismoDiaPermitido() throws Exception {
            long pid = crearPaciente("Elena Triple", "DOC-DIA-001");

            reservar(pid, MEDICO_1, "2026-06-23T08:30:00");
            reservar(pid, MEDICO_2, "2026-06-23T09:00:00");
            reservar(pid, MEDICO_3, "2026-06-23T09:30:00");

            listarCitasDe(pid)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)));
        }

        @Test
        @DisplayName("La 4ª cita en el mismo día es rechazada con 409 (RN-NEW-1)")
        void cuartaCitaMismoDiaRechazada() throws Exception {
            long pid = crearPaciente("Tomás Exceso", "DOC-DIA-002");

            reservar(pid, MEDICO_1, "2026-06-23T08:30:00");
            reservar(pid, MEDICO_2, "2026-06-23T09:00:00");
            reservar(pid, MEDICO_3, "2026-06-23T09:30:00");

            // RN-NEW-1 dispara antes que RN-02, aunque la franja 10:00 del médico esté libre.
            intentarReservar(pid, MEDICO_1, "2026-06-23T10:00:00")
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", containsString("RN-NEW-1")));
        }

        @Test
        @DisplayName("El contador diario es independiente por día: 3+3 citas en días distintos OK")
        void limiteDiarioNoAfectaOtroDia() throws Exception {
            long pid = crearPaciente("Lucas Organizado", "DOC-DIA-003");

            // Martes — 3 citas (límite exacto).
            reservar(pid, MEDICO_1, "2026-06-23T08:30:00");
            reservar(pid, MEDICO_2, "2026-06-23T09:00:00");
            reservar(pid, MEDICO_3, "2026-06-23T09:30:00");

            // Miércoles — 3 citas más (el contador reinicia con el día).
            reservar(pid, MEDICO_1, "2026-06-24T08:30:00");
            reservar(pid, MEDICO_2, "2026-06-24T09:00:00");
            reservar(pid, MEDICO_3, "2026-06-24T09:30:00");

            listarCitasDe(pid)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(6)));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FLUJO 4 — Conflicto horario del paciente con cualquier médico (RN-NEW-2)
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("RN-NEW-2 — Conflicto horario del paciente (cualquier médico)")
    class FlujoConflictoHorarioPaciente {

        @Test
        @DisplayName("Mismo paciente, misma franja, médico distinto → 409 (RN-NEW-2)")
        void mismaFranjaDiferenteMedicoEsConflicto() throws Exception {
            long pid = crearPaciente("Valeria Conflicto", "DOC-CNF-001");

            reservar(pid, MEDICO_1, "2026-06-23T09:00:00");

            intentarReservar(pid, MEDICO_2, "2026-06-23T09:00:00")
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", containsString("RN-NEW-2")));
        }

        @Test
        @DisplayName("Mismo paciente, distintas franjas, médicos distintos → permitido")
        void distintasFranjasNoGeneranConflicto() throws Exception {
            long pid = crearPaciente("Diego Múltiple", "DOC-CNF-002");

            reservar(pid, MEDICO_1, "2026-06-23T08:30:00");
            reservar(pid, MEDICO_2, "2026-06-23T09:00:00");

            listarCitasDe(pid)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Dos pacientes distintos pueden coincidir en la misma franja con médicos distintos")
        void dosPacientesDistintosMismaFranjaPermitido() throws Exception {
            long pid1 = crearPaciente("Paciente Alfa", "DOC-CNF-003");
            long pid2 = crearPaciente("Paciente Beta", "DOC-CNF-004");

            // RN-NEW-2 es por paciente, no global: cada paciente solo compite consigo mismo.
            reservar(pid1, MEDICO_1, "2026-06-23T09:00:00");
            reservar(pid2, MEDICO_2, "2026-06-23T09:00:00");

            listarCitasDe(pid1).andExpect(jsonPath("$", hasSize(1)));
            listarCitasDe(pid2).andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("Una cita CANCELADA libera la franja del paciente para reservar en la misma hora con otro médico")
        void citaCanceladaLiberaFranjaDelPaciente() throws Exception {
            long pid = crearPaciente("Isabel Libre", "DOC-CNF-005");

            // Reservar y cancelar a tiempo (mañana → sin penalización).
            long citaId = reservar(pid, MEDICO_1, "2026-06-23T09:00:00");
            cancelar(citaId).andExpect(status().isOk());

            // La franja queda libre para el paciente → puede reservar con otro médico.
            intentarReservar(pid, MEDICO_2, "2026-06-23T09:00:00")
                    .andExpect(status().isCreated());
        }
    }
}
