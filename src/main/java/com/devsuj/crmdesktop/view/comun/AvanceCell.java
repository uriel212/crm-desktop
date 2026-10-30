package com.devsuj.crmdesktop.view.comun;

import com.devsuj.crmdesktop.util.Formatos;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * Celda con barra de avance y el porcentaje ("71.43 %"). Verde al llegar al 100 %.
 * El valor llega como porcentaje (71.43), igual que en la API; puede pasar de 100.
 */
public class AvanceCell<S> extends TableCell<S, BigDecimal> {

    private final ProgressBar barra = new ProgressBar();

    public AvanceCell() {
        barra.setPrefWidth(90);
        setContentDisplay(ContentDisplay.LEFT);
        setGraphicTextGap(8);
    }

    @Override
    protected void updateItem(BigDecimal porcentaje, boolean vacia) {
        super.updateItem(porcentaje, vacia);
        if (vacia || porcentaje == null) {
            setGraphic(null);
            setText(vacia ? null : "Sin meta");
            return;
        }
        barra.setProgress(Math.min(1.0, porcentaje.doubleValue() / 100.0));
        barra.getStyleClass().removeAll("avance-cumplido");
        if (porcentaje.compareTo(BigDecimal.valueOf(100)) >= 0) {
            barra.getStyleClass().add("avance-cumplido");
        }
        setGraphic(barra);
        setText(Formatos.porcentaje(porcentaje));
    }

    /** Configura una columna de avance. */
    public static <S> void aplicar(TableColumn<S, BigDecimal> columna, Function<S, BigDecimal> valor) {
        columna.setCellValueFactory(fila -> new ReadOnlyObjectWrapper<>(valor.apply(fila.getValue())));
        columna.setCellFactory(c -> new AvanceCell<>());
    }
}
