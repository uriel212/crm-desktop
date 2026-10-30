package com.devsuj.crmdesktop.util;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Tema visual de AtlantaFX (Primer) en modo claro u oscuro.
 *
 * <p>Basta con cambiar la propiedad {@link #oscuroProperty()}: el listener aplica el tema.
 * Así un botón de la interfaz puede enlazarse ({@code bind}) a ella sin código extra.</p>
 */
public class Tema {

    private final BooleanProperty oscuro = new SimpleBooleanProperty(false);

    public Tema() {
        oscuro.addListener((obs, antes, ahora) -> aplicar());
    }

    public void aplicar() {
        Application.setUserAgentStylesheet(oscuro.get()
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());
    }

    public BooleanProperty oscuroProperty() {
        return oscuro;
    }
}
