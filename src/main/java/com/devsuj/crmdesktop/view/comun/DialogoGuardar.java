package com.devsuj.crmdesktop.view.comun;

import com.devsuj.crmdesktop.util.Recursos;
import com.devsuj.crmdesktop.util.TareaEnSegundoPlano;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Ventana modal con un formulario y los botones "Guardar" y "Cancelar". La usan todos los módulos.
 *
 * <p>"Guardar" no cierra el diálogo de inmediato: primero guarda en segundo plano (la ventana no se
 * congela) y solo cierra si la API respondió bien. Si hubo errores, el diálogo sigue abierto con los
 * campos marcados para que el usuario los corrija sin volver a escribir todo.
 * Enter = Guardar, Esc = Cancelar.</p>
 */
public final class DialogoGuardar {

    private DialogoGuardar() {
    }

    /**
     * Versión para formularios hechos con {@link Formulario}.
     *
     * @param preparar se ejecuta en el hilo de JavaFX al pulsar Guardar: lee los campos y devuelve
     *                 el trabajo que se hará en segundo plano (por ejemplo {@code () -> service.guardar(datos)})
     */
    public static <T> Optional<T> mostrar(Window ventana, String titulo, String encabezado, Formulario formulario,
                                          String textoGuardar, Supplier<Callable<T>> preparar) {
        return mostrar(ventana, titulo, encabezado, formulario.raiz(), textoGuardar,
                () -> {
                    formulario.prepararEnvio();
                    return preparar.get();
                },
                formulario::mostrarError, formulario::activarValidaciones);
    }

    /**
     * Versión general (por ejemplo, para formularios definidos en FXML).
     *
     * @param alFallar  muestra el error en el formulario
     * @param alMostrar se ejecuta cuando la ventana ya está abierta (activar validaciones, enfocar)
     */
    public static <T> Optional<T> mostrar(Window ventana, String titulo, String encabezado, Parent contenido,
                                          String textoGuardar, Supplier<Callable<T>> preparar,
                                          Consumer<Throwable> alFallar, Runnable alMostrar) {
        Dialog<T> dialogo = new Dialog<>();
        dialogo.initOwner(ventana);
        dialogo.setTitle(titulo);
        dialogo.setHeaderText(encabezado);
        dialogo.setResizable(true);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().getStylesheets().add(Recursos.url("css/styles.css").toExternalForm());

        ButtonType guardar = new ButtonType(textoGuardar, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().setAll(guardar, cancelar);
        dialogo.setResultConverter(boton -> null); // solo "Guardar" (abajo) produce un resultado

        BooleanProperty guardando = new SimpleBooleanProperty(false);
        Button botonGuardar = (Button) dialogo.getDialogPane().lookupButton(guardar);
        botonGuardar.getStyleClass().add("accent");
        botonGuardar.disableProperty().bind(guardando);
        botonGuardar.textProperty().bind(Bindings.when(guardando).then("Guardando…").otherwise(textoGuardar));

        // addEventFilter + consume(): evitamos que el botón cierre el diálogo automáticamente.
        botonGuardar.addEventFilter(ActionEvent.ACTION, evento -> {
            evento.consume();
            Callable<T> trabajo;
            try {
                trabajo = preparar.get(); // se lee el formulario en el hilo de JavaFX
            } catch (RuntimeException e) {
                alFallar.accept(e);
                return;
            }
            TareaEnSegundoPlano.ejecutar(trabajo, dialogo::setResult, alFallar, guardando);
        });

        // Dialog avisa "shown" justo ANTES de abrir la ventana; runLater espera a que ya esté abierta.
        dialogo.setOnShown(evento -> Platform.runLater(alMostrar));
        return dialogo.showAndWait();
    }
}
