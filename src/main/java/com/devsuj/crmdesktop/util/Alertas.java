package com.devsuj.crmdesktop.util;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

/**
 * Ventanas modales para el usuario: confirmaciones y errores que requieren su atención.
 * Para avisos breves que no interrumpen (por ejemplo "Cliente guardado") usa {@link Notificaciones}.
 */
public final class Alertas {

    private Alertas() {
    }

    /**
     * Pregunta antes de una acción destructiva. El botón por defecto (Enter) es "Cancelar",
     * para que el usuario no confirme por accidente.
     *
     * @return {@code true} si el usuario confirmó
     */
    public static boolean confirmar(Node origen, String titulo, String mensaje, String textoConfirmar) {
        ButtonType confirmar = new ButtonType(textoConfirmar, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, confirmar, cancelar);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.initOwner(origen.getScene().getWindow());
        Button botonConfirmar = (Button) alerta.getDialogPane().lookupButton(confirmar);
        Button botonCancelar = (Button) alerta.getDialogPane().lookupButton(cancelar);
        botonConfirmar.getStyleClass().add("danger"); // rojo: acción destructiva
        botonConfirmar.setDefaultButton(false);
        botonCancelar.setDefaultButton(true);

        return alerta.showAndWait().filter(confirmar::equals).isPresent();
    }

    public static void error(Node origen, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        alerta.setTitle("Error");
        alerta.setHeaderText("No se pudo completar la operación");
        alerta.initOwner(origen.getScene().getWindow());
        alerta.showAndWait();
    }
}
