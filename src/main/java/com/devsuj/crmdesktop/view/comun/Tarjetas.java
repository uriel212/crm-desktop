package com.devsuj.crmdesktop.view.comun;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

/** Tarjetas de indicador (KPI) con el mismo aspecto en el tablero, fidelización y la vista 360. */
public final class Tarjetas {

    private Tarjetas() {
    }

    /** Ícono y título arriba, valor grande al centro y un detalle abajo. */
    public static Node kpi(String icono, String titulo, String valor, String detalle) {
        FontIcon figura = new FontIcon(icono);
        figura.getStyleClass().add("kpi-icono");
        Label etiquetaTitulo = new Label(titulo, figura);
        etiquetaTitulo.getStyleClass().addAll("text-muted", "kpi-titulo");
        Label etiquetaValor = new Label(valor);
        etiquetaValor.getStyleClass().add("kpi-valor");
        Label etiquetaDetalle = new Label(detalle);
        etiquetaDetalle.getStyleClass().addAll("text-small", "text-muted");
        etiquetaDetalle.setWrapText(true);
        VBox tarjeta = new VBox(6, etiquetaTitulo, etiquetaValor, etiquetaDetalle);
        tarjeta.getStyleClass().add("kpi");
        return tarjeta;
    }
}
