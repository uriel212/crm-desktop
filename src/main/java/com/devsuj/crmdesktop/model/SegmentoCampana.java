package com.devsuj.crmdesktop.model;

/** A quién va dirigida una campaña (coincide con la API). */
public enum SegmentoCampana implements ConEtiqueta {
    TODOS("Todos (no inactivos)"),
    PROSPECTOS("Prospectos"),
    ACTIVOS("Activos"),
    INACTIVOS("Inactivos"),
    SIN_COMPRA_90_DIAS("Sin compra en 90 días");

    private final String etiqueta;

    SegmentoCampana(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }
}
