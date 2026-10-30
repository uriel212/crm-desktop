package com.devsuj.crmdesktop.model;

import java.util.Arrays;
import java.util.List;

/**
 * Etapas del pipeline. Coinciden con el enum {@code EtapaOportunidad} de la API.
 *
 * <p>Reglas (las mismas del backend): GANADA solo la pone la API al registrar la venta, y una
 * oportunidad GANADA o PERDIDA ya no cambia (si se retoma, se crea una nueva ligada).</p>
 */
public enum EtapaOportunidad implements ConEtiqueta {
    PROSPECTO("Prospecto"),
    CALIFICADA("Calificada"),
    PROPUESTA("Propuesta"),
    NEGOCIACION("Negociación"),
    GANADA("Ganada"),
    PERDIDA("Perdida");

    private final String etiqueta;

    EtapaOportunidad(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }

    public boolean esCerrada() {
        return this == GANADA || this == PERDIDA;
    }

    /** Etapas del camino "normal" del proceso, en orden (para dibujar el diagrama). */
    public static List<EtapaOportunidad> camino() {
        return List.of(PROSPECTO, CALIFICADA, PROPUESTA, NEGOCIACION, GANADA);
    }

    /** A qué etapas puede pasar desde esta con {@code PATCH /etapa}: cualquier otra abierta o PERDIDA. */
    public List<EtapaOportunidad> destinosPermitidos() {
        if (esCerrada()) {
            return List.of();
        }
        return Arrays.stream(values()).filter(e -> e != this && e != GANADA).toList();
    }
}
