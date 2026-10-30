package com.devsuj.crmdesktop.util;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

/**
 * Cambia la pantalla que se ve en el área de contenido de la ventana principal.
 *
 * <p>Usa la misma fábrica de controladores que el resto de la app, así cada pantalla recibe sus
 * dependencias por constructor (inversión de dependencias).</p>
 */
public class Navegador {

    private final Pane contenedor;
    private final Callback<Class<?>, Object> fabricaDeControladores;

    public Navegador(Pane contenedor, Callback<Class<?>, Object> fabricaDeControladores) {
        this.contenedor = contenedor;
        this.fabricaDeControladores = fabricaDeControladores;
    }

    /** Carga un FXML (ruta relativa al paquete base, ej. "view/clientes/clientes-view.fxml") y lo muestra. */
    public void mostrar(String rutaFxml) {
        mostrar(cargar(rutaFxml));
    }

    public void mostrar(Node pantalla) {
        contenedor.getChildren().setAll(pantalla);
    }

    private Node cargar(String rutaFxml) {
        return Vistas.cargar(rutaFxml, fabricaDeControladores).raiz();
    }
}
