package com.devsuj.crmdesktop.model;

/**
 * Estados de un cliente. Coinciden con el ENUM de la columna {@code cliente.estado} de la API.
 * Un "lead" es un cliente en estado {@link #PROSPECTO}.
 */
public enum EstadoCliente implements ConEtiqueta {
    PROSPECTO("Prospecto"),
    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String etiqueta;

    EstadoCliente(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }
}
