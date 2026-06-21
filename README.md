# MediSalud API — Sistema de Agendamiento de Citas Médicas

API REST para que una clínica gestione médicos, pacientes y el agendamiento de citas
con control de disponibilidad por franjas de 30 minutos, cancelaciones y penalizaciones
por ausentismo. Backend puro (sin frontend, sin autenticación), construido como prueba
técnica.

---

## 1. Tecnologías

| Componente | Versión / Detalle                                 |
|---|---------------------------------------------------|
| Lenguaje | Java **21**                                       |
| Framework | Spring Boot **3.5.6** (Web, Data JPA, Validation) |
| Build | Gradle **9.1.0** (wrapper incluido)               |
| Persistencia | Hibernate/JPA sobre **H2** en memoria             |
| Boilerplate | **Lombok** (entidades JPA y dominio)              |
| Documentación | **springdoc-openapi** (Swagger UI)                |
| Tests | JUnit 5 + Mockito + AssertJ + MockMvc             |

---

## 2. Cómo ejecutar localmente

Requisito: **JDK 21** disponible (el wrapper de Gradle descarga Gradle 9.1 automáticamente).

```bash
# Linux / macOS
./gradlew bootRun

# Windows (PowerShell / CMD)
gradlew.bat bootRun
```

O empaquetando un JAR ejecutable:

```bash
./gradlew bootJar
java -jar build/libs/medical-appointment-0.0.1-SNAPSHOT.jar
```

La aplicación arranca en **http://localhost:8080**.

| Recurso | URL |
|---|---|
| Base de la API | `http://localhost:8080/api` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| Consola H2 | `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:medisalud`, user `sa`, sin password) |

Al iniciar se ejecutan `schema.sql` (estructura) y `data.sql` (3 médicos de ejemplo).

### Ejecutar los tests

```bash
./gradlew test
```

---

## 3. Arquitectura — Hexagonal (Puertos y Adaptadores)

Se eligió **arquitectura hexagonal** para aislar las reglas de negocio (el activo más
valioso de este dominio) de los detalles de infraestructura (web y base de datos),
de modo que el núcleo sea testeable sin Spring ni H2 y que la persistencia o el transporte
puedan cambiar sin tocar la lógica.

```
┌───────────────────────────────────────────────────────────────────┐
│  infrastructure (ADAPTADORES)                                        │
│                                                                      │
│   in/web  ──► Controllers, DTOs, Mappers, @RestControllerAdvice      │
│      │                                                               │
│      ▼  (puertos de entrada)                                         │
│  ┌──────────────────── application ────────────────────┐            │
│  │  port/in (casos de uso)   service (orquestación)     │            │
│  │            │                      │                  │            │
│  │            ▼                      ▼                  │            │
│  │     ┌──────────── domain (CORE PURO) ──────────┐     │            │
│  │     │  model · policy · exception              │     │            │
│  │     │  (sin Spring, sin JPA, solo Java)        │     │            │
│  │     └──────────────────────────────────────────┘     │            │
│  │                          ▲                            │            │
│  │  port/out (interfaces de repositorio)                 │            │
│  └───────────────────────────│──────────────────────────┘            │
│                               ▼                                       │
│   out/persistence ──► Entidades JPA, Spring Data, Mappers, Adapters   │
└───────────────────────────────────────────────────────────────────┘
```

**Regla de dependencias:** todo apunta hacia adentro. `infrastructure → application → domain`.
El dominio no conoce a nadie.

### Estructura de paquetes

```
com.medical.appointment
├── domain/                         # CORE — Java puro
│   ├── model/                      # Medico, Paciente, Cita, Penalizacion, EstadoCita, FranjaHoraria
│   ├── policy/                     # HorarioAtencion (RN-01), PoliticaPenalizacion (RN-05)
│   └── exception/                  # DomainException + RecursoNoEncontrado/ReglaNegocio/Conflicto
├── application/
│   ├── port/in/                    # MedicoUseCase, PacienteUseCase, CitaUseCase (+ command/)
│   ├── port/out/                   # *RepositoryPort (interfaces)
│   └── service/                    # MedicoService, PacienteService, CitaService
└── infrastructure/
    ├── adapter/in/web/             # controller, dto (request/response), mapper, error
    ├── adapter/out/persistence/    # entity, repository, mapper, adapter
    └── config/                     # beans de dominio (Clock, políticas) + OpenAPI
```

### Decisiones de diseño relevantes

- **Triple modelo + mappers:** `DTO` (web) ↔ `Modelo de dominio` (core) ↔ `Entidad JPA`
  (persistencia). Cada frontera tiene su mapper, evitando que las anotaciones de JPA o
  Jackson contaminen el dominio.
- **`schema.sql` como fuente de verdad:** Hibernate corre con `ddl-auto: none`; el esquema
  y los datos los crean los scripts SQL provistos. Las entidades JPA reflejan exactamente
  tablas, columnas y restricciones.
- **Modelo de dominio rico:** las invariantes y el comportamiento viven en el dominio
  (`Cita.cancelar()`, `Cita.esCancelacionTardia()`, `Paciente.validarAptoParaAgendar()`,
  `HorarioAtencion.esInicioDeFranjaValido()`), no en los servicios.
- **Factorías `crear/programar` vs `reconstituir`:** distinguen la creación de una entidad
  nueva (valida invariantes, sin id) de la rehidratación desde la base de datos.
- **`Clock` inyectable:** las reglas dependientes del tiempo (RN-05) son deterministas y
  testeables con un reloj fijo.

---

## 4. Reglas de negocio implementadas

| Regla | Descripción | Efecto |
|---|---|---|
| **RN-01** | Jornada L-V 08:00–18:00, Sáb 08:00–13:00, Dom no atención. Franjas de 30 min alineadas a `:00`/`:30`. | `400` si la fecha/hora es inválida |
| **RN-02** | Un médico no puede tener dos citas en la misma franja. | `409` |
| **RN-03** | Edad mínima 0; no se admiten fechas de nacimiento futuras (validado al agendar). | `400` |
| **RN-04** | Un paciente no puede tener dos citas con el mismo médico en la misma franja. | `409` |
| **RN-05** | Cancelar con < 2h de antelación genera penalización; con ≥ 3 penalizaciones en 30 días el paciente queda bloqueado para agendar. | penalización + `409` al bloquear |
| **RN-06** | Reprogramar = cancelar la anterior (aplica RN-05) + crear una nueva validando disponibilidad, de forma **transaccional**. | atómico (`@Transactional`) |

---

## 5. Endpoints de la API

### Médicos (RF-01)

| Método | Ruta | Descripción | Éxito |
|---|---|---|---|
| POST | `/api/medicos` | Registrar médico | 201 |
| GET | `/api/medicos` | Listar médicos | 200 |
| GET | `/api/medicos/{id}` | Obtener por id | 200 |

```bash
curl -X POST http://localhost:8080/api/medicos -H "Content-Type: application/json" -d '{
  "nombreCompleto": "Dr. Luis Ramirez",
  "especialidad": "Neurologia",
  "telefono": "555-2002",
  "email": "luis.ramirez@medisalud.com"
}'
# 201 Created
# { "id": 4, "nombreCompleto": "Dr. Luis Ramirez", "especialidad": "Neurologia", ... }
```

### Pacientes (RF-02)

| Método | Ruta | Descripción | Éxito |
|---|---|---|---|
| POST | `/api/pacientes` | Registrar paciente | 201 |
| GET | `/api/pacientes` | Listar pacientes | 200 |
| GET | `/api/pacientes/{id}` | Obtener por id | 200 |

```bash
curl -X POST http://localhost:8080/api/pacientes -H "Content-Type: application/json" -d '{
  "nombreCompleto": "Pedro Gomez",
  "documentoIdentidad": "1122334",
  "telefono": "5550000",
  "email": "pedro@mail.com",
  "fechaNacimiento": "1990-03-10"
}'
# 201 Created → { "id": 1, ... }
# Si el documento ya existe → 409 Conflict
```

### Citas (RF-03 a RF-06, RN-06)

| Método | Ruta | Descripción | Éxito |
|---|---|---|---|
| POST | `/api/citas` | Reservar cita | 201 |
| GET | `/api/citas/disponibilidad?medicoId&fechaInicio&fechaFin` | Franjas disponibles | 200 |
| GET | `/api/citas?medicoId&pacienteId&estado&fechaInicio&fechaFin` | Listar con filtros | 200 |
| POST | `/api/citas/{id}/cancelacion` | Cancelar cita | 200 |
| POST | `/api/citas/{id}/reprogramacion` | Reprogramar cita | 201 |

```bash
# Reservar (RF-03)
curl -X POST http://localhost:8080/api/citas -H "Content-Type: application/json" -d '{
  "pacienteId": 1, "medicoId": 1, "fechaHora": "2026-06-23T09:00:00"
}'
# 201 Created
# { "id": 1, "pacienteId": 1, "medicoId": 1, "fechaHora": "2026-06-23T09:00:00",
#   "estado": "PROGRAMADA", "fechaCancelacion": null }

# Consultar disponibilidad (RF-04)
curl "http://localhost:8080/api/citas/disponibilidad?medicoId=1&fechaInicio=2026-06-23T08:00:00&fechaFin=2026-06-23T10:00:00"
# 200 OK
# [ { "inicio": "2026-06-23T08:00:00", "fin": "2026-06-23T08:30:00" }, ... ]

# Cancelar (RF-05)
curl -X POST http://localhost:8080/api/citas/1/cancelacion
# 200 OK → { ..., "estado": "CANCELADA", "fechaCancelacion": "..." }

# Reprogramar (RN-06)
curl -X POST http://localhost:8080/api/citas/1/reprogramacion -H "Content-Type: application/json" -d '{
  "nuevaFechaHora": "2026-06-24T10:00:00"
}'
# 201 Created → nueva cita PROGRAMADA

# Listar por estado (RF-06)
curl "http://localhost:8080/api/citas?estado=CANCELADA"
```

---

## 6. Manejo de errores

Todas las respuestas de error comparten el mismo formato, emitido por un
`@RestControllerAdvice` global:

```json
{
  "timestamp": "2026-06-23T10:15:30.123",
  "status": 409,
  "error": "Conflict",
  "message": "El médico ya tiene una cita programada en la franja 2026-06-23T09:00 (RN-02)",
  "path": "/api/citas"
}
```

| Código | Cuándo |
|---|---|
| `400 Bad Request` | Validación de DTO, regla de negocio (RN-01/RN-03), JSON o parámetros malformados |
| `404 Not Found` | Médico, paciente o cita inexistente |
| `409 Conflict` | Franja ocupada (RN-02/RN-04), paciente bloqueado (RN-05), documento duplicado |
| `500 Internal Server Error` | Error inesperado (mensaje genérico, sin filtrar detalles internos) |

---

## 7. Pruebas

Cobertura de los flujos críticos con tests unitarios (dominio y aplicación) y de capa web:

- **Dominio:** `FranjaHorariaTest`, `HorarioAtencionTest` (RN-01), `PacienteTest` (RN-03),
  `CitaTest` (RF-05/RN-05).
- **Aplicación:** `CitaServiceTest` con Mockito y `Clock` fijo, cubriendo RN-01 a RN-06
  (reserva, conflictos 409, edad, bloqueo por penalizaciones, cancelación tardía,
  reprogramación); `PacienteServiceTest` (documento duplicado).
- **Web:** `CitaControllerTest` (`@WebMvcTest`) verificando el mapeo HTTP 201/400/404/409.
- **Contexto:** `MedicalAppointmentApplicationTests` levanta el contexto completo.

---

## 8. Seguridad y validación

- **Validación en dos niveles:** Bean Validation en los DTO de entrada (formato/obligatoriedad)
  + invariantes en el dominio (defensa en profundidad).
- **Protección contra inyección SQL:** todo el acceso a datos usa JPA con consultas
  parametrizadas; no hay concatenación de SQL.
- **Sin fuga de detalles internos:** los errores 500 devuelven un mensaje genérico.
- No se implementa autenticación/autorización por estar explícitamente fuera del alcance.

---

## 9. Notas

- `data.sql` inserta los médicos con IDs explícitos (1-3); se añade un
  `ALTER TABLE ... RESTART WITH 4` para que los médicos creados vía API no colisionen con
  el contador IDENTITY.
- El enunciado original (`src/main/resources/enunciado-prueba-java.pdf`) y el
  `schema.sql` se conservan en el repositorio como referencia.
```
