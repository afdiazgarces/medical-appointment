package com.medical.appointment.domain.exception;

/**
 * Conflicto con el estado actual del sistema. Se traduce a HTTP 409 (Conflict).
 * <ul>
 *   <li>RN-02: el médico ya tiene una cita en esa franja.</li>
 *   <li>RN-04: el paciente ya tiene una cita con ese médico en esa franja.</li>
 *   <li>RN-05: el paciente está bloqueado por acumulación de penalizaciones.</li>
 *   <li>Documento de identidad duplicado (restricción UNIQUE).</li>
 * </ul>
 */
public class ConflictoException extends DomainException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
