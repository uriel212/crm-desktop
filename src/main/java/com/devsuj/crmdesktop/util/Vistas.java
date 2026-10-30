package com.devsuj.crmdesktop.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Callback;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Carga archivos FXML en un solo lugar (DRY): todas las pantallas y formularios pasan por aquí.
 */
public final class Vistas {

    private Vistas() {
    }

    /**
     * @param rutaFxml relativa al paquete base, por ejemplo "view/login/login-view.fxml"
     * @param fabrica  crea el controlador que pide el FXML (inyección de dependencias)
     */
    public static <T extends Parent> Cargada<T> cargar(String rutaFxml, Callback<Class<?>, Object> fabrica) {
        FXMLLoader loader = new FXMLLoader(Recursos.url(rutaFxml));
        loader.setControllerFactory(fabrica);
        try {
            T raiz = loader.load();
            return new Cargada<>(raiz, loader.getController());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar la pantalla " + rutaFxml, e);
        }
    }

    /** Carga un FXML cuyo controlador ya está creado (por ejemplo, porque necesita el registro a editar). */
    public static <T extends Parent> T cargarCon(String rutaFxml, Object controlador) {
        return Vistas.<T>cargar(rutaFxml, tipo -> controlador).raiz();
    }

    /** Resultado de cargar un FXML: la raíz y su controlador. */
    public record Cargada<T extends Parent>(T raiz, Object controlador) {
    }
}
