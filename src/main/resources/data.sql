-- ==========================================
-- CARGA DE DATOS INICIALES (RF-01)
-- ==========================================

INSERT INTO medicos (id, nombre_completo, especialidad, telefono, email)
VALUES (1, 'Dra. María González', 'Cardiología', '555-1001', 'maria.gonzalez@medisalud.com'),
       (2, 'Dr. Carlos Ruiz', 'Pediatría', '555-1002', 'carlos.ruiz@medisalud.com'),
       (3, 'Dra. Ana López', 'Dermatología', '555-1003', 'ana.lopez@medisalud.com');

-- Reinicia el contador IDENTITY tras la carga con IDs explícitos, para que los
-- médicos creados vía API (RF-01) no colisionen con los IDs 1-3 ya insertados.
ALTER TABLE medicos ALTER COLUMN id RESTART WITH 4;