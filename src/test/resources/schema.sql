-- =====================================================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS H2 — VERSIÓN PARA TESTS
-- Usa CREATE TABLE IF NOT EXISTS para que el script sea idempotente:
-- varios contextos Spring pueden ejecutarlo sobre el mismo H2 en memoria
-- sin fallar por "tabla ya existente".
-- =====================================================================

CREATE TABLE IF NOT EXISTS medicos
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(100) NOT NULL CHECK (LENGTH(nombre_completo) >= 3),
    especialidad    VARCHAR(100) NOT NULL,
    telefono        VARCHAR(20) CHECK (LENGTH(telefono) >= 7),
    email           VARCHAR(255) CHECK (email LIKE '%@%.%')
);

CREATE TABLE IF NOT EXISTS pacientes
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo     VARCHAR(100) NOT NULL CHECK (LENGTH(nombre_completo) >= 3),
    documento_identidad VARCHAR(50)  NOT NULL UNIQUE CHECK (LENGTH(documento_identidad) >= 7),
    telefono            VARCHAR(20)  NOT NULL CHECK (LENGTH(telefono) >= 7),
    email               VARCHAR(255) NOT NULL CHECK (email LIKE '%@%.%'),
    fecha_nacimiento    DATE
);

CREATE TABLE IF NOT EXISTS citas
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id       BIGINT    NOT NULL,
    medico_id         BIGINT    NOT NULL,
    fecha_hora        TIMESTAMP NOT NULL,
    estado            VARCHAR(20) DEFAULT 'PROGRAMADA' CHECK (estado IN ('PROGRAMADA', 'CANCELADA', 'ATENDIDA')),
    fecha_cancelacion TIMESTAMP,
    FOREIGN KEY (paciente_id) REFERENCES pacientes (id),
    FOREIGN KEY (medico_id)   REFERENCES medicos (id)
);

CREATE TABLE IF NOT EXISTS penalizaciones
(
    id                 BIGINT    AUTO_INCREMENT PRIMARY KEY,
    paciente_id        BIGINT    NOT NULL,
    cita_id            BIGINT    NOT NULL,
    fecha_penalizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (paciente_id) REFERENCES pacientes (id),
    FOREIGN KEY (cita_id)     REFERENCES citas (id)
);
