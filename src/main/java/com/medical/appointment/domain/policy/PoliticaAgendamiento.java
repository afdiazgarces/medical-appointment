package com.medical.appointment.domain.policy;

/**
 * Política de límites de agendamiento por paciente.
 * <p>
 * Encapsula las reglas que restringen cuántas citas puede tener un paciente
 * en un día. El límite se inyecta desde configuración para evitar valores quemados.
 */
public final class PoliticaAgendamiento {

    private final int maxCitasPorPacientePorDia;

    public PoliticaAgendamiento(int maxCitasPorPacientePorDia) {
        if (maxCitasPorPacientePorDia <= 0) {
            throw new IllegalArgumentException(
                    "El máximo de citas por paciente por día debe ser positivo; valor: " + maxCitasPorPacientePorDia);
        }
        this.maxCitasPorPacientePorDia = maxCitasPorPacientePorDia;
    }

    /** {@code true} si la cantidad de citas en el día alcanza o supera el límite configurado. */
    public boolean superaMaximoDiario(long citasEnElDia) {
        return citasEnElDia >= maxCitasPorPacientePorDia;
    }

    public int getMaxCitasPorPacientePorDia() {
        return maxCitasPorPacientePorDia;
    }
}
