package com.devsuj.crmdesktop.util;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Construye el mensaje que se muestra cuando una lista está vacía o falló su carga,
 * con una acción para salir de ese estado ("Aún no hay clientes. Crear cliente").
 */
public final class EstadoVacio {

    private EstadoVacio() {
    }

    /**
     * @param icono       literal de Ikonli, por ejemplo "fth-users"
     * @param textoAccion texto del botón; si es null no se muestra botón
     */
    public static Node crear(String icono, String mensaje, String textoAccion, Runnable accion) {
        FontIcon figura = new FontIcon(icono);
        figura.getStyleClass().add("estado-vacio-icono");

        Label texto = new Label(mensaje);
        texto.getStyleClass().add("text-muted");
        texto.setWrapText(true);

        VBox caja = new VBox(12, figura, texto);
        caja.setAlignment(Pos.CENTER);
        caja.getStyleClass().add("estado-vacio");

        if (textoAccion != null) {
            Button boton = new Button(textoAccion);
            boton.setOnAction(evento -> accion.run());
            caja.getChildren().add(boton);
        }
        return caja;
    }
}
