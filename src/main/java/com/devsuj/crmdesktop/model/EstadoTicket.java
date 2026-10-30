package com.devsuj.crmdesktop.model;

import java.util.Map;
import java.util.Set;

/** Estados de un ticket: ABIERTO ↔ EN_PROCESO → CERRADO (también ABIERTO → CERRADO). Un CERRADO no se reabre. */
public enum EstadoTicket implements ConEtiqueta {
    ABIERTO("Abierto"),
    EN_PROCESO("En proceso"),
    CERRADO("Cerrado");

    private final String etiqueta;

    EstadoTicket(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }

    private static final Map<EstadoTicket, Set<EstadoTicket>> TRANSICIONES = Map.of(
            ABIERTO, Set.of(EN_PROCESO, CERRADO),
            EN_PROCESO, Set.of(ABIERTO, CERRADO),
            CERRADO, Set.of());

    public boolean puedeCambiarA(EstadoTicket destino) {
        return destino != null && TRANSICIONES.get(this).contains(destino);
    }
}
