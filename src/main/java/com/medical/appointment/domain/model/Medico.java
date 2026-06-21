package com.medical.appointment.domain.model;

import com.medical.appointment.domain.exception.ReglaNegocioException;
import lombok.Getter;

/**
 * Médico que ofrece franjas de atención (RF-01).
 * <p>
 * Entidad de dominio pura. Protege sus invariantes estructurales (campos obligatorios
 * y longitud del nombre) en la creación; la validación de formato fino de teléfono/email
 * se delega al borde (Bean Validation en los DTO) y a los CHECK de {@code schema.sql}.
 */
@Getter
public class Medico {

    public static final int NOMBRE_MIN = 3;
    public static final int NOMBRE_MAX = 100;

    private final Long id;
    private final String nombreCompleto;
    private final String especialidad;
    private final String telefono;
    private final String email;

    private Medico(Long id, String nombreCompleto, String especialidad, String telefono, String email) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.email = email;
    }

    /** Crea un médico nuevo (sin id), validando sus invariantes de dominio. */
    public static Medico crear(String nombreCompleto, String especialidad, String telefono, String email) {
        String nombre = normalizarObligatorio(nombreCompleto, "El nombre del médico");
        if (nombre.length() < NOMBRE_MIN || nombre.length() > NOMBRE_MAX) {
            throw new ReglaNegocioException(
                    "El nombre del médico debe tener entre %d y %d caracteres".formatted(NOMBRE_MIN, NOMBRE_MAX));
        }
        String especialidadNorm = normalizarObligatorio(especialidad, "La especialidad");
        return new Medico(null, nombre, especialidadNorm, normalizarOpcional(telefono), normalizarOpcional(email));
    }

    /** Reconstruye un médico existente desde persistencia (datos ya validados). */
    public static Medico reconstituir(Long id, String nombreCompleto, String especialidad,
                                      String telefono, String email) {
        return new Medico(id, nombreCompleto, especialidad, telefono, email);
    }

    private static String normalizarObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException(campo + " es obligatorio");
        }
        return valor.trim();
    }

    private static String normalizarOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
