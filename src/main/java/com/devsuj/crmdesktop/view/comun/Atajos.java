package com.devsuj.crmdesktop.view.comun;

import javafx.scene.Node;
import javafx.scene.input.KeyCombination;

import java.util.Map;

/**
 * Atajos de teclado que solo valen mientras una pantalla está visible: se agregan a la escena al
 * mostrarla y se quitan al salir de ella (así Ctrl+N no crea un cliente estando en Productos).
 */
public final class Atajos {

    private Atajos() {
    }

    /** @param nodo cualquier nodo de la pantalla (cuando entra o sale de la escena se ponen o quitan) */
    public static void instalar(Node nodo, Map<KeyCombination, Runnable> atajos) {
        nodo.sceneProperty().addListener((obs, escenaAnterior, escenaNueva) -> {
            if (escenaAnterior != null) {
                atajos.keySet().forEach(escenaAnterior.getAccelerators()::remove);
            }
            if (escenaNueva != null) {
                escenaNueva.getAccelerators().putAll(atajos);
            }
        });
    }
}
