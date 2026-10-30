package com.devsuj.crmdesktop.model;

/** Tipo de descuento de una promoción (coincide con la API). */
public enum TipoPromocion implements ConEtiqueta {
    PORCENTAJE("Porcentaje"),
    MONTO_FIJO("Monto fijo");

    private final String etiqueta;

    TipoPromocion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }
}
