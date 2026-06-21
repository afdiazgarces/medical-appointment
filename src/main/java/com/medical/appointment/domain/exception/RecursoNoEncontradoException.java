package com.medical.appointment.domain.exception;

/**
 * Se lanza cuando se referencia una entidad inexistente (médico, paciente o cita).
 * <p>Se traduce a HTTP 404 (Not Found).
 */
public class RecursoNoEncontradoException extends DomainException {

    public RecursoNoEncontradoException(String recurso, Object id) {
        super("%s con id %s no encontrado".formatted(recurso, id));
    }
}
