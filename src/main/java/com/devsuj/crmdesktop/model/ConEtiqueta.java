package com.devsuj.crmdesktop.model;

/**
 * Valor (normalmente un enum) que sabe cómo mostrarse en la interfaz.
 * Gracias a esta interfaz, las celdas y combos genéricos sirven para cualquier enum (DRY).
 */
public interface ConEtiqueta {

    /** Texto amigable para mostrar en pantalla (por ejemplo "En negociación"). */
    String etiqueta();
}
