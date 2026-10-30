package com.devsuj.crmdesktop.model;

/** Canal de una campaña (coincide con la API). No se envían mensajes reales. */
public enum CanalCampana implements ConEtiqueta {
    CORREO("Correo"),
    SMS("SMS"),
    REDES("Redes sociales");

    private final String etiqueta;

    CanalCampana(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }
}
