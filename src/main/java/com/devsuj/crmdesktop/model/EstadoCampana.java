package com.devsuj.crmdesktop.model;

/** Estados de una campaña (coinciden con la API). FINALIZADA y CANCELADA ya no se editan ni generan destinatarios. */
public enum EstadoCampana implements ConEtiqueta {
    PLANEADA("Planeada"),
    ACTIVA("Activa"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoCampana(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }

    public boolean estaCerrada() {
        return this == FINALIZADA || this == CANCELADA;
    }
}
