package com.devsuj.crmdesktop.model;

/** Estados de una interacción: PROGRAMADA (agenda) pasa a REALIZADA o CANCELADA. Coinciden con la API. */
public enum EstadoInteraccion implements ConEtiqueta {
    PROGRAMADA("Programada"),
    REALIZADA("Realizada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoInteraccion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }
}
