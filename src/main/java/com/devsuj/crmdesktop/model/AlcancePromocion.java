package com.devsuj.crmdesktop.model;

/** Alcance de una promoción: por producto (renglón) o sobre el total de la venta. */
public enum AlcancePromocion implements ConEtiqueta {
    PRODUCTO("Por producto"),
    VENTA("Sobre el total");

    private final String etiqueta;

    AlcancePromocion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }
}
