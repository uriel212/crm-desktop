package com.devsuj.crmdesktop.model;

/** Prioridad de un ticket de soporte (coincide con la API). */
public enum PrioridadTicket implements ConEtiqueta {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta");

    private final String etiqueta;

    PrioridadTicket(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }
}
