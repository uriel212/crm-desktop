package com.devsuj.crmdesktop.model;

/** Tipos de interacción. Coinciden con el enum {@code TipoInteraccion} de la API. */
public enum TipoInteraccion implements ConEtiqueta {
    LLAMADA("Llamada"),
    CORREO("Correo"),
    REUNION("Reunión"),
    NOTA("Nota");

    private final String etiqueta;

    TipoInteraccion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }
}
