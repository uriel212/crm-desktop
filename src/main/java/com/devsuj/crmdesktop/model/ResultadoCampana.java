package com.devsuj.crmdesktop.model;

/** Resultado de un destinatario: ENVIADO → ABIERTO → RESPONDIO (solo avanza). */
public enum ResultadoCampana implements ConEtiqueta {
    ENVIADO("Enviado"),
    ABIERTO("Abierto"),
    RESPONDIO("Respondió");

    private final String etiqueta;

    ResultadoCampana(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }

    /** Igual que en la API: solo se puede pasar a un resultado posterior. */
    public boolean puedeCambiarA(ResultadoCampana destino) {
        return destino != null && destino.ordinal() > ordinal();
    }
}
