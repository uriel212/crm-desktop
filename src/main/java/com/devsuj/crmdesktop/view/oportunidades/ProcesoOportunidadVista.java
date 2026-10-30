package com.devsuj.crmdesktop.view.oportunidades;

import com.devsuj.crmdesktop.model.EtapaOportunidad;
import com.devsuj.crmdesktop.model.HistorialEtapa;
import com.devsuj.crmdesktop.service.OportunidadService;
import com.devsuj.crmdesktop.service.OportunidadService.EstadoPaso;
import com.devsuj.crmdesktop.util.Formatos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dibuja el proceso de una oportunidad: el diagrama de etapas (qué etapas recorrió, en cuál está
 * y cuáles faltan) y la línea de tiempo con fechas, días en cada etapa, usuario y comentario.
 *
 * <p>Solo construye nodos a partir de los datos; qué etapa está en qué estado lo calcula
 * {@link OportunidadService#diagrama} (sin JavaFX, probado con JUnit).</p>
 */
public final class ProcesoOportunidadVista {

    private ProcesoOportunidadVista() {
    }

    /** Pasos en fila (se acomodan en varias filas si el panel es angosto). */
    public static Node diagrama(EtapaOportunidad actual, List<HistorialEtapa> historial) {
        Map<EtapaOportunidad, EstadoPaso> pasos = OportunidadService.diagrama(actual, historial);
        FlowPane fila = new FlowPane(6, 6);
        fila.setAlignment(Pos.CENTER_LEFT);
        boolean primero = true;
        for (EtapaOportunidad etapa : EtapaOportunidad.camino()) {
            if (!primero) {
                fila.getChildren().add(new FontIcon("fth-chevron-right"));
            }
            primero = false;
            Label paso = new Label(etapa.etiqueta());
            paso.getStyleClass().addAll("paso", "paso-" + pasos.get(etapa).name().toLowerCase(Locale.ROOT));
            if (pasos.get(etapa) == EstadoPaso.ACTUAL) {
                paso.getStyleClass().add("etapa-" + etapa.name().toLowerCase(Locale.ROOT));
            }
            fila.getChildren().add(paso);
        }
        if (actual == EtapaOportunidad.PERDIDA) {
            Label perdida = new Label("Perdida");
            perdida.getStyleClass().addAll("paso", "paso-actual", "etapa-perdida");
            perdida.setGraphic(new FontIcon("fth-x-circle"));
            fila.getChildren().addAll(new FontIcon("fth-corner-down-right"), perdida);
        }
        return fila;
    }

    /** Un renglón por cada paso del historial, del más antiguo al más reciente. */
    public static Node lineaDeTiempo(List<HistorialEtapa> historial) {
        VBox linea = new VBox(0);
        linea.getStyleClass().add("linea-tiempo");
        for (int i = 0; i < historial.size(); i++) {
            linea.getChildren().add(renglon(historial.get(i), i == historial.size() - 1));
        }
        return linea;
    }

    private static Node renglon(HistorialEtapa paso, boolean ultimo) {
        Region punto = new Region();
        punto.getStyleClass().addAll("punto-tiempo", "etapa-" + paso.etapa().name().toLowerCase(Locale.ROOT));
        Region conector = new Region();
        conector.getStyleClass().add(ultimo ? "conector-fin" : "conector-tiempo");
        VBox.setVgrow(conector, Priority.ALWAYS);
        VBox guia = new VBox(punto, conector);
        guia.setAlignment(Pos.TOP_CENTER);
        guia.setMinWidth(18);

        Label titulo = new Label(paso.etapaAnterior() == null
                ? "Creada en " + paso.etapa().etiqueta()
                : paso.etapaAnterior().etiqueta() + " → " + paso.etapa().etiqueta());
        titulo.getStyleClass().add("text-bold");
        Label fechas = new Label(Formatos.fechaHora(paso.fechaInicio())
                + (paso.fechaFin() == null ? (paso.actual() ? " · hasta hoy" : "") : " → " + Formatos.fechaHora(paso.fechaFin()))
                + (paso.diasEnEtapa() == null ? "" : " · " + dias(paso.diasEnEtapa())));
        fechas.getStyleClass().addAll("text-muted", "text-small");
        Label usuario = new Label("Por " + Formatos.textoOGuion(paso.usuarioNombre()));
        usuario.getStyleClass().add("text-small");
        VBox texto = new VBox(2, titulo, fechas, usuario);
        if (paso.comentario() != null && !paso.comentario().isBlank()) {
            Label comentario = new Label("“" + paso.comentario() + "”");
            comentario.setWrapText(true);
            comentario.getStyleClass().addAll("text-small", "comentario-tiempo");
            texto.getChildren().add(comentario);
        }
        if (paso.actual()) {
            Label actual = new Label("Etapa actual");
            actual.getStyleClass().addAll("etiqueta", "etapa-" + paso.etapa().name().toLowerCase(Locale.ROOT));
            texto.getChildren().add(actual);
        }
        texto.setPadding(new Insets(0, 0, 14, 0));
        HBox.setHgrow(texto, Priority.ALWAYS);
        HBox fila = new HBox(10, guia, texto);
        return fila;
    }

    static String dias(long dias) {
        return dias == 1 ? "1 día" : dias + " días";
    }
}
