package com.medical.appointment.domain.exception;

/**
 * Excepción base de todas las violaciones de reglas del dominio.
 * <p>
 * No depende de Spring ni de la capa web: el adaptador web la traduce a un código
 * HTTP en el {@code GlobalExceptionHandler}.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String mensaje) {
        super(mensaje);
    }
}
