# MediSalud API — Sistema de Agendamiento de Citas Médicas

API REST para que una clínica gestione médicos, pacientes y el agendamiento de citas con
control de disponibilidad por franjas configurables, cancelaciones y penalizaciones por
ausentismo. Backend puro (sin frontend, sin autenticación), construido como prueba técnica.

---

## Tabla de contenidos

1. [Tecnologías](#1-tecnologías)
2. [Cómo ejecutar localmente](#2-cómo-ejecutar-localmente)
3. [Arquitectura hexagonal](#3-arquitectura--hexagonal-puertos-y-adaptadores)
4. [Reglas de negocio implementadas](#4-reglas-de-negocio-implementadas)
5. [Endpoints de la API](#5-endpoints-de-la-api)
6. [Manejo de errores](#6-manejo-de-errores)
7. [Pruebas](#7-pruebas)
8. [Seguridad y validación](#8-seguridad-y-validación)
9. [Principios SOLID aplicados](#9-principios-solid-aplicados)
10. [Patrones de diseño aplicados](#10-patrones-de-diseño-aplicados)
11. [Notas](#11-notas)

---

## 1. Tecnologías

| Componente | Versión / Detalle |
|---|---|
| Lenguaje | Java **21** |
| Framework | Spring Boot **3.5.6** (Web, Data JPA, Validation) |
| Build | Gradle **9.1.0** (wrapper incluido) |
| Persistencia | Hibernate/JPA sobre **H2** en memoria |
| Boilerplate | **Lombok** (`@Getter`, `@Builder`, `@NoArgsConstructor`) |
| Documentación | **springdoc-openapi** (Swagger UI) |
| Tests | JUnit 5 + Mockito + AssertJ + MockMvc |

---

## 2. Cómo ejecutar localmente

### 2.1 Requisitos previos

| Herramienta | Versión mínima | Notas |
|---|---|---|
| **JDK** | 21 | Se necesita el JDK completo, no solo el JRE. Gradle lo usa para compilar. |
| **Gradle** | — | **No hace falta instalarlo.** El proyecto incluye el Gradle Wrapper (`gradlew` / `gradlew.bat`) que descarga Gradle 9.1.0 automáticamente la primera vez. |
| **Conexión a internet** | — | Solo la primera ejecución: Gradle descarga sus dependencias (~150 MB) desde Maven Central. Las siguientes ejecuciones usan la caché local. |

#### Instalar JDK 21

<details>
<summary>Windows</summary>

1. Descargar el instalador de [Adoptium Temurin 21](https://adoptium.net/temurin/releases/?version=21) (`.msi`) u Oracle JDK 21.
2. Ejecutar el instalador y marcar la opción **"Set JAVA_HOME"** si está disponible.
3. Verificar en una terminal **nueva**:

```powershell
java -version
# Salida esperada:
# openjdk version "21.x.x" ...   (o Java HotSpot 21.x.x)
```

Si el comando no reconoce `java`, agregar manualmente la variable de entorno:
- `JAVA_HOME` → ruta de instalación del JDK, p. ej. `C:\Program Files\Eclipse Adoptium\jdk-21.0.7.7-hotspot`
- Añadir `%JAVA_HOME%\bin` al `PATH`.

</details>

<details>
<summary>macOS</summary>

```bash
# Con Homebrew:
brew install --cask temurin@21

# Verificar:
java -version
```

Si tienes varias versiones de Java, usa `jenv` o establece `JAVA_HOME` explícitamente:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

</details>

<details>
<summary>Linux (Ubuntu / Debian)</summary>

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk

# Verificar:
java -version
javac -version   # ambos deben mostrar 21.x
```

Si tienes varias versiones, selecciona la 21:
```bash
sudo update-alternatives --config java
```

</details>

---

### 2.2 Obtener el proyecto

```bash
git clone <URL-del-repositorio>
cd medical-appointment
```

Si ya tienes el proyecto descargado, solo asegúrate de estar dentro del directorio raíz
(el que contiene `build.gradle` y `gradlew`).

---

### 2.3 Ejecutar la aplicación

#### Windows — PowerShell o CMD

```powershell
# En el directorio raíz del proyecto:
.\gradlew.bat bootRun
```

#### Linux / macOS — Terminal

```bash
# Dar permiso de ejecución al wrapper (solo la primera vez):
chmod +x gradlew

# Iniciar:
./gradlew bootRun
```

**¿Qué ocurre al ejecutar?**

1. Gradle Wrapper descarga Gradle 9.1.0 si no está en caché (`~/.gradle/wrapper/dists/`).
2. Gradle descarga todas las dependencias de Maven Central si no están en caché (`~/.gradle/caches/`).
3. Compila el proyecto.
4. Spring Boot arranca, ejecuta `schema.sql` (crea las tablas) y `data.sql` (inserta 3 médicos de ejemplo).
5. La aplicación queda lista cuando aparece en consola:

```
Started MedicalAppointmentApplication in X.XXX seconds
```

> **Tiempo estimado:** primera ejecución 2-4 min (descarga de dependencias); ejecuciones
> siguientes 10-20 segundos.

---

### 2.4 Verificar que está corriendo

Una vez arrancada, abrir el navegador o ejecutar los siguientes comandos:

| Recurso | URL |
|---|---|
| Swagger UI (interfaz interactiva) | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON (spec completa) | http://localhost:8080/v3/api-docs |
| Consola H2 (base de datos en memoria) | http://localhost:8080/h2-console |
| Base de la API | http://localhost:8080/api |

**Smoke test rápido** — listar los médicos precargados:

```bash
# Linux / macOS
curl http://localhost:8080/api/medicos

# Windows PowerShell
Invoke-WebRequest -Uri http://localhost:8080/api/medicos | Select-Object -ExpandProperty Content
```

Respuesta esperada (3 médicos del `data.sql`):

```json
[
  { "id": 1, "nombreCompleto": "Dr. Juan Pérez", "especialidad": "Medicina General", ... },
  { "id": 2, "nombreCompleto": "Dra. María González", "especialidad": "Pediatría", ... },
  { "id": 3, "nombreCompleto": "Dr. Carlos López", "especialidad": "Cardiología", ... }
]
```

#### Consola H2

Para inspeccionar las tablas directamente:

1. Abrir http://localhost:8080/h2-console en el navegador.
2. Completar los campos:
   - **JDBC URL:** `jdbc:h2:mem:medisalud`
   - **User Name:** `sa`
   - **Password:** *(vacío, no escribir nada)*
3. Clic en **Connect**.

> **Importante:** H2 es una base de datos **en memoria**. Todos los datos se pierden cuando
> la aplicación se detiene. Cada reinicio vuelve a cargar el estado inicial del `data.sql`.

---

### 2.5 Ejecutar como JAR independiente

Si se prefiere generar un JAR para distribuirlo o ejecutarlo sin Gradle:

```bash
# Compilar y empaquetar:
./gradlew bootJar          # Linux / macOS
.\gradlew.bat bootJar      # Windows

# El JAR queda en:
# build/libs/medical-appointment-0.0.1-SNAPSHOT.jar

# Ejecutar:
java -jar build/libs/medical-appointment-0.0.1-SNAPSHOT.jar
```

Para sobreescribir la configuración sin recompilar (útil para ajustar parámetros):

```bash
java -jar build/libs/medical-appointment-0.0.1-SNAPSHOT.jar \
  --appointment.duracion-minutos=15 \
  --appointment.max-citas-por-paciente-por-dia=5 \
  --server.port=9090
```

---

### 2.6 Ejecutar los tests

```bash
./gradlew test                # Linux / macOS — ejecuta los 86 tests
.\gradlew.bat test            # Windows

./gradlew test --info         # Salida detallada (nombre de cada test)
./gradlew test --tests "*.CitaServiceTest"   # Ejecuta solo una clase de test
```

El reporte HTML completo se genera en:

```
build/reports/tests/test/index.html
```

Abrir ese archivo en el navegador para ver el resultado test a test con trazas de error.

---

### 2.7 Detener la aplicación

| Entorno | Cómo detener |
|---|---|
| Terminal donde corre | `Ctrl + C` |
| En segundo plano (Linux/macOS) | `kill $(lsof -ti:8080)` |
| En segundo plano (Windows PS) | `Stop-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess` |

---

### 2.8 Problemas comunes

| Problema | Causa probable | Solución |
|---|---|---|
| `java: command not found` | JDK no instalado o no en el `PATH` | Instalar JDK 21 y verificar con `java -version` |
| `Error: LinkageError` o `UnsupportedClassVersionError` | Se está usando JDK < 21 | Verificar `java -version` y actualizar |
| `Permission denied: ./gradlew` | El wrapper no tiene permiso de ejecución (Linux/macOS) | Ejecutar `chmod +x gradlew` una vez |
| `Port 8080 was already in use` | Otra aplicación usa el puerto 8080 | Terminar ese proceso o cambiar el puerto: `./gradlew bootRun --args='--server.port=9090'` |
| `Could not resolve com.h2database:h2` | Sin conexión a internet en la primera ejecución | Conectarse a internet para que Gradle descargue las dependencias |
| `Gradle build daemon disappeared` | Poca memoria RAM | Añadir `org.gradle.jvmargs=-Xmx1g` al archivo `gradle.properties` (crear si no existe) |
| Tests fallan con `BeanCreationException` | Falta algún bean de test (configuración Spring) | Revisar que las anotaciones `@WebMvcTest` tengan los `@Import` necesarios |

---

## 3. Arquitectura — Hexagonal (Puertos y Adaptadores)

Se eligió **arquitectura hexagonal** para aislar las reglas de negocio del núcleo de los
detalles de infraestructura (web y base de datos), de modo que el dominio sea testeable sin
Spring ni H2 y que la persistencia o el transporte puedan cambiar sin tocar la lógica.

```
┌──────────────────────────────────────────────────────────────────────┐
│  infrastructure (ADAPTADORES)                                        │
│                                                                      │
│   in/web  ──► Controllers, DTOs, Mappers, @RestControllerAdvice      │
│      │                                                               │
│      ▼  (puertos de entrada — use case interfaces)                   │
│  ┌──────────────────── application ────────────────────┐             │
│  │  port/in (casos de uso)   service (orquestación)    │             │
│  │            │                      │                 │             │
│  │            ▼                      ▼                 │             │
│  │     ┌──────────── domain (CORE PURO) ───────────┐   │             │
│  │     │  model · policy · exception               │   │             │
│  │     │  (sin Spring, sin JPA — Java puro)        │   │             │
│  │     └───────────────────────────────────────────┘   │             │
│  │                          ▲                           │             │
│  │  port/out (interfaces de repositorio)                │             │
│  └──────────────────────────│────────────────────────── ┘             │
│                              ▼                                        │
│   out/persistence ──► Entidades JPA, Spring Data, Mappers, Adapters  │
└──────────────────────────────────────────────────────────────────────┘
```

**Regla de dependencias:** todo apunta hacia adentro. `infrastructure → application → domain`.
El dominio no conoce a nadie.

### Estructura de paquetes

```
com.medical.appointment
├── domain/                         # CORE — Java puro, sin dependencias externas
│   ├── model/                      # Medico, Paciente, Cita, Penalizacion,
│   │                               # EstadoCita, FranjaHoraria
│   ├── policy/                     # HorarioAtencion (RN-01), PoliticaPenalizacion (RN-05),
│   │                               # PoliticaAgendamiento (RN-NEW-1)
│   └── exception/                  # DomainException · RecursoNoEncontrado · ReglaNegocio · Conflicto
├── application/
│   ├── port/in/                    # MedicoUseCase, PacienteUseCase, CitaUseCase,
│   │   │                           # PenalizacionUseCase
│   │   └── command/                # ReservarCitaCommand, FiltroCitas, FiltroPenalizaciones, ...
│   ├── port/out/                   # *RepositoryPort (interfaces hacia persistencia)
│   └── service/                    # MedicoService, PacienteService, CitaService, PenalizacionService
└── infrastructure/
    ├── adapter/in/web/
    │   ├── controller/             # MedicoController, PacienteController, CitaController,
    │   │                           # PenalizacionController
    │   ├── dto/request/            # CrearMedicoRequest, CrearPacienteRequest, ...
    │   ├── dto/response/           # MedicoResponse, CitaResponse, PenalizacionResponse, ...
    │   ├── mapper/                 # *WebMapper (domain → DTO)
    │   └── error/                  # GlobalExceptionHandler
    ├── adapter/out/persistence/
    │   ├── adapter/                # *RepositoryAdapter (implementan los puertos de salida)
    │   ├── entity/                 # *Entity (JPA)
    │   ├── mapper/                 # *PersistenceMapper (domain ↔ JPA entity)
    │   └── repository/             # *JpaRepository (Spring Data)
    └── config/                     # DomainBeansConfig (Clock, políticas),
                                    # AppointmentProperties, OpenApiConfig
```

### Decisiones de diseño relevantes

- **Triple modelo + mappers:** `DTO` (web) ↔ `Modelo de dominio` ↔ `Entidad JPA` (persistencia).
  Cada frontera tiene su propio mapper, evitando que las anotaciones de JPA o Jackson
  contaminen el dominio.
- **`schema.sql` como fuente de verdad:** Hibernate corre con `ddl-auto: none`; el esquema
  y los datos los crean los scripts SQL provistos. Las entidades JPA reflejan exactamente
  tablas, columnas y restricciones.
- **Dominio rico:** las invariantes y el comportamiento viven en el dominio
  (`Cita.cancelar()`, `Cita.esCancelacionTardia()`, `Paciente.validarAptoParaAgendar()`,
  `HorarioAtencion.esInicioDeFranjaValido()`), no dispersos en los servicios.
- **Factorías `crear/programar` vs `reconstituir`:** distinguen la creación de una entidad
  nueva (valida invariantes, sin id) de la rehidratación desde persistencia (datos ya validados).
- **`Clock` inyectable:** las reglas dependientes del tiempo (RN-05) son deterministas y
  testeables con un reloj fijo.
- **Valores configurables desde `application.yaml`:** la duración de las franjas y el
  límite diario de citas se leen desde `AppointmentProperties`; ningún número mágico
  queda en el código de negocio.

---

## 4. Reglas de negocio implementadas

| Regla | Descripción | Efecto HTTP |
|---|---|---|
| **RN-01** | Jornada L-V 08:00–18:00, Sáb 08:00–13:00, Dom sin atención. Franjas alineadas a la duración configurada (por defecto 30 min). | `400` |
| **RN-02** | Un médico no puede tener dos citas en la misma franja. | `409` |
| **RN-03** | No se admiten fechas de nacimiento futuras (validado al agendar). | `400` |
| **RN-05** | Cancelar con < 2 h de antelación genera penalización; ≥ 3 penalizaciones en 30 días bloquea al paciente. | `409` al bloquear |
| **RN-06** | Reprogramar = cancelar la anterior (aplica RN-05) + crear nueva, de forma **transaccional**. | atómico |
| **RN-NEW-1** | Un paciente no puede superar el límite diario de citas (configurable, por defecto 3). | `409` |
| **RN-NEW-2** | Un paciente no puede tener dos citas en la misma franja horaria, aunque sean con médicos distintos. | `409` |

### Parámetros configurables (`application.yaml`)

```yaml
appointment:
  duracion-minutos: 30           # Duración de cada franja de atención
  max-citas-por-paciente-por-dia: 3  # Límite diario de citas por paciente
```

---

## 5. Endpoints de la API

### Médicos — RF-01

| Método | Ruta | Descripción | Éxito |
|---|---|---|---|
| `POST` | `/api/medicos` | Registrar médico | 201 |
| `GET` | `/api/medicos` | Listar todos los médicos | 200 |
| `GET` | `/api/medicos/{id}` | Obtener médico por id | 200 |

```bash
# Registrar médico
curl -X POST http://localhost:8080/api/medicos \
  -H "Content-Type: application/json" \
  -d '{
    "nombreCompleto": "Dr. Luis Ramirez",
    "especialidad": "Neurologia",
    "telefono": "555-2002",
    "email": "luis.ramirez@medisalud.com"
  }'
# 201 Created
# Location: /api/medicos/4
# Body:
{
  "id": 4,
  "nombreCompleto": "Dr. Luis Ramirez",
  "especialidad": "Neurologia",
  "telefono": "555-2002",
  "email": "luis.ramirez@medisalud.com"
}

# Listar médicos
curl http://localhost:8080/api/medicos
# 200 OK → [ { "id": 1, ... }, { "id": 2, ... }, ... ]

# Obtener por id
curl http://localhost:8080/api/medicos/1
# 200 OK → { "id": 1, "nombreCompleto": "...", ... }
# 404 Not Found si no existe
```

---

### Pacientes — RF-02

| Método | Ruta | Descripción | Éxito |
|---|---|---|---|
| `POST` | `/api/pacientes` | Registrar paciente | 201 |
| `GET` | `/api/pacientes` | Listar todos los pacientes | 200 |
| `GET` | `/api/pacientes/{id}` | Obtener paciente por id | 200 |

```bash
# Registrar paciente
curl -X POST http://localhost:8080/api/pacientes \
  -H "Content-Type: application/json" \
  -d '{
    "nombreCompleto": "Pedro Gomez",
    "documentoIdentidad": "1122334",
    "telefono": "5550000",
    "email": "pedro@mail.com",
    "fechaNacimiento": "1990-03-10"
  }'
# 201 Created
# Location: /api/pacientes/1
# Body:
{
  "id": 1,
  "nombreCompleto": "Pedro Gomez",
  "documentoIdentidad": "1122334",
  "telefono": "5550000",
  "email": "pedro@mail.com",
  "fechaNacimiento": "1990-03-10"
}

# Errores posibles:
# 400 Bad Request → campos obligatorios faltantes o inválidos
# 409 Conflict   → documento de identidad ya registrado
```

---

### Citas — RF-03 a RF-06, RN-06

| Método | Ruta | Descripción | Éxito |
|---|---|---|---|
| `POST` | `/api/citas` | Reservar cita | 201 |
| `GET` | `/api/citas/disponibilidad` | Franjas disponibles de un médico | 200 |
| `GET` | `/api/citas` | Listar citas con filtros opcionales | 200 |
| `POST` | `/api/citas/{id}/cancelacion` | Cancelar cita | 200 |
| `POST` | `/api/citas/{id}/reprogramacion` | Reprogramar cita | 201 |

#### Reservar cita (RF-03)

```bash
curl -X POST http://localhost:8080/api/citas \
  -H "Content-Type: application/json" \
  -d '{ "pacienteId": 1, "medicoId": 1, "fechaHora": "2026-06-23T09:00:00" }'

# 201 Created
# Location: /api/citas/1
{
  "id": 1,
  "pacienteId": 1,
  "medicoId": 1,
  "fechaHora": "2026-06-23T09:00:00",
  "estado": "PROGRAMADA",
  "fechaCancelacion": null
}

# Errores posibles:
# 400 → franja fuera de jornada (RN-01), franja no alineada (RN-01),
#        fecha de nacimiento futura (RN-03), campos inválidos
# 404 → médico o paciente no existe
# 409 → médico ocupado (RN-02), conflicto horario del paciente (RN-NEW-2),
#        límite diario superado (RN-NEW-1), paciente bloqueado (RN-05)
```

#### Consultar disponibilidad (RF-04)

```bash
curl "http://localhost:8080/api/citas/disponibilidad?medicoId=1\
&fechaInicio=2026-06-23T08:00:00&fechaFin=2026-06-23T10:00:00"

# 200 OK
[
  { "inicio": "2026-06-23T08:00:00", "fin": "2026-06-23T08:30:00" },
  { "inicio": "2026-06-23T08:30:00", "fin": "2026-06-23T09:00:00" },
  { "inicio": "2026-06-23T09:30:00", "fin": "2026-06-23T10:00:00" }
]
# (la franja 09:00 estaría excluida si el médico ya tiene cita ahí)
```

#### Cancelar cita (RF-05)

```bash
curl -X POST http://localhost:8080/api/citas/1/cancelacion

# 200 OK
{
  "id": 1,
  "pacienteId": 1,
  "medicoId": 1,
  "fechaHora": "2026-06-23T09:00:00",
  "estado": "CANCELADA",
  "fechaCancelacion": "2026-06-23T07:30:00"
}
# Si se cancela con < 2 h de antelación → se registra una penalización (RN-05)
```

#### Reprogramar cita (RN-06)

```bash
curl -X POST http://localhost:8080/api/citas/1/reprogramacion \
  -H "Content-Type: application/json" \
  -d '{ "nuevaFechaHora": "2026-06-24T10:00:00" }'

# 201 Created — se cancela la cita original y se crea una nueva
{
  "id": 2,
  "pacienteId": 1,
  "medicoId": 1,
  "fechaHora": "2026-06-24T10:00:00",
  "estado": "PROGRAMADA",
  "fechaCancelacion": null
}
```

#### Listar citas con filtros (RF-06)

Todos los parámetros son opcionales y se pueden combinar:

| Parámetro | Tipo | Ejemplo |
|---|---|---|
| `medicoId` | Long | `?medicoId=1` |
| `pacienteId` | Long | `?pacienteId=2` |
| `estado` | `PROGRAMADA` \| `CANCELADA` \| `ATENDIDA` | `?estado=CANCELADA` |
| `fechaInicio` | ISO-8601 datetime | `?fechaInicio=2026-06-01T00:00:00` |
| `fechaFin` | ISO-8601 datetime | `?fechaFin=2026-06-30T23:59:59` |

```bash
# Solo citas canceladas
curl "http://localhost:8080/api/citas?estado=CANCELADA"

# Citas de un médico en junio
curl "http://localhost:8080/api/citas?medicoId=1&fechaInicio=2026-06-01T00:00:00&fechaFin=2026-06-30T23:59:59"

# 200 OK → [ { "id": 1, ... }, ... ]
```

---

### Penalizaciones — RN-05

| Método | Ruta | Descripción | Éxito |
|---|---|---|---|
| `GET` | `/api/penalizaciones` | Listar penalizaciones con filtros opcionales | 200 |

Consulta las penalizaciones registradas por cancelaciones tardías. Sin parámetros devuelve
todas las penalizaciones del sistema.

| Parámetro | Tipo | Descripción |
|---|---|---|
| `pacienteId` | Long | Filtra las penalizaciones de un paciente concreto |
| `citaId` | Long | Filtra la penalización asociada a una cita específica |

```bash
# Todas las penalizaciones del sistema
curl http://localhost:8080/api/penalizaciones
# 200 OK
[
  {
    "id": 1,
    "pacienteId": 1,
    "citaId": 3,
    "fechaPenalizacion": "2026-06-20T09:45:00"
  },
  {
    "id": 2,
    "pacienteId": 1,
    "citaId": 7,
    "fechaPenalizacion": "2026-06-22T08:10:00"
  }
]

# Solo las penalizaciones del paciente 1
curl "http://localhost:8080/api/penalizaciones?pacienteId=1"
# 200 OK → [ { "id": 1, "pacienteId": 1, ... }, { "id": 2, ... } ]

# Penalización asociada a la cita 3
curl "http://localhost:8080/api/penalizaciones?citaId=3"
# 200 OK → [ { "id": 1, "pacienteId": 1, "citaId": 3, ... } ]

# Filtro combinado: paciente 1 + cita 3
curl "http://localhost:8080/api/penalizaciones?pacienteId=1&citaId=3"
# 200 OK → [ { "id": 1, ... } ]

# Si pacienteId no es numérico → 400 Bad Request
curl "http://localhost:8080/api/penalizaciones?pacienteId=abc"
# 400 Bad Request
```

---

## 6. Manejo de errores

Todas las respuestas de error comparten el mismo formato, emitido por `GlobalExceptionHandler`
(`@RestControllerAdvice`):

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
| `400 Bad Request` | Bean Validation, regla de negocio (RN-01/RN-03), JSON malformado, tipo de parámetro incorrecto |
| `404 Not Found` | Médico, paciente o cita inexistente |
| `409 Conflict` | Franja ocupada (RN-02), conflicto horario del paciente (RN-NEW-2), límite diario superado (RN-NEW-1), paciente bloqueado (RN-05), documento duplicado |
| `500 Internal Server Error` | Error inesperado (mensaje genérico, sin filtrar detalles internos) |

---

## 7. Pruebas

**86 tests — 0 fallos** distribuidos en capas:

### Dominio (puro, sin Spring)

| Clase | Tests | Cubre |
|---|---|---|
| `FranjaHorariaTest` | 8 | Creación con duración por defecto y configurada, alineación, rechazo de valores inválidos |
| `HorarioAtencionTest` | 8 | Jornadas por día, primera/última franja, slot inválido, conteo de franjas por duración |
| `PacienteTest` | 4 | Cálculo de edad, rechazo de nacimiento futuro |
| `CitaTest` | 4 | Cancelación, double-cancel, cancelación tardía |

### Aplicación (servicios con Mockito)

| Clase | Tests | Cubre |
|---|---|---|
| `CitaServiceTest` | 24 | RN-01..RN-06, RN-NEW-1, RN-NEW-2; reserva, conflictos, bloqueo, cancelación, reprogramación, disponibilidad |
| `PacienteServiceTest` | 6 | Registro, documento duplicado, listar, obtener OK, 404 |
| `MedicoServiceTest` | 6 | Registro, validaciones de nombre/especialidad, listar, obtener OK, 404 |
| `PenalizacionServiceTest` | 6 | Listar sin filtros, por paciente, por cita, combinado, lista vacía |

### Web (MockMvc `@WebMvcTest`)

| Clase | Tests | Cubre |
|---|---|---|
| `CitaControllerTest` | 4 | POST 201, POST 400, POST 409, POST 404 cancelación |
| `MedicoControllerTest` | 5 | POST 201+Location, POST 400, GET lista, GET 200, GET 404 |
| `PacienteControllerTest` | 6 | POST 201+Location, POST 400, POST 409, GET lista, GET 200, GET 404 |
| `PenalizacionControllerTest` | 6 | GET todas, GET vacía, filtro paciente, filtro cita, combinado, parámetro inválido 400 |

### Integración

| Clase | Tests | Cubre |
|---|---|---|
| `MedicalAppointmentApplicationTests` | 1 | Contexto Spring completo levanta correctamente |

---

## 8. Seguridad y validación

- **Validación en dos niveles:** Bean Validation en los DTO de entrada (formato / obligatoriedad)
  + invariantes en el dominio (defensa en profundidad).
- **Protección contra inyección SQL:** todo el acceso a datos usa JPA con consultas
  parametrizadas (`@Param`); no hay concatenación de SQL.
- **Sin fuga de detalles internos:** los errores 500 devuelven un mensaje genérico.
- **Doble restricción de unicidad:** el documento de identidad está validado en la capa
  de servicio _y_ por la restricción `UNIQUE` de `schema.sql`.
- No se implementa autenticación/autorización por estar explícitamente fuera del alcance.

---

## 9. Principios SOLID aplicados

### S — Single Responsibility Principle (Principio de Responsabilidad Única)

Cada clase tiene una única razón para cambiar:

| Clase | Responsabilidad única |
|---|---|
| `HorarioAtencion` | Solo conoce las reglas de jornada y genera franjas |
| `PoliticaPenalizacion` | Solo encapsula los umbrales de penalización (ventana, máximo) |
| `PoliticaAgendamiento` | Solo encapsula el límite diario de citas por paciente |
| `CitaService` | Solo orquesta los casos de uso de citas |
| `GlobalExceptionHandler` | Solo traduce excepciones a respuestas HTTP |
| `CitaWebMapper` | Solo convierte entre dominio y DTOs de cita |
| `CitaPersistenceMapper` | Solo convierte entre dominio y entidad JPA de cita |
| `CitaRepositoryAdapter` | Solo implementa el acceso a datos de citas |

**Consecuencia práctica:** cambiar la regla de cuántas horas son "cancelación tardía" solo
requiere tocar `PoliticaPenalizacion`, no el servicio ni el controlador.

---

### O — Open/Closed Principle (Principio Abierto/Cerrado)

El sistema está abierto para extensión pero cerrado para modificación:

- **Puertos de repositorio:** agregar una nueva implementación de persistencia (p. ej.
  PostgreSQL) no requiere cambiar ningún servicio. Solo se crea un nuevo adaptador que
  implemente la misma interfaz.
- **Nuevas políticas:** `PoliticaAgendamiento` se incorporó al sistema (RN-NEW-1) sin
  modificar la firma de `CitaService`; simplemente se inyecta un nuevo colaborador.
- **Filtros extensibles:** `FiltroCitas` y `FiltroPenalizaciones` son records que se pueden
  extender con nuevos campos sin romper los consumidores existentes.
- **Jerarquía de excepciones:** añadir un nuevo tipo de error solo requiere crear una
  subclase de `DomainException`; el `GlobalExceptionHandler` la recoge si se agrega
  un `@ExceptionHandler`, sin tocar los existentes.

---

### L — Liskov Substitution Principle (Principio de Sustitución de Liskov)

Cualquier implementación de un puerto puede sustituir a otra sin alterar el comportamiento:

- `CitaRepositoryAdapter` implementa completamente `CitaRepositoryPort`: el servicio que
  inyecta el puerto nunca necesita saber que hay un adaptador JPA detrás.
- Todos los servicios (`CitaService`, `PacienteService`, `MedicoService`, `PenalizacionService`)
  implementan íntegramente sus respectivos `*UseCase`: los controladores solo conocen el
  contrato de la interfaz.
- Toda excepción de negocio extiende `DomainException`; el handler global puede capturar
  la base e iterar subtipos sin romperse.

**Verificación en tests:** los tests de servicio usan `@Mock` de los puertos (interfaces),
no mocks de las implementaciones concretas.

---

### I — Interface Segregation Principle (Principio de Segregación de Interfaces)

Ningún cliente está obligado a depender de métodos que no usa:

| En lugar de... | Se tienen interfaces separadas |
|---|---|
| Un único `AppUseCase` gigante | `CitaUseCase`, `PacienteUseCase`, `MedicoUseCase`, `PenalizacionUseCase` |
| Un único `RepositoryPort` global | `CitaRepositoryPort`, `PacienteRepositoryPort`, `MedicoRepositoryPort`, `PenalizacionRepositoryPort` |

- `PenalizacionController` solo inyecta `PenalizacionUseCase`; no tiene acceso a los
  métodos de cancelación ni de reserva.
- `CitaService` inyecta `PenalizacionRepositoryPort` únicamente para guardar y contar
  penalizaciones; no ve los métodos de listado que solo usa `PenalizacionService`.

---

### D — Dependency Inversion Principle (Principio de Inversión de Dependencias)

Los módulos de alto nivel no dependen de los de bajo nivel; ambos dependen de abstracciones:

```
CitaService ──────────► CitaRepositoryPort  (abstracción)
                                 ▲
                                 │ implementa
                        CitaRepositoryAdapter (detalle)
```

- `CitaService` declara su dependencia sobre `CitaRepositoryPort` (interfaz del dominio),
  no sobre `CitaRepositoryAdapter` (implementación JPA). Spring inyecta el adaptador
  correcto en tiempo de ejecución.
- `Clock` se inyecta como bean de Spring en lugar de llamar `LocalDateTime.now()` directamente;
  en tests se reemplaza por `Clock.fixed(...)` sin tocar ninguna clase de producción.
- Las políticas de dominio (`HorarioAtencion`, `PoliticaPenalizacion`, `PoliticaAgendamiento`)
  se construyen en `DomainBeansConfig` leyendo `AppointmentProperties`; el dominio puro
  no conoce Spring ni `@Value`.

---

## 10. Patrones de diseño aplicados

### Patrones Creacionales

#### Factory Method

Separa la creación de objetos nuevos (con validación de invariantes) de su rehidratación
desde persistencia (datos ya verificados).

| Método | Clase | Propósito |
|---|---|---|
| `Cita.programar(...)` | `Cita` | Crea cita nueva en estado PROGRAMADA; valida no-nulos |
| `Cita.reconstituir(...)` | `Cita` | Rehidrata desde BD sin re-validar |
| `Paciente.crear(...)` | `Paciente` | Valida nombre (longitud), documento, etc. |
| `Paciente.reconstituir(...)` | `Paciente` | Carga datos ya validados |
| `Medico.crear(...)` | `Medico` | Valida nombre y especialidad |
| `Medico.reconstituir(...)` | `Medico` | Carga sin validar |
| `Penalizacion.registrar(...)` | `Penalizacion` | Crea penalización nueva con no-null guard |
| `Penalizacion.reconstituir(...)` | `Penalizacion` | Rehidrata desde BD |
| `FranjaHoraria.de(inicio)` | `FranjaHoraria` | Factory con duración por defecto |
| `FranjaHoraria.de(inicio, duracion)` | `FranjaHoraria` | Factory con duración explícita |

**Beneficio:** si el esquema de validación de `Paciente` cambia, solo se modifica
`Paciente.crear()` y todos los puntos de creación se actualizan automáticamente.

---

#### Builder

Las entidades JPA con muchos campos usan `@Builder` de Lombok para construcción fluida
y legible sin constructores con larga lista de parámetros.

```java
// CitaPersistenceMapper.java
return CitaEntity.builder()
        .id(cita.getId())
        .pacienteId(cita.getPacienteId())
        .medicoId(cita.getMedicoId())
        .fechaHora(cita.getFechaHora())
        .estado(cita.getEstado())
        .fechaCancelacion(cita.getFechaCancelacion())
        .build();
```

Aplicado en: `CitaEntity`, `PacienteEntity`, `MedicoEntity`, `PenalizacionEntity`.

---

#### Singleton (vía Spring IoC)

Las políticas de dominio y los mappers son stateless y se registran como beans singleton
en `DomainBeansConfig`. Una única instancia se comparte en todo el contexto.

```java
// DomainBeansConfig.java
@Bean
public HorarioAtencion horarioAtencion(AppointmentProperties props) {
    return new HorarioAtencion(Duration.ofMinutes(props.duracionMinutos()));
}
```

Aplicado en: `HorarioAtencion`, `PoliticaPenalizacion`, `PoliticaAgendamiento`,
`Clock`, todos los `*WebMapper` y `*PersistenceMapper`.

---

### Patrones Estructurales

#### Adapter (Adaptador)

Los adaptadores de repositorio traducen entre la interfaz del puerto de dominio (lo que
la aplicación necesita) y la API de Spring Data JPA (lo que la infraestructura provee).

```
CitaRepositoryPort  ◄──── implementa ────  CitaRepositoryAdapter
     (dominio)                                 (infraestructura)
                                                       │
                                                       ▼
                                              CitaJpaRepository
                                              (Spring Data)
```

El servicio de aplicación habla con `CitaRepositoryPort`. El adaptador convierte
esa llamada en el método JPA correspondiente y transforma el resultado
(`CitaEntity` → `Cita` de dominio) usando el mapper de persistencia.

Aplicado en: `CitaRepositoryAdapter`, `PacienteRepositoryAdapter`, `MedicoRepositoryAdapter`,
`PenalizacionRepositoryAdapter`.

---

#### Facade (Fachada)

`CitaService` presenta una interfaz simplificada (`CitaUseCase`) que oculta la complejidad
de coordinar múltiples repositorios, políticas y reglas de dominio en cada operación.

```
CitaController
     │ reservar(request)
     ▼
CitaService.reservar()           ← fachada que coordina:
    ├── pacienteRepository        ← buscar paciente
    ├── medicoRepository          ← verificar médico
    ├── paciente.validar(...)     ← RN-03 dominio
    ├── verificarPacienteNoBloqueado() ← RN-05
    ├── horarioAtencion.esValido() ← RN-01
    ├── verificarMaximoCitasPorDia() ← RN-NEW-1
    ├── citaRepository.existeMedico() ← RN-02
    ├── citaRepository.existePaciente() ← RN-NEW-2
    └── citaRepository.guardar()
```

El controlador no necesita conocer ninguno de estos pasos; solo llama a `reservar()`.

---

#### Mapper (objeto dedicado a conversión)

Cada frontera entre capas tiene su propio mapper responsable de la transformación de
modelos, manteniendo cada capa con su propio contrato de datos:

| Mapper | Transforma |
|---|---|
| `CitaWebMapper` | `ReservarCitaRequest` → `ReservarCitaCommand`, `Cita` → `CitaResponse` |
| `PacienteWebMapper` | `CrearPacienteRequest` → `RegistrarPacienteCommand`, `Paciente` → `PacienteResponse` |
| `MedicoWebMapper` | `CrearMedicoRequest` → `RegistrarMedicoCommand`, `Medico` → `MedicoResponse` |
| `PenalizacionWebMapper` | `Penalizacion` → `PenalizacionResponse` |
| `CitaPersistenceMapper` | `Cita` ↔ `CitaEntity` |
| `PacientePersistenceMapper` | `Paciente` ↔ `PacienteEntity` |
| `MedicoPersistenceMapper` | `Medico` ↔ `MedicoEntity` |
| `PenalizacionPersistenceMapper` | `Penalizacion` ↔ `PenalizacionEntity` |

---

### Patrones de Comportamiento

#### Command (Comando)

Las operaciones de escritura se encapsulan como objetos Command inmutables (records) que
transportan todos los parámetros de la operación, desacoplando quién dispara la acción
de quién la ejecuta.

```java
// El controller construye el comando
ReservarCitaCommand command = mapper.aComando(request);
// El use case lo ejecuta
Cita cita = citaUseCase.reservar(command);
```

| Command | Operación |
|---|---|
| `ReservarCitaCommand` | Reservar una cita |
| `RegistrarMedicoCommand` | Registrar un médico |
| `RegistrarPacienteCommand` | Registrar un paciente |

Los objetos de filtro (`FiltroCitas`, `FiltroPenalizaciones`) aplican el mismo patrón
para las consultas de solo lectura.

---

#### State (Estado)

`EstadoCita` es una máquina de estados que encapsula las transiciones válidas del ciclo
de vida de una cita, evitando que estados ilegales sean posibles en el sistema.

```
PROGRAMADA ──► CANCELADA
PROGRAMADA ──► ATENDIDA
CANCELADA  ──X  (no puede volver a cancelarse)
```

```java
// EstadoCita.java
public boolean permiteCancelacion() {
    return this == PROGRAMADA;
}

// Cita.java
public void cancelar(LocalDateTime ahora) {
    if (!estado.permiteCancelacion()) {
        throw new ReglaNegocioException("Solo se puede cancelar una cita PROGRAMADA; estado: " + estado);
    }
    this.estado = EstadoCita.CANCELADA;
}
```

La lógica de qué transiciones son válidas reside en el enum, no en cada lugar que llame
a `cancelar()`.

---

#### Strategy (Estrategia)

`HorarioAtencion.horaCierre()` selecciona dinámicamente la política de horario según
el día de la semana, variando el comportamiento del algoritmo sin condicionales externos.

```java
public Optional<LocalTime> horaCierre(DayOfWeek dia) {
    return switch (dia) {
        case SATURDAY -> Optional.of(CIERRE_SABADO);     // estrategia: sábado
        case SUNDAY   -> Optional.empty();               // estrategia: cerrado
        default       -> Optional.of(CIERRE_ENTRE_SEMANA); // estrategia: entre semana
    };
}
```

Añadir un nuevo comportamiento (p. ej. festivos) solo requiere extender el switch,
no modificar los métodos que consultan la hora de cierre.

---

#### Template Method (Método Plantilla)

`GlobalExceptionHandler` define el esquema fijo de construcción de respuestas de error
(`construir(HttpStatus, String, HttpServletRequest)`) y cada método `@ExceptionHandler`
especializado llama a esa plantilla con sus propios parámetros.

```java
// Plantilla fija:
private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje, HttpServletRequest req) {
    ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), mensaje, req.getRequestURI());
    return ResponseEntity.status(status).body(body);
}

// Cada handler invoca la plantilla:
@ExceptionHandler(ConflictoException.class)
public ResponseEntity<ErrorResponse> manejarConflicto(ConflictoException ex, HttpServletRequest req) {
    return construir(HttpStatus.CONFLICT, ex.getMessage(), req);  // ← usa la plantilla
}
```

---

#### Repository (Repositorio)

Abstrae el mecanismo de acceso a datos detrás de interfaces orientadas al dominio,
presentando colecciones de objetos de dominio en lugar de consultas SQL o detalles JPA.

```
                 dominio / aplicación                    infraestructura
╔══════════════════════════════════════╗    ╔═════════════════════════════════╗
║ CitaRepositoryPort (interfaz)        ║    ║ CitaRepositoryAdapter           ║
║   + guardar(Cita): Cita             ║◄──║   + CitaJpaRepository           ║
║   + buscarPorId(id): Optional<Cita> ║    ║   + CitaPersistenceMapper       ║
║   + existeCitaProgramadaDeMedico()  ║    ║                                 ║
╚══════════════════════════════════════╝    ╚═════════════════════════════════╝
```

Los métodos del puerto hablan el lenguaje del dominio (`existeCitaProgramadaDeMedico`),
no el lenguaje de la BD (`SELECT COUNT(*) WHERE estado = 'PROGRAMADA' AND ...`).

---

### Patrones de Arquitectura y DDD

#### Value Object (Objeto de Valor)

`FranjaHoraria` es un record inmutable que representa un concepto del dominio sin
identidad propia: dos franjas con el mismo `inicio` y `duracion` son equivalentes.

```java
public record FranjaHoraria(LocalDateTime inicio, Duration duracion) {
    // Validación automática en el compact constructor
    // fin() calculado, no almacenado
    // Sin setters — inmutable
}
```

La inmutabilidad garantiza que una franja no puede "mutar" mientras se usa, evitando
errores sutiles de concurrencia o aliasing.

---

#### Aggregate Root (Raíz de Agregado)

`Cita` es el agregado raíz que controla su propio ciclo de vida y protege sus invariantes.
Todo cambio de estado pasa obligatoriamente por sus métodos, nunca por acceso directo a campos.

```java
// CORRECTO — respeta el agregado:
cita.cancelar(ahora);

// INCORRECTO — no existe esta posibilidad:
cita.setEstado(EstadoCita.CANCELADA); // campo privado, no hay setter
```

---

#### Domain Policy / Specification (Política de Dominio)

Las reglas de negocio complejas que no pertenecen a una entidad específica se encapsulan
en clases de política puras, sin dependencias de framework:

| Política | Regla encapsulada |
|---|---|
| `HorarioAtencion` | Jornada laboral, generación y validación de franjas (RN-01) |
| `PoliticaPenalizacion` | Umbrales de penalización: 2 h mínimo, 3 máx en 30 días (RN-05) |
| `PoliticaAgendamiento` | Límite diario de citas por paciente (RN-NEW-1, configurable) |

Al ser clases sin estado y sin dependencias externas, son **testeables de forma aislada**
y reutilizables en cualquier contexto (batch, workers, etc.).

---

#### Ports and Adapters (Puertos y Adaptadores)

Es el patrón arquitectónico central del proyecto:

- **Puerto de entrada (`port/in`):** interfaz que define *qué* puede hacer la aplicación.
  `CitaUseCase`, `PacienteUseCase`, etc.
- **Puerto de salida (`port/out`):** interfaz que define *qué necesita* la aplicación del
  exterior. `CitaRepositoryPort`, `PenalizacionRepositoryPort`, etc.
- **Adaptador de entrada (`adapter/in/web`):** traduce HTTP → comando de aplicación.
  Los controladores con DTOs y mappers.
- **Adaptador de salida (`adapter/out/persistence`):** traduce llamadas de dominio → JPA.
  Los `*RepositoryAdapter` con Spring Data y mappers de persistencia.

El núcleo (dominio + aplicación) no sabe si está siendo llamado por un REST controller,
un job batch o un test unitario.

---

## 11. Notas

- `data.sql` inserta los médicos con IDs explícitos (1-3); se añade un
  `ALTER TABLE ... RESTART WITH 4` para que los médicos creados vía API no colisionen con
  el contador IDENTITY.
- El enunciado original (`src/main/resources/enunciado-prueba-java.pdf`) y el `schema.sql`
  se conservan en el repositorio como referencia.
- La duración de las franjas (`appointment.duracion-minutos`) debe ser un divisor de 60
  para garantizar que los inicios de franja estén siempre alineados a valores enteros de minuto
  dentro de la hora (15, 20, 30 y 60 son los valores más prácticos).
