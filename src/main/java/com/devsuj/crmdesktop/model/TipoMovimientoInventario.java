package com.devsuj.crmdesktop.model;

/** Tipos de movimiento del kardex. ENTRADA y SALIDA son manuales; VENTA y CANJE los genera la API. */
public enum TipoMovimientoInventario implements ConEtiqueta {
    ENTRADA("Entrada"),
    SALIDA("Salida"),
    VENTA("Venta"),
    CANJE("Canje");

    private final String etiqueta;

    TipoMovimientoInventario(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }
}
