package com.devsuj.crmdesktop.view.tablero;

import com.devsuj.crmdesktop.util.Formatos;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.math.BigDecimal;

/**
 * Ayudas comunes para las gráficas del tablero (DRY): eje de quetzales, tooltips con el monto
 * exacto.
 */
public final class Graficas {

    private Graficas() {
    }

    /** Eje numérico que muestra montos abreviados (Q85 k, Q1.2 M). */
    public static void ejeMoneda(NumberAxis eje, String titulo) {
        eje.setLabel(titulo);
        eje.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number valor) {
                return Formatos.monedaCorta(valor.doubleValue());
            }

            @Override
            public Number fromString(String texto) {
                return 0;
            }
        });
    }

    /** Al pasar el ratón por la barra o el punto se ve el valor exacto. */
    public static <X, Y> void tooltip(XYChart.Data<X, Y> dato, String texto) {
        dato.nodeProperty().addListener((obs, antes, nodo) -> {
            if (nodo != null) {
                instalar(nodo, texto);
            }
        });
        if (dato.getNode() != null) {
            instalar(dato.getNode(), texto);
        }
    }

    public static void tooltip(PieChart.Data dato, String texto) {
        dato.nodeProperty().addListener((obs, antes, nodo) -> {
            if (nodo != null) {
                instalar(nodo, texto);
            }
        });
        if (dato.getNode() != null) {
            instalar(dato.getNode(), texto);
        }
    }

    private static void instalar(Node nodo, String texto) {
        Tooltip tooltip = new Tooltip(texto);
        tooltip.setShowDelay(Duration.millis(100));
        Tooltip.install(nodo, tooltip);
    }

    public static double numero(BigDecimal valor) {
        return valor == null ? 0 : valor.doubleValue();
    }
}
