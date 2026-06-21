package com.medical.appointment.domain.model;

import com.medical.appointment.domain.exception.ReglaNegocioException;
import lombok.Getter;

import java.time.LocalDate;
import java.time.Period;

/**
 * Paciente que reserva citas (RF-02).
 * <p>
 * La fecha de nacimiento es opcional. La regla de edad (RN-03) se evalúa al agendar,
 * no al registrar: si no hay fecha de nacimiento se asume edad 0, y nunca se admite
 * una fecha futura.
 */
@Getter
public class Paciente {

    public static final int NOMBRE_MIN = 3;
    public static final int NOMBRE_MAX = 100;
    public static final int DOCUMENTO_MIN = 7;
    public static final int EDAD_MINIMA = 0;

    private final Long id;
    private final String nombreCompleto;
    private final String documentoIdentidad;
    private final String telefono;
    private final String email;
    private final LocalDate fechaNacimiento;

    private Paciente(Long id, String nombreCompleto, String documentoIdentidad,
                     String telefono, String email, LocalDate fechaNacimiento) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.documentoIdentidad = documentoIdentidad;
        this.telefono = telefono;
        this.email = email;
        this.fechaNacimiento = fechaNacimiento;
    }

    /** Crea un paciente nuevo (sin id), validando sus invariantes de dominio. */
    public static Paciente crear(String nombreCompleto, String documentoIdentidad,
                                 String telefono, String email, LocalDate fechaNacimiento) {
        String nombre = obligatorio(nombreCompleto, "El nombre del paciente");
        if (nombre.length() < NOMBRE_MIN || nombre.length() > NOMBRE_MAX) {
            throw new ReglaNegocioException(
                    "El nombre del paciente debe tener entre %d y %d caracteres".formatted(NOMBRE_MIN, NOMBRE_MAX));
        }
        String documento = obligatorio(documentoIdentidad, "El documento de identidad");
        if (documento.length() < DOCUMENTO_MIN) {
            throw new ReglaNegocioException(
                    "El documento de identidad debe tener al menos %d caracteres".formatted(DOCUMENTO_MIN));
        }
        String tel = obligatorio(telefono, "El teléfono");
        String correo = obligatorio(email, "El email");
        return new Paciente(null, nombre, documento, tel, correo, fechaNacimiento);
    }

    /** Reconstruye un paciente existente desde persistencia (datos ya validados). */
    public static Paciente reconstituir(Long id, String nombreCompleto, String documentoIdentidad,
                                        String telefono, String email, LocalDate fechaNacimiento) {
        return new Paciente(id, nombreCompleto, documentoIdentidad, telefono, email, fechaNacimiento);
    }

    /**
     * Edad del paciente en años a la fecha indicada (RN-03). Si no se registró fecha
     * de nacimiento se asume edad 0.
     */
    public int edadEn(LocalDate referencia) {
        if (fechaNacimiento == null) {
            return EDAD_MINIMA;
        }
        return Period.between(fechaNacimiento, referencia).getYears();
    }

    /**
     * Verifica que el paciente puede agendar según RN-03: la fecha de nacimiento, si
     * existe, no puede ser futura respecto a la fecha indicada.
     */
    public void validarAptoParaAgendar(LocalDate referencia) {
        if (fechaNacimiento != null && fechaNacimiento.isAfter(referencia)) {
            throw new ReglaNegocioException("La fecha de nacimiento no puede ser futura");
        }
    }

    private static String obligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException(campo + " es obligatorio");
        }
        return valor.trim();
    }
}
