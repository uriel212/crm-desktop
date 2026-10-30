package com.devsuj.crmdesktop.model;

import java.util.Map;
import java.util.Set;

/** Estados de un contrato. Transiciones iguales a la API: BORRADOR → VIGENTE | CANCELADO; VIGENTE → VENCIDO | CANCELADO. */
public enum EstadoContrato implements ConEtiqueta {
    BORRADOR("Borrador"),
    VIGENTE("Vigente"),
    VENCIDO("Vencido"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoContrato(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }

    private static final Map<EstadoContrato, Set<EstadoContrato>> TRANSICIONES = Map.of(
            BORRADOR, Set.of(VIGENTE, CANCELADO),
            VIGENTE, Set.of(VENCIDO, CANCELADO),
            VENCIDO, Set.of(),
            CANCELADO, Set.of());

    public boolean puedeCambiarA(EstadoContrato destino) {
        return destino != null && TRANSICIONES.get(this).contains(destino);
    }
}
