package com.devsuj.crmdesktop.service;

import java.util.Map;

/**
 * Los datos de un formulario no cumplen las reglas. Se detecta en la app ANTES de llamar a la API,
 * así el usuario recibe la respuesta al instante.
 */
public class ValidacionException extends RuntimeException {

    private final Map<String, String> erroresPorCampo;

    public ValidacionException(Map<String, String> erroresPorCampo) {
        super("Revisa los campos marcados.");
        this.erroresPorCampo = Map.copyOf(erroresPorCampo);
    }

    public Map<String, String> erroresPorCampo() {
        return erroresPorCampo;
    }
}
