-- =====================================================================
-- DATOS INICIALES — VERSIÓN PARA TESTS (idempotente con MERGE)
-- MERGE INTO ... KEY(id) actúa como upsert: inserta si no existe,
-- actualiza (sin cambios) si ya existe. Permite que varios contextos
-- Spring ejecuten este script sobre el mismo H2 en memoria sin error.
-- =====================================================================

MERGE INTO medicos (id, nombre_completo, especialidad, telefono, email)
    KEY (id)
    VALUES (1, 'Dra. María González', 'Cardiología',   '555-1001', 'maria.gonzalez@medisalud.com'),
           (2, 'Dr. Carlos Ruiz',     'Pediatría',     '555-1002', 'carlos.ruiz@medisalud.com'),
           (3, 'Dra. Ana López',      'Dermatología',  '555-1003', 'ana.lopez@medisalud.com');

-- Reinicia el contador IDENTITY para que los médicos creados vía API
-- no colisionen con los IDs 1-3 ya insertados.
ALTER TABLE medicos ALTER COLUMN id RESTART WITH 4;
