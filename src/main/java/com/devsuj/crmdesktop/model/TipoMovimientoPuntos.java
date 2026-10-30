package com.devsuj.crmdesktop.model;

/** Movimiento del estado de cuenta de puntos. Coincide con el enum {@code TipoMovimientoPuntos} de la API. */
public enum TipoMovimientoPuntos implements ConEtiqueta {
    GANADO("Ganado"),
    CANJEADO("Canjeado");

    private final String etiqueta;

    TipoMovimientoPuntos(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }
}
