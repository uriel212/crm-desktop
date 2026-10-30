package com.devsuj.crmdesktop.util;

import atlantafx.base.controls.Notification;
import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Avisos breves ("toasts") en la esquina inferior derecha de la ventana principal.
 *
 * <p>Usan el control {@code Notification} de AtlantaFX: fondo sólido, colores del tema
 * (claro u oscuro) y un ícono según el tipo de mensaje. Se cierran solos a los 3 segundos
 * o con su botón de cerrar; si el ratón está encima, esperan a que salga.</p>
 *
 * <p>Se dibujan en el contenedor con la clase CSS {@code capa-notificaciones} que está en
 * {@code main-view.fxml}, encima del contenido. Todos los módulos deben usar esta clase para sus
 * avisos (consistencia y DRY).</p>
 */
public final class Notificaciones {

    private static final Duration TIEMPO_VISIBLE = Duration.seconds(3);
    private static final Duration ANIMACION = Duration.millis(250);
    private static final double ANCHO = 380;
    private static final String CAPA = ".capa-notificaciones";

    /** Tipo de aviso: define el ícono (Feather) y el color (clase de AtlantaFX). */
    public enum Tipo {
        EXITO("fth-check-circle", Styles.SUCCESS),
        ADVERTENCIA("fth-alert-triangle", Styles.WARNING),
        ERROR("fth-x-circle", Styles.DANGER);

        private final String icono;
        private final String estilo;

        Tipo(String icono, String estilo) {
            this.icono = icono;
            this.estilo = estilo;
        }
    }

    private Notificaciones() {
    }

    public static void exito(Node origen, String mensaje) {
        mostrar(origen, Tipo.EXITO, mensaje);
    }

    public static void advertencia(Node origen, String mensaje) {
        mostrar(origen, Tipo.ADVERTENCIA, mensaje);
    }

    public static void error(Node origen, String mensaje) {
        mostrar(origen, Tipo.ERROR, mensaje);
    }

    /** @param origen cualquier nodo de la ventana (por ejemplo, la tabla de la pantalla actual) */
    public static void mostrar(Node origen, Tipo tipo, String mensaje) {
        Pane capa = buscarCapa(origen);
        if (capa == null) {
            // No debería pasar en la app; por si acaso, el mensaje no se pierde.
            new Alert(tipo == Tipo.EXITO ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING, mensaje).show();
            return;
        }

        Notification aviso = new Notification(mensaje, new FontIcon(tipo.icono));
        aviso.getStyleClass().addAll(tipo.estilo, Styles.ELEVATED_2);
        aviso.setPrefWidth(ANCHO);
        aviso.setMaxWidth(ANCHO);
        aviso.setMaxHeight(Region.USE_PREF_SIZE);

        PauseTransition espera = new PauseTransition(TIEMPO_VISIBLE);
        espera.setOnFinished(evento -> cerrar(capa, aviso));
        aviso.setOnClose(evento -> {
            espera.stop();
            cerrar(capa, aviso);
        });
        aviso.setOnMouseEntered(evento -> espera.pause());
        aviso.setOnMouseExited(evento -> espera.play());

        capa.getChildren().add(aviso);
        Animations.slideInUp(aviso, ANIMACION).playFromStart();
        espera.playFromStart();
    }

    private static void cerrar(Pane capa, Notification aviso) {
        if (aviso.isDisabled()) {
            return; // ya se está cerrando (por tiempo y por botón a la vez)
        }
        aviso.setDisable(true);
        Timeline salida = Animations.fadeOut(aviso, ANIMACION);
        salida.setOnFinished(evento -> capa.getChildren().remove(aviso));
        salida.playFromStart();
    }

    /** Busca la capa en la ventana del nodo o, si es un diálogo, en la ventana que lo abrió. */
    private static Pane buscarCapa(Node origen) {
        Scene escena = origen.getScene();
        if (escena == null) {
            return null;
        }
        Node capa = escena.lookup(CAPA);
        if (capa == null && escena.getWindow() instanceof Stage ventana
                && ventana.getOwner() != null && ventana.getOwner().getScene() != null) {
            capa = ventana.getOwner().getScene().lookup(CAPA);
        }
        return capa instanceof Pane panel ? panel : null;
    }
}
