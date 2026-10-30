package com.devsuj.crmdesktop.model;

/** Roles de usuario. Coinciden con el enum {@code Rol} de la API. */
public enum Rol implements ConEtiqueta {
    VENDEDOR("Vendedor"),
    GERENTE("Gerente"),
    ADMIN("Administrador");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String etiqueta() {
        return etiqueta;
    }
}
