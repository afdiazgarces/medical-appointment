package com.medical.appointment.domain.exception;

/**
 * Violación de una regla de negocio cuyo origen es una entrada inválida del cliente
 * (p. ej. horario fuera de la jornada laboral RN-01, fecha de nacimiento futura RN-03,
 * paciente bloqueado por penalizaciones RN-05).
 * <p>Se traduce a HTTP 400 (Bad Request).
 */
public class ReglaNegocioException extends DomainException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
