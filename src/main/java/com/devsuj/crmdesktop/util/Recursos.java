package com.devsuj.crmdesktop.util;

import com.devsuj.crmdesktop.CrmApplication;

import java.net.URL;
import java.util.Objects;

/**
 * Localiza archivos de {@code src/main/resources/com/devsuj/crmdesktop/} (FXML, CSS).
 */
public final class Recursos {

    private Recursos() {
    }

    /** @param ruta relativa al paquete base, por ejemplo "view/main-view.fxml" */
    public static URL url(String ruta) {
        return Objects.requireNonNull(CrmApplication.class.getResource(ruta),
                "No se encontró el recurso: " + ruta);
    }
}
