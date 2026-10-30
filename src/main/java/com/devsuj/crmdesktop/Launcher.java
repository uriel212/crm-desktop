package com.devsuj.crmdesktop;

/**
 * Punto de entrada de la aplicación.
 *
 * <p>No extiende {@link javafx.application.Application} a propósito: si la clase
 * con {@code main} extendiera Application, la JVM exigiría tener JavaFX en el
 * module path y al ejecutarla desde el IDE fallaría con
 * "JavaFX runtime components are missing".</p>
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        CrmApplication.main(args);
    }
}
