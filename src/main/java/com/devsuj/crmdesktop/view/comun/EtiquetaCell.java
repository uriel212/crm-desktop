package com.devsuj.crmdesktop.view.comun;

import com.devsuj.crmdesktop.model.ConEtiqueta;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Region;

import java.util.Locale;

/**
 * Celda de tabla que muestra un enum como una "etiqueta" de color. La clase CSS se arma con un
 * prefijo y el nombre del valor: {@code etapa-} + {@code PROPUESTA} = {@code etapa-propuesta}.
 *
 * <p>Sirve para cualquier enum que implemente {@link ConEtiqueta} (abierto a extensión): estados de
 * cliente, etapas de oportunidad, roles, tipos de interacción...</p>
 */
public class EtiquetaCell<S, T extends Enum<T> & ConEtiqueta> extends TableCell<S, T> {

    private final String prefijoCss;
    private final Label etiqueta = new Label();

    /** @param prefijoCss por ejemplo "estado-" o "etapa-" */
    public EtiquetaCell(String prefijoCss) {
        this.prefijoCss = prefijoCss;
        etiqueta.setMinWidth(Region.USE_PREF_SIZE); // la etiqueta no se recorta con "..."
    }

    @Override
    protected void updateItem(T valor, boolean vacia) {
        super.updateItem(valor, vacia);
        if (vacia || valor == null) {
            setGraphic(null);
            return;
        }
        etiqueta.setText(valor.etiqueta());
        etiqueta.getStyleClass().setAll("etiqueta", prefijoCss + valor.name().toLowerCase(Locale.ROOT));
        setGraphic(etiqueta);
    }
}
