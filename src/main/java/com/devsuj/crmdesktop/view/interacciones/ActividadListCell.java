package com.devsuj.crmdesktop.view.interacciones;

import com.devsuj.crmdesktop.model.Interaccion;
import com.devsuj.crmdesktop.util.Formatos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

/** Celda de "Próximas actividades": fecha y hora, asunto, cliente y una marca si ya venció. */
public class ActividadListCell extends ListCell<Interaccion> {

    public ActividadListCell() {
        // Ancho preferido 0: la celda toma el ancho de la lista y el texto se ajusta en varias líneas.
        setPrefWidth(0);
    }

    @Override
    protected void updateItem(Interaccion actividad, boolean vacia) {
        super.updateItem(actividad, vacia);
        if (vacia || actividad == null) {
            setGraphic(null);
            setText(null);
            return;
        }
        Label cuando = new Label(Formatos.fechaHora(actividad.fecha()) + " · " + actividad.tipo().etiqueta());
        cuando.getStyleClass().addAll("text-small", "text-muted");
        Label asunto = new Label(actividad.asunto());
        asunto.getStyleClass().add("text-bold");
        asunto.setWrapText(true);
        Label cliente = new Label(actividad.clienteNombre() + " · " + Formatos.textoOGuion(actividad.usuarioNombre()));
        cliente.getStyleClass().add("text-small");
        VBox texto = new VBox(2, cuando, asunto, cliente);
        HBox.setHgrow(texto, Priority.ALWAYS);
        HBox fila = new HBox(8, new FontIcon(actividad.vencida() ? "fth-alert-circle" : "fth-calendar"), texto);
        if (actividad.vencida()) {
            Label vencida = new Label("Vencida");
            vencida.getStyleClass().addAll("etiqueta", "etiqueta-peligro");
            fila.getChildren().add(vencida);
        }
        setText(null);
        setGraphic(fila);
    }
}
